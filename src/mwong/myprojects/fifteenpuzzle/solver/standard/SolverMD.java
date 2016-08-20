package mwong.myprojects.fifteenpuzzle.solver.standard;

import mwong.myprojects.fifteenpuzzle.solver.AbstractSolver;
import mwong.myprojects.fifteenpuzzle.solver.HeuristicOptions;
import mwong.myprojects.fifteenpuzzle.solver.SolverConstants;
import mwong.myprojects.fifteenpuzzle.solver.components.Board;
import mwong.myprojects.fifteenpuzzle.solver.components.Direction;

/**
 * SolverMD extends AbstractSolver.  It is the 15 puzzle optimal solver.
 * It takes a Board object of the puzzle and solve it with IDA* using Manhattan
 * distance with linear conflict option.
 *
 * <p>Dependencies : AbstractSolver.java, Board.java, Direction.java,
 *                   HeuristicOptions.java, PuzzleProperties.java SolverConstants.java
 *
 * @author   Meisze Wong
 *           www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public class SolverMD extends AbstractSolver {
    protected byte[] tilesSym;
    protected boolean flagLinearConflict;
    protected int idaCount;

    /**
     * Initializes SolverMD object.
     */
    public SolverMD() {
        this(!SolverConstants.isTagLinearConflict());
    }

    /**
     * Initializes SolverMD object.
     *
     * @param lcFlag boolean flag for linear conflict feature
     */
    public SolverMD(boolean lcFlag) {
        super();
        linearConflictSwitch(lcFlag);
    }

    /**
     *  Set the linear conflict feature with the given flag.
     *
     *  @param lcFlag the boolean represent the ON/OFF linear conflict feature
     */
    public void linearConflictSwitch(boolean lcFlag) {
        clearHistory();
        lastBoard = SolverConstants.getGoalBoard();
        flagLinearConflict = lcFlag;
        if (lcFlag) {
            inUseHeuristic = HeuristicOptions.MDLC;
        } else {
            inUseHeuristic = HeuristicOptions.MD;
        }
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
            initialize(board);
            tilesSym = board.getTilesSym();
            int base = 0;

            for (int row = 0; row < rowSize; row++) {
                final int baseRange = base + rowSize;
                for (int col = 0; col < rowSize; col++) {
                    int value = tiles[base + col];
                    if (value > 0) {
                        priorityGoal += Math.abs((value - 1) % rowSize - col);
                        priorityGoal += Math.abs((((value - 1)
                                - (value - 1) % rowSize) / rowSize) - row);

                        // linear conflict horizontal
                        if (flagLinearConflict) {
                            if (value > base && value <= baseRange) {
                                for (int col2 = col + 1; col2 < rowSize; col2++) {
                                    int value2 = tiles[base + col2];
                                    if ((value2 > base) && (value2 < value)) {
                                        priorityGoal += 2;
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    // linear conflict vertical
                    if (flagLinearConflict && tilesSym[base + col] > 0) {
                        value = tilesSym[base + col];
                        if (value > base && value <= baseRange) {
                            for (int col2 = col + 1; col2 < rowSize; col2++) {
                                int value2 = tilesSym[base + col2];
                                if ((value2 > base) && (value2 < value)) {
                                    priorityGoal += 2;
                                    break;
                                }
                            }
                        }
                    }
                }
                base += rowSize;
            }
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
            dfsStartingOrder(zeroX, zeroY, 0, limit, priorityGoal);
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
    protected void dfsStartingOrder(int orgX, int orgY, int cost, int limit, int orgPrio) {
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
                    lastDepthSummary[firstMoveIdx]
                            = shiftRight(orgX, orgY, zeroPos, zeroSym, costPlus1, limit, orgPrio);
                } else if (firstMoveIdx == Direction.DOWN.getValue()) {
                    lastDepthSummary[firstMoveIdx]
                            = shiftDown(orgX, orgY, zeroPos, zeroSym, costPlus1, limit, orgPrio);
                } else if (firstMoveIdx == Direction.LEFT.getValue()) {
                    lastDepthSummary[firstMoveIdx]
                            = shiftLeft(orgX, orgY, zeroPos, zeroSym, costPlus1, limit, orgPrio);
                } else if (firstMoveIdx == Direction.UP.getValue()) {
                    lastDepthSummary[firstMoveIdx]
                            = shiftUp(orgX, orgY, zeroPos, zeroSym, costPlus1, limit, orgPrio);
                }
                if (terminated) {
                    return;
                }
                lastDepthSummary[firstMoveIdx + rowSize] = idaCount - startCounter;
                estimate1stMove[firstMoveIdx] = endOfSearch;
            }
        } while (!terminated && estimate != endOfSearch);
    }

    // recursive depth first search until it reach the goal state or timeout
    private int recursiveDFS(int orgX, int orgY, int cost, int limit, int orgPrio) {
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
        int newEstimate = orgPrio;

        boolean nonIdentical = true;
        if (zeroPos == zeroSym) {
            nonIdentical = false;
            for (int idx = puzzleSize - 1; idx > -1; idx--) {
                if (tiles[idx] != tilesSym[idx]) {
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
                newEstimate = Math.min(newEstimate,
                        shiftRight(orgX, orgY, zeroPos, zeroSym, costPlus1, limit, orgPrio));
            }
            if (nonIdentical) {
                // UP
                if (orgY > 0) {
                    newEstimate = Math.min(newEstimate,
                            shiftUp(orgX, orgY, zeroPos, zeroSym, costPlus1, limit, orgPrio));
                }
                // DOWN
                if (orgY < rowSize - 1) {
                    newEstimate = Math.min(newEstimate,
                            shiftDown(orgX, orgY, zeroPos, zeroSym, costPlus1, limit, orgPrio));
                }
            }
        } else if (prevMove == Direction.DOWN) {
            // DOWN
            if (orgY < rowSize - 1) {
                newEstimate = Math.min(newEstimate,
                        shiftDown(orgX, orgY, zeroPos, zeroSym, costPlus1, limit, orgPrio));
            }
            if (nonIdentical) {
                // LEFT
                if (orgX > 0) {
                    newEstimate = Math.min(newEstimate,
                            shiftLeft(orgX, orgY, zeroPos, zeroSym, costPlus1, limit, orgPrio));
                }
                // RIGHT
                if (orgX < rowSize - 1) {
                    newEstimate = Math.min(newEstimate,
                            shiftRight(orgX, orgY, zeroPos, zeroSym, costPlus1, limit, orgPrio));
                }
            }
        } else if (prevMove == Direction.LEFT) {
            // LEFT
            if (orgX > 0) {
                newEstimate = Math.min(newEstimate,
                        shiftLeft(orgX, orgY, zeroPos, zeroSym, costPlus1, limit, orgPrio));
            }
            if (nonIdentical) {
                // DOWN
                if (orgY < rowSize - 1) {
                    newEstimate = Math.min(newEstimate,
                            shiftDown(orgX, orgY, zeroPos, zeroSym, costPlus1, limit, orgPrio));
                }
                // UP
                if (orgY > 0) {
                    newEstimate = Math.min(newEstimate,
                            shiftUp(orgX, orgY, zeroPos, zeroSym, costPlus1, limit, orgPrio));
                }
            }
        } else if (prevMove == Direction.UP) {
            // UP
            if (orgY > 0) {
                newEstimate = Math.min(newEstimate,
                        shiftUp(orgX, orgY, zeroPos, zeroSym, costPlus1, limit, orgPrio));
            }
            if (nonIdentical) {
                // RIGHT
                if (orgX < rowSize - 1) {
                    newEstimate = Math.min(newEstimate,
                            shiftRight(orgX, orgY, zeroPos, zeroSym, costPlus1, limit, orgPrio));
                }
                // LEFT
                if (orgX > 0) {
                    newEstimate = Math.min(newEstimate,
                            shiftLeft(orgX, orgY, zeroPos, zeroSym, costPlus1, limit, orgPrio));
                }
            }
        }
        return newEstimate;
    }

    // shift the space to right
    private int shiftRight(int orgX, int orgY, int zeroPos, int zeroSym, int costPlus1, int limit,
            int orgPrio) {
        if (terminated) {
            return endOfSearch;
        }
        byte value = tilesSym[zeroSym + rowSize];
        byte valuePos = (byte) (value - 1);
        int priority = orgPrio - 1;
        if (valuePos / rowSize > orgX) {
            priority = orgPrio + 1;
        }
        if (flagLinearConflict) {
            priority = updateLinearConflict(orgY, orgX, valuePos / rowSize, priority, value,
                    1, tilesSym);
        }
        solutionMove[costPlus1] = Direction.RIGHT;
        return nextMove(orgX + 1, orgY, zeroPos, zeroSym, costPlus1, limit,
                priority, zeroPos + 1, zeroSym + rowSize);
    }

    // shift the space to down
    private int shiftDown(int orgX, int orgY, int zeroPos, int zeroSym, int costPlus1, int limit,
            int orgPriority) {
        if (terminated) {
            return endOfSearch;
        }
        byte value = tiles[zeroPos + rowSize];
        byte valuePos = (byte) (value - 1);
        int priority = orgPriority - 1;
        if (valuePos / rowSize > orgY) {
            priority = orgPriority + 1;
        }
        if (flagLinearConflict) {
            priority = updateLinearConflict(orgX, orgY, valuePos / rowSize, priority, value,
                    1, tiles);
        }
        solutionMove[costPlus1] = Direction.DOWN;
        return nextMove(orgX, orgY + 1, zeroPos, zeroSym, costPlus1, limit,
                priority, zeroPos + rowSize, zeroSym + 1);
    }

    // shift the space to left
    private int shiftLeft(int orgX, int orgY, int zeroPos, int zeroSym, int costPlus1, int limit,
            int orgPriority) {
        if (terminated) {
            return endOfSearch;
        }
        byte value = tilesSym[zeroSym - rowSize];
        byte valuePos = (byte) (value - 1);
        int priority = orgPriority - 1;
        if (valuePos / rowSize < orgX) {
            priority = orgPriority + 1;
        }
        if (flagLinearConflict) {
            priority = updateLinearConflict(orgY, orgX, valuePos / rowSize, priority, value,
                    -1, tilesSym);
        }
        solutionMove[costPlus1] = Direction.LEFT;
        return nextMove(orgX - 1, orgY, zeroPos, zeroSym, costPlus1, limit,
                priority, zeroPos - 1, zeroSym - rowSize);
    }

    // shift the space to up
    private int shiftUp(int orgX, int orgY, int zeroPos, int zeroSym, int costPlus1, int limit,
            int orgPriority) {
        if (terminated) {
            return endOfSearch;
        }
        byte value = tiles[zeroPos - rowSize];
        byte valuePos = (byte) (value - 1);
        int priority = orgPriority - 1;
        if (valuePos / rowSize < orgY) {
            priority = orgPriority + 1;
        }
        if (flagLinearConflict) {
            priority = updateLinearConflict(orgX, orgY, valuePos / rowSize, priority, value,
                    -1, tiles);
        }
        solutionMove[costPlus1] = Direction.UP;
        return nextMove(orgX, orgY - 1, zeroPos, zeroSym, costPlus1, limit,
                priority, zeroPos - rowSize, zeroSym - 1);
    }

    // continue to next move if not reach goal state or over limit
    private int nextMove(int orgX, int orgY, int zeroPos, int zeroSym, int cost,
            int limit, int priority, int nextPos, int nextSym) {
        int updatePrio = priority;
        if (priority == 0) {
            stopwatch.stop();
            steps = (byte) cost;
            solved = true;
            terminated = true;
            updatePrio = endOfSearch;
        } else if (priority < limit) {
            tiles[zeroPos] = tiles[nextPos];
            tiles[nextPos] = 0;
            tilesSym[zeroSym] = tilesSym[nextSym];
            tilesSym[nextSym] = 0;
            updatePrio = Math.min(updatePrio, recursiveDFS(orgX, orgY, cost, limit - 1, priority));
            tiles[nextPos] = tiles[zeroPos];
            tiles[zeroPos] = 0;
            tilesSym[nextSym] = tilesSym[zeroSym];
            tilesSym[zeroSym] = 0;
        }
        return updatePrio;
    }

    // update horizontal linear conflict when the tile move vertically
    private int updateLinearConflict(int orgX, int orgY, int key, int oldValue, byte value,
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
