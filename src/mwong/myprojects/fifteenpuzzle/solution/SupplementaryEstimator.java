package mwong.myprojects.fifteenpuzzle.solution;

import java.rmi.UnexpectedException;
import java.util.Map;
import java.util.Map.Entry;

import mwong.myprojects.fifteenpuzzle.puzzle.Board;
import mwong.myprojects.fifteenpuzzle.solution.ai.ReferenceBoard;
import mwong.myprojects.fifteenpuzzle.solution.ai.ReferenceMoves;

/**
 * SmartSolverExtra has the add on functions for advanced version. It return the
 * reference moves and solutions if the puzzle has been stored as a reference board.
 * It use Manhattan distance to calculate the advanced estimate from the collection
 * of reference boards.
 *
 * <p>Dependencies : Board.java, ReferenceBoard.java, ReferenceMoves.java,
 *                   SolverConstants.java, SolverBuilder.java, SolverMd.java
 *
 * @author <a href="http://www.linkedin.com/pub/macy-wong/46/550/37b/"
 *            target="_blank">Meisze Wong (linkedin)</a>
 * @see <a href="http://www.github.com/mwong510ca/15PuzzleOptimalSolver/"
 *         target="_blank">GitHub (full project)</a>
 */
final class SupplementaryEstimator {
  /** The byte array of reference lookup of zero space.
   *  @see mwong.myprojects.fifteenpuzzle.solution.ai.ReferenceConstants#REFERENCE_LOOKUP */
  private static final byte[] REF_LOOKUP_TABLE = SolverConstants.getReferenceLookup();
  /** The byte array of reference group of zero space.
   *  @see mwong.myprojects.fifteenpuzzle.solution.ai.ReferenceConstants#REFERENCE_GROUP */
  private static final byte[] REF_GROUP_TABLE = SolverConstants.getReferenceGroup();
  /** The byte value of mirror flip group.
   *  @see mwong.myprojects.fifteenpuzzle.solution.ai.ReferenceConstants#MIRROR_FLIP_GROUP */
  private static final byte MIRROR_FLIP_GROUP = SolverConstants.getRefMirrorFlipGroup();
  /** The applicable distance to reference boards up to 20 moves.  */
  private static final int ALLOWANCE = 20;
  /** The number of stored partial solution moves.
   *  @see mwong.myprojects.fifteenpuzzle.solution.ai.ReferenceConstants#NUM_PARTIAL_MOVES */
  private static final int NUM_PARTIAL_MOVES = SolverConstants.getNumPartialMoves();
  /** The SolverMd instance. */
  private SolverMd solverMd;

  /**
   * Initialize SupplementaryEstimator object.
   */
  SupplementaryEstimator() {
    solverMd = new SolverBuilder().internalSolverMd();
  }

  /**
   * Returns the AdvancedRecord object if the given board is one of the reference board.
   * It carries the reference estimate and partial solutions for searching if exists.
   * Otherwise return null.
   *
   * @param board the given board object
   * @param refMap the given reference collection in HashMap.
   * @return AdvancedRecord object if the given board is one of the reference board.
   * @throws UnexpectedException unexpected connection error
   */
  SupplementaryData referenceContains(final Board board,
      final Map<ReferenceBoard, ReferenceMoves> refMap) throws UnexpectedException {
    if (refMap == null || refMap.size() == 0) {
      throw new UnexpectedException("inverseEstimate -reference map is null");
    }

    byte lookupKey = REF_LOOKUP_TABLE[board.getZero1d()];
    int group = REF_GROUP_TABLE[board.getZero1d()];

    ReferenceBoard checkBoard = new ReferenceBoard(board);
    ReferenceBoard checkBoardMirror = null;

    if (group == 0 || group == 2) {
      checkBoardMirror = new ReferenceBoard(new Board(board.getTilesMirror()));
    }

    if (refMap.containsKey(checkBoard)) {
      ReferenceMoves advMoves = refMap.get(checkBoard);
      final byte steps = advMoves.getEstimate(lookupKey);

      if (advMoves.hasInitialMoves(lookupKey)) {
        Board.Move[] solutionMove = new Board.Move[steps + 1];
        solutionMove[0] = Board.Move.NONE;
        if (group == MIRROR_FLIP_GROUP) {
          System.arraycopy(advMoves.getInitialMoves(lookupKey, true), 0,
              solutionMove, 1, NUM_PARTIAL_MOVES);
          assert checkVaildMoves(board, solutionMove)
              : "Incorrect initial moves (group 3 mirror)";
        } else {
          System.arraycopy(advMoves.getInitialMoves(lookupKey, false), 0,
              solutionMove, 1, NUM_PARTIAL_MOVES);
          assert checkVaildMoves(board, solutionMove)
              : "Incorrect initial moves";
        }
        return new SupplementaryData(steps, solutionMove);
      } else {
        return new SupplementaryData(steps);
      }
    } else if (refMap.containsKey(checkBoardMirror)) {
      ReferenceMoves advMoves = refMap.get(checkBoardMirror);
      if (lookupKey == 1) {
        lookupKey = MIRROR_FLIP_GROUP;
      } else if (lookupKey == MIRROR_FLIP_GROUP) {
        lookupKey = 1;
      }
      final byte steps = advMoves.getEstimate(lookupKey);

      if (advMoves.hasInitialMoves(lookupKey)) {
        Board.Move[] solutionMove = new Board.Move[steps + 1];
        solutionMove[0] = Board.Move.NONE;
        System.arraycopy(advMoves.getInitialMoves(lookupKey, true), 0,
            solutionMove, 1, NUM_PARTIAL_MOVES);
        assert checkVaildMoves(board, solutionMove)
            : "Incorrect initial moves (group 0 or 2 symmetry)";
        return new SupplementaryData(steps, solutionMove);
      } else {
        return new SupplementaryData(steps);
      }
    }
    return null;
  }

  /**
   * Returns the boolean value if the partial solutions are the valid of the given board.
   *
   * @param initial the given Board object
   * @param partialMoves the Board.Move array of partial moves
   * @return boolean value if the partial solutions are the valid of the given board
   */
  private boolean checkVaildMoves(final Board initial, final Board.Move[] partialMoves) {
    if (initial == null) {
      throw new IllegalArgumentException("Board is null");
    }
    Board board = new Board(initial.getTiles());
    for (int i = 1; i <= NUM_PARTIAL_MOVES; i++) {
      board = board.shift(partialMoves[i]);
      if (board == null) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns the boolean value if the given board is one of the reference board with
   * partial solutions stored.
   *
   * @param board the given board object
   * @param refMap the given reference collection in HashMap.
   * @return boolean value if the given board is a reference board with partial solutions
   *     stored in reference collection.
   * @throws UnexpectedException unexpected connection error
   */
  boolean hasPartialSolution(final Board board,
      final Map<ReferenceBoard, ReferenceMoves> refMap) throws UnexpectedException {
    SupplementaryData record = referenceContains(board, refMap);
    if (record == null) {
      return false;
    }
    return record.hasPartialMoves();
  }

  /**
   * Returns the best estimate of the given reference collection.
   *
   * @param board the given board object
   * @param estimate the current estimate of the given puzzle.
   * @param refMap the given reference collection in HashMap.
   * @return AdvancedRecord object if the given board is one of the reference board.
   * @throws UnexpectedException unexpected connection error
   */
  int inverseEstimate(final Board board, final int estimate,
      final Map<ReferenceBoard, ReferenceMoves> refMap) throws UnexpectedException {
    if (refMap == null || refMap.size() == 0) {
      throw new UnexpectedException("inverseEstimate - reference map is null");
    }

    final int rowSize = SolverConstants.getRowSize();
    int newEstimate = estimate;
    for (Entry<ReferenceBoard, ReferenceMoves> entry
        : refMap.entrySet()) {
      byte[] transTiles = entry.getKey().transformer(board.getTiles());

      int transPriority = 0;
      int value;
      int base = 0;
      for (int row = 0; row < rowSize; row++) {
        for (int col = 0; col < rowSize; col++) {
          value = transTiles[base + col];
          if (value > 0) {
            transPriority += Math.abs((value - 1) % rowSize - col);
            transPriority += Math.abs((((value - 1)
                - (value - 1) % rowSize) / rowSize) - row);
          }
        }
        base += rowSize;
      }

      if (transPriority > ALLOWANCE) {
        continue;
      }
      if (entry.getValue().getEstimate() - transPriority <= newEstimate) {
        continue;
      }

      Board temp = new Board(transTiles);
      if (inverseDistance(temp, transPriority,
          entry.getValue().getEstimate() - newEstimate)) {
        newEstimate = (byte) (entry.getValue().getEstimate() - solverMd.moves());
      }
    }
    return newEstimate;
  }

  /**
   * Returns the boolean value represent the given board is solve in the given range.
   *
   * @param board the given Board object
   * @param lowerLimit the lower boundary to start the search
   * @param upperLimit the maximum number of moves allowed
   * @return boolean value if solution found within the given range
   */
  private boolean inverseDistance(final Board board, final int lowerLimit, final int upperLimit) {
    if (lowerLimit > upperLimit) {
      return false;
    }

    solverMd.clearHistory();
    solverMd.heuristic(board);
    solverMd.resetDepthSummary(board);
    int orgLimit = lowerLimit;
    int initLimit = lowerLimit;
    while (initLimit <= upperLimit) {
      solverMd.dfsStartingOrder(initLimit, orgLimit);
      if (solverMd.solved) {
        return true;
      }
      initLimit += 2;
    }
    return false;
  }
}
