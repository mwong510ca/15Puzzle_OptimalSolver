package mwong.myprojects.fifteenpuzzle.solver.standard;

import mwong.myprojects.fifteenpuzzle.solver.HeuristicOptions;
import mwong.myprojects.fifteenpuzzle.solver.components.Board;
import mwong.myprojects.fifteenpuzzle.solver.components.Direction;

/**
 * SolverWDMD extends SolverWD.  It is the 15 puzzle optimal solver.
 * It takes a Board object of the puzzle and solve it with IDA* using combination of
 * Walking Distance and Manhattan Distance with Linear Conflict.
 *
 * <p>Dependencies : Board.java, Direction.java, HeuristicOptions.java, SolverWD.java
 *
 * @author   Meisze Wong
 *           www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public class SolverWDMD extends SolverWD {
    protected byte mdlcValue;

    /**
     * Initializes SolverWDMD object.
     */
    public SolverWDMD() {
        super();
        inUseHeuristic = HeuristicOptions.WDMD;
    }

    /**
     * Returns the heuristic value of the given board.
     *
     * @param board the initial puzzle Board object to solve
     * @return byte value of the heuristic value of the given board
     */
    @Override
    public byte heuristic(Board board) {
        if (board == null) {
            throw new IllegalArgumentException("Board is null");
        }
        if (!board.isSolvable()) {
            return -1;
        }

        if (!board.equals(lastBoard)) {
            // walking distance from parent/superclass
            priorityGoal = super.heuristic(board);
            wdIdxH = getWdIdxH();
            wdIdxV = getWdIdxV();
            wdValueH = getWdValueH();
            wdValueV = getWdValueV();

            mdlcValue = 0;
            int value;
            int baseRange;
            int base = 0;
            for (int row = 0; row < rowSize; row++) {
                baseRange = base + rowSize;
                for (int col = 0; col < rowSize; col++) {
                    value = tiles[base + col];
                    if (value > 0) {
                        mdlcValue += Math.abs((value - 1) % rowSize - col);
                        mdlcValue += Math.abs((((value - 1)
                                - (value - 1) % rowSize) / rowSize) - row);

                        // linear conflict horizontal
                        if (value > base && value <= baseRange) {
                            for (int col2 = col + 1; col2 < rowSize; col2++) {
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
                            for (int col2 = col + 1; col2 < rowSize; col2++) {
                                int value2 = tilesSym[base + col2];
                                if ((value2 > base) && (value2 < value)) {
                                    mdlcValue += 2;
                                    break;
                                }
                            }
                        }
                    }
                }
                base += rowSize;
            }
            priorityGoal = (byte) (Math.max(priorityGoal, mdlcValue));
        }
        return priorityGoal;
    }

    // solve the puzzle using interactive deepening A* algorithm
    protected void idaStar(int limit) {
        while (limit <= maxMoves) {
            idaCount = 0;
            if (flagMessage) {
                System.out.print("ida limit " + limit);
            }
            dfsStartingOrder(zeroX, zeroY, 0, limit, mdlcValue, wdIdxH, wdIdxV, wdValueH, wdValueV);

            searchDepth = limit;
            searchNodeCount += idaCount;

            if (timeout) {
                if (flagMessage) {
                    System.out.printf("\tNodes : %-15s timeout\n", Integer.toString(idaCount));
                }
                return;
            } else {
                if (flagMessage) {
                    System.out.printf("\tNodes : %-15s " + stopwatch.currentTime() + "s\n",
                            Integer.toString(idaCount));
                }
                if (solved) {
                    return;
                }
            }
            limit += 2;
        }
    }

    // recursive depth first search until it reach the goal state or timeout, the least estimate and
    // node counts will be use to determine the starting order of next search
    protected void dfsStartingOrder(int orgX, int orgY, int cost, int limit,
            int orgMDLC, int idxH, int idxV, int valH, int valV) {
        int zeroPos = orgY * rowSize + orgX;
        int zeroSym = symmetryPos[zeroPos];
        int costPlus1 = cost + 1;
        int [] estimate1stMove = new int[rowSize * 2];
        System.arraycopy(lastDepthSummary, 0, estimate1stMove, 0, rowSize * 2);

        int estimate = limit;
        do {
            int firstMoveIdx = -1;
            int nodeCount = 0;

            estimate = endOfSearch;
            for (int i = 0; i < 4; i++) {
                if (estimate1stMove[i] == endOfSearch) {
                    continue;
                } else if (lastDepthSummary[i + rowSize] > nodeCount) {
                    estimate = estimate1stMove[i];
                    nodeCount = lastDepthSummary[i + rowSize];
                    firstMoveIdx = i;
                } else {
                    lastDepthSummary[i] = endOfSearch;
                }
            }

            if (estimate < endOfSearch) {
                int startCounter = idaCount++;
                if (firstMoveIdx == Direction.RIGHT.getValue()) {
                    lastDepthSummary[firstMoveIdx] = shiftRight(orgX, orgY, zeroPos, zeroSym,
                            costPlus1, limit, orgMDLC, idxH, idxV, valH, valV, 0);
                } else if (firstMoveIdx == Direction.DOWN.getValue()) {
                    lastDepthSummary[firstMoveIdx] = shiftDown(orgX, orgY, zeroPos, zeroSym,
                            costPlus1, limit, orgMDLC, idxH, idxV, valH, valV, 0);
                } else if (firstMoveIdx == Direction.LEFT.getValue()) {
                    lastDepthSummary[firstMoveIdx] = shiftLeft(orgX, orgY, zeroPos, zeroSym,
                            costPlus1, limit, orgMDLC, idxH, idxV, valH, valV, 0);
                } else if (firstMoveIdx == Direction.UP.getValue()) {
                    lastDepthSummary[firstMoveIdx] = shiftUp(orgX, orgY, zeroPos, zeroSym,
                            costPlus1, limit, orgMDLC, idxH, idxV, valH, valV, 0);
                }
                lastDepthSummary[firstMoveIdx + rowSize] = idaCount - startCounter;
                estimate1stMove[firstMoveIdx] = endOfSearch;
            }
        } while (!terminated && estimate != endOfSearch);
    }

    // recursive depth first search until it reach the goal state or timeout
    private int recursiveDFS(int orgX, int orgY, int cost, int limit,
            int orgMDLC, int idxH, int idxV, int valH, int valV, int swirlKey) {
        idaCount++;
        if (terminated) {
            return endOfSearch;
        }
        if (flagTimeout && stopwatch.currentTime() > searchTimeoutLimit) {
            stopwatch.stop();
            timeout = true;
            terminated = true;
            return endOfSearch;
        }
        assert stopwatch.isActive() : "stopwatch is not running.";

        int zeroPos = orgY * rowSize + orgX;
        int zeroSym = symmetryPos[zeroPos];
        int costPlus1 = cost + 1;
        int newEstimate = Math.min(orgMDLC, valH + valV);

        boolean nonIdentical = true;
        if (zeroPos == zeroSym) {
            nonIdentical = false;
            for (int i = puzzleSize - 1; i > -1; i--) {
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
            if (orgX < rowSize - 1) {
                newEstimate = Math.min(newEstimate, shiftRight(orgX, orgY, zeroPos, zeroSym,
                        costPlus1, limit, orgMDLC, idxH, idxV, valH, valV, 0));
            }
            if (nonIdentical) {
                // UP
                if (orgY > 0 && isValidCounterClockwise(swirlKey)) {
                    newEstimate = Math.min(newEstimate, shiftUp(orgX, orgY, zeroPos, zeroSym,
                            costPlus1, limit, orgMDLC, idxH, idxV, valH, valV,
                            swirlKey << 2 | Rotation.CCW.getValue()));
                }
                // DOWN
                if (orgY < rowSize - 1 && isValidClockwise(swirlKey)) {
                    newEstimate = Math.min(newEstimate, shiftDown(orgX, orgY, zeroPos, zeroSym,
                            costPlus1, limit, orgMDLC, idxH, idxV, valH, valV,
                            swirlKey << 2 | Rotation.CW.getValue()));
                }
            }
        } else if (prevMove == Direction.DOWN) {
            // DOWN
            if (orgY < rowSize - 1) {
                newEstimate = Math.min(newEstimate, shiftDown(orgX, orgY, zeroPos, zeroSym,
                        costPlus1, limit, orgMDLC, idxH, idxV, valH, valV, 0));
            }
            if (nonIdentical) {
                // LEFT
                if (orgX > 0 && isValidClockwise(swirlKey)) {
                    newEstimate = Math.min(newEstimate, shiftLeft(orgX, orgY, zeroPos, zeroSym,
                            costPlus1, limit, orgMDLC, idxH, idxV, valH, valV,
                            swirlKey << 2 | Rotation.CW.getValue()));
                }
                // RIGHT
                if (orgX < rowSize - 1 && isValidCounterClockwise(swirlKey)) {
                    newEstimate = Math.min(newEstimate, shiftRight(orgX, orgY, zeroPos, zeroSym,
                            costPlus1, limit, orgMDLC, idxH, idxV, valH, valV,
                            swirlKey << 2 | Rotation.CCW.getValue()));
                }
            }
        } else if (prevMove == Direction.LEFT) {
            // LEFT
            if (orgX > 0) {
                newEstimate = Math.min(newEstimate, shiftLeft(orgX, orgY, zeroPos, zeroSym,
                        costPlus1, limit, orgMDLC, idxH, idxV, valH, valV, 0));
            }
            if (nonIdentical) {
                // DOWN
                if (orgY < rowSize - 1 && isValidCounterClockwise(swirlKey)) {
                    newEstimate = Math.min(newEstimate, shiftDown(orgX, orgY, zeroPos, zeroSym,
                            costPlus1, limit, orgMDLC, idxH, idxV, valH, valV,
                            swirlKey << 2 | Rotation.CCW.getValue()));
                }
                // UP
                if (orgY > 0 && isValidClockwise(swirlKey)) {
                    newEstimate = Math.min(newEstimate, shiftUp(orgX, orgY, zeroPos, zeroSym,
                            costPlus1, limit, orgMDLC, idxH, idxV, valH, valV,
                            swirlKey << 2 | Rotation.CW.getValue()));
                }
            }
        } else if (prevMove == Direction.UP) {
            // UP
            if (orgY > 0) {
                newEstimate = Math.min(newEstimate, shiftUp(orgX, orgY, zeroPos, zeroSym,
                        costPlus1, limit, orgMDLC, idxH, idxV, valH, valV, 0));
            }
            if (nonIdentical) {
                // RIGHT
                if (orgX < rowSize - 1 && isValidClockwise(swirlKey)) {
                    newEstimate = Math.min(newEstimate, shiftRight(orgX, orgY, zeroPos, zeroSym,
                            costPlus1, limit, orgMDLC, idxH, idxV, valH, valV,
                            swirlKey << 2 | Rotation.CW.getValue()));
                }
                // LEFT
                if (orgX > 0 && isValidCounterClockwise(swirlKey)) {
                    newEstimate = Math.min(newEstimate, shiftLeft(orgX, orgY, zeroPos, zeroSym,
                            costPlus1, limit, orgMDLC, idxH, idxV, valH, valV,
                            swirlKey << 2 | Rotation.CCW.getValue()));
                }
            }
        }
        return newEstimate;
    }

    // shift the space to right
    private int shiftRight(int orgX, int orgY, int zeroPos, int zeroSym, int costPlus1, int limit,
            int orgMDLC, int idxH, int idxV, int valH, int valV, int swirlKey) {
        if (terminated) {
            return endOfSearch;
        }
        int newEstimate = Math.max(valH + valV, orgMDLC);
        int newWdIdx = getWDPtnIdx(idxV, (tiles[zeroPos + 1] - 1) % rowSize, forward);
        int newWdVal = getWDValue(newWdIdx);
        int priorityWD = valH + newWdVal;
        if (priorityWD == 0) {
            return goalReached(Direction.RIGHT, costPlus1);
        } else if (priorityWD < limit) {
            byte value = tilesSym[zeroSym + rowSize];
            byte valuePos = (byte) (value - 1);
            int priorityMDLC = orgMDLC - 1;
            if (valuePos / rowSize > orgX) {
                priorityMDLC = orgMDLC + 1;
            }
            priorityMDLC = updateLCHorizontal(orgY, orgX, valuePos / rowSize, priorityMDLC, value,
                    1, tilesSym);
            if (Math.max(priorityWD, priorityMDLC) < limit) {
                solutionMove[costPlus1] = Direction.RIGHT;
                return newEstimate = Math.min(newEstimate, nextMove(orgX + 1, orgY,
                        zeroPos, zeroSym, costPlus1, limit, priorityMDLC,
                        zeroPos + 1, zeroSym + rowSize, idxH, newWdIdx, valH, newWdVal, swirlKey));
            }
            return priorityMDLC;
        }
        return priorityWD;
    }

    // shift the space to down
    private int shiftDown(int orgX, int orgY, int zeroPos, int zeroSym, int costPlus1, int limit,
            int orgMDLC, int idxH, int idxV, int valH, int valV, int swirlKey) {
        if (terminated) {
            return endOfSearch;
        }
        int newEstimate = Math.max(valH + valV, orgMDLC);
        int newWdIdx = getWDPtnIdx(idxH, (tiles[zeroPos + rowSize] - 1) / rowSize, forward);
        int newWdVal = getWDValue(newWdIdx);
        int priorityWD = valV + newWdVal;
        if (priorityWD == 0) {
            return goalReached(Direction.DOWN, costPlus1);
        } else if (priorityWD < limit) {
            byte value = tiles[zeroPos + rowSize];
            byte valuePos = (byte) (value - 1);
            int priorityMDLC = orgMDLC - 1;
            if (valuePos / rowSize > orgY) {
                priorityMDLC = orgMDLC + 1;
            }
            priorityMDLC = updateLCHorizontal(orgX, orgY, valuePos / rowSize, priorityMDLC, value,
                    1, tiles);
            if (Math.max(priorityWD, priorityMDLC) < limit) {
                solutionMove[costPlus1] = Direction.DOWN;
                return newEstimate = Math.min(newEstimate, nextMove(orgX, orgY + 1,
                        zeroPos, zeroSym, costPlus1, limit, priorityMDLC,
                        zeroPos + rowSize, zeroSym + 1, newWdIdx, idxV, newWdVal, valV, swirlKey));
            }
            return priorityMDLC;
        }
        return priorityWD;
    }

    // shift the space to left
    private int shiftLeft(int orgX, int orgY, int zeroPos, int zeroSym, int costPlus1, int limit,
            int orgMDLC, int idxH, int idxV, int valH, int valV, int swirlKey) {
        if (terminated) {
            return endOfSearch;
        }
        int newEstimate = Math.max(valH + valV, orgMDLC);
        int newWdIdx = getWDPtnIdx(idxV, (tiles[zeroPos - 1] - 1) % rowSize, backward);
        int newWdVal = getWDValue(newWdIdx);
        int priorityWD = valH + newWdVal;
        if (priorityWD == 0) {
            return goalReached(Direction.LEFT, costPlus1);
        } else if (priorityWD < limit) {
            byte value = tilesSym[zeroSym - rowSize];
            byte valuePos = (byte) (value - 1);
            int priorityMDLC = orgMDLC - 1;
            if (valuePos / rowSize < orgX) {
                priorityMDLC = orgMDLC + 1;
            }
            priorityMDLC = updateLCHorizontal(orgY, orgX, valuePos / rowSize, priorityMDLC, value,
                    -1, tilesSym);
            if (Math.max(priorityWD, priorityMDLC) < limit) {
                solutionMove[costPlus1] = Direction.LEFT;
                return newEstimate = Math.min(newEstimate, nextMove(orgX - 1, orgY,
                        zeroPos, zeroSym, costPlus1, limit, priorityMDLC,
                        zeroPos - 1, zeroSym - rowSize, idxH, newWdIdx, valH, newWdVal, swirlKey));
            }
            return priorityMDLC;
        }
        return priorityWD;
    }

    // shift the space to up
    private int shiftUp(int orgX, int orgY, int zeroPos, int zeroSym, int costPlus1, int limit,
            int orgMDLC, int idxH, int idxV, int valH, int valV, int swirlKey) {
        if (terminated) {
            return endOfSearch;
        }
        int newEstimate = Math.max(valH + valV, orgMDLC);
        int newWdIdx = getWDPtnIdx(idxH, (tiles[zeroPos - rowSize] - 1) / rowSize, backward);
        int newWdVal = getWDValue(newWdIdx);
        int priorityWD = valV + newWdVal;
        if (priorityWD == 0) {
            return goalReached(Direction.UP, costPlus1);
        } else if (priorityWD < limit) {
            byte value = tiles[zeroPos - rowSize];
            byte valuePos = (byte) (value - 1);
            int priorityMDLC = orgMDLC - 1;
            if (valuePos / rowSize < orgY) {
                priorityMDLC = orgMDLC + 1;
            }
            priorityMDLC = updateLCHorizontal(orgX, orgY, valuePos / rowSize, priorityMDLC, value,
                    -1, tiles);
            if (Math.max(priorityWD, priorityMDLC) < limit) {
                solutionMove[costPlus1] = Direction.UP;
                return newEstimate = Math.min(newEstimate, nextMove(orgX, orgY - 1,
                        zeroPos, zeroSym, costPlus1, limit, priorityMDLC,
                        zeroPos - rowSize, zeroSym - 1, newWdIdx, idxV, newWdVal, valV, swirlKey));
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
        return endOfSearch;
    }

    // continue to next move if not reach goal state or over limit
    private int nextMove(int orgX, int orgY, int zeroPos, int zeroSym, int cost,
            int limit, int priority, int nextPos, int nextSym, int idxH, int idxV,
            int valH, int valV, int swirlKey) {
        tiles[zeroPos] = tiles[nextPos];
        tiles[nextPos] = 0;
        tilesSym[zeroSym] = tilesSym[nextSym];
        tilesSym[nextSym] = 0;
        final int updatePrio = recursiveDFS(orgX, orgY, cost, limit - 1, priority,
                idxH, idxV, valH, valV, swirlKey);
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
            int base = key * rowSize;
            int baseRange = base + rowSize;
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
            tilesSet[orgY * rowSize + orgX] = value;
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
            tilesSet[orgY * rowSize + orgX] = 0;
        } else if (key == orgY + diff) {
            int base = key * rowSize;
            int baseRange = base + rowSize;
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
            tilesSet[(orgY + diff) * rowSize + orgX] = 0;
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
            tilesSet[(orgY + diff) * rowSize + orgX] = value;
        }
        return newValue;
    }
}
