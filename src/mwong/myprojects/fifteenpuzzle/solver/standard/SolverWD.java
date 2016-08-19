package mwong.myprojects.fifteenpuzzle.solver.standard;

import mwong.myprojects.fifteenpuzzle.solver.AbstractSolver;
import mwong.myprojects.fifteenpuzzle.solver.HeuristicOptions;
import mwong.myprojects.fifteenpuzzle.solver.components.Board;
import mwong.myprojects.fifteenpuzzle.solver.components.Direction;
import mwong.myprojects.fifteenpuzzle.solver.components.WalkingDistance;

import java.util.HashMap;

/**
 * SolverWD extends AbstractSolver.  It is the 15 puzzle optimal solver.
 * It takes a Board object of the puzzle and solve it with IDA* using Walking Distance.
 *
 * <p>Dependencies : AbstractSolver.java, Board.java, Direction.java, HeuristicOptions.java,
 *                   WalkingDistance.java
 *
 * @author   Meisze Wong
 *           www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public class SolverWD extends AbstractSolver {
    protected final boolean forward = true;
    protected final boolean backward = !forward;

    // Walking Distance Components
    protected static HashMap<Integer, Integer> wdRowKeys;
    protected static HashMap<Integer, Integer> wdPtnKeys;
    protected static byte [] wdPattern;
    protected static int [] wdPtnLink;

    protected byte [] tilesSym;
    protected byte wdValueH;
    protected byte wdValueV;
    protected int wdIdxH;
    protected int wdIdxV;
    protected int idaCount;

    /**
     * Initializes SolverWD object.
     */
    public SolverWD() {
        super();
        inUseHeuristic = HeuristicOptions.WD;
        loadWDComponents();
    }

    // load the walking distance components from the data file
    // if data file not exists, generate a new set
    private void loadWDComponents() {
        WalkingDistance wd15 = new WalkingDistance();
        wdRowKeys = wd15.getRowKeys();
        wdPtnKeys = wd15.getPtnKeys();
        wdPattern = wd15.getPattern();
        wdPtnLink = wd15.getPtnLink();
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

            byte [] ctwdh = new byte[puzzleSize];
            byte [] ctwdv = new byte[puzzleSize];

            for (int i = 0; i < 16; i++) {
                int value = tiles[i];
                if (value != 0) {
                    int col = (value - 1) / rowSize;
                    ctwdh[(i / rowSize) * rowSize + col]++;

                    col = value % rowSize - 1;
                    if (col < 0) {
                        col = rowSize - 1;
                    }
                    ctwdv[(i % rowSize) * rowSize + col]++;
                }
            }

            wdIdxH = getWDPtnIdx(ctwdh, zeroY);
            wdIdxV = getWDPtnIdx(ctwdv, zeroX);
            wdValueH = getWDValue(wdIdxH);
            wdValueV = getWDValue(wdIdxV);

            priorityGoal = (byte) (wdValueH + wdValueV);
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
            dfsStartingOrder(zeroX, zeroY, 0, limit, wdIdxH, wdIdxV, wdValueH, wdValueV);
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

    // overload idaStar to solve the puzzle with the given max limit for advancedEstimate
    protected void idaStar(int limit, int maxLimit) {
        while (limit <= maxLimit) {
            dfsStartingOrder(zeroX, zeroY, 0, limit, wdIdxH, wdIdxV, wdValueH, wdValueV);
            if (solved) {
                return;
            }
            limit += 2;
        }
    }

    // recursive depth first search until it reach the goal state or timeout, the least estimate and
    // node counts will be use to determine the starting order of next search
    protected void dfsStartingOrder(int orgX, int orgY, int cost, int limit, int idxH, int idxV,
            int valH, int valV) {
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
                            costPlus1, limit, idxH, idxV, valH, valV);
                } else if (firstMoveIdx == Direction.DOWN.getValue()) {
                    lastDepthSummary[firstMoveIdx] = shiftDown(orgX, orgY, zeroPos, zeroSym,
                            costPlus1, limit, idxH, idxV, valH, valV);
                } else if (firstMoveIdx == Direction.LEFT.getValue()) {
                    lastDepthSummary[firstMoveIdx] = shiftLeft(orgX, orgY, zeroPos, zeroSym,
                            costPlus1, limit, idxH, idxV, valH, valV);
                } else if (firstMoveIdx == Direction.UP.getValue()) {
                    lastDepthSummary[firstMoveIdx] = shiftUp(orgX, orgY, zeroPos, zeroSym,
                            costPlus1, limit, idxH, idxV, valH, valV);
                }
                lastDepthSummary[firstMoveIdx + rowSize] = idaCount - startCounter;
                estimate1stMove[firstMoveIdx] = endOfSearch;
            }
        } while (!terminated && estimate != endOfSearch);
    }

    // recursive depth first search until it reach the goal state or timeout
    private int recursiveDFS(int orgX, int orgY, int cost, int limit, int idxH, int idxV,
            int valH, int valV) {
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
        //assert stopwatch.isActive() : "stopwatch is not running.";

        int zeroPos = orgY * rowSize + orgX;
        int zeroSym = symmetryPos[zeroPos];
        int costPlus1 = cost + 1;
        int newEstimate = valH + valV;

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
                        costPlus1, limit, idxH, idxV, valH, valV));
            }
            if (nonIdentical) {
                // UP
                if (orgY > 0) {
                    newEstimate = Math.min(newEstimate, shiftUp(orgX, orgY, zeroPos, zeroSym,
                            costPlus1, limit, idxH, idxV, valH, valV));
                }
                // DOWN
                if (orgY < rowSize - 1) {
                    newEstimate = Math.min(newEstimate, shiftDown(orgX, orgY, zeroPos, zeroSym,
                            costPlus1, limit, idxH, idxV, valH, valV));
                }
            }
        } else if (prevMove == Direction.DOWN) {
            // DOWN
            if (orgY < rowSize - 1) {
                newEstimate = Math.min(newEstimate, shiftDown(orgX, orgY, zeroPos, zeroSym,
                        costPlus1, limit, idxH, idxV, valH, valV));
            }
            if (nonIdentical) {
                // LEFT
                if (orgX > 0) {
                    newEstimate = Math.min(newEstimate, shiftLeft(orgX, orgY, zeroPos, zeroSym,
                            costPlus1, limit, idxH, idxV, valH, valV));
                }
                // RIGHT
                if (orgX < rowSize - 1) {
                    newEstimate = Math.min(newEstimate, shiftRight(orgX, orgY, zeroPos, zeroSym,
                            costPlus1, limit, idxH, idxV, valH, valV));
                }
            }
        } else if (prevMove == Direction.LEFT) {
            // LEFT
            if (orgX > 0) {
                newEstimate = Math.min(newEstimate, shiftLeft(orgX, orgY, zeroPos, zeroSym,
                        costPlus1, limit, idxH, idxV, valH, valV));
            }
            if (nonIdentical) {
                // DOWN
                if (orgY < rowSize - 1) {
                    newEstimate = Math.min(newEstimate, shiftDown(orgX, orgY, zeroPos, zeroSym,
                            costPlus1, limit, idxH, idxV, valH, valV));
                }
                // UP
                if (orgY > 0) {
                    newEstimate = Math.min(newEstimate, shiftUp(orgX, orgY, zeroPos, zeroSym,
                            costPlus1, limit, idxH, idxV, valH, valV));
                }
            }
        } else if (prevMove == Direction.UP) {
            // UP
            if (orgY > 0) {
                newEstimate = Math.min(newEstimate, shiftUp(orgX, orgY, zeroPos, zeroSym,
                        costPlus1, limit, idxH, idxV, valH, valV));
            }
            if (nonIdentical) {
                // RIGHT
                if (orgX < rowSize - 1) {
                    newEstimate = Math.min(newEstimate, shiftRight(orgX, orgY, zeroPos, zeroSym,
                            costPlus1, limit, idxH, idxV, valH, valV));
                }
                // LEFT
                if (orgX > 0) {
                    newEstimate = Math.min(newEstimate, shiftLeft(orgX, orgY, zeroPos, zeroSym,
                            costPlus1, limit, idxH, idxV, valH, valV));
                }
            }
        }
        return newEstimate;
    }

    // shift the space to right
    private int shiftRight(int orgX, int orgY, int zeroPos, int zeroSym, int costPlus1, int limit,
            int idxH, int idxV, int valH, int valV) {
        if (terminated) {
            return endOfSearch;
        }
        int nextPos = zeroPos + 1;
        byte value = tiles[nextPos];
        int newIdx = getWDPtnIdx(idxV, (value - 1) % rowSize, forward);
        int newValue = getWDValue(newIdx);
        int priority = valH + newValue;
        solutionMove[costPlus1] = Direction.RIGHT;
        return nextMove(orgX + 1, orgY, zeroPos, zeroSym, priority, costPlus1,
                limit, nextPos, zeroSym + rowSize, idxH, newIdx, valH, newValue);
    }

    // shift the space to down
    private int shiftDown(int orgX, int orgY, int zeroPos, int zeroSym, int costPlus1, int limit,
            int idxH, int idxV, int valH, int valV) {
        if (terminated) {
            return endOfSearch;
        }
        int nextPos = zeroPos + rowSize;
        byte value = tiles[nextPos];
        int newIdx = getWDPtnIdx(idxH, (value - 1) / rowSize, forward);
        int newValue = getWDValue(newIdx);
        int priority = valV + newValue;
        solutionMove[costPlus1] = Direction.DOWN;
        return nextMove(orgX, orgY + 1, zeroPos, zeroSym, priority, costPlus1,
                limit, nextPos, zeroSym + 1, newIdx, idxV, newValue, valV);
    }

    // shift the space to left
    private int shiftLeft(int orgX, int orgY, int zeroPos, int zeroSym, int costPlus1, int limit,
            int idxH, int idxV, int valH, int valV) {
        if (terminated) {
            return endOfSearch;
        }
        int nextPos = zeroPos - 1;
        byte value = tiles[nextPos];
        int newIdx = getWDPtnIdx(idxV, (value - 1) % rowSize, backward);
        int newValue = getWDValue(newIdx);
        int priority = valH + newValue;
        solutionMove[costPlus1] = Direction.LEFT;
        return nextMove(orgX - 1, orgY, zeroPos, zeroSym, priority, costPlus1,
                limit, nextPos, zeroSym - rowSize, idxH, newIdx, valH, newValue);
    }

    // shift the space to up
    private int shiftUp(int orgX, int orgY, int zeroPos, int zeroSym, int costPlus1, int limit,
            int idxH, int idxV, int valH, int valV) {
        if (terminated) {
            return endOfSearch;
        }
        int nextPos = zeroPos - rowSize;
        byte value = tiles[nextPos];
        int newIdx = getWDPtnIdx(idxH, (value - 1) / rowSize, backward);
        int newValue = getWDValue(newIdx);
        int priority = valV + newValue;
        solutionMove[costPlus1] = Direction.UP;
        return nextMove(orgX, orgY - 1, zeroPos, zeroSym, priority, costPlus1,
                limit, nextPos, zeroSym - 1, newIdx, idxV, newValue, valV);
    }

    // continue to next move if not reach goal state or over limit
    private int nextMove(int orgX, int orgY, int zeroPos, int zeroSym, int priority,
            int cost, int limit, int nextPos, int nextSym, int idxH, int idxV, int valH, int valV) {
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
            updatePrio = Math.min(updatePrio,
                    recursiveDFS(orgX, orgY, cost, limit - 1, idxH, idxV, valH, valV));
            tiles[nextPos] = tiles[zeroPos];
            tiles[zeroPos] = 0;
            tilesSym[nextSym] = tilesSym[zeroSym];
            tilesSym[zeroSym] = 0;
        }
        return updatePrio;
    }

    // take a set of walking distance values and row index of zero position,
    // compress into 32 bit key, and return the key index
    protected int getWDPtnIdx(byte [] ctwd, int zeroRow) {
        int key = 0;
        int count = 0;

        while (count < ctwd.length) {
            int temp = 0;
            for (int i = 0; i < rowSize; i++) {
                temp = (temp << 3) | ctwd[count++];
            }
            key = (key << 6) | wdRowKeys.get(temp);
            assert (wdRowKeys.get(temp) != -1) : " Invalid index : -1";
        }
        key = (key << 4) | zeroRow;
        return wdPtnKeys.get(key);
    }

    // take a key index, the column index of move and direction
    // return the key index after the move
    protected int getWDPtnIdx(int idx, int col, boolean isForward) {
        if (isForward) {
            return wdPtnLink[idx * rowSize * 2 + col * 2];
        } else {
            return wdPtnLink[idx * rowSize * 2 + col * 2 + 1];
        }
    }

    // take a key index and return the value of walking distance
    protected byte getWDValue(int idx) {
        return wdPattern[idx];
    }

    // return horizontal walking distance value
    protected final byte getWdValueH() {
        return wdValueH;
    }

    // return vertical walking distance value
    protected final byte getWdValueV() {
        return wdValueV;
    }

    // return horizontal walking distance index
    protected final int getWdIdxH() {
        return wdIdxH;
    }

    // return vertical walking distance index
    protected final int getWdIdxV() {
        return wdIdxV;
    }
}
