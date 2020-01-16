package mwong.myprojects.fifteenpuzzle.solution;

import mwong.myprojects.fifteenpuzzle.puzzle.Board;
import mwong.myprojects.fifteenpuzzle.puzzle.HeuristicOptions;
import mwong.myprojects.fifteenpuzzle.puzzle.PatternOptions;
import mwong.myprojects.fifteenpuzzle.solution.ai.ReferenceRecorder;

/**
 * SolverPdb78 extends SolverPdb use preset pattern database 78 only.  Special version
 * for reference collection use such as ReferenceMoves.java.
 *
 * <p>Dependencies : Board.java, HeuristicOptions.java, PatternOptions.java, Solver.java
 *                   SolverBuilder.java, SolverConstants.java, SolverPdb.java and Stopwatch.java
 *
 * @author <a href="http://www.linkedin.com/pub/macy-wong/46/550/37b/"
 *            target="_blank">Meisze Wong (linkedin)</a>
 * @see <a href="http://www.github.com/mwong510ca/15PuzzleOptimalSolver/"
 *         target="_blank">GitHub (full project)</a>
 */
public final class SolverPdb78 extends SolverPdb {
  /**
   * Initializes SolverPdb78 object using existing Solver object by solver builder, special design
   * for ReferenceLog object only.
   *
   * @param inSolver the given Solver object must using pattern database 78
   * @param appMode the given applicationMode
   */
  SolverPdb78(final Solver inSolver, final ApplicationMode appMode) {
    super(inSolver, appMode);
    if (appMode == ApplicationMode.GUI) {
      throw new UnsupportedOperationException("Pdb split version for console application only.");
    }
    if (inSolver.getHeuristic() != HeuristicOptions.PD78) {
      throw new IllegalArgumentException("Given solver is not using pattern database 78.");
    }
  }

  /**
   * Initializes SolverPdb78 object using pattern database 78 with reference collection
   * from ReferenecRecorder object itself, special design for ReferenecRecorder object only.
   *
   * @param recorder the given ReferenceRecorder object
   * @param appMode the applicationMode
   */
  SolverPdb78(final ReferenceRecorder recorder, final ApplicationMode appMode) {
    super(PatternOptions.Pattern_78, appMode);
  }

  /**
   * Solve the puzzle up to the given limit and given estimate instead of original heuristic.
   * Notes: special function use by ReferenceMoves.
   *
   * @param board the given Board object
   * @param estimate the estimate of minimum move
   */
  public void findOptimalPath(final Board board, final int estimate) {
    if (board.isSolvable()) {
      clearHistory();
      stopwatch = new Stopwatch();
      resetDepthSummary(board);
      // initializes the board by calling heuristic function using original priority
      // then solve the puzzle with given estimate instead
      int limit = heuristic(board, getVersion(), SolverAction.SEARCH);
      assert limit <= estimate : "priority > custom estimate";
      super.idaStar(estimate);
      stopwatch = null;
    }
  }
}
