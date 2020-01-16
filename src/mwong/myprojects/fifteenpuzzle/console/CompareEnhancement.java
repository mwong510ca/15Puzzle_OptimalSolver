package mwong.myprojects.fifteenpuzzle.console;

import java.io.IOException;
import java.rmi.RemoteException;

import mwong.myprojects.fifteenpuzzle.puzzle.Board;
import mwong.myprojects.fifteenpuzzle.solution.Solver.ApplicationMode;
import mwong.myprojects.fifteenpuzzle.solution.SolverBuilder;
import mwong.myprojects.fifteenpuzzle.solution.SolverPdb78Enh;
import mwong.myprojects.fifteenpuzzle.solution.SolverPdb78Enh.Level;

/**
 * CompareEnhancement is the final concrete class of console application extends
 * AbstractApplication. It use special SolverPdb78Enh version instead Solver interface
 * allow to break up the enhancement levels.  It takes a 16 numbers or choice of random board.
 * It use pattern database 7-8 heuristic function, display the process time and number
 * of nodes generated during the search by adding enhancement one at a time. If original estimate
 * and boost estimate are the same, it will not continue on last 2 enhancements that using
 * reference collection.
 *
 * <p>Dependencies : AbstractApplication.java, SolverBuilder.java, SolverPdb78Enh.java
 *
 * @author <a href="http://www.linkedin.com/pub/macy-wong/46/550/37b/"
 *            target="_blank">Meisze Wong (linkedin)</a>
 * @see <a href="http://www.github.com/mwong510ca/15PuzzleOptimalSolver/"
 *         target="_blank">GitHub (full project)</a>
 */
public final class CompareEnhancement extends AbstractApplication {
  /** The instance of SolverPdb78Enh object. */
  private SolverPdb78Enh solver;

  /**
   * Create CompareEnhancement application object.
   * The boolean flag of on going search with Prime version.
   *
   * @throws IOException any unexpected IOException
   */
  public CompareEnhancement() throws IOException {
    super();
    SolverBuilder builder = new SolverBuilder(ApplicationMode.CONSOLE);
    builder.setReference(getRefConnection());
    solver = builder.createSolverPdb78Enh();
  }

  /**
   * It take a solver and a 15 puzzle board, display the the process time and number of
   * nodes generated during the search.  Returns boolean value represents the board has added
   * to reference collection after search completed.
   *
   * @param board the given Board object
   * @param level the choice of enhancement level
   * @return boolean value represents the board has added to reference collection.
   */
  private boolean solvePuzzle(final Board board, final Level level) {
    solver.findOptimalPath(board, level);
    System.out.printf("%-15s %-20s\n", solver.searchTime() + "s", solver.searchNodeCount());
    return solver.isNewReference();
  }

  @Override
  public void run() throws IOException {
    System.out.println("Compare 15 puzzle solver enhancement using "
        + solver.getHeuristic().getDescription() + "\n");
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

      System.out.println(board);

      if (board.isSolvable()) {
        int heuristicStandard = solver.heuristicBasis(board);
        int heuristicAdvanced = solver.heuristicBoost(board);
        System.out.print("Original estimate : " + heuristicStandard + "\t\t");
        System.out.println("  Boost estimate : " + heuristicAdvanced);
        System.out.printf("%-36s", "");
        System.out.println("Time\t    Nodes");

        System.out.printf("%-36s", "1. No enhancement : ");
        solvePuzzle(board, Level.NONE);
        System.out.printf("%-36s", "2. Add symmetry reduction : ");
        solvePuzzle(board, Level.SYMMETRY_REDUCTION);
        System.out.printf("%-36s", "3. Add circular reduction : ");
        solvePuzzle(board, Level.CIRCULAR_RECUCTION);
        System.out.printf("%-36s", "4. Add starting order detection : ");
        boolean flagNewReference = solvePuzzle(board, Level.PRIME);

        if (isRequestedRemote()) {
          resetRemoteConnection(solver);
        }

        if (flagNewReference) {
          heuristicAdvanced = solver.heuristicBoost(board);
        }

        if (heuristicAdvanced > heuristicStandard) {
          System.out.printf("%-36s", "5. Advanced estimate : ");
          flagNewReference = flagNewReference || solvePuzzle(board, Level.BOOST_ESTIMATE);

          if (solver.hasPartialSolution(board)) {
            System.out.printf("%-36s", "6. Use preset partial solution :");
            solvePuzzle(board, Level.OPTIMUM);
          } else {
            System.out.println("6.\t Skip - No preset partial solution.");
          }

        } else {
          System.out.println("5 & 6.\t Skip - Both estimate are the same.");
        }
        System.out.println();

        if (flagNewReference) {
          try {
            getRefConnection().updateLastSearch(solver, board);
          } catch (RemoteException e) {
            e.printStackTrace();
          }
        } else {
          System.out.println("No update.");
        }
      } else {
        System.out.println("The board is unsolvable, try again!");
      }
    }
  }
}
