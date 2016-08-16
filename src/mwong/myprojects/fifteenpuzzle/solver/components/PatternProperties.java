package mwong.myprojects.fifteenpuzzle.solver.components;

public class PatternProperties {
    // Remarks - key size : group of 3 = 3! = 6, group of 6 = 6! = 120
    private static final int[] KEY_SIZE = {0, 1, 2, 6, 24, 120, 720, 5040, 40320};
    // Remarks - format size : group of 3 = 16C3 = 560, group of 6 = 16C6 = 8008
    private static final int[] FORMAT_SIZE = {0, 16, 120, 560, 1820, 4368, 8008, 11440, 12870};
    // 16 bits represent the position of the tile
    private static final int[] FORMAT_BIT_16 = {1 << 15, 1 << 14, 1 << 13, 1 << 12, 1 << 11,
        1 << 10, 1 << 9, 1 << 8, 1 << 7, 1 << 6, 1 << 5, 1 << 4, 1 << 3, 1 << 2, 1 << 1, 1};
    private static final int[] MAX_SHIFT_X2 = {0, 0, 2, 4, 6, 6, 6, 6, 6};
    private static final int MAX_GROUP_SIZE = 8;

    /**
     * Return the keySize.
     *
     * @return the keySize
     */
    public static final int[] getKeySize() {
        return KEY_SIZE;
    }

    /**
     * Return the keySize.
     *
     * @return the keySize
     */
    public static final int getKeySize(int group) {
        return KEY_SIZE[group];
    }

    /**
     * Return the formatSize.
     *
     * @return the formatSize
     */
    public static final int[] getFormatSize() {
        return FORMAT_SIZE;
    }

    /**
     * Return the formatSize.
     *
     * @return the formatSize
     */
    public static final int getFormatSize(int group) {
        return FORMAT_SIZE[group];
    }

    /**
     * Returns the format bit.
     *
     * @return the formatBit16
     */
    public static final int[] getFormatBit16() {
        return FORMAT_BIT_16;
    }

    /**
     * Returns the max shift times 2.
     *
     * @return the maxShiftX2
     */
    public static final int[] getMaxShiftX2() {
        return MAX_SHIFT_X2;
    }

    /**
     * Returns the max shift times 2.
     *
     * @return the maxShiftX2
     */
    public static final int getMaxShiftX2(int group) {
        return MAX_SHIFT_X2[group];
    }

    /**
     * Returns the max group size.
     *
     * @return the maxGroupSize
     */
    public static final int getMaxGroupSize() {
        return MAX_GROUP_SIZE;
    }
}