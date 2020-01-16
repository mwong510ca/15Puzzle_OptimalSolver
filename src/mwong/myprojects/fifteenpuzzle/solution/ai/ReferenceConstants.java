package mwong.myprojects.fifteenpuzzle.solution.ai;

import mwong.myprojects.fifteenpuzzle.puzzle.HeuristicOptions;
import mwong.myprojects.fifteenpuzzle.puzzle.PuzzleConstants;

/**
 * ReferenceConstants contains all constant variables of reference collection.
 *
 * <p>Dependencies : HeuristicOptions.java, SolverConstants.java
 *
 * @author <a href="http://www.linkedin.com/pub/macy-wong/46/550/37b/"
 *            target="_blank">Meisze Wong (linkedin)</a>
 * @see <a href="http://www.github.com/mwong510ca/15PuzzleOptimalSolver/"
 *         target="_blank">GitHub (full project)</a>
 */
public final class ReferenceConstants {
  /** Remote host field name, use by remote.property file.
   *  @see ReferenceFactory
   *  @see ReferenceConstants
   */
  private static final String REMOTE_HOST_FIELD_NAME = "remoteHost";
  /** Remote service field name, use by remote.property file.
   *  @see ReferenceFactory
   *  @see ReferenceConstants
   */
  private static final String REMOTE_SERVICE_FIELD_NAME = "remoteServiceName";
  /** Remote port field name, use by remote.property file.
   *  @see ReferenceFactory
   *  @see ReferenceConstants
   */
  private static final String REMOTE_PORT_FIELD_NAME = "remotePort";
  /** The mandatory heuristic function must be pattern database 7-8. */
  private static final HeuristicOptions CORE_HEURISTIC = HeuristicOptions.PD78;

  // Constants for ReferenceBoard
  /** The puzzle size.
   *  @see mwong.myprojects.fifteenpuzzle.puzzle.PuzzleConstants#SIZE */
  private static final int PUZZLE_SIZE = PuzzleConstants.getSize();
  /** The byte array of 90 degrees rotation conversion. */
  private static final byte[] ROTATE_90_POS =
      {12, 8, 4, 0, 13, 9, 5, 1, 14, 10, 6, 2, 15, 11, 7, 3};
  /** The byte array of 180 degrees rotation conversion. */
  private static final byte[] ROTATE_180_POS =
      {15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0};
  /** The byte array of reference lookup of zero space. */
  private static final byte[] REFERENCE_LOOKUP =
      {0, 1, 3, 0, 3, 2, 2, 1, 3, 2, 2, 3, 0, 1, 1, 0};
  /** The byte array of reference group of zero space. */
  private static final byte[] REFERENCE_GROUP =
      {2, 2, 1, 1, 2, 2, 1, 1, 3, 3, 0, 0, 3, 3, 0, 0};
  /** The double byte array combines group with lookup of zero space. */
  private static final byte[][] GROUP_LOOKUP_POS;
  /** The byte value of mirror flip group. */
  private static final byte MIRROR_FLIP_GROUP = 3;
  /** The buffer size is 34.
   *  21 bytes for ReferenceBoard and 13 bytes for ReferenceMove.
   *  @see ReferenceBoard
   *  @see ReferenceMoves */
  private static final int BUFFER_SIZE_PER_RECORD = 34;
  /** The number of lookups, split in 4 groups, 4 lookup per group. */
  private static final int NUM_LOOKUPS = 4;

  // Constants for ReferenceMoves
  /** Status bit, 1 bit per lookup. 0001, 0010(2), 0100(4), 1000(8) */
  private static final byte[] STATUS_BIT = {1, 2, 4, 8};
  /** Status complete 00001111 =&gt; 15.*/
  private static final byte STATUS_COMPLETED = 15;
  /** Store 8 partial move as short value with 16 bits, 2 bits per move. */
  private static final byte NUM_PARTIAL_MOVES = 8;

  static {
    byte[][] combine = new byte[NUM_LOOKUPS][NUM_LOOKUPS];
    for (byte i = 0; i < PUZZLE_SIZE; i++) {
      combine[REFERENCE_GROUP[i]][REFERENCE_LOOKUP[i]] = i;
    }
    GROUP_LOOKUP_POS = combine;
  }

  /** private constructor, no instance. */
  private ReferenceConstants() {
    // not called.
  }

  /**
   * Returns the string of remote host field name.
   *
   * @return string of remote host field name
   * @see ReferenceFactory
   * @see ReferenceConstants
   */
  public static String getRemoteHostFieldName() {
    return REMOTE_HOST_FIELD_NAME;
  }

  /**
   * Returns the string of remote service field name.
   *
   * @return string of remote service field name
   * @see ReferenceFactory
   * @see ReferenceConstants
   */
  public static String getRemoteServiceFieldName() {
    return REMOTE_SERVICE_FIELD_NAME;
  }

  /**
   * Returns the string remote port field name.
   *
   * @return string remote port field name
   * @see ReferenceFactory
   * @see ReferenceConstants
   */
  public static String getRemotePortFieldName() {
    return REMOTE_PORT_FIELD_NAME;
  }

  /**
   * Returns the HeuristicOption that reference collection is using must be
   * pattern database 78.
   *
   * @return HeuristicOption the reference collection is using.
   */
  HeuristicOptions getCoreHeuristic() {
    return CORE_HEURISTIC;
  }

  /**
   * Return the byte array of rotate 90 degree clockwise conversion.
   *
   * @return byte array of rotate 90 degree clockwise conversion
   */
  static byte[] getRotate90Pos() {
    return ROTATE_90_POS;
  }

  /**
   * Return the byte array of rotate 180 degree conversion.
   *
   * @return byte array of rotate 180 degree clockwise conversion
   */
  static byte[] getRotate180Pos() {
    return ROTATE_180_POS;
  }

  /**
   * Return the byte array of reference lookup key.
   *
   * @return byte array of reference lookup key
   */
  public static byte[] getReferenceLookup() {
    return REFERENCE_LOOKUP;
  }

  /**
   * Return the byte value of reference lookup key of the given zero position.
   *
   * @param zeroPos the given zero position
   * @return byte value of reference lookup key of the given zero position
   */
  static byte getReferenceLookup(final int zeroPos) {
    return REFERENCE_LOOKUP[zeroPos];
  }

  /**
   * Return the byte array of reference group.
   *
   * @return byte array of reference group
   */
  public static byte[] getReferenceGroup() {
    return REFERENCE_GROUP;
  }

  /**
   * Return the byte value of reference group of the given zero position.
   *
   * @param zeroPos the given zero position
   * @return byte value of reference group of the given zero position
   */
  static byte getReferenceGroup(final int zeroPos) {
    return REFERENCE_GROUP[zeroPos];
  }

  /**
   * Return the double byte array of group lookup position.
   *
   * @return double byte array of group lookup position
   */
  static byte[][] getGroupLookupPos() {
    return GROUP_LOOKUP_POS;
  }

  /**
   * Return the byte value of mirror flip group.
   *
   * @return byte value of mirror flip group
   */
  public static byte getMirrorFlipGroup() {
    return MIRROR_FLIP_GROUP;
  }

  /**
   * Return the integer value of buffer size per record.
   *
   * @return integer value of buffer size per record
   */
  static int getBufferSizePerRecord() {
    return BUFFER_SIZE_PER_RECORD;
  }

  /**
   * Returns the integer value of the puzzle size.
   *
   * @return integer value of the puzzle size
   * @see mwong.myprojects.fifteenpuzzle.puzzle.PuzzleConstants#SIZE
   */
  static int getPuzzleSize() {
    return PUZZLE_SIZE;
  }

  /**
   * Returns the integer value of tile bit size.
   *
   * @return integer value of tile bit size
   * @see mwong.myprojects.fifteenpuzzle.puzzle.PuzzleConstants#TILE_BIT_SIZE
   */
  static int getTileBitSize() {
    return PuzzleConstants.getTileBitSize();
  }

  /**
   * Returns the integer value of tile bits for value 0 to 15.
   *
   * @return integer value of tile bits for value 0 to 15
   * @see mwong.myprojects.fifteenpuzzle.puzzle.PuzzleConstants#TILE_BITS
   */
  static int getTileBits() {
    return PuzzleConstants.getTileBits();
  }

  /**
   * Returns the byte array of status in bits.
   *
   * @return byte array of status in bits
   */
  static byte[] getStatusBit() {
    return STATUS_BIT;
  }

  /**
   * Returns the byte value represent the status of completion.
   *
   * @return byte value represent the status of completion
   */
  static byte getStatusCompleted() {
    return STATUS_COMPLETED;
  }

  /**
   * Returns the byte value of number of partial moves has stored.
   *
   * @return byte value of number of partial moves
   */
  public static byte getNumPartialMoves() {
    return NUM_PARTIAL_MOVES;
  }

  /**
   * Return the integer value of number of lookups.
   *
   * @return integer value of number of lookups
   */
  static int getNumLookups() {
    return NUM_LOOKUPS;
  }
}
