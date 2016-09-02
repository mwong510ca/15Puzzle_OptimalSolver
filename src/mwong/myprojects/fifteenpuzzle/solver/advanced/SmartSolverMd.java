package mwong.myprojects.fifteenpuzzle.solver.advanced;

import mwong.myprojects.fifteenpuzzle.solver.SolverConstants;
import mwong.myprojects.fifteenpuzzle.solver.ai.ReferenceAccumulator;
import mwong.myprojects.fifteenpuzzle.solver.components.Board;
import mwong.myprojects.fifteenpuzzle.solver.components.Direction;
import mwong.myprojects.fifteenpuzzle.solver.standard.SolverMd;

import java.util.Arrays;

/**
 * SmartSolverMd extends SolverMd.  The advanced version extend the standard solver
 * using the reference boards collection to boost the initial estimate.
 *
 * <p>Dependencies : AdvancedRecord.java, Board.java, Direction.java, ReferenceAccumulator.java,
 *                   SmartSolverConstants.java, SmartSolverExtra.java, SolverConstants.java,
 *                   SolverMD.java
 *
 * @author   Meisze Wong
 *           www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public class SmartSolverMd extends SolverMd {
    private final byte numPartialMoves;
    private final byte refCutoff;
    private final ReferenceAccumulator refAccumulator;
    private final SmartSolverExtra extra;

    /**
     * Initializes SmartSolverMd object.
     *
     * @param refAccumulator the given ReferenceAccumulator object
     */
    public SmartSolverMd(ReferenceAccumulator refAccumulator) {
        this(!SolverConstants.isTagLinearConflict(), refAccumulator);
    }

    /**
     * Initializes SmartSolverMd object.  If refAccumlator is null or empty,
     * it will act as standard version.
     *
     * @param lcFlag boolean flag for linear conflict feature
     * @param refAccumulator the given ReferenceAccumulator object
     */
    public SmartSolverMd(boolean lcFlag, ReferenceAccumulator refAccumulator) {
        super(lcFlag);
        if (refAccumulator == null || refAccumulator.getActiveMap() == null) {
            System.out.println("Attention: Referece board collection unavailable."
                    + " Advanced estimate will use standard estimate.");
            extra = null;
            this.refAccumulator = null;
            refCutoff = 0;
            numPartialMoves = 0;
        } else {
            activeSmartSolver = true;
            extra = new SmartSolverExtra();
            this.refAccumulator = refAccumulator;
            refCutoff = SolverConstants.getReferenceCutoff();
            numPartialMoves = SolverConstants.getNumPartialMoves();
        }
    }

    /**
     *  Print solver description.
     */
    @Override
    public void printDescription() {
        extra.printDescription(flagAdvancedVersion, inUseHeuristic);
    }

    /**
     * Returns the heuristic value of the given board based on the solver setting.
     *
     * @param board the initial puzzle Board object to solve
     * @return byte value of the heuristic value of the given board
     */
    @Override
    public byte heuristic(Board board) {
        return heuristic(board, flagAdvancedVersion, tagSearch);
    }

    // overload method to calculate the heuristic value of the given board and conditions
    private byte heuristic(Board board, boolean isAdvanced, boolean isSearch) {
        if (!board.isSolvable()) {
            return -1;
        }

        if (!board.equals(lastBoard)) {
            initialize(board);
            tilesSym = board.getTilesSym();
            setLastDepthSummary(board);

            priorityGoal = 0;
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
        } else if (isSearch) {
            zeroX = board.getZeroX();
            zeroY = board.getZeroY();
            tiles = board.getTiles();
            tilesSym = board.getTilesSym();
            setLastDepthSummary(board);
        }

        if (!isAdvanced) {
            return priorityGoal;
        } else if (!isSearch && priorityAdvanced != -1) {
            return priorityAdvanced;
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

        priorityAdvanced = extra.advancedEstimate(board, priorityAdvanced, refCutoff,
                refAccumulator.getActiveMap());

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
    @Override
    protected void idaStar(int limit) {
        if (solutionMove[1] != null) {
            advancedSearch(limit);
            return;
        }
        super.idaStar(limit);
    }

    // skip the first 8 moves from stored record then solve the remaining puzzle
    // using depth first search with exact number of steps of optimal solution
    private void advancedSearch(int limit) {
        Direction[] dupSolution = new Direction[limit + 1];
        System.arraycopy(solutionMove, 1, dupSolution, 1, numPartialMoves);

        Board board = new Board(tiles);
        for (int i = 1; i < numPartialMoves; i++) {
            board = board.shift(dupSolution[i]);
            assert board != null : i + "board is null" + Arrays.toString(solutionMove)
            + (new Board(tiles));
        }
        clearHistory();
        heuristic(board, tagStandard, tagSearch);
        setLastDepthSummary(dupSolution[numPartialMoves]);

        idaCount = numPartialMoves;
        if (flagMessage) {
            System.out.print("ida limit " + limit);
        }
        dfsStartingOrder(zeroX, zeroY, limit - numPartialMoves + 1, priorityGoal);
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
