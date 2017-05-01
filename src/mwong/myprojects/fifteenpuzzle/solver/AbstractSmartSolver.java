package mwong.myprojects.fifteenpuzzle.solver;

import mwong.myprojects.fifteenpuzzle.solver.ai.ReferenceRemote;
import mwong.myprojects.fifteenpuzzle.solver.components.ApplicationMode;
import mwong.myprojects.fifteenpuzzle.solver.components.Board;
import mwong.myprojects.fifteenpuzzle.solver.components.Direction;

import java.rmi.RemoteException;
import java.util.Arrays;

/**
 * AbstractSmartSolver is the abstract class extends AbstractSolver implements SmartSolver
 * Interface.  It contains all Solver variables and methods with SmartSolver add on features.
 *
 * <p>Dependencies : AbstractSolver.java, Board.java, Direction.java, ReferenceRemote.java
 *                   SmartSolver.java
 *
 * @author Meisze Wong
 *         www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public abstract class AbstractSmartSolver extends AbstractSolver implements SmartSolver {
    // constants
    protected final boolean tagStandard;
    protected final boolean tagAdvanced;
    protected final boolean tagSearch;
    protected final boolean tagReview;

    // solver setting
    protected final byte numPartialMoves;
    protected final byte refCutoff;
    protected boolean flagAdvancedVersion;
    protected boolean activeSmartSolver;
    protected ReferenceRemote refConnection;
    protected SmartSolverExtra extra;
    protected ApplicationMode appMode;
    // search related
    protected byte priorityAdvanced;

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
        flagAdvancedVersion = tagAdvanced;
        searchTimeoutLimit = defaultTimeoutLimit;
        activeSmartSolver = false;
        refCutoff = SolverConstants.getReferenceCutoff();
        numPartialMoves = SolverConstants.getNumPartialMoves();
        extra = null;
        this.refConnection = null;
        appMode = ApplicationMode.CONSOLE;
    }

    protected AbstractSmartSolver(ApplicationMode appMode) {
        this();
        this.appMode = appMode;
    }

    // ----- solver information lookup -----

    /**
     * Print the solver description.
     */
    @Override
    public void printDescription() {
        extra.printDescription(flagAdvancedVersion, inUseHeuristic);
    }

    // ----- Add on functions for advanced version, default setting disable feature.

    /**
     * Set the advance search feature with the given flag.
     *
     * @param flag the boolean represent the ON/OFF advanced feature
     */
    @Override
    public boolean versionSwitch(boolean flag) {
        if (activeSmartSolver) {
            flagAdvancedVersion = flag;
            return true;
        } else {
            flagAdvancedVersion = tagStandard;
            return false;
        }
    }

    /**
     * Set the ReferenceRemote connection with the given connection.
     *
     * @param refConnection set the ReferenceRemote connection with the given connection
     */
    @Override
    public void setReferenceConnection(ReferenceRemote refConnection) {
    	activeSmartSolver = false;
    	flagAdvancedVersion = tagStandard;
    	this.refConnection = null;
        try {
            if (refConnection != null && refConnection.getActiveMap() == null) {
                System.out.println("Attention: Reference board collection unavailable."
                        + " Advanced estimate will use standard estimate.");
            } else {
                activeSmartSolver = true;
                extra = new SmartSolverExtra();
                this.refConnection = refConnection;
            }
        } catch (RemoteException ex) {
            System.err.println(this.getClass().getSimpleName()
                    + " - Attention: Server connection failed. Resume to standard version.\n");
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
        lastBoard = board;
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

    // set priorityAdvanced with given board or type of search
    protected void setPriorityAdvanced(Board board, boolean isSearch) {
        if (!activeSmartSolver) {
            priorityAdvanced = priorityGoal;
            return;
        }

        AdvancedRecord record = null;
        try {
            record = extra.advancedContains(board, isSearch,
                    refConnection.getActiveMap());
        } catch (RemoteException ex) {
            System.err.println("\n" + this.getClass().getSimpleName() + " - Connection lost."
                    + "  Remaining process resume to standard version.");
            activeSmartSolver = false;
            flagAdvancedVersion = tagStandard;
            this.refConnection = null;
            priorityAdvanced = priorityGoal;
            return;
        }

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

        try {
            priorityAdvanced = extra.advancedEstimate(board, priorityAdvanced, refCutoff,
                    refConnection.getActiveMap());
        } catch (RemoteException ex) {
            System.err.println("\n" + this.getClass().getSimpleName() + " - Connection lost."
                    + "  Remaining process resume to standard version.");
            activeSmartSolver = false;
            flagAdvancedVersion = tagStandard;
            this.refConnection = null;
            priorityAdvanced = priorityGoal;
            return;
        }

        if ((priorityAdvanced - priorityGoal) % 2 == 1) {
            priorityAdvanced++;
        }
    }

    // shift the preset moves and return Board object after the last move.
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

    // update the search results after finished advaned search.
    protected void afterAdvancedSearch(int limit, Direction[] dupSolution) {
        if (solved) {
            System.arraycopy(solutionMove, 2, dupSolution, numPartialMoves + 1,
                    limit - numPartialMoves);
            solutionMove = dupSolution;
        }
        steps = (byte) limit;

        if (flagMessage) {
            if (timeout) {
                System.out.printf("\tNodes : %-15s timeout\n", Integer.toString(searchNodeCount));
            } else {
                System.out.printf("\tNodes : %-15s  " + stopwatch.currentTime() + "s\n",
                        Integer.toString(searchNodeCount));
            }
        }
    }
}
