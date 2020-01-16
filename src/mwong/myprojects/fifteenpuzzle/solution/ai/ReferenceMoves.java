package mwong.myprojects.fifteenpuzzle.solution.ai;

import java.io.Serializable;
import java.util.Arrays;

import mwong.myprojects.fifteenpuzzle.puzzle.Board;
import mwong.myprojects.fifteenpuzzle.solution.SolverPdb78;

/**
 * ReferenceMoves is the data type that stored number of moves or temporary estimate
 * and partial solution associated with ReferenceBoard object for reference collection.
 *
 * <p>Dependencies : Board.java, SolverPdb78.java
 *
 * @author <a href="http://www.linkedin.com/pub/macy-wong/46/550/37b/"
 *            target="_blank">Meisze Wong (linkedin)</a>
 * @see <a href="http://www.github.com/mwong510ca/15PuzzleOptimalSolver/"
 *         target="_blank">GitHub (full project)</a>
 */
public final class ReferenceMoves implements Serializable {
  private static final long serialVersionUID = 17195273121L;
  /** The double byte array combines group with lookup of zero space.
   *  @see ReferenceConstants#GROUP_LOOKUP_POS */
  private static final byte[][] GROUP_LOOKUP_POS = ReferenceConstants.getGroupLookupPos();
  /** The number of lookups, split in 4 groups, 4 lookup per group.
   *  @see ReferenceConstants#NUM_LOOKUPS */
  private static final int NUM_LOOKUPS = ReferenceConstants.getNumLookups();
  /** Move value from 0 to 3, the bit size is 2, move bits is 0011(3).
   *  @see mwong.myprojects.fifteenpuzzle.puzzle.Board.Move */
  private static final int MOVE_BITS = 0x03;

  /** The byte array of status bit per group lookup.
   *  @see ReferenceConstants#STATUS_BIT */
  private static final byte[] STATUS_BIT = ReferenceConstants.getStatusBit();
  /** Complete status of 4 group lookups.
   *  @see ReferenceConstants#STATUS_COMPLETED */
  private static final byte STATUS_COMPLETED = ReferenceConstants.getStatusCompleted();
  /** Store 8 partial move as short value with 16 bits, 2 bits per move.
   *  @see ReferenceConstants#NUM_PARTIAL_MOVES */
  private static final int NUM_PARTIAL_MOVES = ReferenceConstants.getNumPartialMoves();

  /** The byte array of number of solution moves per group lookup. */
  private byte[] moves;
  /** The short array of compress move code of partial solution per group lookup. */
  private short[] initMoves; // 8 * 2 bits for first 8 solution
  /** The status represents the group lookup has been reviewed with partial solution. */
  private byte status;

  /**
   * Initializes ReferenceMoves object with given zero position with given
   * unverified estimate and no partial solution.
   *
   * @param zeroPos the position of zero space
   * @param steps number of moves to solve the puzzle
   */
  public ReferenceMoves(final byte zeroPos, final byte steps) {
    moves = new byte[NUM_LOOKUPS];
    initMoves = new short[NUM_LOOKUPS];
    status = 0;

    int refLookup = ReferenceConstants.getReferenceLookup(zeroPos);
    moves[refLookup] = steps;
    int count = 1;
    while (refLookup - count >= 0) {
      moves[refLookup - count] = (byte) (steps - count);
      count++;
    }

    count = 1;
    while (refLookup + count < NUM_LOOKUPS) {
      moves[refLookup + count] = (byte) (steps - count);
      count++;
    }
  }

  /**
   * Initializes ReferenceMoves object with stored variables.
   *
   * @param moves the given moves to copy
   * @param initMoves the given partial moves to copy
   * @param status the status to copy
   */
  public ReferenceMoves(final byte[] moves, final short[] initMoves, final byte status) {
    this.moves = new byte[NUM_LOOKUPS];
    System.arraycopy(moves, 0, this.moves, 0, NUM_LOOKUPS);
    this.initMoves = new short[NUM_LOOKUPS];
    System.arraycopy(initMoves, 0, this.initMoves, 0, NUM_LOOKUPS);
    this.status = status;
  }

  /**
   * While ReferenceMoves object already exists, update unverified moves and
   * partial solutions only with given values.
   *
   * @param steps the new set of moves.
   * @param updateMoves the new set of updateMoves
   * @param status2 the given status
   */
  public void updateMoves(final byte[] steps, final short[] updateMoves, final byte status2) {
    status |= status2;
    for (int lookup = 0; lookup < NUM_LOOKUPS; lookup++) {
      if (moves[lookup] < steps[lookup]) {
        moves[lookup] = steps[lookup];
        this.initMoves[lookup] = updateMoves[lookup];
      } else if (this.initMoves[lookup] == 0) {
        this.initMoves[lookup] = updateMoves[lookup];
      }
    }
  }

  /**
   * Update moves and partial solution at the given lookup key.
   *
   * @param lookup the given lookup key
   * @param steps the number of move
   * @param solution partial solution
   * @param flagMirror represents of mirror lookup
   */
  public void updateSolution(final byte lookup, final byte steps, final Board.Move[] solution,
      final boolean flagMirror) {
    status |= STATUS_BIT[lookup];
    moves[lookup] = steps;
    initMoves[lookup] = initialMoves2value(solution, flagMirror);
  }

  /**
   * Update a full set of moves and partial solutions with a given reference board
   * and a SolverPdb78 object.
   *
   * @param advBoard the given board
   * @param solver the given solver
   */
  public void updateSolutions(final ReferenceBoard advBoard, final SolverPdb78 solver) {
    assert solver != null : "Require solver is null";
    byte group = advBoard.getGroup();
    byte[] blocks = advBoard.getTiles();
    for (int lookup = 0; lookup < NUM_LOOKUPS; lookup++) {
      if ((status & STATUS_BIT[lookup]) == 0) {
        Board board = new Board(blocks);
        solver.findOptimalPath(board, moves[lookup]);
        assert solver.solution() != null : "No solution from updateSolutions function";
        moves[lookup] = solver.moves();
        initMoves[lookup] = initialMoves2value(solver.solution(), false);
      }
      blocks = shiftOne(blocks, group, lookup);
    }
    status = STATUS_COMPLETED;
  }

  /**
   * Convert the first 8 directions from the given array into short value.
   *
   * @param dir the Board.Move array of partial moves
   * @param flagMirror the boolean value represents mirror reflection
   * @return short value that compress the partial moves with 2 bits each
   */
  private short initialMoves2value(final Board.Move[] dir, final boolean flagMirror) {
    short value = 0;
    for (int i = NUM_PARTIAL_MOVES; i > 1; i--) {
      if (flagMirror) {
        value |= dir[i].mirrorDirection().getValue();
      } else {
        value |= dir[i].getValue();
      }
      value <<= 2;
    }

    if (flagMirror) {
      value |= dir[1].mirrorDirection().getValue();
    } else {
      value |= dir[1].getValue();
    }
    return value;
  }

  /**
   * Returns the Direction arrays of the given lookup key. Restore the
   * solution key for the first 8 moves in Direction array.
   *
   * @param lookup the give lookup key of the reference board
   * @param flagMirror represent mirror reflection
   * @return the Direction arrays of the given lookup key
   */
  public Board.Move[] getInitialMoves(final int lookup, final boolean flagMirror) {
    short value = initMoves[lookup];
    Board.Move[] movesFirst8 = new Board.Move[NUM_PARTIAL_MOVES];
    for (int i = 0; i < NUM_PARTIAL_MOVES; i++) {
      int dir = value & MOVE_BITS;
      movesFirst8[i] = Board.Move.values()[dir];
      value >>>= 2;
    }

    if (flagMirror) {
      for (int i = 0; i < NUM_PARTIAL_MOVES; i++) {
        movesFirst8[i] = movesFirst8[i].mirrorDirection();
      }
    }
    return movesFirst8;
  }

  /**
   * Returns the boolean represents the given lookup key has partial solution.
   *
   * @param lookup the give lookup key of the reference board
   * @return boolean represents the given lookup key has partial solution
   */
  public boolean hasInitialMoves(final int lookup) {
    return initMoves[lookup] != 0;
  }

  /**
   * Returns the byte array of the given blocks shift to next lookup position.
   *
   * @param blocks the given blocks of tiles
   * @param group the given reference board group
   * @param lookup the give lookup position
   * @return byte array after one shift from the given blocks
   */
  private byte[] shiftOne(final byte[] blocks, final int group, final int lookup) {
    if (lookup > 2) {
      return blocks;
    }

    byte[] copy = blocks.clone();
    copy[GROUP_LOOKUP_POS[group][lookup]] = blocks[GROUP_LOOKUP_POS[group][lookup + 1]];
    copy[GROUP_LOOKUP_POS[group][lookup + 1]] = 0;
    return copy;
  }

  /**
   * Return the boolean represents the full set of moves and partial solutions
   * has been verified.
   *
   * @return boolean represents the full set has been verified
   */
  public boolean isCompleted() {
    return status == STATUS_COMPLETED;
  }

  /**
   * Return the byte of estimate of the reference board.
   *
   * @return byte of estimate of the reference board
   */
  public byte getEstimate() {
    return moves[0];
  }

  /**
   * Return the byte of estimate of the given lookup position of reference board.
   *
   * @param lookup the give lookup key of the reference board
   * @return byte of estimate of the lookup position of reference board
   */
  public byte getEstimate(final int lookup) {
    return moves[lookup];
  }

  @Override
  public String toString() {
    String str = Integer.toBinaryString(status) + "\n";
    for (int i = 0; i < NUM_LOOKUPS; i++) {
      str += moves[i] + "\t" + initMoves[i] + "\t"
          + Arrays.toString(getInitialMoves(i, false)) + "\n";
    }
    return str;
  }

  /**
   * Return the byte array of moves.
   *
   * @return byte array of moves
   */
  public byte[] getMoves() {
    return moves;
  }

  /**
   * Return the short array of compress partial solution.
   *
   * @return short array of compress partial solution
   */
  public short[] getInitMoves() {
    return initMoves;
  }

  /**
   * Return the byte of status.
   *
   * @return byte of status
   */
  public byte getStatus() {
    return status;
  }
}
