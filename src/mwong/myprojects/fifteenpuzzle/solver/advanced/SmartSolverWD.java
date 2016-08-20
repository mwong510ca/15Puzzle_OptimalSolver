package mwong.myprojects.fifteenpuzzle.solver.advanced;

import mwong.myprojects.fifteenpuzzle.solver.advanced.ai.ReferenceAccumulator;
import mwong.myprojects.fifteenpuzzle.solver.components.Board;
import mwong.myprojects.fifteenpuzzle.solver.components.Direction;
import mwong.myprojects.fifteenpuzzle.solver.standard.SolverWD;

/**
 * SmartSolverWD extends SolverWD.  The advanced version extend the standard solver
 * using the reference boards collection to boost the initial estimate.
 *
 * <p>Dependencies : AdvancedRecord.java, Board.java, Direction.java, ReferenceAccumulator.java,
 *                   SmartSolverConstants.java, SmartSolverExtra.java, SolverWD.java
 *
 * @author   Meisze Wong
 *           www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public class SmartSolverWD extends SolverWD {
    private final byte numPartialMoves;
    private final byte refCutoff;
    private final ReferenceAccumulator refAccumulator;
    private final SmartSolverExtra extra;

    /**
     * Initializes SolverWD object.  If refAccumlator is null or empty,
     * it will act as standard version.
     *
     * @param refAccumulator the given ReferenceAccumulator object
     */
    public SmartSolverWD(ReferenceAccumulator refAccumulator) {
        super();
        if (refAccumulator == null || refAccumulator.getActiveMap() == null) {
            System.out.println("Referece board collection unavailable."
                    + " Resume to the 15 puzzle solver standard version.");
            extra = null;
            this.refAccumulator = null;
            refCutoff = 0;
            numPartialMoves = 0;
        } else {
            activeSmartSolver = true;
            extra = new SmartSolverExtra();
            this.refAccumulator = refAccumulator;
            refCutoff = SmartSolverConstants.getReferenceCutoff();
            numPartialMoves = SmartSolverConstants.getNumPartialMoves();
        }
    }

    /**
     *  Print solver description.
     */
    @Override
    public void printDescription() {
        extra.printDescription(flagAdvancedPriority, inUseHeuristic);
    }

    /**
     * Returns the heuristic value of the given board based on the solver setting.
     *
     * @param board the initial puzzle Board object to solve
     * @return byte value of the heuristic value of the given board
     */
    @Override
    public byte heuristic(Board board) {
        return heuristic(board, flagAdvancedPriority, tagSearch);
    }

    // overload method to calculate the heuristic value of the given board and conditions
    private byte heuristic(Board board, boolean isAdvanced, boolean isSearch) {
        if (!board.isSolvable()) {
            return -1;
        }

        priorityAdvanced = -1;
        if (!board.equals(lastBoard) || isSearch) {
            lastBoard = board;

            zeroX = board.getZeroX();
            zeroY = board.getZeroY();
            tiles = board.getTiles();
            tilesSym = board.getTilesSym();
            lastDepthSummary = new int [rowSize * 2];
            System.arraycopy(board.getValidMoves(), 0, lastDepthSummary, rowSize, rowSize);

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
        if (!isAdvanced) {
            return priorityGoal;
        }

        AdvancedRecord record = extra.advancedContains(board, isSearch, refAccumulator);
        if (record != null) {
            priorityAdvanced = record.getEstimate();
            if (record.hasPartialMoves()) {
                solutionMove = record.getPartialMoves();
            }
        }
        if (priorityAdvanced != -1) {
            return priorityAdvanced;
        }

        priorityAdvanced = priorityGoal;
        if (priorityAdvanced < refCutoff) {
            return priorityAdvanced;
        }

        priorityAdvanced = extra.advancedEstimate(board, priorityAdvanced,
                refCutoff, refAccumulator.getActiveMap());

        if ((priorityAdvanced - priorityGoal) % 2 == 1) {
            priorityAdvanced++;
        }
        return priorityAdvanced;
    }

    /**
     * Returns the original heuristic value of the given board.
     *
     * @return byte value of the original heuristic value of the given board
     */
    @Override
    public byte heuristicStandard(Board board) {
        if (board == null) {
            throw new IllegalArgumentException("Board is null");
        }
        if (!board.isSolvable()) {
            return -1;
        }
        return heuristic(board, tagStandard, tagReview);
    }

    /**
     * Returns the advanced heuristic value of the given board.
     *
     * @return byte value of the advanced heuristic value of the given board
     */
    @Override
    public byte heuristicAdvanced(Board board) {
        if (board == null) {
            throw new IllegalArgumentException("Board is null");
        }
        if (!board.isSolvable()) {
            return -1;
        }
        if (!activeSmartSolver) {
            heuristic(board, tagStandard, tagReview);
        }
        return heuristic(board, tagAdvanced, tagReview);
    }

    // solve the puzzle using interactive deepening A* algorithm
    protected void idaStar(int limit) {
        if (solutionMove[1] != null) {
            advancedSearch(limit);
            return;
        }

        int countDir = 0;
        for (int i = 0; i < rowSize; i++) {
            if (lastDepthSummary[i + rowSize] > 0) {
                countDir++;
            }
        }

        // quick scan for advanced priority, determine the start order for optimization
        if (flagAdvancedPriority && countDir > 1) {
            int initLimit = priorityGoal;
            while (initLimit < limit) {
                idaCount = 0;
                dfsStartingOrder(zeroX, zeroY, 0, initLimit, wdIdxH, wdIdxV, wdValueH, wdValueV);
                initLimit += 2;

                boolean overload = false;
                for (int i = rowSize; i < rowSize * 2; i++) {
                    if (lastDepthSummary[i] > 10000) {
                        overload = true;
                        break;
                    }
                }
                if (overload) {
                    break;
                }
            }
        }

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
                    System.out.printf("\tNodes : %-15s  " + stopwatch.currentTime() + "s\n",
                            Integer.toString(idaCount));
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
        System.arraycopy(solutionMove, 1, dupSolution, 1, numPartialMoves);

        Board board = new Board(tiles);
        for (int i = 1; i < numPartialMoves; i++) {
            board = board.shift(dupSolution[i]);
        }
        heuristic(board, tagStandard, tagSearch);

        int firstDirValue = dupSolution[numPartialMoves].getValue();
        for (int i = 0; i < 4; i++) {
            if (i != firstDirValue) {
                lastDepthSummary[i + 4] = 0;
            } else {
                lastDepthSummary[i + 4] = 1;
            }
        }

        idaCount = numPartialMoves;
        if (flagMessage) {
            System.out.print("ida limit " + limit);
        }
        dfsStartingOrder(zeroX, zeroY, 0, limit - numPartialMoves + 1, wdIdxH, wdIdxV,
                wdValueH, wdValueV);
        if (solved) {
            System.arraycopy(solutionMove, 2, dupSolution, numPartialMoves + 1,
                    limit - numPartialMoves);
            solutionMove = dupSolution;
        }
        steps = (byte) limit;
        searchDepth = limit;
        searchNodeCount += idaCount;

        if (flagMessage) {
            if (timeout) {
                System.out.printf("\tNodes : %-15s timeout\n", Integer.toString(idaCount));
            } else {
                System.out.printf("\tNodes : %-15s  " + stopwatch.currentTime() + "s\n",
                        Integer.toString(idaCount));
            }
        }
    }
}
