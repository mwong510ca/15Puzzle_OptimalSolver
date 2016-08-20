package mwong.myprojects.fifteenpuzzle.solver.advanced;

import mwong.myprojects.fifteenpuzzle.solver.advanced.ai.ReferenceAccumulator;
import mwong.myprojects.fifteenpuzzle.solver.components.Board;
import mwong.myprojects.fifteenpuzzle.solver.components.Direction;
import mwong.myprojects.fifteenpuzzle.solver.components.PatternOptions;
import mwong.myprojects.fifteenpuzzle.solver.standard.SolverPDWD;

/**
 * SmartSolverPDWD extends SolverPDWD.  The advanced version extend the standard solver
 * using the reference boards collection to boost the initial estimate.
 *
 * <p>Dependencies : AdvancedRecord.java, Board.java, Direction.java, PatternOptions.java,
 *                   ReferenceAccumulator.java, SmartSolverConstants.java, SmartSolverExtra.java,
 *                   SolverPDWD.java
 *
 * @author   Meisze Wong
 *           www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public class SmartSolverPDWD extends SolverPDWD {
    private final byte numPartialMoves;
    private final byte refCutoff;
    private final ReferenceAccumulator refAccumulator;
    private final SmartSolverExtra extra;

    /**
     * Initializes SolverPDWD object using default preset pattern.
     *
     * @param refAccumulator the given ReferenceAccumulator object
     */
    public SmartSolverPDWD(ReferenceAccumulator refAccumulator) {
        this(defaultPattern, refAccumulator);
    }

    /**
     * Initializes SolverPDWD object with given preset pattern.
     *
     * @param presetPattern the given preset pattern type
     * @param refAccumulator the given ReferenceAccumulator object
     */
    public SmartSolverPDWD(PatternOptions presetPattern, ReferenceAccumulator refAccumulator) {
        this(presetPattern, 0, refAccumulator);
    }

    /**
     * Initializes SolverPDWD object with given preset pattern and option. If refAccumlator
     * is null or empty, it will act as standard version.
     *
     * @param presetPattern the given preset pattern type
     * @param choice the given preset pattern option
     * @param refAccumulator the given ReferenceAccumulator object
     */
    public SmartSolverPDWD(PatternOptions presetPattern, int choice,
            ReferenceAccumulator refAccumulator) {
        super(presetPattern, choice);
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
        printInUsePattern();
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
            // walking distance from parent/superclass
            priorityGoal = super.heuristic(board);
            priorityAdvanced = -1;
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
                dfsStartingOrder(zeroX, zeroY, 0, initLimit, regVal, symVal);
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

            dfsStartingOrder(zeroX, zeroY, 0, limit, regVal, symVal);
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

        dfsStartingOrder(zeroX, zeroY, 0, limit - numPartialMoves + 1, regVal, symVal);
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
