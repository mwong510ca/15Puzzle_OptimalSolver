package mwong.myprojects.fifteenpuzzle.solver.components;

/**
 * PuzzleConstants contains all constant variables of 15 puzzle.
 *
 * @author   Meisze Wong
 *           www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public class PuzzleConstants {
    private static final byte SIZE = 16;
    private static final byte ROW_SIZE = 4;
    private static final byte MAX_MOVES = 80;
    private static final byte [] GOAL_TILES =
        {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 0};
    private static final int GOAL_KEY1 = 0x12345678;
    private static final int GOAL_KEY2 = 0x9ABCDEF0;
    private static final byte[] SYMMETRY_VAL =
        {0, 1, 5, 9, 13, 2, 6, 10, 14, 3, 7, 11, 15, 4, 8, 12};
    private static final byte[] SYMMETRY_POS =
        {0, 4, 8, 12, 1, 5, 9, 13, 2, 6, 10, 14, 3, 7, 11, 15};

    /**
     * Returns the byte value of puzzle size.
     *
     * @return byte value of puzzle size
     */
    public static final byte getSize() {
        return SIZE;
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
     * Returns the byte array of tiles of goal state.
     *
     * @return byte array of tiles of goal state
     */
    public static final byte[] getGoalTiles() {
        return GOAL_TILES;
    }

    /**
     * Returns the integer value of goal key 1.
     *
     * @return integer value of goal key 1
     */
    static final int getGoalKey1() {
        return GOAL_KEY1;
    }

    /**
     * Returns the integer value of goal key 2.
     *
     * @return integer value of goal key 2
     */
    static final int getGoalKey2() {
        return GOAL_KEY2;
    }

    /**
     * Returns the byte array of symmetry tile position conversion.
     *
     * @return byte array of symmetry position conversion
     */
    public static final byte[] getSymmetryPos() {
        return SYMMETRY_POS;
    }

    /**
     * Returns the byte array of symmetry tile value conversion.
     *
     * @return byte array of symmetry value conversion
     */
    public static final byte[] getSymmetryVal() {
        return SYMMETRY_VAL;
    }
}
