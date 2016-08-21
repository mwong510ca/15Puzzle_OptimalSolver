package mwong.myprojects.fifteenpuzzle.solver.components;

/**
 * PuzzlePrepertiess contains the preset hard boards of 15 puzzle.
 *
 * @author   Meisze Wong
 *           www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public class PuzzleProperties {
    // initializes hard boards to be use to generate random board
    private static final byte[][] HARD_ZERO_0 = {
        {0, 11,  9, 13, 12, 15, 10, 14,  3,  7,  6,  2,  4,  8,  5,  1},
        {0, 15,  9, 13, 11, 12, 10, 14,  3,  7,  6,  2,  4,  8,  5,  1},
        {0, 12,  9, 13, 15, 11, 10, 14,  3,  7,  6,  2,  4,  8,  5,  1},
        {0, 12,  9, 13, 15, 11, 10, 14,  3,  7,  2,  5,  4,  8,  6,  1},
        {0, 12, 10, 13, 15, 11, 14,  9,  3,  7,  2,  5,  4,  8,  6,  1},
        {0, 12, 14, 13, 15, 11,  9, 10,  3,  7,  6,  2,  4,  8,  5,  1},
        {0, 12, 10, 13, 15, 11, 14,  9,  3,  7,  6,  2,  4,  8,  5,  1},
        {0, 12, 11, 13, 15, 14, 10,  9,  3,  7,  6,  2,  4,  8,  5,  1},
        {0, 12, 10, 13, 15, 11,  9, 14,  7,  3,  6,  2,  4,  8,  5,  1},
        {0, 12,  9, 13, 15, 11, 14, 10,  3,  8,  6,  2,  4,  7,  5,  1},
        {0, 12,  9, 13, 15, 11, 10, 14,  8,  3,  6,  2,  4,  7,  5,  1},
        {0, 12, 14, 13, 15, 11,  9, 10,  8,  3,  6,  2,  4,  7,  5,  1},
        {0, 12,  9, 13, 15, 11, 10, 14,  7,  8,  6,  2,  4,  3,  5,  1},
        {0, 12, 10, 13, 15, 11, 14,  9,  7,  8,  6,  2,  4,  3,  5,  1},
        {0, 12,  9, 13, 15,  8, 10, 14, 11,  7,  6,  2,  4,  3,  5,  1},
        {0, 12,  9, 13, 15, 11, 10, 14,  3,  7,  5,  6,  4,  8,  2,  1},
        {0, 12,  9, 13, 15, 11, 10, 14,  7,  8,  5,  6,  4,  3,  2,  1},
        {0, 15, 14, 13, 12, 11, 10,  9,  8,  7,  6,  5,  4,  3,  2,  1},
        {0, 15,  8,  3, 12, 11,  7,  4, 14, 10,  6,  5,  9, 13,  2,  1},
        {0, 12, 14,  4, 15, 11,  7,  3,  8, 10,  6,  5, 13,  9,  2,  1},
        {0, 12,  7,  3, 15, 11,  8,  4, 10, 14,  6,  2,  9, 13,  5,  1},
        {0, 12,  7,  4, 15, 11,  8,  3, 10, 14,  6,  2, 13,  9,  5,  1},
        {0, 12,  8,  3, 15, 11, 10,  4, 14,  7,  6,  5,  9, 13,  2,  1},
        {0, 12,  8,  3, 15, 11,  7,  4, 14, 10,  6,  2,  9, 13,  5,  1},
        {0, 12,  8,  4, 15, 11,  7,  3, 14, 10,  6,  2, 13,  9,  5,  1},
        {0, 12,  8,  7, 15, 11,  4,  3, 14, 13,  6,  2, 10,  9,  5,  1},
        {0, 15,  4, 10, 12, 11,  8,  3, 13, 14,  6,  2,  7,  9,  5,  1},
        {0, 15,  7,  4, 12, 11,  8,  5, 10, 14,  6,  3, 13,  2,  9,  1},
        {0, 15,  7,  8, 12, 11,  4,  3, 10, 13,  6,  5, 14,  9,  2,  1},
        {0, 15,  8, 10, 12, 11,  4,  3, 14, 13,  6,  2,  7,  9,  5,  1},
        {0, 15,  8,  3, 12, 11, 10,  4, 14,  7,  6,  2,  9, 13,  5,  1},
        {0, 15,  8,  4, 12, 11,  7,  3, 14, 10,  6,  5, 13,  9,  2,  1},
        {0, 15,  8,  4, 12, 11,  7,  5, 14, 10,  6,  3, 13,  2,  9,  1},
        {0, 15,  8,  7, 12, 11,  4,  3, 14, 13,  6,  5, 10,  9,  2,  1},
        {0,  2,  9, 13,  5,  1, 10, 14,  3,  7,  6, 15,  4,  8, 12, 11},
        {0,  5,  9, 13,  2,  1, 10, 14,  3,  7, 11, 15,  4,  8, 12,  6},
        {0,  5,  9, 13,  2,  6, 10, 14,  3,  7,  1, 15,  4,  8, 12, 11},
        {0,  5,  9, 14,  2,  6, 10, 13,  3,  7,  1, 15,  8,  4, 12, 11}
    };
    // initializes hard boards to be use to generate random board
    private static final byte[][] HARD_ZERO_15 = {
        {1, 10, 14, 13,  7,  6,  5,  9,  8,  2, 11, 15,  4,  3, 12,  0},
        {1, 10,  9, 13,  7,  6,  5, 14,  3,  2, 11, 15,  4,  8, 12,  0},
        {1,  5, 14, 13,  2,  6, 10,  9,  8,  7, 11, 15,  4,  3, 12,  0},
        {1,  5,  9, 13,  2,  6, 10, 14,  3,  7, 11, 15,  4,  8, 12,  0},
        {6,  5, 13,  9,  2,  1, 10, 14,  4,  7, 11, 12,  3,  8, 15,  0},
        {6,  5, 14, 13,  2,  1, 10,  9,  8,  7, 11, 12,  4,  3, 15,  0},
        {6,  5,  9, 13,  2,  1, 10, 14,  3,  7, 11, 12,  4,  8, 15,  0},
        {6,  5,  9, 14,  2,  1, 10, 13,  3,  7, 11, 12,  8,  4, 15,  0}
    };

    /**
     * Returns the integer of the number of preset hard puzzles with zero position 0.
     *
     * @return integer of the number of preset hard puzzles with zero position 0
     */
    static final int getHardZero0Size() {
        return HARD_ZERO_0.length;
    }

    /**
     * Returns the byte array of preset hard puzzle with zero position 0 of the given index.
     *
     * @param index the index preset hard puzzle
     * @return the byte array of preset hard puzzle of the given index
     */
    static final byte[] getHardZero0(int index) {
        return HARD_ZERO_0[index];
    }

    /**
     * Returns the integer of the number of preset hard puzzles with zero position 15.
     *
     * @return integer of the number of preset hard puzzles with zero at position 15
     */
    static final int getHardZero15Size() {
        return HARD_ZERO_15.length;
    }

    /**
     * Returns the byte array of preset hard puzzle with zero position 15 of the given index.
     *
     * @param index the index preset hard puzzle
     * @return the byte array of preset hard puzzle of the given index.
     */
    static final byte[] getHardZero15(int index) {
        return HARD_ZERO_15[index];
    }

    /**
     * Returns the byte array of tiles after symmetry conversion.
     *
     * @param original the byte array of given tiles
     * @return byte array of tiles after symmetry conversion
     */
    public static final byte[] tiles2sym(byte[] original) {
        final int puzzleSize = PuzzleConstants.getSize();
        final byte[] symmetryPos = PuzzleConstants.getSymmetryPos();
        final byte[] symmetryVal = PuzzleConstants.getSymmetryVal();
        byte[] tiles2sym = new byte[puzzleSize];

        for (int i = 0; i < puzzleSize; i++) {
            tiles2sym[symmetryPos[i]] = symmetryVal[original[i]];
        }
        return tiles2sym;
    }
}
