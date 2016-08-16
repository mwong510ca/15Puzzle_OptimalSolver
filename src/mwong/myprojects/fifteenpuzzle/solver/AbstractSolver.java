/****************************************************************************
 *  @author   Meisze Wong
 *            www.linkedin.com/pub/macy-wong/46/550/37b/
 *
 *  Compilation  : javac SolverAbstract.java
 *  Dependencies : Board.java, Direction.java, Stopwatch.java,
 *                 SolverInterface.java, AdvancedAccumulator.java,
 *                 AdvancedBoard.java, AdvancedMoves.java
 *
 *  SolverAbstract class implements SolverInterface of 15 puzzle that has the
 *  following variables and methods.
 *
 ****************************************************************************/

package mwong.myprojects.fifteenpuzzle.solver;

import mwong.myprojects.fifteenpuzzle.solver.components.Board;
import mwong.myprojects.fifteenpuzzle.solver.components.Direction;
import mwong.myprojects.fifteenpuzzle.solver.components.PuzzleProperties;
import mwong.myprojects.fifteenpuzzle.utilities.Stopwatch;

import java.util.Arrays;

public abstract class AbstractSolver implements Solver {
    protected final int puzzleSize;
    protected final int rowSize;
    protected final int maxMoves;
    protected final byte endOfSearch;
    protected final int defaultTimeoutLimit;
    protected final boolean onSwitch;
    protected final boolean offSwitch;
    protected final boolean tagStandard;
    protected final boolean tagAdvanced;
    protected final boolean tagSearch;
    protected final boolean tagReview;
    protected final byte[] symmetryPos;
    protected final byte[] symmetryVal;

    protected boolean flagTimeout;
    protected boolean flagMessage;
    protected boolean flagAdvancedPriority;
    protected int searchTimeoutLimit;
    protected HeuristicType inUseHeuristic;

    protected byte[] tiles;
    protected Board lastBoard;
    protected byte priorityGoal;
    protected byte priorityAdvanced;
    protected int zeroX;
    protected int zeroY;

    protected byte steps;
    protected int searchDepth;
    protected int searchNodeCount;
    protected double searchTime;
    protected boolean solved;
    protected boolean timeout;
    protected boolean terminated;
    protected boolean isSolvable;
    protected Direction[] solutionMove;
    protected int[] priority1stMove;
    protected Stopwatch stopwatch;

    protected AbstractSolver() {
        puzzleSize = SolverProperties.getPuzzleSize();
        rowSize = SolverProperties.getRowSize();
        maxMoves = SolverProperties.getMaxMoves();
        endOfSearch = SolverProperties.getEndOfSearch();
        defaultTimeoutLimit = SolverProperties.getDefaultTimeoutLimit();
        onSwitch = SolverProperties.isOnSwitch();
        offSwitch = !onSwitch;
        tagAdvanced = SolverProperties.isTagAdvanced();
        tagStandard = !tagAdvanced;
        tagSearch = SolverProperties.isTagSearch();
        tagReview = !tagSearch;
        symmetryPos = SolverProperties.getSymmetryPos();
        symmetryVal = SolverProperties.getSymmetryVal();

        lastBoard = new Board(PuzzleProperties.getGoalTiles());
        flagMessage = onSwitch;
        flagTimeout = onSwitch;
        searchTimeoutLimit = defaultTimeoutLimit;
    }

    /**
     *  Return HeuristicType of object instance that implements SolverInterface.
     *
     *  @return HeuristicType of solver
     */
    @Override
    public final HeuristicType getHeuristicType() {
        return inUseHeuristic;
    }

    /**
     *  Print solver description.
     */
    @Override
    public void printDescription() {
        System.out.println("15 puzzle solver using " + inUseHeuristic.getDescription());
    }

    /**
     *  Set the message feature with the given flag.
     *
     *  @param flag the boolean represent the ON/OFF message feature
     */
    @Override
    public final void messageSwitch(boolean flag) {
        flagMessage = flag;
    }

    /**
     *  Set the timeout feature with the given flag.
     *
     *  @param flag the boolean represent the ON/OFF timeout feature
     */
    @Override
    public final void timeoutSwitch(boolean flag) {
        flagTimeout = flag;
    }

	@Override
	public void advPrioritySwitch(boolean flag) {
		flagAdvancedPriority = flag;	
	}

    /**
     * Returns the flagTimeout.
     *
     * @return the flagTimeout
     */
    @Override
    public final boolean isFlagTimeout() {
        return flagTimeout;
    }

    /**
     *  Set the timeout limit with the given value in seconds.
     *
     *  @param seconds the integer represent the timeout limit in seconds
     */
    @Override
    public final void setTimeoutLimit(int seconds) {
        searchTimeoutLimit = seconds;
    }

    /**
     * Rreturn the searchTimeoutLimit.
     *
     * @return the searchTimeoutLimit
     */
    @Override
    public final int getSearchTimeoutLimit() {
        return searchTimeoutLimit;
    }

    // reset and clear variables from previous search results
    protected final void clearHistory() {
        isSolvable = true;
        solved = false;
        timeout = false;
        terminated = false;
        searchDepth = 0;
        searchNodeCount = 0;
        priority1stMove = new int [rowSize * 2];
        solutionMove = new Direction[maxMoves + 1];
        solutionMove[0] = Direction.NONE;
        steps = 0;
    }

    // reset and clear variables from previous search results
    protected final void initialize(Board board) {
        lastBoard = board;
        zeroX = board.getZeroX();
        zeroY = board.getZeroY();
        tiles = board.getTiles();
        priorityGoal = 0;
    }

    /**
     * Returns the original heuristic value of the board.
     *
     * @return byte value of the original heuristic value of the board
     */
    @Override
    public final byte heuristic(Board board) {
        return heuristicStandard(board);
    }

    /**
     * Returns the original heuristic value of the board.
     *
     * @return byte value of the original heuristic value of the board
     */
    @Override
    public byte heuristicStandard(Board board) {
        if (board == null) {
            throw new IllegalArgumentException("Board is null");
        }
        if (board.equals(lastBoard)) {
            return priorityGoal;
        }
        return heuristic(board, tagStandard, tagReview);
    }

    /**
     * Returns the original heuristic value of the board.
     *
     * @return byte value of the original heuristic value of the board
     */
    @Override
    public final byte heuristicAdvanced(Board board) {
        if (board == null) {
            throw new IllegalArgumentException("Board is null");
        }
        if (board.equals(lastBoard) && priorityAdvanced != -1) {
        	return priorityAdvanced;
        }
        if (stopwatch == null) {
            stopwatch = new Stopwatch();
        }
        byte estimate = heuristic(board, tagAdvanced, tagReview);
        
        stopwatch = null;
        return estimate;
    }
    
    // calculate the heuristic value of the given board and save the properties
    protected abstract byte heuristic(Board board, boolean isAdvanced, boolean isSearch);

    /**
     *  Find the optimal path to goal state if the given board is solvable,
     *  and return the search time in seconds.
     *
     *  @param board the initial puzzle Board object to solve
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
                priority1stMove = new int[rowSize * 2];
                System.arraycopy(board.getValidMoves(), 0, priority1stMove, rowSize, rowSize);
                limit = heuristic(board, flagAdvancedPriority, tagSearch);
                idaStar(limit);
                assert checkGoal(board) : "Not end at goal state.";
            }
        } else {
            isSolvable = false;
        }
        searchTime = stopwatch.currentTime();
        stopwatch = null;
    }

    // check the initial board reach the goal state after the solution moves.
    private boolean checkGoal(Board initial) {
        if (initial == null) {
            throw new IllegalArgumentException("Board is null");
        }
        if (solved) {
            Board board = new Board(initial.getTiles());
            int count = 0;
            Direction dir = Direction.NONE;
            do {
                count++;
                if (count <= steps) {
                    dir = solutionMove[count];
                    if (board == null) {
                        System.out.println(steps + "\t" + Arrays.toString(solutionMove));
                        System.out.println("stop at " + count);
                        System.out.println(new Board(initial.getTiles()));
                    }
                    board = board.shift(dir);
                }
            } while (count <= steps);
            if (!board.isGoal()) {
                return false;
            }
            return true;
        }
        return true;
    }

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

    // solve the puzzle using interactive deepening A* algorithm
    protected abstract void idaStar(int limit);

    /**
     * Returns the double value of total time of search in seconds.
     *
     * @return double value of total time of search in seconds
     */
    @Override
    public final double searchTime() {
        return searchTime;
    }

    // convert the given tiles to symmetry tiles
    protected final byte[] tiles2sym(byte[] original) {
        byte[] tiles2sym = new byte[puzzleSize];
        for (int i = 0; i < puzzleSize; i++) {
            tiles2sym[symmetryPos[i]] = symmetryVal[original[i]];
        }
        return tiles2sym;
    }

    protected final String num2string(int num) {
        String str = Integer.toString(num);
        while (str.length() < 15) {
            str += " ";
        }
        return str;
    }
}
