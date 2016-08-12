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

import mwong.myprojects.fifteenpuzzle.solver.components.AdvancedAccumulator;
import mwong.myprojects.fifteenpuzzle.solver.components.AdvancedBoard;
import mwong.myprojects.fifteenpuzzle.solver.components.AdvancedMoves;
import mwong.myprojects.fifteenpuzzle.solver.components.Board;
import mwong.myprojects.fifteenpuzzle.solver.components.Direction;
import mwong.myprojects.utilities.Stopwatch;

import java.util.Arrays;
import java.util.Map;

abstract class SolverAbstract implements SolverInterface {
    private final boolean isSymmetry = AdvancedMoves.isSymmetry();
    protected final boolean tagAdvanced = SWITCH_ON;
    protected final boolean tagOriginal = !tagAdvanced;
    protected final boolean tagSearch = SWITCH_ON;
    protected final boolean tagReview = !tagSearch;

    protected boolean flagTimeout = SWITCH_ON;
    protected boolean flagMessage;
    protected boolean flagAdvancedPriority;
    protected int searchTimeoutLimit = DEFAULT_TIMEOUT_LIMIT;
    protected int cutoffAdvancedPriority;
    protected AdvancedAccumulator advAccumulator;
    protected HeuristicType inUseHeuristic;

    protected byte[] tiles;
    protected Board lastBoard = new Board(Board.getBoardgoal());
    protected byte priorityGoal = 0;
    protected byte priorityAdvanced = 0;
    protected byte steps = 0;
    protected int idaCount = 0;
    protected int searchDepth = 0;
    protected int searchNodeCount = 0;
    protected int zeroX = 3;
    protected int zeroY = 3;
    protected double searchTime = 0.0;
    protected boolean solved = true;
    protected boolean timeout = false;
    protected boolean terminated = false;
    protected boolean isSolvable = true;
    protected Direction[] solutionMove;
    protected int [] priority1stMove;
    protected Stopwatch stopwatch;

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
        if (flagAdvancedPriority) {
            System.out.println("Advance option - initial estimate use the goal state and "
                    + "archived boards.");
        } else {
            System.out.println("Original option - initial estimate use the goal state only.");
        }
    }

    /**
     *  Print solver heading of given ApplicationType.
     */
    @Override
    public final void printHeading(ApplicationType type) {
        if (type != ApplicationType.CustomPattern) {
            if (type != ApplicationType.Stats) {
                System.out.println();
            }
            System.out.print(inUseHeuristic.getDescription());
        }

        if (type != ApplicationType.CompareHeuristic) {
            if (flagAdvancedPriority) {
                System.out.print(" (Advanced Heruistic)");
            } else {
                System.out.print(" (Original Heruistic)");
            }
        }

        if (flagTimeout || type == ApplicationType.CustomPattern) {
            System.out.println(" will timeout at " + searchTimeoutLimit + "s:");
        } else {
            System.out.println(" will run until solution found:");
        }
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

    /**
     *  Set the advanced priority with the given flag.
     *
     *  @param flag the boolean represent the active status of advanced priority
     */
    @Override
    public final void advPrioritySwitch(boolean flag) {
        flagAdvancedPriority = flag;
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
     *  Set the AdvancedAccumulator for advance heuristic.
     *
     *  @param advAccumulator represent the AdvancedAccumulator object
     */
    @Override
    public final void setAdvancedAccumulator(AdvancedAccumulator advAccumulator) {
        this.advAccumulator = advAccumulator;
    }

    // reset and clear variables from previous search results
    final void clearHistory() {
        isSolvable = true;
        solved = false;
        timeout = false;
        terminated = false;
        searchDepth = 0;
        searchNodeCount = 0;
        priority1stMove = new int [ROW_SIZE * 2];
        solutionMove = new Direction[MAX_MOVES + 1];
        solutionMove[0] = Direction.NONE;
        steps = 0;
    }

    /**
     *  Find the optimal path to goal state if the given board is solvable,
     *  and return the search time in seconds.
     *
     *  @param board the initial puzzle Board object to solve
     */
    @Override
    public void findOptimalPath(Board board) {
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
                priority1stMove = new int[ROW_SIZE * 2];
                System.arraycopy(board.getValidMoves(), 0, priority1stMove, ROW_SIZE, ROW_SIZE);
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
     * Returns the original heuristic value of the board.
     *
     * @return byte value of the original heuristic value of the board
     */
    @Override
    public final byte heuristicOrg(Board board) {
        if (board == null) {
            throw new IllegalArgumentException("Board is null");
        }
        if (board.equals(lastBoard)) {
            return priorityGoal;
        }
        return heuristic(board, tagOriginal, tagReview);
    }

    /**
     * Returns the advanced heuristic value of the board.
     *
     * @return byte value of the advanced heuristic value of the board
     */
    @Override
    public final byte heuristicAdv(Board board) {
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

    // calculate the heuristic value of the given board and save the properties
    abstract byte heuristic(Board board, boolean inAdvanced, boolean inSearch);

    // solve the puzzle using interactive deepening A* algorithm
    abstract void idaStar(int limit);

    // quick check if the given board is one of the reference board.  If so,
    // use the reference estimate.  If search in progress, also update partial
    // solutions if exists.
    final void advancedContains(Board board, boolean inSearch) {
        Map<AdvancedBoard, AdvancedMoves> advMap;
        if (advAccumulator == null) {
            advMap = AdvancedAccumulator.getDefaultMap();
        } else {
            advMap = advAccumulator.getActiveMap();
        }

        byte lookupKey = getAdvLookupKey(board.getZero1d());
        int group = getAdvGroup(board.getZero1d());

        AdvancedBoard checkBoard = new AdvancedBoard(board);
        AdvancedBoard checkBoardSym = null;
        if (group == 0 || group == 2) {
            checkBoardSym = new AdvancedBoard(new Board(board.getTilesSym()));
        }

        if (advMap.containsKey(checkBoard)) {
            AdvancedMoves advMoves = advMap.get(checkBoard);
            priorityAdvanced = advMoves.getEstimate(lookupKey);

            if (inSearch && advMoves.hasInitialMoves(lookupKey)) {
                if (group == 3) {
                    System.arraycopy(advMoves.getInitialMoves(lookupKey, isSymmetry), 0,
                            solutionMove, 1, ADV_PARTIAL_MOVES);
                    assert checkVaildMoves(board) : "Incorrect initial moves (group 3 symmetry)";
                } else {
                    System.arraycopy(advMoves.getInitialMoves(lookupKey, !isSymmetry), 0,
                            solutionMove, 1, ADV_PARTIAL_MOVES);
                    assert checkVaildMoves(board) : "Incorrect initial moves";
                }
            }
        } else if (advMap.containsKey(checkBoardSym)) {
            AdvancedMoves advMoves = advMap.get(checkBoardSym);
            if (lookupKey == 1) {
                lookupKey = 3;
            } else if (lookupKey == 3) {
                lookupKey = 1;
            }
            priorityAdvanced = advMoves.getEstimate(lookupKey);

            if (inSearch && advMoves.hasInitialMoves(lookupKey)) {
                System.arraycopy(advMoves.getInitialMoves(lookupKey, isSymmetry), 0,
                        solutionMove, 1, ADV_PARTIAL_MOVES);
                assert checkVaildMoves(board) : "Incorrect initial moves (group 0 or 2 symmetry)";
            }
        }
    }

    // check all partial solution are the valid of the given baord
    private boolean checkVaildMoves(Board initial) {
        if (initial == null) {
            throw new IllegalArgumentException("Board is null");
        }
        Board board = new Board(initial.getTiles());
        for (int i = 1; i <= ADV_PARTIAL_MOVES; i++) {
            board = board.shift(solutionMove[i]);
            if (board == null) {
                return false;
            }
        }
        return true;
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

    // convert the given tiles to symmetry tiles
    final byte[] tiles2sym(byte[] original) {
        byte[] tiles2sym = new byte[BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            tiles2sym[SYMMETRY_POS[i]] = SYMMETRY_VAL[original[i]];
        }
        return tiles2sym;
    }

    // get the advanced lookup key of the given zero position
    final byte getAdvLookupKey(byte zeroPos) {
        return AdvancedBoard.getLookupKey(zeroPos);
    }

    // get the advanced group of the given zero position
    final byte getAdvGroup(byte zeroPos) {
        return AdvancedBoard.getGroup(zeroPos);
    }
    
    final String num2string(int num) {
    	String str = Integer.toString(num);
    	while (str.length() < 15) {
    		str += " ";
    	}
    	return str;
    }
}
