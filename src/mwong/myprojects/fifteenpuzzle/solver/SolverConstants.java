package mwong.myprojects.fifteenpuzzle.solver;

import mwong.myprojects.fifteenpuzzle.solver.advanced.ai.ReferenceConstants;
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
    // SmartSolver constants
    private static final byte[] REFERENCE_LOOKUP = ReferenceConstants.getReferenceLookup();
    private static final byte[] REFERENCE_GROUP = ReferenceConstants.getReferenceGroup();
    private static final boolean SYMMETRY = ReferenceConstants.isSymmetry();
    private static final byte NUM_PARTIAL_MOVES = ReferenceConstants.getNumPartialMoves();
    private static final byte REFERENCE_CUTOFF = 30;

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
    protected static final byte getMaxMoves() {
        return MAX_MOVES;
    }

    /**
     * Returns the byte array of symmetry position conversion.
     *
     * @return byte array of symmetry position
     */
    protected static final byte[] getSymmetryPos() {
        return SYMMETRY_POS;
    }

    /**
     * Returns the byte array of symmetry tiles conversion.
     *
     * @return byte array of symmetry value
     */
    protected static final byte[] getSymmetryVal() {
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
    protected static final boolean isTagSearch() {
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
    protected static final byte getEndOfSearch() {
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

    /**
     * Returns the byte array of the reference lookup key.
     *
     * @return byte array of the reference lookup key
     */
    protected static final byte[] getReferenceLookup() {
        return REFERENCE_LOOKUP;
    }

    /**
     * Returns the byte value of the reference lookup key of the given zero position.
     *
     * @param zeroPos the given zero position
     * @return byte value of the reference lookup key of the given zero position
     */
    public static final byte getReferenceLookup(int zeroPos) {
        return REFERENCE_LOOKUP[zeroPos];
    }

    /**
     * Returns the byte array of the reference group.
     *
     * @return byte array of the reference group
     */
    protected static final byte[] getReferenceGroup() {
        return REFERENCE_GROUP;
    }

    /**
     * Returns the byte value of the reference group of the given zero position.
     *
     * @param zeroPos the given zero position
     * @return byte value of the reference group of the given zero position
     */
    public static final byte getReferenceGroup(int zeroPos) {
        return REFERENCE_GROUP[zeroPos];
    }

    /**
     * Returns the boolean represent the symmetry of the reference board.
     *
     * @return boolean represent the symmetry of the reference board
     */
    public static final boolean isSymmetry() {
        return SYMMETRY;
    }

    /**
     * Returns the byte value of the number of partial solution.
     *
     * @return byte value of the number of partial solution
     */
    public static final byte getNumPartialMoves() {
        return NUM_PARTIAL_MOVES;
    }

    /**
     * Returns the byte value of the cutoff estimate for advanced search.
     *
     * @return byte value of the cutoff estimate for advanced search
     */
    public static final byte getReferenceCutoff() {
        return REFERENCE_CUTOFF;
    }
}