package mwong.myprojects.fifteenpuzzle.solver;

import mwong.myprojects.fifteenpuzzle.solver.ai.ReferenceAccumulator;
import mwong.myprojects.fifteenpuzzle.solver.components.Board;
import mwong.myprojects.fifteenpuzzle.solver.components.Direction;

import java.util.Arrays;

/**
 * AbstractSmartSolver is the abstract class extends AbstraceSolver implements SmartSolver Interface
 * with additional functions for 15 puzzle optimal solver advanced version.
 *
 * <p>Dependencies : AbstractSolver.java, Board.java, Direction.java, ReferenceAccumulator.java
 *                   SmartSolver.java
 *
 * @author   Meisze Wong
 *           www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public abstract class AbstractSmartSolver extends AbstractSolver implements SmartSolver {
    // constants
    protected final boolean tagStandard;
    protected final boolean tagAdvanced;
    protected final boolean tagSearch;
    protected final boolean tagReview;

    // solver setting
    protected boolean flagAdvancedVersion;
    protected boolean activeSmartSolver;
    // search related
    protected byte priorityAdvanced;
    // search results
    protected final byte numPartialMoves;
    protected final byte refCutoff;
    protected ReferenceAccumulator refAccumulator;
    protected SmartSolverExtra extra;

    protected AbstractSmartSolver() {
        // load the constants
        tagAdvanced = SolverConstants.isTagAdvanced();
        tagStandard = !tagAdvanced;
        tagSearch = SolverConstants.isTagSearch();
        tagReview = !tagSearch;

        // initialize default setting
        lastBoard = goalBoard;
        flagMessage = onSwitch;
        flagTimeout = onSwitch;
        flagAdvancedVersion = tagStandard;
        searchTimeoutLimit = defaultTimeoutLimit;
        activeSmartSolver = false;
        refCutoff = SolverConstants.getReferenceCutoff();
        numPartialMoves = SolverConstants.getNumPartialMoves();
        extra = null;
        this.refAccumulator = null;
    }

    // ----- solver settings -----

    /**
     *  Print the solver description.
     */
    @Override
    public void printDescription() {
        extra.printDescription(flagAdvancedVersion, inUseHeuristic);
    }

    // ----- Add on functions for advanced version, default setting disable feature.

    /**
     *  Set the advance search feature with the given flag.
     *
     *  @param flag the boolean represent the ON/OFF advanced feature
     */
    @Override
    public boolean versionSwitch(boolean flag) {
        if (activeSmartSolver) {
            flagAdvancedVersion = flag;
            return true;
        } else {
            System.out.println("Referece board collection unavailable."
                + " Advanced search feature will act as standard search.");
            flagAdvancedVersion = tagStandard;
            return false;
        }
    }

    /**
     * Returns the byte value of the standard version of the heuristic value of the board.
     *
     * @return byte value of the standard version of the heuristic value
     */
    @Override
    public byte heuristicStandard(Board board) {
        if (board == null) {
            throw new IllegalArgumentException("Board is null");
        }
        if (!board.isSolvable()) {
            return -1;
        }
        return heuristic(board);
    }

    /**
     * Returns the byte value of the advanced version of the heuristic value of the board,
     * if advanced version is unavailable, return standard version instead.
     *
     * @return byte value of the advanced version of the heuristic value
     */
    @Override
    public byte heuristicAdvanced(Board board) {
        return heuristicStandard(board);
    }

    // ----- heuristic and solve the puzzle -----

    // board initial
    protected final void initialize(Board board) {
        super.initialize(board);
        priorityAdvanced = -1;
    }

    // advanced version only: initialize lastDepthSummary from the given Direction
    protected final void setLastDepthSummary(Direction dir) {
        lastDepthSummary = new int[4 * 2];
        int dirValue = dir.getValue();
        for (int i = 0; i < 4; i++) {
            if (i == dirValue) {
                lastDepthSummary[i + 4] = 1;
            } else {
                lastDepthSummary[i] = endOfSearch;
            }
        }
    }

    protected void setPriorityAdvanced(Board board, boolean isSearch) {
        AdvancedRecord record = extra.advancedContains(board, isSearch, refAccumulator);
        if (record != null) {
            priorityAdvanced = record.getEstimate();
            if (record.hasPartialMoves()) {
                solutionMove = record.getPartialMoves();
            }
        }

        if (priorityAdvanced != -1) {
            return;
        }

        priorityAdvanced = priorityGoal;
        if (priorityAdvanced < refCutoff) {
            return;
        }

        priorityAdvanced = extra.advancedEstimate(board, priorityAdvanced, refCutoff,
                refAccumulator.getActiveMap());

        if ((priorityAdvanced - priorityGoal) % 2 == 1) {
            priorityAdvanced++;
        }
    }

    protected Board prepareAdvancedSearch(int limit, Direction[] dupSolution) {
        System.arraycopy(solutionMove, 1, dupSolution, 1, numPartialMoves);
        Board board = new Board(tiles);
        for (int i = 1; i < numPartialMoves; i++) {
            board = board.shift(dupSolution[i]);
            assert board != null : i + "board is null" + Arrays.toString(solutionMove)
            + (new Board(tiles));
        }
        clearHistory();
        return board;
    }

    protected void afterAdvancedSearch(int limit, Direction[] dupSolution) {
        if (solved) {
            System.arraycopy(solutionMove, 2, dupSolution, numPartialMoves + 1,
                    limit - numPartialMoves);
            solutionMove = dupSolution;
        }
        steps = (byte) limit;
        searchDepth = limit;

        if (flagMessage) {
            if (timeout) {
                System.out.printf("\tNodes : %-15s timeout\n", Integer.toString(searchNodeCount));
            } else {
                System.out.printf("\tNodes : %-15s  " + stopwatch.currentTime() + "s\n",
                        Integer.toString(searchNodeCount));
            }
        }
    }

    // solve the puzzle using interactive deepening A* algorithm
    protected abstract void idaStar(int limit);

}
