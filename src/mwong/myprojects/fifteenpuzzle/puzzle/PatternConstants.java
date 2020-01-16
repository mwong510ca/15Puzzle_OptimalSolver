package mwong.myprojects.fifteenpuzzle.puzzle;

/**
 * PatternConstants contains shared constant variables for Pattern Database,
 * Pattern Element and Solver from group size 2 to 8.
 *
 * <p>Dependencies : PuzzleConstants.java
 *
 * @author <a href="http://www.linkedin.com/pub/macy-wong/46/550/37b/"
 *            target="_blank">Meisze Wong (linkedin)</a>
 * @see <a href="http://www.github.com/mwong510ca/15PuzzleOptimalSolver/"
 *         target="_blank">GitHub (full project)</a>
 */
public final class PatternConstants {
  /** Preset the key size of group size from 0 to 8.
  /*  Remarks - key size : group of 3 = 3! = 6, group of 6 = 6! = 720 */
  private static final int[] KEY_SIZE = {0, 1, 2, 6, 24, 120, 720, 5040, 40320};
  /** Preset the format size of group size from 0 to 8.
  /*  Remarks - format size : group of 3 = 16C3 = 560, group of 6 = 16C6 = 8008 */
  private static final int[] FORMAT_SIZE = {0, 16, 120, 560, 1820, 4368, 8008, 11440, 12870};
  /** Preset the standard group for common use for pattern 555, 663 and 78.
  /*  Remarks - standard group are 3, 5, 6, 7 and 8 */
  private static final boolean[] STANDARD_GROUPS
    = {false, false, false, true, false, true, true, true, true};
  /** Preset the maximum key shift per group. Row size is 4, max shift is 3.
  /*  Remarks - group 1: 0; group 2: 1; group 3: 2; group 4+: 3 */
  private static final int[] MAX_SHIFT = {0, 0, 1, 2, 3, 3, 3, 3, 3};
  /** Maximum pattern group size is 8. */
  private static final int MAX_GROUP_SIZE = 8;
  /** The format move size is 16 tile times 4 directions. */
  private static final int FORMAT_MOVE_SIZE = PuzzleConstants.getSize()
      * PuzzleConstants.getDirectionSize();
  /** The array of single format bit of 16 tiles.
  /*  From  1&lt;&lt;15, 1&lt;&lt;14, ... 1&lt;&lt;1, 1 */
  private static final int[] FORMAT_BIT_16;

  // Generate 1 << 15, 1<< 14, ... 1<<1, 1
  static {
    int size = PuzzleConstants.getSize();
    int shift = size - 1;
    int[] bit16 = new int[size];
    for (int i = 0; i < size; i++) {
      bit16[i] = 1 << shift--;
    }
    FORMAT_BIT_16 = bit16;
  }

  /** private constructor, no instances. */
  private PatternConstants() {
    // Not called
  }

  /**
   * Return the integer array of key size set from group 0 to 8.
   *
   * @return integer array of key size set
   */
  static int[] getKeySize() {
    return KEY_SIZE;
  }

  /**
   * Return the integer value of key size of the given group.
   *
   * @param group the given pattern group
   * @return integer value of key size of the given group
   */
  static int getKeySize(final int group) {
    return KEY_SIZE[group];
  }

  /**
   * Return the integer array of format size set from group 0 to 8.
   *
   * @return integer array of format size set
   */
  static int[] getFormatSize() {
    return FORMAT_SIZE;
  }

  /**
   * Return the integer value of format size of the given group.
   *
   * @param group the given pattern group
   * @return integer value of format size of the given group
   */
  public static int getFormatSize(final int group) {
    return FORMAT_SIZE[group];
  }

  /**
   * Returns the boolean array of the standard groups.
   *
   * @return boolean array of the standard groups
   */
  static boolean[] getStandardGroups() {
    return STANDARD_GROUPS;
  }

  /**
   * Returns the integer array represent the 16 bits format position.
   *
   * @return integer array represent the 16 bits format position
   */
  static int[] getFormatBit16() {
    return FORMAT_BIT_16;
  }

  /**
   * Returns the integer array represent the max shift for group 0 to 8.
   *
   * @return integer array represent the max shift
   */
  static int[] getMaxShift() {
    return MAX_SHIFT;
  }

  /**
   * Returns the integer value of the max shift times 2 of the given group.
   *
   * @param group the given pattern group size
   * @return integer value of the max shift times 2 of the given group
   */
  public static int getMaxShiftX2(final int group) {
    return MAX_SHIFT[group] * 2;
  }

  /**
   * Returns the integer value of the maximum pattern database group size.
   *
   * @return integer value of the maximum group size
   */
  public static int getMaxGroupSize() {
    return MAX_GROUP_SIZE;
  }

  /**
   * Return the integer value of combo size of number of formats times number of moves.
   *
   * @return integer value of combo size of number of formats times number of moves
   */
  public static int getFormatMoveSize() {
    return FORMAT_MOVE_SIZE;
  }

  /**
   * Return the integer value of single key bit size.
   *
   * @return integer value of single key bit size
   * @see PuzzleConstants#getTileBitSize
   */
  public static int getKeyBitSize() {
    return PuzzleConstants.getTileBitSize();
  }

  /**
   * Return the integer value of key bits.
   *
   * @return integer value of key bits
   * @see PuzzleConstants#getTileBits
   */
  public static int getKeyBits() {
    return PuzzleConstants.getTileBits();
  }
}
