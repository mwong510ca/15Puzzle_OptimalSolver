package mwong.myprojects.fifteenpuzzle.console;

import java.io.IOException;
import java.rmi.RemoteException;

import mwong.myprojects.fifteenpuzzle.puzzle.Board;
import mwong.myprojects.fifteenpuzzle.puzzle.HeuristicOptions;
import mwong.myprojects.fifteenpuzzle.puzzle.PatternOptions;
import mwong.myprojects.fifteenpuzzle.solution.Solver;
import mwong.myprojects.fifteenpuzzle.solution.SolverBuilder;
import mwong.myprojects.fifteenpuzzle.solution.Solver.ApplicationMode;
import mwong.myprojects.fifteenpuzzle.solution.SolverQuick;

/**
 * SolverHeuristic is the final concrete class of console application extends AbstractApplication.
 * User can select the choice of heuristic function and version. It takes a 16 numbers or choice
 * of random board. It display the process time and number of nodes generated at each depth.
 * It will timeout after timeout setting (in resources/config.properties or default is 10 seconds),
 * except pattern database 78.
 *
 * <p>Dependencies : AbstractApplication.java, Board.java, HeuristicOptions.java,
 *                   PatternOptions.java, Solver.java, SolverBuilder.java, SolverQuick.java,
 *                   UniversalSolverFactory.java
 *
 * @author <a href="http://www.linkedin.com/pub/macy-wong/46/550/37b/"
 *            target="_blank">Meisze Wong (linkedin)</a>
 * @see <a href="http://www.github.com/mwong510ca/15PuzzleOptimalSolver/"
 *         target="_blank">GitHub (full project)</a>
 */
public final class SolverHeuristic extends AbstractApplication {
  /** The variable of custom maximum timeout limit. */
  private static final int FIXED_MAX_TIMEOUT = 30;
  /** The default heuristic function to start the application. */
  private static final MenuHeuristic DEFAULT_HEURISTIC = MenuHeuristic.PATTERN_DB_663;
  /** The instance of SolverBuilder. */
  private SolverBuilder builder;
  /** The instance of Solver object. */
  private Solver solver;
  /** The instance of SolverQuick object for non optimal solution. */
  private SolverQuick quickSolver;
  /** The MenuOptions array of default menu. */
  private final MenuOptions[] defaultMenu;

  // Verify the sub-menu.
  static {
    if (!verifySubmenuOrdering(MenuHeuristic.values())) {
      throw new RuntimeException(MenuHeuristic.class.getName() + " - check distint key setting");
    }
  }

  /**
   * Create SolverHeuristic application object.
   *
   * @throws IOException any unexpected IOException
   */
  public SolverHeuristic() throws IOException {
    super(FIXED_MAX_TIMEOUT);
    builder = new SolverBuilder(ApplicationMode.CONSOLE, true, getTimeoutLimit());
    builder.setReference(getRefConnection());
    solver = builder.createSolver(DEFAULT_HEURISTIC.getHeuristic(), true);
    quickSolver = new SolverQuick();
    defaultMenu = new MenuOptions[] {MenuOptions.CHANGE_HEURISTIC, MenuOptions.TIMEOUT_LIMIT,
        MenuOptions.CREATE_PUZZLE, MenuOptions.KEY_IN_PUZZLE};
  }

  /**
   * Display a list of main menu options.
   *
   * @return Board object to main function
   */
  private Board menuMain() {
    return menuMain(null, -1, null);
  }

  /**
   * Display a list of main menu options with solution display from search results.
   *
   * @param lastBoard the last search board with solution
   * @param steps the given number of moves from solution
   * @param moves the Board.Move array of solution
   * @return Board object to main function
   */
  private Board menuMain(final Board lastBoard, final int steps, final Board.Move[] moves) {
    boolean[] mainList = createMenuList(defaultMenu);
    boolean flagSolution = false;
    if (lastBoard != null) {
      mainList = createMenuList(defaultMenu, MenuOptions.LIST_MOVES, MenuOptions.DISPLAY_MOVES);
      flagSolution = true;
    }
    if (solver.getHeuristic() == HeuristicOptions.PD78) {
      mainList[MenuOptions.TIMEOUT_LIMIT.getIndex()] = false;
    }
    String optionStr = menuOption(mainList, solver, MenuHeuristic.class);

    Board board = null;
    while (true) {
      if (scanner.hasNextInt()) {
        board = keyInBoard();
        if (board != null) {
          return board;
        }
        System.out.println(optionStr);
      } else {
        char choice = scanner.next().toUpperCase().charAt(0);
        if (choice == 'Q') {
          appExit();
        } else if (mainList[MenuOptions.CHANGE_HEURISTIC.getIndex()] && choice == 'C') {
          solver = menuChangeSolver(builder, solver, MenuHeuristic.class);
          solver.printDescription();
          if (solver.getHeuristic() == HeuristicOptions.PD78) {
            mainList[MenuOptions.TIMEOUT_FEATURE.getIndex()] = true;
          } else {
            mainList[MenuOptions.TIMEOUT_FEATURE.getIndex()] = false;
          }
          mainList[MenuOptions.CHANGE_HEURISTIC.getIndex()] = false;
          optionStr = menuOption(mainList, solver);
        } else if (mainList[MenuOptions.SWITCH_VERSION.getIndex()] && choice == 'V') {
          builder.setVersion(flipVersion(solver));
          mainList[MenuOptions.SWITCH_VERSION.getIndex()] = false;
          optionStr = menuOption(mainList, solver);
        } else if (mainList[MenuOptions.TIMEOUT_LIMIT.getIndex()] && choice == 'T'
            && solver.isTimerOn()) {
          changeTimeout(solver);
          builder.setTimeoutLimit(getTimeoutLimit());
          mainList[MenuOptions.TIMEOUT_LIMIT.getIndex()] = false;
          optionStr = menuOption(mainList, solver);
        } else if (flagSolution) {
          if (choice == 'L') {
            solutionList(lastBoard, steps, moves);
          } else if (choice == 'D') {
            solutionDetail(lastBoard, steps, moves);
          } else {
            board = createBoard(choice);
            if (board != null) {
              return board;
            }
          }
          flagSolution = false;
          mainList = createMenuList(defaultMenu);
          optionStr = menuOption(mainList, solver);
        } else {
          board = createBoard(choice);
          if (board != null) {
            return board;
          }
          System.out.println(optionStr);
        }
      }
    }
  }

  @Override
  public void run() {
    solver.printDescription();
    Board board = menuMain();
    while (true) {
      System.out.print(solver.getHeuristic().getDescription());
      if (solver.getVersion().isOptimum()) {
        System.out.print(" (Otpimum version) ");
      } else {
        System.out.print(" (Prime version) ");
      }

      if (solver.isTimerOn()) {
        System.out.println("will timeout at " + solver.getTimeoutLimit() + "s:");
      } else {
        System.out.println("will run until solution found:");
      }

      System.out.println(board);
      if (!board.isSolvable()) {
        System.out.println("The board is unsolvable, try again!\n");
        board = menuMain();
        continue;
      }

      System.out.println(solver.heuristic(board));

      solver.findOptimalPath(board);
      if (solver.isSearchTimeout()) {
        System.out.println("Search terminated at " + solver.searchTime() + "s.\n");
        quickSolver.findPathQuickSearch(board, solver);
        System.out.println("Alternative non optimal solution:");
        System.out.println("Number of moves " + quickSolver.moves() + ", search time "
            + quickSolver.searchTime() + " seconds.\n");

        board = menuMain(board, quickSolver.moves(), quickSolver.solution());
      } else {
        System.out.println("Minimum number of moves = "
            + solver.moves() + "\n");

        // Notes: updateLastSearch is optional.
        if (solver.isNewReference()) {
          try {
            getRefConnection().updateLastSearch(solver);
          } catch (RemoteException ex) {
            // Standalone.
          }
        }
        board = menuMain(board, solver.moves(), solver.solution());
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
    MANHATTAN_DISTANCE(1, HeuristicOptions.MD, false, "SolverMd"),
    /** Manhattan distance with linear conflict. */
    MANHATTAN_LINEAR_CONFLICT(2, HeuristicOptions.MDLC, false, "SolverMd"),
    /** Walking distance. */
    WALKING_DISTANCE(3, HeuristicOptions.WD, false, "SolverWd"),
    /** Walking distance with Manhattan distance. */
    MANHATTAN_X_WALKING(4, HeuristicOptions.WDMD, false, "SolverWdMd"),
    /** Pattern database use 555 pattern. */
    PATTERN_DB_555(5, HeuristicOptions.PD555, false, "SolverPdb"),
    /** Pattern database use 555 pattern with Walking distance. */
    PDB555_X_WALKING(6, HeuristicOptions.PD555, true, "SolverPdbWd"),
    /** Pattern database use 663 pattern. */
    PATTERN_DB_663(7, HeuristicOptions.PD663, false, "SolverPdb"),
    /** Pattern database use 663 pattern with Walking distance. */
    PDB663_X_WALKING(8, HeuristicOptions.PD663, true, "SolverPdbWd"),
    /** Pattern database use 78 pattern. */
    PATTERN_DB_78(9, HeuristicOptions.PD78, false, "SolverPdb");

    /** The variable of order number. */
    private int order;
    /** The variable of HeuristicOptions. */
    private HeuristicOptions heuristic;
    /** The boolean value of walking distance. */
    private boolean optionalWd;
    /** The variable of String of simple class name. */
    private String className;

    /**
     * Initialize the heuristic menu option.
     *
     * @param order the given menu order
     * @param heuristic the given HeuristicOptions
     * @param optionalWd the boolean flag represents walking distance in use
     * @param className the given String of simple class name
     */
    MenuHeuristic(final int order, final HeuristicOptions heuristic,
        final boolean optionalWd, final String className) {
      this.order = order;
      this.heuristic = heuristic;
      this.optionalWd = optionalWd;
      this.className = className;
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
      return className;
    }

    @Override
    public String getMessage() {
      throw new UnsupportedOperationException();
    }
  }
}
