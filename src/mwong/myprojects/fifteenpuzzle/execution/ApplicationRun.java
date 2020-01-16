package mwong.myprojects.fifteenpuzzle.execution;

import mwong.myprojects.fifteenpuzzle.console.AbstractApplication;
import mwong.myprojects.fifteenpuzzle.console.CompareEnhancement;
import mwong.myprojects.fifteenpuzzle.console.CompareHeuristic;
import mwong.myprojects.fifteenpuzzle.console.DemoSolverPdb78;
import mwong.myprojects.fifteenpuzzle.console.SolverHeuristic;
import mwong.myprojects.fifteenpuzzle.console.SolverHeuristicStats;
import mwong.myprojects.fifteenpuzzle.console.SolverPdbCustomPattern;

/**
 * ApplicationRun is the utility class.  The main console application of the 15 puzzle
 * optimal solver.  Choice of 6 applications.
 *
 * <p>Dependencies : AbstractApplication.java, CompareEnhancement.java, CompareHeuristic.java,
 *                   DemoSolverPdb78.java, SolverHeuristic.java, SolverHeuristicStats.java,
 *                   SolverPdbCustomPattern.java
 *
 * @author <a href="http://www.linkedin.com/pub/macy-wong/46/550/37b/"
 *            target="_blank">Meisze Wong (linkedin)</a>
 * @see <a href="http://www.github.com/mwong510ca/15PuzzleOptimalSolver/"
 *         target="_blank">GitHub (full project)</a>
 */
public final class ApplicationRun {
  /** Option 1, Pattern database 7-8 demo. */
  private static final int PDB78_DEMO = 1;
  /** Option 2, enhancement comparison. */
  private static final int ENHANCEMENT = 2;
  /** Option 3, heuristic comparison.  Default option. */
  private static final int COMPARE_HEURISTIC = 3;
  /** Option 4, user define pattern include custom pattern up to group 7. */
  private static final int CUSTOM_PATTERN = 4;
  /** Option 5, one heuristic at a time, display search status and with display solution option. */
  private static final int ANY_SOLVER = 5;
  /** Option 6, statistic, run a number of trails and calculate the average time. */
  private static final int STATISTIC = 6;

  /** private constructor, no instance. */
  private ApplicationRun() {
    // Not called
  }

  /**
   * The main method takes the choice of console application of 15 puzzle solver.
   * <p>Option 1: Demo version of pattern database 7-8 with monitor reference collection.
   * <br>Option 2: Compare each enhancement based on pattern database 7-8 solver.
   * <br>Option 3: Compare all 7 heuristic functions.
   * <br>Option 4: User choice of preset pattern database or user defined custom pattern.
   * <br>Option 5: User choice of heuristic functions, with option to display the solution.
   * <br>Option 6: Run a number trails, display the average solved time and number of puzzles
   *      has been timeout.
   * <p>Default : Use option 3 if invalid entry.
   *
   * @param args standard argument main function
   * @throws Exception any exceptions
   */
  public static void main(final String[] args) throws Exception {
    AbstractApplication app;

    int choice = COMPARE_HEURISTIC;
    if (args.length > 0 && args[0].matches("\\d+")) {
      choice = Integer.parseInt(args[0]);
    }

    switch (choice) {
      case PDB78_DEMO : app = new DemoSolverPdb78();
        break;
      case ENHANCEMENT : app = new CompareEnhancement();
        break;
      case COMPARE_HEURISTIC : app = new CompareHeuristic();
        break;
      case CUSTOM_PATTERN : app = new SolverPdbCustomPattern();
        break;
      case ANY_SOLVER : app = new SolverHeuristic();
        break;
      case STATISTIC : app = new SolverHeuristicStats();
        break;
      default: app = new CompareHeuristic();
    }

    app.run();
  }
}
