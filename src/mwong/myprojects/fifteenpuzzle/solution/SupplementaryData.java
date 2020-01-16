package mwong.myprojects.fifteenpuzzle.solution;

import mwong.myprojects.fifteenpuzzle.puzzle.Board;

/**
 * A immutable data type stored the number of move, partial moves and boolean flag determine
 * the partial moves exists.
 *
 * <p>Dependencies : Board.java
 *
 * @author <a href="http://www.linkedin.com/pub/macy-wong/46/550/37b/"
 *            target="_blank">Meisze Wong (linkedin)</a>
 * @see <a href="http://www.github.com/mwong510ca/15PuzzleOptimalSolver/"
 *         target="_blank">GitHub (full project)</a>
 */
final class SupplementaryData {
  /** The byte value of estimate. */
  private byte estimate;
  /** The Borad.Move array of the partial moves. */
  private Board.Move[] partialMoves;
  /** The boolean value represent the record contains partial moves. */
  private boolean hasPartialMoves;

  /**
   * Initialize SupplementaryData object with estimate only.
   *
   * @param estimate the number of moves
   */
  SupplementaryData(final byte estimate) {
    this.estimate = estimate;
    hasPartialMoves = false;
    partialMoves = null;
  }

  /**
   * Initialize SupplementaryData object with estimate and partial solution.
   *
   * @param estimate the number of moves
   * @param partialMoves the Board.Move array
   */
  SupplementaryData(final byte estimate, final Board.Move[] partialMoves) {
    this.estimate = estimate;
    hasPartialMoves = true;
    this.partialMoves = partialMoves;
  }

  /**
   * Returns the byte values of number of moves.
   *
   * @return byte values of number of moves
   */
  byte getEstimate() {
    return estimate;
  }

  /**
   * Returns the Directions array of partial solution from record.
   *
   * @return Directions array of partial solution
   */
  Board.Move[] getPartialMoves() {
    return partialMoves;
  }

  /**
   * Returns the boolean represent record has partial solution.
   *
   * @return boolean represent record has partial solution
   */
  boolean hasPartialMoves() {
    return hasPartialMoves;
  }
}
