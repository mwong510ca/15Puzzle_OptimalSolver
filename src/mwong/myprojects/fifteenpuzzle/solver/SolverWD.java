/****************************************************************************
 *  @author   Meisze Wong
 *            www.linkedin.com/pub/macy-wong/46/550/37b/
 *
 *  Compilation  : javac SolverWD.java
 *  Dependencies : Board.java, Direction.java, Stopwatch.java,
 *                 SolverAbstract, WDCombo.java, AdvancedAccumulator.java
 *                 AdvancedBoard.java, AdvancedMoves.java
 *
 *  SolverWD implements SolverInterface.  It take a Board object and solve
 *  the puzzle with IDA* using walking distance.
 *
 ****************************************************************************/

package mwong.myprojects.fifteenpuzzle.solver;

import mwong.myprojects.fifteenpuzzle.solver.components.AdvancedAccumulator;
import mwong.myprojects.fifteenpuzzle.solver.components.AdvancedBoard;
import mwong.myprojects.fifteenpuzzle.solver.components.AdvancedMoves;
import mwong.myprojects.fifteenpuzzle.solver.components.Board;
import mwong.myprojects.fifteenpuzzle.solver.components.Direction;
import mwong.myprojects.fifteenpuzzle.solver.components.WDCombo;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class SolverWD extends SolverAbstract {
    static final boolean forward = true;
    static final boolean backward = !forward;

    // Walking Distance Components
    private static HashMap<Integer, Integer> wdRowKeys;
    private static HashMap<Integer, Integer> wdPtnKeys;
    private static byte [] wdPattern;
    private static int [] wdPtnLink;

    byte [] tilesSym;
    byte wdValueH;
    byte wdValueV;
    int wdIdxH;
    int wdIdxV;

    /**
     * Initializes SolverWD object.
     */
    public SolverWD() {
        flagMessage = SWITCH_ON;
        inUseHeuristic = HeuristicType.WD;
        loadWDComponents();
    }

    // load the walking distance components from the data file
    // if data file not exists, generate a new set
    private void loadWDComponents() {
        WDCombo wd15 = new WDCombo();
        wdRowKeys = wd15.getRowKeys();
        wdPtnKeys = wd15.getPtnKeys();
        wdPattern = wd15.getPattern();
        wdPtnLink = wd15.getPtnLink();
    }

    // calculate the heuristic value of the given board and save the properties
    byte heuristic(Board board, boolean inAdvanced, boolean inSearch) {
        if (!board.isSolvable()) {
            return -1;
        }

        priorityAdvanced = -1;
        if (!board.equals(lastBoard) || inSearch) {
            lastBoard = board;

            zeroX = board.getZeroX();
            zeroY = board.getZeroY();
            tiles = board.getTiles();
            tilesSym = board.getTilesSym();
            priority1stMove = new int [ROW_SIZE * 2];
            System.arraycopy(board.getValidMoves(), 0, priority1stMove, ROW_SIZE, ROW_SIZE);

            byte [] ctwdh = new byte[BOARD_SIZE];
            byte [] ctwdv = new byte[BOARD_SIZE];

            for (int i = 0; i < 16; i++) {
                int value = tiles[i];
                if (value != 0) {
                    int col = (value - 1) / ROW_SIZE;
                    ctwdh[(i / ROW_SIZE) * ROW_SIZE + col]++;

                    col = value % ROW_SIZE - 1;
                    if (col < 0) {
                        col = ROW_SIZE - 1;
                    }
                    ctwdv[(i % ROW_SIZE) * ROW_SIZE + col]++;
                }
            }

            wdIdxH = getWDPtnIdx(ctwdh, zeroY);
            wdIdxV = getWDPtnIdx(ctwdv, zeroX);
            wdValueH = getWDValue(wdIdxH);
            wdValueV = getWDValue(wdIdxV);

            priorityGoal = (byte) (wdValueH + wdValueV);
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

    // calculate the advanced estimate from the stored boards
    boolean advancedEstimate(Board board) {
        Map<AdvancedBoard, AdvancedMoves> advMap;
        if (advAccumulator == null) {
            advMap = AdvancedAccumulator.getDefaultMap();
        } else {
            advMap = advAccumulator.getActiveMap();
        }

        byte maxEstimate = priorityGoal;
        byte[] orgTiles = tiles.clone();
        boolean reset = false;
        for (Entry<AdvancedBoard, AdvancedMoves> entry
                : advMap.entrySet()) {
            byte[] transTiles = entry.getKey().transformer(orgTiles);

            int orgX = 0;
            int orgY = 0;
            for (int j = 0; j < 16; j++) {
                int pos = 15 - j;
                if (transTiles[pos] == 0) {
                    orgX = pos % ROW_SIZE;
                    orgY = pos / ROW_SIZE;
                    break;
                }
            }

            byte [] ctwdh = new byte[BOARD_SIZE];
            byte [] ctwdv = new byte[BOARD_SIZE];
            for (int j = 0; j < 16; j++) {
                int value = transTiles[j];
                if (value != 0) {
                    int col = (value - 1) / ROW_SIZE;
                    ctwdh[(j / ROW_SIZE) * ROW_SIZE + col]++;

                    col = value % ROW_SIZE - 1;
                    if (col < 0) {
                        col = ROW_SIZE - 1;
                    }
                    ctwdv[(j % ROW_SIZE) * ROW_SIZE + col]++;
                }
            }

            int wdIdxHtrans = getWDPtnIdx(ctwdh, orgY);
            int wdIdxVtrans = getWDPtnIdx(ctwdv, orgX);
            int wdValueHtrans = getWDValue(wdIdxHtrans);
            int wdValueVtrans = getWDValue(wdIdxVtrans);

            int transPriority = wdValueHtrans + wdValueVtrans;
            if (transPriority > CUTOFF_ADV_PRIORITY) {
                continue;
            }
            if (entry.getValue().getEstimate() - transPriority <= maxEstimate) {
                continue;
            }

            reset = true;
            clearHistory();
            Board temp = new Board(transTiles);
            zeroX = temp.getZeroX();
            zeroY = temp.getZeroY();
            tiles = temp.getTiles();
            tilesSym = temp.getTilesSym();
            wdValueH = (byte) wdValueHtrans;
            wdValueV = (byte) wdValueVtrans;
            wdIdxH = wdIdxHtrans;
            wdIdxV = wdIdxVtrans;
            System.arraycopy(temp.getValidMoves(), 0, priority1stMove, ROW_SIZE, ROW_SIZE);
            idaStar(transPriority, entry.getValue().getEstimate() - maxEstimate);
            if (!stopwatch.isActive()) {
                stopwatch.start();
            }
            if (solved) {
                maxEstimate = (byte) (entry.getValue().getEstimate() - steps);
            }
        }
        priorityAdvanced = maxEstimate;
        return reset;
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
                dfs1stPrio(zeroX, zeroY, 0, initLimit, wdIdxH, wdIdxV, wdValueH, wdValueV);
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
            dfs1stPrio(zeroX, zeroY, 0, limit, wdIdxH, wdIdxV, wdValueH, wdValueV);
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

    // overload idaStar to solve the puzzle with the given max limit for advancedEstimate
    void idaStar(int limit, int maxLimit) {
        while (limit <= maxLimit) {
            dfs1stPrio(zeroX, zeroY, 0, limit, wdIdxH, wdIdxV, wdValueH, wdValueV);
            if (solved) {
                return;
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
        dfs1stPrio(zeroX, zeroY, 0, limit - ADV_PARTIAL_MOVES + 1, wdIdxH, wdIdxV,
                wdValueH, wdValueV);
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
    private void dfs1stPrio(int orgX, int orgY, int cost, int limit, int idxH, int idxV,
            int valH, int valV) {
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
                            costPlus1, limit, idxH, idxV, valH, valV);
                } else if (firstMoveIdx == 1) {
                    priority1stMove[firstMoveIdx] = shiftDown(orgX, orgY, zeroPos, zeroSym,
                            costPlus1, limit, idxH, idxV, valH, valV);
                } else if (firstMoveIdx == 2) {
                    priority1stMove[firstMoveIdx] = shiftLeft(orgX, orgY, zeroPos, zeroSym,
                            costPlus1, limit, idxH, idxV, valH, valV);
                } else if (firstMoveIdx == 3) {
                    priority1stMove[firstMoveIdx] = shiftUp(orgX, orgY, zeroPos, zeroSym,
                            costPlus1, limit, idxH, idxV, valH, valV);
                }
                priority1stMove[firstMoveIdx + ROW_SIZE] = idaCount - startCounter;
                estimate1stMove[firstMoveIdx] = END_OF_SEARCH;
            }
        } while (!terminated && estimate != END_OF_SEARCH);
    }

    // recursive depth first search until it reach the goal state or timeout
    private int recursiveDFS(int orgX, int orgY, int cost, int limit, int idxH, int idxV,
            int valH, int valV) {
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
        //assert stopwatch.isActive() : "stopwatch is not running.";

        int zeroPos = orgY * ROW_SIZE + orgX;
        int zeroSym = SYMMETRY_POS[zeroPos];
        int costPlus1 = cost + 1;
        int newEstimate = valH + valV;

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
                        costPlus1, limit, idxH, idxV, valH, valV));
            }
            if (nonIdentical) {
                // UP
                if (orgY > 0) {
                    newEstimate = Math.min(newEstimate, shiftUp(orgX, orgY, zeroPos, zeroSym,
                            costPlus1, limit, idxH, idxV, valH, valV));
                }
                // DOWN
                if (orgY < ROW_SIZE - 1) {
                    newEstimate = Math.min(newEstimate, shiftDown(orgX, orgY, zeroPos, zeroSym,
                            costPlus1, limit, idxH, idxV, valH, valV));
                }
            }
        } else if (prevMove == Direction.DOWN) {
            // DOWN
            if (orgY < ROW_SIZE - 1) {
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
                if (orgX < ROW_SIZE - 1) {
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
                if (orgY < ROW_SIZE - 1) {
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
                if (orgX < ROW_SIZE - 1) {
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
            return END_OF_SEARCH;
        }
        int nextPos = zeroPos + 1;
        byte value = tiles[nextPos];
        int newIdx = getWDPtnIdx(idxV, (value - 1) % ROW_SIZE, forward);
        int newValue = getWDValue(newIdx);
        int priority = valH + newValue;
        solutionMove[costPlus1] = Direction.RIGHT;
        return nextMove(orgX + 1, orgY, zeroPos, zeroSym, priority, costPlus1,
                limit, nextPos, zeroSym + ROW_SIZE, idxH, newIdx, valH, newValue);
    }

    // shift the space to down
    private int shiftDown(int orgX, int orgY, int zeroPos, int zeroSym, int costPlus1, int limit,
            int idxH, int idxV, int valH, int valV) {
        if (terminated) {
            return END_OF_SEARCH;
        }
        int nextPos = zeroPos + ROW_SIZE;
        byte value = tiles[nextPos];
        int newIdx = getWDPtnIdx(idxH, (value - 1) / ROW_SIZE, forward);
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
            return END_OF_SEARCH;
        }
        int nextPos = zeroPos - 1;
        byte value = tiles[nextPos];
        int newIdx = getWDPtnIdx(idxV, (value - 1) % ROW_SIZE, backward);
        int newValue = getWDValue(newIdx);
        int priority = valH + newValue;
        solutionMove[costPlus1] = Direction.LEFT;
        return nextMove(orgX - 1, orgY, zeroPos, zeroSym, priority, costPlus1,
                limit, nextPos, zeroSym - ROW_SIZE, idxH, newIdx, valH, newValue);
    }

    // shift the space to up
    private int shiftUp(int orgX, int orgY, int zeroPos, int zeroSym, int costPlus1, int limit,
            int idxH, int idxV, int valH, int valV) {
        if (terminated) {
            return END_OF_SEARCH;
        }
        int nextPos = zeroPos - ROW_SIZE;
        byte value = tiles[nextPos];
        int newIdx = getWDPtnIdx(idxH, (value - 1) / ROW_SIZE, backward);
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
            updatePrio = END_OF_SEARCH;
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
    private int getWDPtnIdx(byte [] ctwd, int zeroRow) {
        int key = 0;
        int count = 0;

        while (count < ctwd.length) {
            int temp = 0;
            for (int i = 0; i < ROW_SIZE; i++) {
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
    int getWDPtnIdx(int idx, int col, boolean isForward) {
        if (isForward) {
            return wdPtnLink[idx * ROW_SIZE * 2 + col * 2];
        } else {
            return wdPtnLink[idx * ROW_SIZE * 2 + col * 2 + 1];
        }
    }

    // take a key index and return the value of walking distance
    byte getWDValue(int idx) {
        return wdPattern[idx];
    }

    // return horizontal walking distance value
    final byte getWdValueH() {
        return wdValueH;
    }

    // return vertical walking distance value
    final byte getWdValueV() {
        return wdValueV;
    }

    // return horizontal walking distance index
    final int getWdIdxH() {
        return wdIdxH;
    }

    // return vertical walking distance index
    final int getWdIdxV() {
        return wdIdxV;
    }
}
