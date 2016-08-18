package mwong.myprojects.fifteenpuzzle.solver.components;

/**
 * PatternConstants contains all constant variables for Pattern Database
 * group size 2 to 8.
 *
 * @author   Meisze Wong
 *           www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public class PatternConstants {
    // Remarks - key size : group of 3 = 3! = 6, group of 6 = 6! = 120
    private static final int[] KEY_SIZE = {0, 1, 2, 6, 24, 120, 720, 5040, 40320};
    // Remarks - format size : group of 3 = 16C3 = 560, group of 6 = 16C6 = 8008
    private static final int[] FORMAT_SIZE = {0, 16, 120, 560, 1820, 4368, 8008, 11440, 12870};
    // Standard group are 3, 5, 6, 7, 8 for patterns (663, 555 oar 78)
    private static boolean[] STANDATD_GROUPS
        = {false, false, false, true, false, true, true, true, true};
    // 16 bits represent the position of the tile
    private static final int[] FORMAT_BIT_16 = {1 << 15, 1 << 14, 1 << 13, 1 << 12, 1 << 11,
        1 << 10, 1 << 9, 1 << 8, 1 << 7, 1 << 6, 1 << 5, 1 << 4, 1 << 3, 1 << 2, 1 << 1, 1};
    private static final int[] MAX_SHIFT_X2 = {0, 0, 2, 4, 6, 6, 6, 6, 6};
    private static final int MAX_GROUP_SIZE = 8;

    /**
     * Return the integer array of key size set from group 0 to 8.
     *
     * @return integer array of key size set
     */
    public static final int[] getKeySize() {
        return KEY_SIZE;
    }

    /**
     * Return the integer value of key size of the given group.
     *
     * @param group the given pattern group
     * @return integer value of key size of the given group
     */
    public static final int getKeySize(int group) {
        return KEY_SIZE[group];
    }

    /**
     * Return the integer array of format size set from group 0 to 8.
     *
     * @return integer array of format size set
     */
    public static final int[] getFormatSize() {
        return FORMAT_SIZE;
    }

    /**
     * Return the integer value of format size of the given group.
     *
     * @param group the given pattern group
     * @return integer value of format size of the given group
     */
    public static final int getFormatSize(int group) {
        return FORMAT_SIZE[group];
    }

    /**
     * @Returns the boolean array of the standard groups.
     *
     * @return boolean array of the standard groups
     */
    static final boolean[] getStandatdGroups() {
        return STANDATD_GROUPS;
    }

    /**
     * Returns the integer array represent the 16 bits format position.
     *
     * @return integer array represent the 16 bits format position
     */
    public static final int[] getFormatBit16() {
        return FORMAT_BIT_16;
    }

    /**
     * Returns the integer array represent the max shift times 2 for group 0 to 8.
     *
     * @return integer array represent the max shift times 2
     */
    public static final int[] getMaxShiftX2() {
        return MAX_SHIFT_X2;
    }

    /**
     * Returns the integer value of the max shift times 2 of the given group.
     *
     * @param group the given pattern group size
     * @return integer value of the max shift times 2 of the given group
     */
    public static final int getMaxShiftX2(int group) {
        return MAX_SHIFT_X2[group];
    }

    /**
     * Returns the integer value of the maximum pattern database group size.
     *
     * @return integer value of the maximum group size
     */
    public static final int getMaxGroupSize() {
        return MAX_GROUP_SIZE;
    }
}