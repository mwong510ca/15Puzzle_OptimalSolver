package mwong.myprojects.fifteenpuzzle.console;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Scanner;

import mwong.myprojects.fifteenpuzzle.puzzle.Board;
import mwong.myprojects.fifteenpuzzle.puzzle.HeuristicOptions;
import mwong.myprojects.fifteenpuzzle.puzzle.PatternOptions;
import mwong.myprojects.fifteenpuzzle.solution.Solver;
import mwong.myprojects.fifteenpuzzle.solution.SolverBuilder;
import mwong.myprojects.fifteenpuzzle.solution.Solver.ApplicationMode;

/**
 * SolverHeuristicStats is the final concrete class of console application extends
 * AbstractApplication. It takes a number of trial T and run the solver T time with random board.
 * User may choose the type of heuristic function, type of random board, and change the timeout
 * limit from 1 to 60 seconds. Only pattern database 7-8 has an option to turn timeout feature off.
 * It will display the total process time, number of timeout boards, and average time of boards
 * with solution.
 *
 * <p>Dependencies : AbstractApplication.java, Board.java, HeuristicOptions.java,
 *                   PatternOptions.java, Solver.java, SolverBuilder.java,
 *                   UniversalSolverFactory.java
 *
 * @author <a href="http://www.linkedin.com/pub/macy-wong/46/550/37b/"
 *            target="_blank">Meisze Wong (linkedin)</a>
 * @see <a href="http://www.github.com/mwong510ca/15PuzzleOptimalSolver/"
 *         target="_blank">GitHub (full project)</a>
 */
public final class SolverHeuristicStats extends AbstractApplication {
  /** The variable of custom maximum timeout limit. */
  private static final int SUGGESTED_MAX_TIMEOUT = 60;
  /** The variable of prefer timeout limit. */
  private static final int PREFER_TIMEOUT = 10;
  /** The variable of prefer number of trails based on difficulty level. */
  private static final int PREFER_TRAILS = 100;
  /** The variable of base trails count per line. */
  private static final int LN_COUNT_BASE = 20;
  /** The integer array of trails count per line for Easy boards. */
  private static final int[] LN_COUNT_EASY = {1000000, 50000};
  /** The integer array of trails count per line for random or Moderate boards. */
  private static final int[] LN_COUNT_RANDOM = {1000, 100, LN_COUNT_BASE};
  /** The integer array of trails count per line for Hard boards. */
  private static final int[] LN_COUNT_HARD = {50, LN_COUNT_BASE};
  /** The instance of SolverBuilder. */
  private SolverBuilder builder;
  /** The instance of Solver object. */
  private Solver solver;
  /** The MenuOptions array of default menu. */
  private final MenuOptions[] defaultMenu;

  // Verify the sub-menu.
  static {
    if (!verifySubmenuOrdering(MenuHeuristic.values())) {
      throw new RuntimeException(MenuHeuristic.class.getName() + " - check distint key setting");
    }
  }

  /**
   * Create SolverHeuristicStats application object.
   *
   * @throws IOException any unexpected IOException
   */
  public SolverHeuristicStats() throws IOException {
    this(SUGGESTED_MAX_TIMEOUT);
  }

  /**
   * Initial SolverHeuristicStats object with custom define maximum timeout limit.
   *
   * @param customTimeoutMax Allow user to change the timeout limit up to customTimeoutMax.
   * @throws IOException any unexpected IOException
   */
  public SolverHeuristicStats(final int customTimeoutMax) throws IOException {
    super(customTimeoutMax);
    builder = new SolverBuilder(ApplicationMode.CONSOLE, getTimeoutLimit());
    builder.setReference(getRefConnection());
    solver = builder.createSolver(HeuristicOptions.PD78);
    builder.setVersion(solver.getVersion());
    defaultMenu = new MenuOptions[] {MenuOptions.CHANGE_HEURISTIC, MenuOptions.SWITCH_VERSION};
  }

  /**
   * Display a list of main menu options.
   *
   * @throws Exception any unexpected Exception
   */
  private void menuMain() throws Exception {
    boolean[] mainList = createMenuList(defaultMenu);
    if (solver.isTimerOn()) {
      mainList[MenuOptions.TIMEOUT_LIMIT.getIndex()] = true;
    }
    if (solver.getHeuristic() == HeuristicOptions.PD78) {
      mainList[MenuOptions.TIMEOUT_FEATURE.getIndex()] = true;
    }
    String optionStr = menuOption(mainList, solver) + "\nOr a positive integer of number of trials";
    System.out.println("   Or a positive integer of number of trials");

    while (true) {
      if (scanner.hasNextInt()) {
        break;
      }
      char choice = scanner.next().toUpperCase().charAt(0);
      if (choice == 'Q') {
        appExit();
      } else if (mainList[MenuOptions.CHANGE_HEURISTIC.getIndex()] && choice == 'C') {
        solver = menuChangeSolver(builder, solver, MenuHeuristic.class);
        //resumeVersion(solver, inUseVersion);
        solver.printDescription();
        if (solver.getHeuristic() == HeuristicOptions.PD78) {
          mainList[MenuOptions.TIMEOUT_FEATURE.getIndex()] = true;
        } else {
          mainList[MenuOptions.TIMEOUT_FEATURE.getIndex()] = false;
        }
        mainList[MenuOptions.CHANGE_HEURISTIC.getIndex()] = false;
        if (solver.isTimerOn()) {
          mainList[MenuOptions.TIMEOUT_LIMIT.getIndex()] = true;
        }
        optionStr = menuOption(mainList, solver) + "\nOr a positive integer of number of trials";
        System.out.println("   Or a positive integer of number of trials");
      } else if (mainList[MenuOptions.SWITCH_VERSION.getIndex()] && choice == 'V') {
        builder.setVersion(flipVersion(solver));
        mainList[MenuOptions.SWITCH_VERSION.getIndex()] = false;
        optionStr = menuOption(mainList, solver) + "\nOr a positive integer of number of trials";
        System.out.println("   Or a positive integer of number of trials");
      } else if (mainList[MenuOptions.TIMEOUT_FEATURE.getIndex()] && choice == 'O') {
        if (solver.getHeuristic() != HeuristicOptions.PD78) {
          throw new Exception();
        }
        solver.setTimerOn(!solver.isTimerOn());
        if (solver.isTimerOn()) {
          mainList[MenuOptions.TIMEOUT_LIMIT.getIndex()] = true;
        }
        mainList[MenuOptions.TIMEOUT_FEATURE.getIndex()] = false;
        optionStr = menuOption(mainList, solver) + "\nOr a positive integer of number of trials";
        System.out.println("   Or a positive integer of number of trials");
      } else if (mainList[MenuOptions.TIMEOUT_LIMIT.getIndex()] && choice == 'T'
          && solver.isTimerOn()) {
        final int limitPdb78 = 120;
        final int limitPdb = 60;
        final int limitOther = 30;
        int limit = limitOther;
        if (solver.getHeuristic() == HeuristicOptions.PD78) {
          limit = limitPdb78;
        } else if (solver.getHeuristic() == HeuristicOptions.PD663
            || solver.getHeuristic() == HeuristicOptions.PD555) {
          limit = limitPdb;
        }
        changeTimeout(solver, limit);
        mainList[MenuOptions.TIMEOUT_LIMIT.getIndex()] = false;
        optionStr = menuOption(mainList, solver) + "\nOr a positive integer of number of trials";
        System.out.println("   Or a positive integer of number of trials");
      } else {
        System.out.println(optionStr);
      }
    }
  }

  @Override
  public void run() throws Exception {
    scanner = new Scanner(System.in, "UTF-8");
    solver.setStatusOn(false);
    solver.setTimeoutLimit(getTimeoutLimit());
    solver.printDescription();

    while (true) {
      menuMain();
      int lineCount = LN_COUNT_BASE;
      Board.DifficultyLevel boardLevel = Board.DifficultyLevel.RANDOM;

      int trails = 0;
      while (trails <= 0) {
        if (!scanner.hasNextInt()) {
          if (scanner.next().charAt(0) == 'q') {
            System.exit(0);
          }
        } else {
          trails = scanner.nextInt();
        }
      }

      HeuristicOptions inUseHeuristic = solver.getHeuristic();
      String strOption = "Choose type of board: 'R'andom, 'E'asy, 'M'oderate";
      if (inUseHeuristic == HeuristicOptions.PD78) {
        strOption = strOption + ", 'H'ard";
        lineCount = LN_COUNT_RANDOM[0];
      } else if (inUseHeuristic == HeuristicOptions.PD555
          || inUseHeuristic == HeuristicOptions.PD663) {
        if (getTimeoutLimit() <= PREFER_TIMEOUT) {
          strOption = strOption + ", 'H'ard";
        }
        lineCount = LN_COUNT_RANDOM[1];
      } else {
        lineCount = LN_COUNT_RANDOM[2];
      }
      System.out.println(strOption + "\n    Invalid entry will use random board");

      char choice = scanner.next().toUpperCase().charAt(0);
      System.out.println();
      if (choice == 'E') {
        boardLevel = Board.DifficultyLevel.EASY;
        if (inUseHeuristic == HeuristicOptions.PD78 || inUseHeuristic == HeuristicOptions.PD663
            || inUseHeuristic == HeuristicOptions.PD555) {
          lineCount = LN_COUNT_EASY[0];
        } else {
          lineCount = LN_COUNT_EASY[1];
        }
      } else if (choice == 'M') {
        boardLevel = Board.DifficultyLevel.MODERATE;
      } else if (choice == 'H') {
        if (inUseHeuristic == HeuristicOptions.PD78 || (getTimeoutLimit() <= PREFER_TIMEOUT
            && (inUseHeuristic == HeuristicOptions.PD555
            || inUseHeuristic == HeuristicOptions.PD663))) {
          boardLevel = Board.DifficultyLevel.HARD;
          if (inUseHeuristic == HeuristicOptions.PD78) {
            lineCount = LN_COUNT_HARD[0];
          } else {
            lineCount = LN_COUNT_HARD[1];
          }
        }
      }

      if (lineCount == LN_COUNT_BASE && trails > PREFER_TRAILS) {
        System.out.println("Warning: " + trails + " trails may take too long to run, adjust to "
            + PREFER_TRAILS + ".");
      }
      System.out.print(trails + " trials of " + boardLevel + " random board with ");

      final int divisorAll = lineCount / 10;
      int divisor = divisorAll;
      if (lineCount == LN_COUNT_EASY[0]) {
        final int divisorLarge = lineCount / 4;
        divisor = divisorLarge;
      }
      final int quarter = divisor / 4;
      final int half = divisor / 2;
      HashSet<Integer> remainders = new HashSet<>();
      remainders.add(quarter);
      remainders.add(half);
      remainders.add(half + quarter);

      System.out.print("\n" + solver.getHeuristic().getDescription());
      if (solver.getVersion().isOptimum()) {
        System.out.print(" (Advanced version) ");
      } else {
        System.out.print(" (Standard version) ");
      }
      if (solver.isTimerOn()) {
        System.out.println("will timeout at " + solver.getTimeoutLimit() + "s:");
      } else {
        System.out.println("will run until solution found:");
      }

      double totalSearchTime = 0.0;
      int timeoutCounter = 0;
      int overLimitCounter = 0;

      for (int i = 1; i <= trails; i++) {
        Board board = new Board(boardLevel);
        if (i % lineCount == 1) {
          System.out.printf("\n%1.2fs : ", totalSearchTime);
        }
        solver.findOptimalPath(board);

        totalSearchTime += solver.searchTime();
        if (solver.isSearchTimeout()) {
          timeoutCounter++;
        } else {
          if (inUseHeuristic == HeuristicOptions.PD78 && solver.searchTime() > getTimeoutLimit()) {
            overLimitCounter++;
          }
        }

        if (i % divisor == 1 || lineCount == LN_COUNT_BASE) {
          System.out.print(" " + i + " ");
        } else if (remainders.contains(i % divisor)) {
          System.out.print(".");
        }
      }

      System.out.printf("\n" + trails + " trails completed - %1.2fs include timeout.\n",
          totalSearchTime);
      if (timeoutCounter > 0) {
        System.out.println(timeoutCounter + " out of " + trails
            + " boards are timeout after " + solver.getTimeoutLimit() + "s,");
        if (timeoutCounter < trails) {
          double runtime = totalSearchTime - timeoutCounter * getTimeoutLimit();
          System.out.printf("Average time per completed search: %1.6fs \n",
              runtime / trails);
        }
      } else {
        System.out.printf("Average time per board: %1.6fs \n", totalSearchTime / trails);
        if (overLimitCounter > 0) {
          System.out.println(overLimitCounter + " out of " + trails + " boards takes over "
              + getTimeoutLimit() + "s to solver using Pattern Database 78.");
        }
      }
      System.out.println();

      if (solver.getVersion().isOptimum() && inUseHeuristic == HeuristicOptions.PD78) {
        try {
          getRefConnection().updatePending(solver);
        } catch (RemoteException ex) {
          // standalone
        }
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
