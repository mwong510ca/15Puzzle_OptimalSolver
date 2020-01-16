package mwong.myprojects.fifteenpuzzle.console;

import java.io.IOException;
import java.rmi.RemoteException;

import mwong.myprojects.fifteenpuzzle.puzzle.Board;
import mwong.myprojects.fifteenpuzzle.puzzle.HeuristicOptions;
import mwong.myprojects.fifteenpuzzle.solution.Solver;
import mwong.myprojects.fifteenpuzzle.solution.Solver.ApplicationMode;
import mwong.myprojects.fifteenpuzzle.solution.SolverBuilder;
import mwong.myprojects.fifteenpuzzle.solution.ai.Reference.ConnectionType;

/**
 * DemoSolverPdb78 is the final concrete class of console application extends AbstractApplication.
 * It use pattern database 7-8 heuristic function with monitor the changes on reference collection.
 * It takes a 16 numbers or choice of random board. It solved with prime and optimum version, while
 * the estimate are different. If it takes more than cutoff setting with buffer and added to
 * reference collection, it will display the new count of reference collection. It will search again
 * and showed the reduced search time after the puzzle added to the reference collection.
 *
 * <p>Dependencies : AbstractApplication.java, Board.java, SolverBuilder.java, Reference.java,
 *                   SolverPdb78.java
 *
 * @author <a href="http://www.linkedin.com/pub/macy-wong/46/550/37b/"
 *            target="_blank">Meisze Wong (linkedin)</a>
 * @see <a href="http://www.github.com/mwong510ca/15PuzzleOptimalSolver/"
 *         target="_blank">GitHub (full project)</a>
 */
public final class DemoSolverPdb78 extends AbstractApplication {
  /** The instance of Solver object using pattern database 78. */
  private Solver solver;

  /**
   * Create DemoSolverPdb78 application object.
   *
   * @throws IOException any unexpected IOException
   */
  public DemoSolverPdb78() throws IOException {
    super(ConnectionType.REMOTESERVER);
    SolverBuilder builder = new SolverBuilder(ApplicationMode.CONSOLE, true);
    builder.setReference(getRefConnection());
    solver = builder.createSolver(HeuristicOptions.PD78);
  }

  /**
   * It take a solver and a 15 puzzle board with pattern database 78 standard version.
   * Solver will automatically add to reference collection. And search again to display the
   * searching time has improved.
   *
   * @param board the given Board object
   * @throws IOException any unexpected IOException
   */
  private void solvePuzzle(final Board board) throws IOException {
    try {
      if (referenceContains(board)) {
        System.out.println("\t\tThis is NOT a reference board.\n");
      } else {
        System.out.println("\t\tExists in stored reference collection.\n");
      }
      solver.shiftPrime();
      int heuristicStandard = solver.heuristicBasis(board);

      System.out.println("Standard Estimate\t" + heuristicStandard);
      solver.findOptimalPath(board);
      if (solver.isSearchTimeout()) {
        System.out.println("\t\tTimeout: " + solver.searchTime() + "s at depth "
            + solver.searchDepth() + "\t"
            + solver.searchNodeCount());
      } else {
        System.out.printf("\t\tTotal : %-15s Time : "
            + solver.searchTime() + "s\n\n", solver.searchNodeCount());
      }

      boolean flagNewReference = solver.isNewReference();
      if (flagNewReference) {
        System.out.println("System detect the boost estimate is the same, added to"
            + " reference collection after the search.");
        System.out.println(getRefConnection().getActiveMap().size()
            + " reference board in system.\n");
      }

      if (isRequestedRemote()) {
        resetRemoteConnection(solver);
      }

      solver.shiftOptimum();
      int heuristicAdvanced = solver.heuristicBoost(board);
      if (heuristicStandard == heuristicAdvanced) {
        System.out.println("Advanced Estimate\t" + "Same value");
      } else {
        System.out.println("Advanced Estimate \t" + heuristicAdvanced);
        solver.findOptimalPath(board);
        System.out.printf("\t\tTotal : %-15s Time : "
            + solver.searchTime() + "s\n", solver.searchNodeCount());

        if (solver.isNewReference()) {
          flagNewReference = true;
          System.out.println("\nIt added to reference collection after the search.");
          System.out.println(getRefConnection().getActiveMap().size()
              + " reference board in system.\n");
          heuristicAdvanced = solver.heuristicBoost(board);
          System.out.println("Estimate change to\t" + heuristicAdvanced
              + "\t\t(Search again)");
          solver.findOptimalPath(board);
          System.out.printf("\t\tTotal : %-15s Time : "
              + solver.searchTime() + "s\n", solver.searchNodeCount());
        }

        if (flagNewReference) {
          System.out.println("System update in process, please wait...");
          getRefConnection().updateLastSearch(solver, board);
          System.out.println("System update completed.");
        }
      }
    } catch (RemoteException ex) {
      System.err.println("Counnection lost: " + ex);
      resetRemoteConnection(solver);
      System.out.println("Try again:");
      solvePuzzle(board);
    }
  }

  @Override
  public void run() throws IOException {
    try {
      System.out.println(getRefConnection().getActiveMap().size()
          + " reference boards in system.");
    } catch (RemoteException ex) {
      System.err.println("Counnection lost: " + ex);
      System.exit(0);
    }
    boolean[] menuList = createMenuList(MenuOptions.CREATE_PUZZLE, MenuOptions.KEY_IN_PUZZLE);

    while (true) {
      String optionStr = menuOption(menuList);

      Board board = null;
      while (true) {
        if (scanner.hasNextInt()) {
          board = keyInBoard();
          break;
        }
        char choice = scanner.next().toUpperCase().charAt(0);
        if (choice == 'Q') {
          appExit();
        }
        board = createBoard(choice);
        if (board != null) {
          break;
        }
        System.out.println(optionStr);
      }

      System.out.print(board);

      if (board.isSolvable()) {
        solvePuzzle(board);
      } else {
        System.out.println("The board is unsolvable, try again!");
      }
      System.out.println();
    }
  }
}
