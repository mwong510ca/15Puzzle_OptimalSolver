package mwong.myprojects.fifteenpuzzle.puzzle;

/**
 * PuzzleConstants contains all constant variables of 15 puzzle.
 *
 * @author <a href="http://www.linkedin.com/pub/macy-wong/46/550/37b/"
 *            target="_blank">Meisze Wong (linkedin)</a>
 * @see <a href="http://www.github.com/mwong510ca/15PuzzleOptimalSolver/"
 *         target="_blank">GitHub (full project)</a>
 */
public final class PuzzleConstants {
  /** The puzzle size is 16 include space. */
  private static final byte SIZE = 16;
  /** The puzzle row size is 4, 4 by 4 puzzle. */
  private static final byte ROW_SIZE = 4;
  /** Total 4 direction moves - left, right, up and down. */
  private static final byte DIRECTION_SIZE = 4;
  /** Maximum move size is 80. */
  private static final byte MAX_MOVES = 80;
  /** Space at lower right corner, index is 15. */
  private static final byte GOAL_ZERO_INDEX = 15;
  /** The byte array of tiles at goal state. */
  private static final byte[] GOAL_TILES =
      {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 0};
  /** The tile bit size is 4 for 0-15. */
  private static final int TILE_BIT_SIZE = 4;
  /** The tile bits in binary is 00001111 =&gt; 0x0F. */
  private static final int TILE_BITS = 0x0F;
  /** Compress the top half to an integer goal key 1. */
  private static final int GOAL_KEY1 = 0x12345678;
  /** Compress the bottom half to an integer goal key 2. */
  private static final int GOAL_KEY2 = 0x9ABCDEF0;
  /** Mirror reflection, value conversion table. */
  private static final byte[] MIRROR_VALUE =
      {0, 1, 5, 9, 13, 2, 6, 10, 14, 3, 7, 11, 15, 4, 8, 12};
  /** Mirror reflection, position conversion table. */
  private static final byte[] MIRROR_POSITION =
      {0, 4, 8, 12, 1, 5, 9, 13, 2, 6, 10, 14, 3, 7, 11, 15};

  /** private constructor, no instance. */
  private PuzzleConstants() {
    // not called
  }

  /**
   * Returns the byte value of puzzle size.
   *
   * @return byte value of puzzle size
   */
  public static byte getSize() {
    return SIZE;
  }

  /**
   * Returns the byte value of row size.
   *
   * @return byte value of row size
   */
  public static byte getRowSize() {
    return ROW_SIZE;
  }

  /**
   * Returns the byte value of row size.
   *
   * @return byte value of row size
   */
  public static byte getDirectionSize() {
    return DIRECTION_SIZE;
  }

  /**
   * Returns the byte value of maximum moves of 15 puzzle.
   *
   * @return byte value of maximum moves
   */
  public static byte getMaxMoves() {
    return MAX_MOVES;
  }

  /**
   * Returns the byte array of tiles of goal state.
   *
   * @return byte array of tiles of goal state
   */
  static byte getGoalZeroIdx() {
    return GOAL_ZERO_INDEX;
  }

  /**
   * Returns the byte array of tiles of goal state.
   *
   * @return byte array of tiles of goal state
   */
  public static byte[] getGoalTiles() {
    return GOAL_TILES;
  }

  /**
   * Returns the integer value of tile bit size.
   *
   * @return integer value of tile bit size
   */
  public static int getTileBitSize() {
    return TILE_BIT_SIZE;
  }

  /**
   * Returns the integer value of tile bits for value 0 to 15.
   *
   * @return integer value of tile bits for value 0 to 15
   */
 public static int getTileBits() {
    return TILE_BITS;
  }

  /**
   * Returns the integer value of goal key 1.
   *
   * @return integer value of goal key 1
   */
  static int getGoalKey1() {
    return GOAL_KEY1;
  }

  /**
   * Returns the integer value of goal key 2.
   *
   * @return integer value of goal key 2
   */
  static int getGoalKey2() {
    return GOAL_KEY2;
  }

  /**
   * Returns the byte array of tiles after symmetry conversion.
   *
   * @param original the byte array of given tiles
   * @return byte array of tiles after symmetry conversion
   */
  public static byte[] tiles2mirror(final byte[] original) {
    byte[] mirrorTiles = new byte[SIZE];
    for (int i = 0; i < SIZE; i++) {
      mirrorTiles[MIRROR_POSITION[i]] = MIRROR_VALUE[original[i]];
    }
    return mirrorTiles;
  }

  /**
   * Return the byte array of mirror tiles position conversion.
   *
   * @return byte array of mirror tiles position conversion
   */
  public static byte[] getMirrorPosition() {
    return MIRROR_POSITION.clone();
  }

  /**
   * Return the byte array of mirror tiles value conversion.
   *
   * @return byte array of mirror tiles value conversion
   */
  public static byte[] getMirrorValue() {
    return MIRROR_VALUE.clone();
  }
}
