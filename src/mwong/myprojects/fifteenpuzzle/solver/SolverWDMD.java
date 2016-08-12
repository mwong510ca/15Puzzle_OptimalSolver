/****************************************************************************
 *  @author   Meisze Wong
 *            www.linkedin.com/pub/macy-wong/46/550/37b/
 *
 *  Compilation  : javac SolverWDMD.java
 *  Dependencies : Board.java, Direction.java, SolverWD
 *
 *  SolverMD implements SolverInterface.  It take a Board object
 *  and solve the puzzle with IDA* using combination of manhattan distance
 *  with linear conflict and walking distance.
 *
 ****************************************************************************/

package mwong.myprojects.fifteenpuzzle.solver;

import mwong.myprojects.fifteenpuzzle.solver.components.Board;
import mwong.myprojects.fifteenpuzzle.solver.components.Direction;

public class SolverWDMD extends SolverWD {
    private byte mdlcValue;

    public SolverWDMD() {
        super();
        inUseHeuristic = HeuristicType.WDMD;
    }

    // calculate the heuristic value of the given board and save the properties
    byte heuristic(Board board, boolean inAdvanced, boolean inSearch) {
        if (!board.isSolvable()) {
            return -1;
        }
        priorityAdvanced = -1;

        if (!board.equals(lastBoard) || inSearch) {
            // walking distance from parent/superclass
            priorityGoal = super.heuristic(board, tagOriginal, tagReview);
            wdIdxH = getWdIdxH();
            wdIdxV = getWdIdxV();
            wdValueH = getWdValueH();
            wdValueV = getWdValueV();

            mdlcValue = 0;
            int value;
            int baseRange;
            int base = 0;
            for (int row = 0; row < ROW_SIZE; row++) {
                baseRange = base + ROW_SIZE;
                for (int col = 0; col < ROW_SIZE; col++) {
                    value = tiles[base + col];
                    if (value > 0) {
                        mdlcValue += Math.abs((value - 1) % ROW_SIZE - col);
                        mdlcValue += Math.abs((((value - 1)
                                - (value - 1) % ROW_SIZE) / ROW_SIZE) - row);

                        // linear conflict horizontal
                        if (value > base && value <= baseRange) {
                            for (int col2 = col + 1; col2 < ROW_SIZE; col2++) {
                                int value2 = tiles[base + col2];
                                if ((value2 > base) && (value2 < value)) {
                                    mdlcValue += 2;
                                    break;
                                }
                            }
                        }
                    }

                    // linear conflict vertical
                    if (tilesSym[base + col] > 0) {
                        value = tilesSym[base + col];
                        if (value > base && value <= baseRange) {
                            for (int col2 = col + 1; col2 < ROW_SIZE; col2++) {
                                int value2 = tilesSym[base + col2];
                                if ((value2 > base) && (value2 < value)) {
                                    mdlcValue += 2;
                                    break;
                                }
                            }
                        }
                    }
                }
                base += ROW_SIZE;
            }
            priorityGoal = (byte) (Math.max(priorityGoal, mdlcValue));
        }

        if (!inAdvanced) {
            return priorityGoal;
        }

        advancedContains(board, inSearch);
        if (priorityAdvanced != -1) {
            return priorityAdvanced;
        }

        priorityAdvanced = priorityGoal;
        if (priorityAdvanced < CUTOFF_ADV_PRIORITY) {
            return priorityAdvanced;
        }

        byte orgValH = wdValueH;
        byte orgValV = wdValueV;
        int orgIdxH = wdIdxH;
        int orgIdxV = wdIdxV;

        // calculate advanced estimate from parent/superclass
        if (advancedEstimate(board)) {
            clearHistory();
            zeroX = board.getZeroX();
            zeroY = board.getZeroY();
            tiles = board.getTiles();
            tilesSym = board.getTilesSym();
            wdValueH = orgValH;
            wdValueV = orgValV;
            wdIdxH = orgIdxH;
            wdIdxV = orgIdxV;
            priority1stMove = new int [ROW_SIZE * 2];
            System.arraycopy(board.getValidMoves(), 0, priority1stMove, ROW_SIZE, ROW_SIZE);
        }

        if ((priorityAdvanced - priorityGoal) % 2 == 1) {
            priorityAdvanced++;
        }
        return priorityAdvanced;
    }

    // solve the puzzle using interactive deepening A* algorithm
    void idaStar(int limit) {
        if (solutionMove[1] != null) {
            advancedSearch(limit);
            return;
        }

        int countDir = 0;
        for (int i = 0; i < ROW_SIZE; i++) {
            if (priority1stMove[i + ROW_SIZE] > 0) {
                countDir++;
            }
        }

        // quick scan for advanced priority, determine the start order for optimization
        if (flagAdvancedPriority && countDir > 1) {
            int initLimit = priorityGoal;
            while (initLimit < limit) {
                idaCount = 0;
                dfs1stPrio(zeroX, zeroY, 0, initLimit, mdlcValue, wdIdxH, wdIdxV,
                        wdValueH, wdValueV);
                initLimit += 2;

                boolean overload = false;
                for (int i = ROW_SIZE; i < ROW_SIZE * 2; i++) {
                    if (priority1stMove[i] > 10000) {
                        overload = true;
                        break;
                    }
                }
                if (overload) {
                    break;
                }
            }
        }

        while (limit <= MAX_MOVES) {
            idaCount = 0;
            if (flagMessage) {
                System.out.print("ida limit " + limit);
            }
            dfs1stPrio(zeroX, zeroY, 0, limit, mdlcValue, wdIdxH, wdIdxV, wdValueH, wdValueV);

            searchDepth = limit;
            searchNodeCount += idaCount;

            if (timeout) {
                if (flagMessage) {
                	System.out.println("\tNodes : " + num2string(idaCount) + "timeout");
                }
                return;
            } else {
            	if (flagMessage) {
            		System.out.println("\tNodes : " + num2string(idaCount) + stopwatch.currentTime() + "s");
            	}
            	if (solved) {
            		return;
            	}
            }
            limit += 2;
        }
    }

    // skip the first 8 moves from stored record then solve the remaining puzzle
    // using depth first search with exact number of steps of optimal solution
    private void advancedSearch(int limit) {
        Direction[] dupSolution = new Direction[limit + 1];
        System.arraycopy(solutionMove, 1, dupSolution, 1, ADV_PARTIAL_MOVES);

        Board board = new Board(tiles);
        for (int i = 1; i < ADV_PARTIAL_MOVES; i++) {
            board = board.shift(dupSolution[i]);
        }
        heuristic(board, tagOriginal, tagSearch);

        int firstDirValue = dupSolution[ADV_PARTIAL_MOVES].getValue();
        for (int i = 0; i < 4; i++) {
            if (i != firstDirValue) {
                priority1stMove[i + 4] = 0;
            } else {
                priority1stMove[i + 4] = 1;
            }
        }

        idaCount = ADV_PARTIAL_MOVES;
        if (flagMessage) {
            System.out.print("ida limit " + limit);
        }
        dfs1stPrio(zeroX, zeroY, 0, limit - ADV_PARTIAL_MOVES + 1, mdlcValue,
                wdIdxH, wdIdxV, wdValueH, wdValueV);
        if (solved) {
            System.arraycopy(solutionMove, 2, dupSolution, ADV_PARTIAL_MOVES + 1,
                    limit - ADV_PARTIAL_MOVES);
            solutionMove = dupSolution;
        }
        steps = (byte) limit;
        searchDepth = limit;
        searchNodeCount += idaCount;

        if (flagMessage) {
            if (timeout) {
            	System.out.println("\tNodes : " + num2string(idaCount) + "timeout");
            } else {
            	System.out.println("\tNodes : " + num2string(idaCount) + stopwatch.currentTime() + "s");
            }
        }
    }

    // recursive depth first search until it reach the goal state or timeout, the least estimate and
    // node counts will be use to determine the starting order of next search
    private void dfs1stPrio(int orgX, int orgY, int cost, int limit,
            int orgMDLC, int idxH, int idxV, int valH, int valV) {
        int zeroPos = orgY * ROW_SIZE + orgX;
        int zeroSym = SYMMETRY_POS[zeroPos];
        int costPlus1 = cost + 1;
        int [] estimate1stMove = new int[ROW_SIZE * 2];
        System.arraycopy(priority1stMove, 0, estimate1stMove, 0, ROW_SIZE * 2);

        int estimate = limit;
        do {
            int firstMoveIdx = -1;  // 0 - Right, 1 - Down, 2 - Left, 3 - Up
            int nodeCount = 0;

            estimate = END_OF_SEARCH;
            for (int i = 0; i < 4; i++) {
                if (estimate1stMove[i] == END_OF_SEARCH) {
                    continue;
                } else if (priority1stMove[i + ROW_SIZE] > nodeCount) {
                    estimate = estimate1stMove[i];
                    nodeCount = priority1stMove[i + ROW_SIZE];
                    firstMoveIdx = i;
                } else {
                    priority1stMove[i] = END_OF_SEARCH;
                }
            }

            if (estimate < END_OF_SEARCH) {
                int startCounter = idaCount++;
                if (firstMoveIdx == 0) {
                    priority1stMove[firstMoveIdx] = shiftRight(orgX, orgY, zeroPos, zeroSym,
                            costPlus1, limit, orgMDLC, idxH, idxV, valH, valV);
                } else if (firstMoveIdx == 1) {
                    priority1stMove[firstMoveIdx] = shiftDown(orgX, orgY, zeroPos, zeroSym,
                            costPlus1, limit, orgMDLC, idxH, idxV, valH, valV);
                } else if (firstMoveIdx == 2) {
                    priority1stMove[firstMoveIdx] = shiftLeft(orgX, orgY, zeroPos, zeroSym,
                            costPlus1, limit, orgMDLC, idxH, idxV, valH, valV);
                } else if (firstMoveIdx == 3) {
                    priority1stMove[firstMoveIdx] = shiftUp(orgX, orgY, zeroPos, zeroSym,
                            costPlus1, limit, orgMDLC, idxH, idxV, valH, valV);
                }
                priority1stMove[firstMoveIdx + ROW_SIZE] = idaCount - startCounter;
                estimate1stMove[firstMoveIdx] = END_OF_SEARCH;
            }
        } while (!terminated && estimate != END_OF_SEARCH);
    }

    // recursive depth first search until it reach the goal state or timeout
    private int recursiveDFS(int orgX, int orgY, int cost, int limit,
            int orgMDLC, int idxH, int idxV, int valH, int valV) {
        idaCount++;
        if (terminated) {
            return END_OF_SEARCH;
        }
        if (flagTimeout && stopwatch.currentTime() > searchTimeoutLimit) {
            stopwatch.stop();
            timeout = true;
            terminated = true;
            return END_OF_SEARCH;
        }
        assert stopwatch.isActive() : "stopwatch is not running.";

        int zeroPos = orgY * ROW_SIZE + orgX;
        int zeroSym = SYMMETRY_POS[zeroPos];
        int costPlus1 = cost + 1;
        int newEstimate = Math.min(orgMDLC, valH + valV);

        boolean nonIdentical = true;
        if (zeroPos == zeroSym) {
            nonIdentical = false;
            for (int i = BOARD_SIZE - 1; i > -1; i--) {
                if (tiles[i] != tilesSym[i]) {
                    nonIdentical = true;
                    break;
                }
            }
        }

        // hard code different order to next moves base on the current move
        Direction prevMove = solutionMove[cost];
        if (prevMove == Direction.RIGHT) {
            // RIGHT
            if (orgX < ROW_SIZE - 1) {
                newEstimate = Math.min(newEstimate, shiftRight(orgX, orgY, zeroPos, zeroSym,
                        costPlus1, limit, orgMDLC, idxH, idxV, valH, valV));
            }
            if (nonIdentical) {
                // UP
                if (orgY > 0) {
                    newEstimate = Math.min(newEstimate, shiftUp(orgX, orgY, zeroPos, zeroSym,
                            costPlus1, limit, orgMDLC, idxH, idxV, valH, valV));
                }
                // DOWN
                if (orgY < ROW_SIZE - 1) {
                    newEstimate = Math.min(newEstimate, shiftDown(orgX, orgY, zeroPos, zeroSym,
                            costPlus1, limit, orgMDLC, idxH, idxV, valH, valV));
                }
            }
        } else if (prevMove == Direction.DOWN) {
            // DOWN
            if (orgY < ROW_SIZE - 1) {
                newEstimate = Math.min(newEstimate, shiftDown(orgX, orgY, zeroPos, zeroSym,
                        costPlus1, limit, orgMDLC, idxH, idxV, valH, valV));
            }
            if (nonIdentical) {
                // LEFT
                if (orgX > 0) {
                    newEstimate = Math.min(newEstimate, shiftLeft(orgX, orgY, zeroPos, zeroSym,
                            costPlus1, limit, orgMDLC, idxH, idxV, valH, valV));
                }
                // RIGHT
                if (orgX < ROW_SIZE - 1) {
                    newEstimate = Math.min(newEstimate, shiftRight(orgX, orgY, zeroPos, zeroSym,
                            costPlus1, limit, orgMDLC, idxH, idxV, valH, valV));
                }
            }
        } else if (prevMove == Direction.LEFT) {
            // LEFT
            if (orgX > 0) {
                newEstimate = Math.min(newEstimate, shiftLeft(orgX, orgY, zeroPos, zeroSym,
                        costPlus1, limit, orgMDLC, idxH, idxV, valH, valV));
            }
            if (nonIdentical) {
                // DOWN
                if (orgY < ROW_SIZE - 1) {
                    newEstimate = Math.min(newEstimate, shiftDown(orgX, orgY, zeroPos, zeroSym,
                            costPlus1, limit, orgMDLC, idxH, idxV, valH, valV));
                }
                // UP
                if (orgY > 0) {
                    newEstimate = Math.min(newEstimate, shiftUp(orgX, orgY, zeroPos, zeroSym,
                            costPlus1, limit, orgMDLC, idxH, idxV, valH, valV));
                }
            }
        } else if (prevMove == Direction.UP) {
            // UP
            if (orgY > 0) {
                newEstimate = Math.min(newEstimate, shiftUp(orgX, orgY, zeroPos, zeroSym,
                        costPlus1, limit, orgMDLC, idxH, idxV, valH, valV));
            }
            if (nonIdentical) {
                // RIGHT
                if (orgX < ROW_SIZE - 1) {
                    newEstimate = Math.min(newEstimate, shiftRight(orgX, orgY, zeroPos, zeroSym,
                            costPlus1, limit, orgMDLC, idxH, idxV, valH, valV));
                }
                // LEFT
                if (orgX > 0) {
                    newEstimate = Math.min(newEstimate, shiftLeft(orgX, orgY, zeroPos, zeroSym,
                            costPlus1, limit, orgMDLC, idxH, idxV, valH, valV));
                }
            }
        }
        return newEstimate;
    }

    // shift the space to right
    private int shiftRight(int orgX, int orgY, int zeroPos, int zeroSym, int costPlus1, int limit,
            int orgMDLC, int idxH, int idxV, int valH, int valV) {
        if (terminated) {
            return END_OF_SEARCH;
        }
        int newEstimate = Math.max(valH + valV, orgMDLC);
        int newWdIdx = getWDPtnIdx(idxV, (tiles[zeroPos + 1] - 1) % ROW_SIZE, forward);
        int newWdVal = getWDValue(newWdIdx);
        int priorityWD = valH + newWdVal;
        if (priorityWD == 0) {
            return goalReached(Direction.RIGHT, costPlus1);
        } else if (priorityWD < limit) {
            byte value = tilesSym[zeroSym + ROW_SIZE];
            byte valuePos = (byte) (value - 1);
            int priorityMDLC = orgMDLC - 1;
            if (valuePos / ROW_SIZE > orgX) {
                priorityMDLC = orgMDLC + 1;
            }
            priorityMDLC = updateLCHorizontal(orgY, orgX, valuePos / ROW_SIZE, priorityMDLC, value,
                    1, tilesSym);
            if (Math.max(priorityWD, priorityMDLC) < limit) {
                solutionMove[costPlus1] = Direction.RIGHT;
                return newEstimate = Math.min(newEstimate, nextMove(orgX + 1, orgY,
                        zeroPos, zeroSym, costPlus1, limit, priorityMDLC,
                        zeroPos + 1, zeroSym + ROW_SIZE, idxH, newWdIdx, valH, newWdVal));
            }
            return priorityMDLC;
        }
        return priorityWD;
    }

    // shift the space to down
    private int shiftDown(int orgX, int orgY, int zeroPos, int zeroSym, int costPlus1, int limit,
            int orgMDLC, int idxH, int idxV, int valH, int valV) {
        if (terminated) {
            return END_OF_SEARCH;
        }
        int newEstimate = Math.max(valH + valV, orgMDLC);
        int newWdIdx = getWDPtnIdx(idxH, (tiles[zeroPos + ROW_SIZE] - 1) / ROW_SIZE, forward);
        int newWdVal = getWDValue(newWdIdx);
        int priorityWD = valV + newWdVal;
        if (priorityWD == 0) {
            return goalReached(Direction.DOWN, costPlus1);
        } else if (priorityWD < limit) {
            byte value = tiles[zeroPos + ROW_SIZE];
            byte valuePos = (byte) (value - 1);
            int priorityMDLC = orgMDLC - 1;
            if (valuePos / ROW_SIZE > orgY) {
                priorityMDLC = orgMDLC + 1;
            }
            priorityMDLC = updateLCHorizontal(orgX, orgY, valuePos / ROW_SIZE, priorityMDLC, value,
                    1, tiles);
            if (Math.max(priorityWD, priorityMDLC) < limit) {
                solutionMove[costPlus1] = Direction.DOWN;
                return newEstimate = Math.min(newEstimate, nextMove(orgX, orgY + 1,
                        zeroPos, zeroSym, costPlus1, limit, priorityMDLC,
                        zeroPos + ROW_SIZE, zeroSym + 1, newWdIdx, idxV, newWdVal, valV));
            }
            return priorityMDLC;
        }
        return priorityWD;
    }

    // shift the space to left
    private int shiftLeft(int orgX, int orgY, int zeroPos, int zeroSym, int costPlus1, int limit,
            int orgMDLC, int idxH, int idxV, int valH, int valV) {
        if (terminated) {
            return END_OF_SEARCH;
        }
        int newEstimate = Math.max(valH + valV, orgMDLC);
        int newWdIdx = getWDPtnIdx(idxV, (tiles[zeroPos - 1] - 1) % ROW_SIZE, backward);
        int newWdVal = getWDValue(newWdIdx);
        int priorityWD = valH + newWdVal;
        if (priorityWD == 0) {
            return goalReached(Direction.LEFT, costPlus1);
        } else if (priorityWD < limit) {
            byte value = tilesSym[zeroSym - ROW_SIZE];
            byte valuePos = (byte) (value - 1);
            int priorityMDLC = orgMDLC - 1;
            if (valuePos / ROW_SIZE < orgX) {
                priorityMDLC = orgMDLC + 1;
            }
            priorityMDLC = updateLCHorizontal(orgY, orgX, valuePos / ROW_SIZE, priorityMDLC, value,
                    -1, tilesSym);
            if (Math.max(priorityWD, priorityMDLC) < limit) {
                solutionMove[costPlus1] = Direction.LEFT;
                return newEstimate = Math.min(newEstimate, nextMove(orgX - 1, orgY,
                        zeroPos, zeroSym, costPlus1, limit, priorityMDLC,
                        zeroPos - 1, zeroSym - ROW_SIZE, idxH, newWdIdx, valH, newWdVal));
            }
            return priorityMDLC;
        }
        return priorityWD;
    }

    // shift the space to up
    private int shiftUp(int orgX, int orgY, int zeroPos, int zeroSym, int costPlus1, int limit,
            int orgMDLC, int idxH, int idxV, int valH, int valV) {
        if (terminated) {
            return END_OF_SEARCH;
        }
        int newEstimate = Math.max(valH + valV, orgMDLC);
        int newWdIdx = getWDPtnIdx(idxH, (tiles[zeroPos - ROW_SIZE] - 1) / ROW_SIZE, backward);
        int newWdVal = getWDValue(newWdIdx);
        int priorityWD = valV + newWdVal;
        if (priorityWD == 0) {
            return goalReached(Direction.UP, costPlus1);
        } else if (priorityWD < limit) {
            byte value = tiles[zeroPos - ROW_SIZE];
            byte valuePos = (byte) (value - 1);
            int priorityMDLC = orgMDLC - 1;
            if (valuePos / ROW_SIZE < orgY) {
                priorityMDLC = orgMDLC + 1;
            }
            priorityMDLC = updateLCHorizontal(orgX, orgY, valuePos / ROW_SIZE, priorityMDLC, value,
                    -1, tiles);
            if (Math.max(priorityWD, priorityMDLC) < limit) {
                solutionMove[costPlus1] = Direction.UP;
                return newEstimate = Math.min(newEstimate, nextMove(orgX, orgY - 1,
                        zeroPos, zeroSym, costPlus1, limit, priorityMDLC,
                        zeroPos - ROW_SIZE, zeroSym - 1, newWdIdx, idxV, newWdVal, valV));
            }
            return priorityMDLC;
        }
        return priorityWD;
    }

    // update solution after reached goal state
    private int goalReached(Direction dir, int cost) {
        stopwatch.stop();
        solutionMove[cost] = dir;
        steps = (byte) cost;
        solved = true;
        terminated = true;
        return END_OF_SEARCH;
    }

    // continue to next move if not reach goal state or over limit
    private int nextMove(int orgX, int orgY, int zeroPos, int zeroSym, int cost,
            int limit, int priority, int nextPos, int nextSym, int idxH, int idxV,
            int valH, int valV) {
        tiles[zeroPos] = tiles[nextPos];
        tiles[nextPos] = 0;
        tilesSym[zeroSym] = tilesSym[nextSym];
        tilesSym[nextSym] = 0;
        final int updatePrio
                = recursiveDFS(orgX, orgY, cost, limit - 1, priority, idxH, idxV, valH, valV);
        tiles[nextPos] = tiles[zeroPos];
        tiles[zeroPos] = 0;
        tilesSym[nextSym] = tilesSym[zeroSym];
        tilesSym[zeroSym] = 0;
        return updatePrio;
    }

    // update horizontal linear conflict when the tile move vertically
    private int updateLCHorizontal(int orgX, int orgY, int key, int oldValue, byte value,
            int diff, byte [] tilesSet) {
        int newValue = oldValue;
        if (key == orgY) {
            int base = key * ROW_SIZE;
            int baseRange = base + ROW_SIZE;
            for (int col = base; col < baseRange; col++) {
                int val = tilesSet[col];
                if (val > base && val <= baseRange) {
                    for (int col2 = col + 1; col2 < baseRange; col2++) {
                        int val2 = tilesSet[col2];
                        if (val2 > base && val2 < val) {
                            newValue -= 2;
                            break;
                        }
                    }
                }
            }
            tilesSet[orgY * ROW_SIZE + orgX] = value;
            for (int col = base; col < baseRange; col++) {
                int val = tilesSet[col];
                if (val > base && val <= baseRange) {
                    for (int col2 = col + 1; col2 < baseRange; col2++) {
                        int val2 = tilesSet[col2];
                        if (val2 > base && val2 < val) {
                            newValue += 2;
                            break;
                        }
                    }
                }
            }
            tilesSet[orgY * ROW_SIZE + orgX] = 0;
        } else if (key == orgY + diff) {
            int base = key * ROW_SIZE;
            int baseRange = base + ROW_SIZE;
            for (int col = base; col < baseRange; col++) {
                int val = tilesSet[col];
                if (val > base && val <= baseRange) {
                    for (int col2 = col + 1; col2 < baseRange; col2++) {
                        int val2 = tilesSet[col2];
                        if (val2 > base && val2 < val) {
                            newValue -= 2;
                            break;
                        }
                    }
                }
            }
            tilesSet[(orgY + diff) * ROW_SIZE + orgX] = 0;
            for (int col = base; col < baseRange; col++) {
                int val = tilesSet[col];
                if (val > base && val <= baseRange) {
                    for (int col2 = col + 1; col2 < baseRange; col2++) {
                        int val2 = tilesSet[col2];
                        if (val2 > base && val2 < val) {
                            newValue += 2;
                            break;
                        }
                    }
                }
            }
            tilesSet[(orgY + diff) * ROW_SIZE + orgX] = value;
        }
        return newValue;
    }
}
