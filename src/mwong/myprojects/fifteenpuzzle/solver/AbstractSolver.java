package mwong.myprojects.fifteenpuzzle.solver;

import mwong.myprojects.fifteenpuzzle.solver.components.Board;
import mwong.myprojects.fifteenpuzzle.solver.components.Direction;
import mwong.myprojects.fifteenpuzzle.utilities.Stopwatch;

import java.util.Arrays;

/**
 * AbstractSolver is the abstract class extends Solver Interface of 15 puzzle that
 * has the following variables and methods.
 *
 * <p>Dependencies : Board.java, Direction.java, Solver.java, Stopwatch.java
 *
 * @author Meisze Wong
 *         www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public abstract class AbstractSolver implements Solver {
    // constants
    protected final int puzzleSize;
    protected final int rowSize;
    protected final int maxMoves;
    protected final byte endOfSearch;
    protected final int defaultTimeoutLimit;
    protected final boolean onSwitch;
    protected final boolean offSwitch;
    protected final byte[] symmetryPos;
    protected final byte[] symmetryVal;
    protected final Board goalBoard;
    protected final int resetKey;
    protected final int cwKey;
    protected final int ccwKey;

    // solver setting
    protected boolean flagTimeout;
    protected boolean flagMessage;
    protected int searchTimeoutLimit;
    protected HeuristicOptions inUseHeuristic;
    // board related
    protected byte[] tiles;
    protected int zeroX;
    protected int zeroY;
    protected boolean isSolvable;
    // search related
    protected Board lastBoard;
    protected byte priorityGoal;
    protected int[] lastDepthSummary;
    protected Stopwatch stopwatch;
    // search results
    protected byte steps;
    protected int searchDepth;
    protected int searchNodeCount;
    protected double searchTime;
    protected boolean solved;
    protected boolean timeout;
    protected boolean terminated;
    protected Direction[] solutionMove;

    protected AbstractSolver() {
        // load the constants
        puzzleSize = SolverConstants.getPuzzleSize();
        rowSize = SolverConstants.getRowSize();
        maxMoves = SolverConstants.getMaxMoves();
        endOfSearch = SolverConstants.getEndOfSearch();
        defaultTimeoutLimit = SolverProperties.getTimeoutLimit();
        onSwitch = SolverConstants.isOnSwitch();
        offSwitch = !onSwitch;
        symmetryPos = SolverConstants.getSymmetryPos();
        symmetryVal = SolverConstants.getSymmetryVal();
        goalBoard = SolverConstants.getGoalBoard();
        resetKey = Rotation.RST.getValue();
        cwKey = Rotation.CW.getValue();
        ccwKey = Rotation.CCW.getValue();

        // initialize default setting
        lastBoard = goalBoard;
        flagMessage = onSwitch;
        flagTimeout = onSwitch;
        searchTimeoutLimit = defaultTimeoutLimit;
    }

    // ----- solver settings -----

    /**
     * Return HeuristicOptions of object instance that implements Solver interface.
     *
     * @return HeuristicOptions of solver
     */
    @Override
    public final HeuristicOptions getHeuristicOptions() {
        return inUseHeuristic;
    }

    /**
     * Set the message feature with the given flag.
     *
     * @param flag the boolean represent the ON/OFF message feature
     */
    @Override
    public final void messageSwitch(boolean flag) {
        flagMessage = flag;
    }

    /**
     * Set the timeout feature with the given flag.
     *
     * @param flag the boolean represent the ON/OFF timeout feature
     */
    @Override
    public final void timeoutSwitch(boolean flag) {
        flagTimeout = flag;
    }

    /**
     * Set the timeout limit with the given value in seconds.
     *
     * @param seconds the integer represent the timeout limit in seconds
     */
    @Override
    public final void setTimeoutLimit(int seconds) {
        searchTimeoutLimit = seconds;
    }

    // ----- solver information lookup -----

    /**
     * Print the solver description.
     */
    @Override
    public void printDescription() {
        System.out.println("15 puzzle solver using " + inUseHeuristic.getDescription());
    }

    /**
     * Returns the boolean value represent timeout feature in use.
     *
     * @return boolean value represent timeout feature
     */
    @Override
    public final boolean isFlagTimeout() {
        return flagTimeout;
    }

    /**
     * Returns the integer of timeout limit in use.
     *
     * @return integer of timeout limit
     */
    @Override
    public final int getSearchTimeoutLimit() {
        return searchTimeoutLimit;
    }

    // ----- heuristic and solve the puzzle -----

    // reset and clear variables from previous search results
    protected final void clearHistory() {
        isSolvable = true;
        solved = false;
        timeout = false;
        terminated = false;
        searchDepth = 0;
        searchNodeCount = 0;
        lastDepthSummary = new int [4 * 2];
        solutionMove = new Direction[maxMoves + 1];
        solutionMove[0] = Direction.NONE;
        steps = 0;
    }

    // board initial
    protected void initialize(Board board) {
        lastBoard = board;
        zeroX = board.getZeroX();
        zeroY = board.getZeroY();
        tiles = board.getTiles();
        priorityGoal = 0;
    }

    /**
     * Find the optimal path to goal state if the given board is solvable,
     * and return the search time in seconds.
     *
     * @param board the initial puzzle Board object to solve
     */
    @Override
    public final void findOptimalPath(Board board) {
        if (board == null) {
            throw new IllegalArgumentException("Board is null");
        }

        stopwatch = new Stopwatch();
        stopwatch.stop();
        stopwatch.reset();
        int limit = -1;
        if (board.isSolvable()) {
            clearHistory();
            if (board.isGoal()) {
                solved = true;
                terminated = true;
            } else {
                stopwatch.start();
                setLastDepthSummary(board);
                limit = heuristic(board);
                assert limit > 0 : "Board must be solvable and is not the goal state.";
                idaStar(limit);
                assert checkGoal(board) : "Not end at goal state.";
            }
        } else {
            isSolvable = false;
        }
        searchTime = stopwatch.currentTime();
        stopwatch = null;
    }

    // initialize lastDepthSummary from the given board object
    protected final void setLastDepthSummary(Board board) {
        lastDepthSummary = new int[4 * 2];
        int[] validMoves = board.getValidMoves();
        for (int i = 0; i < 4; i++) {
            if (validMoves[i] == 0) {
                lastDepthSummary[i] = endOfSearch;
            } else {
                lastDepthSummary[i + 4] = board.getValidMoves()[i];
            }
        }
    }

    // solve the puzzle using interactive deepening A* algorithm
    protected abstract void idaStar(int limit);

    // maximum allow 5 continues clockwise turn.
    protected boolean isValidClockwise(int swirlKey) {
        return (swirlKey & 0x07FF) != 0x0155;
    }

    // maximum allow 4 continues counterclockwise turn.
    protected boolean isValidCounterClockwise(int swirlKey) {
        return (swirlKey & 0x00FF) != 0x00AA;
    }

    // assertion tool : check the initial board reach the goal state after the solution moves.
    private boolean checkGoal(Board initial) {
        if (initial == null) {
            throw new IllegalArgumentException("Board is null");
        }
        if (solved) {
            Board board = new Board(initial.getTiles());
            int count = 0;
            Direction dir = Direction.NONE;
            while (count < steps) {
                if (++count <= steps) {
                    dir = solutionMove[count];
                    if (board == null) {
                        System.out.println(steps + "\t" + Arrays.toString(solutionMove));
                        System.out.println("stop at " + count);
                        System.out.println(new Board(initial.getTiles()));
                    }
                    board = board.shift(dir);
                }
            }
            if (!board.isGoal()) {
                return false;
            }
            return true;
        }
        return true;
    }

    // ----- search results -----

    /**
     * Returns the boolean value represent the search has timeout.
     *
     * @return boolean value represent the search has timeout
     */
    @Override
    public final boolean isSearchTimeout() {
        if (!isSolvable) {
            return false;
        }
        return timeout;
    }

    /**
     * Returns the integer value of search depth when the search terminated.
     *
     * @return integer value of search depth when the search terminated
     */
    @Override
    public final int searchTerminateAtDepth() {
        if (!isSolvable) {
            return -1;
        }
        return searchDepth;
    }

    /**
     * Returns the integer value of total number of nodes generated during the search.
     *
     * @return integer value of total number of nodes generated during the search
     */
    @Override
    public final int searchNodeCount() {
        if (!isSolvable) {
            return -1;
        }
        return searchNodeCount;
    }

    /**
     * Returns the double value of total time of search in seconds.
     *
     * @return double value of total time of search in seconds
     */
    @Override
    public final double searchTime() {
        return searchTime;
    }

    /**
     * Returns the integer value of minimum moves to the goal state.
     *
     * @return integer value of minimum moves to the goal state
     */
    @Override
    public final byte moves() {
        if (!isSolvable) {
            return -1;
        }
        if (timeout) {
            return -1;
        }
        return steps;
    }

    /**
     * Returns the array of Directions of each move to the goal state.
     *
     * @return array of Directions of each move to the goal state
     */
    @Override
    public final Direction[] solution() {
        if (!isSolvable) {
            return null;
        }
        if (timeout) {
            return null;
        }
        return solutionMove;
    }
}
