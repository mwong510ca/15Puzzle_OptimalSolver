package mwong.myprojects.fifteenpuzzle.solver;

import mwong.myprojects.fifteenpuzzle.solver.components.Board;
import mwong.myprojects.fifteenpuzzle.solver.components.PuzzleConstants;

/**
 * SolverConstants contains all constant variables for any solver.
 *
 * <p>Dependencies : Board.java, PuzzleConstants.java
 *
 * @author   Meisze Wong
 *           www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public class SolverConstants {
    private static final byte PUZZLE_SIZE = PuzzleConstants.getSize();
    private static final byte ROW_SIZE = PuzzleConstants.getRowSize();
    private static final byte MAX_MOVES = PuzzleConstants.getMaxMoves();
    private static final byte[] SYMMETRY_POS =
            PuzzleConstants.getSymmetryPos();
    private static final byte[] SYMMETRY_VAL =
            PuzzleConstants.getSymmetryVal();
    private static final boolean ON_SWITCH = true;
    private static final boolean TAG_ADVANCED = true;
    private static final boolean TAG_SEARCH = true;
    private static final boolean TAG_LINEAR_CONFLICT = true;
    private static final byte END_OF_SEARCH = (byte) (MAX_MOVES + 1);
    private static final Board GOAL_BOARD = new Board(PuzzleConstants.getGoalTiles());

    /**
     * Returns the byte value of the puzzle size.
     *
     * @return byte value of the puzzle size
     */
    public static final byte getPuzzleSize() {
        return PUZZLE_SIZE;
    }

    /**
     * Returns the byte value of row size.
     *
     * @return byte value of row size
     */
    public static final byte getRowSize() {
        return ROW_SIZE;
    }

    /**
     * Returns the byte value of maximum moves of 15 puzzle.
     *
     * @return byte value of maximum moves
     */
    public static final byte getMaxMoves() {
        return MAX_MOVES;
    }

    /**
     * Returns the byte array of symmetry position conversion.
     *
     * @return byte array of symmetry position
     */
    public static final byte[] getSymmetryPos() {
        return SYMMETRY_POS;
    }

    /**
     * Returns the byte array of symmetry tiles conversion.
     *
     * @return byte array of symmetry value
     */
    public static final byte[] getSymmetryVal() {
        return SYMMETRY_VAL;
    }

    /**
     * Returns the boolean value represent the switch is ON status.
     *
     * @return boolean value represent ON status
     */
    public static final boolean isOnSwitch() {
        return ON_SWITCH;
    }

    /**
     * Returns the boolean value represent the advanced feature in use.
     *
     * @return boolean value represent the advanced feature
     */
    public static final boolean isTagAdvanced() {
        return TAG_ADVANCED;
    }

    /**
     * Returns the boolean value represent the search feature in use.
     *
     * @return boolean value represent the advanced feature
     */
    public static final boolean isTagSearch() {
        return TAG_SEARCH;
    }

    /**
     * Returns the boolean value represent the linear conflict feature of Manhatten Distance in use.
     *
     * @return boolean value represent the linear conflict feature
     */
    public static final boolean isTagLinearConflict() {
        return TAG_LINEAR_CONFLICT;
    }

    /**
     * Returns the byte value of end of search, use for terminate search process.
     *
     * @return byte value of end of search.
     */
    public static final byte getEndOfSearch() {
        return END_OF_SEARCH;
    }

    /**
     * Returns the Board object of the goal board.
     *
     * @return Board object of the goal board
     */
    public static final Board getGoalBoard() {
        return GOAL_BOARD;
    }
}