package mwong.myprojects.fifteenpuzzle.solution;

import mwong.myprojects.fifteenpuzzle.puzzle.Board;
import mwong.myprojects.fifteenpuzzle.puzzle.PatternConstants;
import mwong.myprojects.fifteenpuzzle.puzzle.PuzzleConstants;
import mwong.myprojects.fifteenpuzzle.puzzle.WalkingDistance;
import mwong.myprojects.fifteenpuzzle.solution.ai.ReferenceConstants;

/**
 * SolverConstants contains all constant variables for any solver.
 *
 * <p>Dependencies : Board.java, PuzzleConstants.java, ReferenceConstants.java
 *
 * @author <a href="http://www.linkedin.com/pub/macy-wong/46/550/37b/"
 *            target="_blank">Meisze Wong (linkedin)</a>
 * @see <a href="http://www.github.com/mwong510ca/15PuzzleOptimalSolver/"
 *         target="_blank">GitHub (full project)</a>
 */
public final class SolverConstants {
  /** private constructor, no instance. */
  private SolverConstants() {
    // not called
  }

  /**
   * Returns the byte value of the puzzle size.
   *
   * @return byte value of the puzzle size
   * @see mwong.myprojects.fifteenpuzzle.puzzle.PuzzleConstants#SIZE
   */
  public static byte getPuzzleSize() {
    return PuzzleConstants.getSize();
  }

  /**
   * Returns the byte value of row size.
   *
   * @return byte value of row size
   * @see mwong.myprojects.fifteenpuzzle.puzzle.PuzzleConstants#ROW_SIZE
   */
  public static byte getRowSize() {
    return PuzzleConstants.getRowSize();
  }

  /**
   * Returns the byte value of direction or moves size.
   *
   * @return byte value of direction or moves size
   * @see mwong.myprojects.fifteenpuzzle.puzzle.PuzzleConstants#DIRECTION_SIZE
   */
  public static byte getDirectionSize() {
    return PuzzleConstants.getDirectionSize();
  }

  /**
   * Returns the byte value of maximum moves of 15 puzzle.
   *
   * @return byte value of maximum moves
   * @see mwong.myprojects.fifteenpuzzle.puzzle.PuzzleConstants#MAX_MOVES
   */
  public static byte getMaxMoves() {
    return PuzzleConstants.getMaxMoves();
  }

  /**
   * Returns the byte value of end of search, use for terminate search process.
   *
   * @return byte value of end of search.
   */
  static byte getEndOfSearch() {
    return (byte) (getMaxMoves() + 1);
  }

  /**
   * Returns the Board object of the goal board.
   *
   * @return Board object of the goal board
   * @see mwong.myprojects.fifteenpuzzle.puzzle.PuzzleConstants#GOAL_TILES
   */
  public static Board getGoalBoard() {
    return new Board(PuzzleConstants.getGoalTiles());
  }

  /**
   * Returns the byte array of mirror conversion.
   *
   * @param original the byte array of original tiles
   * @return byte array of mirror conversion
   * @see mwong.myprojects.fifteenpuzzle.puzzle.PuzzleConstants#tiles2mirror(byte[] original)
   */
  public static byte[] tiles2mirror(final byte[] original) {
    return PuzzleConstants.tiles2mirror(original);
  }

  /**
   * Returns the byte value of mirror position conversion.
   *
   * @return byte value of mirror position conversion
   * @see mwong.myprojects.fifteenpuzzle.puzzle.PuzzleConstants#MIRROR_POSITION
   */
  public static byte[] getMirrorPosition() {
    return PuzzleConstants.getMirrorPosition();
  }

  /**
   * Returns the byte value of mirror value conversion.
   *
   * @return byte value of mirror value conversion
   * @see mwong.myprojects.fifteenpuzzle.puzzle.PuzzleConstants#MIRROR_VALUE
   */
  public static byte[] getMirrorValue() {
    return PuzzleConstants.getMirrorValue();
  }

  /**
   * Returns the integer value of walking distance key bit size.
   *
   * @return integer value of walking distance key bit size
   * @see mwong.myprojects.fifteenpuzzle.puzzle.WalkingDistance#KEY_BIT_SIZE
   */
  public static int getWdKeyBitSize() {
    return WalkingDistance.getKeyBitSize();
  }

  /**
   * Returns the integer value of walking distance key index bit size.
   *
   * @return integer value of walking distance key index bit size
   * @see mwong.myprojects.fifteenpuzzle.puzzle.WalkingDistance#KEY_IDX_BIT_SIZE
   */
  public static int getWdKeyIdxBitSize() {
    return WalkingDistance.getKeyIdxBitSize();
  }

  /**
   * Returns the integer value of walking distance shift bit for zero row.
   *
   * @return integer value of walking distance shift bit for zero row
   * @see mwong.myprojects.fifteenpuzzle.puzzle.WalkingDistance#ZERO_ROW_BIT_SHIFT
   */
  public static int getWdZeroRowBitShift() {
    return WalkingDistance.getZeroRowBitShift();
  }

  /**
   * Returns the integer represent total number of format move size.
   * (format size times move size)
   *
   * @return integer represent total number of format move size
   * @see mwong.myprojects.fifteenpuzzle.puzzle.PatternConstants#FORMAT_MOVE_SIZE
   */
  public static int getPdbFormatMoveSize() {
    return PatternConstants.getFormatMoveSize();
  }

  /**
   * Returns the integer value of pattern database key bit size.
   *
   * @return integer value of pattern database key bit size
   * @see mwong.myprojects.fifteenpuzzle.puzzle.PuzzleConstants#TILE_BIT_SIZE
   */
  public static int getPdbKeyBitSize() {
    return PatternConstants.getKeyBitSize();
  }

  /**
   * Returns the pattern database key bits in integer value.
   *
   * @return integer value of pattern database key bits
   * @see mwong.myprojects.fifteenpuzzle.puzzle.PuzzleConstants#TILE_BITS
   */
  public static int getPdbKeyBits() {
    return PatternConstants.getKeyBits();
  }

  /**
   * Returns integer value represents the pattern format size of the given group.
   *
   * @param group the given pattern group size
   * @return integer value represents the pattern format size of the given group
   * @see mwong.myprojects.fifteenpuzzle.puzzle.PatternConstants#getFormatSize(int group)
   */
  public static int getPdbFormatSize(final int group) {
    return PatternConstants.getFormatSize(group);
  }

  /**
   * Returns the integer value of the max shift times 2 of the given group.
   *
   * @param group the given pattern group size
   * @return integer value of the max shift times 2 of the given group
   * @see mwong.myprojects.fifteenpuzzle.puzzle.PatternConstants#getMaxShiftX2(int group)
   */
  public static int getPdbMaxShiftX2(final int group) {
    return PatternConstants.getMaxShiftX2(group);
  }

  /**
   * Returns the integer value of the maximum pattern database group size.
   *
   * @return integer value of the maximum group size
   * @see mwong.myprojects.fifteenpuzzle.puzzle.PatternConstants#MAX_GROUP_SIZE
   */
  public static int getPdbMaxGroupSize() {
    return PatternConstants.getMaxGroupSize();
  }

  /**
   * Returns the byte array of the reference lookup key.
   *
   * @return byte array of the reference lookup key
   * @see mwong.myprojects.fifteenpuzzle.solution.ai.ReferenceConstants#REFERENCE_LOOKUP
   */
  public static byte[] getReferenceLookup() {
    return ReferenceConstants.getReferenceLookup();
  }

  /**
   * Returns the byte array of the reference group.
   *
   * @return byte array of the reference group
   * @see mwong.myprojects.fifteenpuzzle.solution.ai.ReferenceConstants#REFERENCE_GROUP
   */
  public static byte[] getReferenceGroup() {
    return ReferenceConstants.getReferenceGroup();
  }

  /**
   * Returns the byte array of reference group lookup mirror conversion.
   *
   * @return byte array of reference group lookup mirror conversion
   * @see mwong.myprojects.fifteenpuzzle.solution.ai.ReferenceConstants#MIRROR_FLIP_GROUP
   */
  public static byte getRefMirrorFlipGroup() {
    return ReferenceConstants.getMirrorFlipGroup();
  }

  /**
   * Returns the byte value of the number of partial solution.
   *
   * @return byte value of the number of partial solution
   * @see mwong.myprojects.fifteenpuzzle.solution.ai.ReferenceConstants#NUM_PARTIAL_MOVES
   */
  public static byte getNumPartialMoves() {
    return ReferenceConstants.getNumPartialMoves();
  }
}
