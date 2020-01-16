package mwong.myprojects.fifteenpuzzle.console;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Comparator;

import mwong.myprojects.fifteenpuzzle.puzzle.Board;
import mwong.myprojects.fifteenpuzzle.puzzle.HeuristicOptions;
import mwong.myprojects.fifteenpuzzle.puzzle.PatternOptions;
import mwong.myprojects.fifteenpuzzle.solution.Solver;
import mwong.myprojects.fifteenpuzzle.solution.SolverBuilder;
import mwong.myprojects.fifteenpuzzle.solution.Solver.ApplicationMode;

/**
 * CompareHeuristic is the final concrete class of console application extends AbstractApplication.
 * It takes a 16 numbers or choice of random board. It will go through each heuristic function from
 * fastest to slowest. It display the process time and number of nodes generated during the search.
 * If it timeout after timeout setting (in resources/config.properties or default is 10 seconds)
 * except pattern database 78. The remaining heuristic function will display the estimate only.
 *
 * <p>Dependencies : AbstractApplication.java, Board.java, PatternOptions.java, Solver.java
 *                   SolverBuilder.java, UniversalSolverFactory.java
 *
 * @author <a href="http://www.linkedin.com/pub/macy-wong/46/550/37b/"
 *            target="_blank">Meisze Wong (linkedin)</a>
 * @see <a href="http://www.github.com/mwong510ca/15PuzzleOptimalSolver/"
 *         target="_blank">GitHub (full project)</a>
 */
public final class CompareHeuristic extends AbstractApplication {
  /** The Solver array choices of Solvers. */
  private Solver[] solverList;
  /** The boolean flag of on going search with Prime version. */
  private boolean activePrime;
  /** The boolean flag of on going search with Optimum version. */
  private boolean activeOptimum;

  // Verify the sub-menu.
  static {
    if (!verifySubmenuOrdering(MenuHeuristic.values())) {
      throw new RuntimeException(MenuHeuristic.class.getName() + " - check distint key setting");
    }
  }

  /**
   * Create CompareHeuristic application object.
   *
   * @throws IOException any unexpected IOException
   */
  public CompareHeuristic() throws IOException {
    super();
    final int suggestTimeout = 10;
    if (getTimeoutLimit() > suggestTimeout) {
      System.out.println("Warnings: Timeout limit is " + getTimeoutLimit() + ".\n"
          + "Recommand to use " + suggestTimeout + " seconds or less,"
          + " you may change the setting in config.properties file.\n");
    }

    SolverBuilder builder = new SolverBuilder(ApplicationMode.CONSOLE, getTimeoutLimit());
    builder.setReference(getRefConnection());

    MenuHeuristic[] submenu = MenuHeuristic.values();
    solverList = new Solver[submenu.length];
    Arrays.sort(submenu, Comparator.comparing(MenuHeuristic::getOrder));
    int i = 0;
    for (SubmenuHeuristic item : submenu) {
      solverList[i++] = builder.createSolver(item.getHeuristic(), item.getWdFlag());
    }
  }

  /**
   * It take a solver and a 15 puzzle board, display the the process time and number of
   * nodes generated with standard version. If advanced estimate is difference, also display
   * advanced search. It will time out after 10 seconds if timeout feature is on.
   *
   * @param solver the given Solver object
   * @param board the given Board object
   */
  private void solvePuzzle(final Solver solver, final Board board) {
    System.out.print(solver.getHeuristic().getDescription());
    if (solver.getClass().getSimpleName().equals("SolverPdbWd")) {
      System.out.print(" + Walking Distance");
    }
    if (solver.isTimerOn()) {
      System.out.println(" will timeout at " + solver.getTimeoutLimit() + "s:");
    } else {
      System.out.println(" will run until solution found:");
    }

    solver.shiftPrime();
    int heuristicStandard = solver.heuristicBasis(board);

    System.out.print("Standard\t" + heuristicStandard + "\t\t");

    if (activePrime) {
      solver.findOptimalPath(board);

      if (solver.isSearchTimeout()) {
        System.out.println("Timeout: " + solver.searchTime() + "s at depth "
            + solver.searchDepth() + "\t" + solver.searchNodeCount());
        if (solver.getHeuristic() != HeuristicOptions.WD) {
          activePrime = false;
        }
      } else {
        System.out.printf("%-15s %-15s " + solver.searchNodeCount() + "\n",
            solver.searchTime() + "s", solver.moves());
      }
    } else {
      System.out.println("Skip searching - will not solved in " + getTimeoutLimit() + "s.");
    }

    if (solver.shiftOptimum()) {
      int heuristicAdvanced = solver.heuristicBoost(board);

      if (heuristicStandard == heuristicAdvanced) {
        System.out.println("Advanced\t" + "Same value");
      } else {
        System.out.print("Advanced\t" + heuristicAdvanced + "\t\t");

        if (activeOptimum) {
          solver.findOptimalPath(board);

          if (solver.isSearchTimeout()) {
            System.out.println("Timeout: " + solver.searchTime() + "s at depth "
                + solver.searchDepth() + "\t"
                + solver.searchNodeCount());
            if (solver.getHeuristic() != HeuristicOptions.WD) {
              activePrime = false;
              activeOptimum = false;
            }
          } else {
            System.out.printf("%-15s %-15s " + solver.searchNodeCount() + "\n",
                solver.searchTime() + "s", solver.moves());
          }
        } else {
          System.out.println("Skip searching - will not solved in " + getTimeoutLimit() + "s.");
        }
      }
    }
    System.out.println();
  }

  @Override
  public void run() {
    System.out.println("Compare heuristic functions with prime and optimum version.\n");
    boolean[] menuList = createMenuList(MenuOptions.CREATE_PUZZLE, MenuOptions.KEY_IN_PUZZLE);

    while (true) {
      String optionStr = menuOption(menuList);

      Board board = null;
      while (true) {
        if (scanner.hasNextInt()) {
          board = keyInBoard();
        } else {
          char choice = scanner.next().toUpperCase().charAt(0);
          if (choice == 'Q') {
            appExit();
          }
          board = createBoard(choice);
        }
        if (board != null) {
          break;
        }
        System.out.println(optionStr);
      }

      System.out.print("\n" + board + "\n");

      if (board.isSolvable()) {
        System.out.println("\t\tEstimate\tTime\t\tMinimum Moves\tNodes generated");
        activePrime = true;
        activeOptimum = true;

        for (Solver solver : solverList) {
          if (solver != null) {
            solvePuzzle(solver, board);
          }
          // Notes: updateLastSearch is optional.
          if (solver.isNewReference()) {
            try {
              getRefConnection().updateLastSearch(solver);
            } catch (RemoteException ex) {
              // standalone
            }
          }
        }
      } else {
        System.out.println("The board is unsolvable, try again!\n");
      }
    }
  }

  /**
   * Sub-menu of heuristic choices with easy maintenance.
   * Notes: Remove an item, simply comment out the item.
   *        For ordering: simply change the order numbers, make sure from 1 to size.
   *
   * @author <a href="http://www.linkedin.com/pub/macy-wong/46/550/37b/"
   *            target="_blank">Meisze Wong (linkedin)</a>
   */
  private enum MenuHeuristic implements SubmenuHeuristic {
    /** Manhattan distance. */
    MANHATTAN_DISTANCE(10, HeuristicOptions.MD, false),
    /** Manhattan distance with linear conflict. */
    MANHATTAN_LINEAR_CONFLICT(9, HeuristicOptions.MDLC, false),
    /** Walking distance. */
    WALKING_DISTANCE(8, HeuristicOptions.WD, false),
    /** Walking distance with Manhattan distance. */
    MANHATTAN_X_WALKING(7, HeuristicOptions.WDMD, false),
    /** Pattern database use 555 pattern. */
    PATTERN_DB_555(6, HeuristicOptions.PD555, false),
    /** Pattern database use 555 pattern with Walking distance. */
    PDB555_X_WALKING(5, HeuristicOptions.PD555, true),
    /** Pattern database use 663 pattern. */
    PATTERN_DB_663(4, HeuristicOptions.PD663, false),
    /** Pattern database use 663 pattern with Walking distance. */
    PDB663_X_WALKING(3, HeuristicOptions.PD663, true),
    /** Pattern database use 78 pattern. */
    PATTERN_DB_78(2, HeuristicOptions.PD78, false),
    /** Pattern database use 78 pattern with Walking distance. */
    PDB78_X_WALKING(1, HeuristicOptions.PD78, true);

    /** The variable of order number. */
    private int order;
    /** The variable of HeuristicOptions. */
    private HeuristicOptions heuristic;
    /** The boolean value of walking distance. */
    private boolean optionalWd;

    /**
     * Initialize the heuristic menu option.
     *
     * @param order the given menu order
     * @param heuristic the given HeuristicOptions
     * @param optionalWd the boolean flag represents walking distance in use
     */
    MenuHeuristic(final int order, final HeuristicOptions heuristic,
        final boolean optionalWd) {
      this.order = order;
      this.heuristic = heuristic;
      this.optionalWd = optionalWd;
    }

    @Override
    public int getOrder() {
      return order;
    }

    @Override
    public HeuristicOptions getHeuristic() {
      return heuristic;
    }

    @Override
    public PatternOptions getPtnOption() {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean getWdFlag() {
      return optionalWd;
    }

    @Override
    public String getClassName() {
      throw new UnsupportedOperationException();
    }

    @Override
    public String getMessage() {
      throw new UnsupportedOperationException();
    }
  }
}
