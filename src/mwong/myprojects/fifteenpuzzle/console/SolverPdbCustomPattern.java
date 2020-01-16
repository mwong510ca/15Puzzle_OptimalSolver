package mwong.myprojects.fifteenpuzzle.console;

import java.io.IOException;
import java.rmi.RemoteException;

import mwong.myprojects.fifteenpuzzle.puzzle.Board;
import mwong.myprojects.fifteenpuzzle.puzzle.HeuristicOptions;
import mwong.myprojects.fifteenpuzzle.puzzle.PatternOptions;
import mwong.myprojects.fifteenpuzzle.solution.Solver;
import mwong.myprojects.fifteenpuzzle.solution.SolverBuilder;
import mwong.myprojects.fifteenpuzzle.solution.Solver.ApplicationMode;

/**
 * SolverPdbCustomPattern is the final concrete class of console application extends
 * AbstractApplication allow user to enter custom defined pattern or choice of preset patterns.
 * User may change the timeout limit from 3 to 300 seconds. It will display the process time,
 * number of solution moves, and number of nodes generated. The solver will timeout after
 * timeout limit include pattern database 78.
 *
 * <p>Dependencies : AbstractApplication.java, Board.java, HeuristicOptions.java,
 *                   PatternOptions.java, Solver.java, SolverBuilder.java,
 *                   UniversalSolverFactory.javabb
 *
 * @author <a href="http://www.linkedin.com/pub/macy-wong/46/550/37b/"
 *            target="_blank">Meisze Wong (linkedin)</a>
 * @see <a href="http://www.github.com/mwong510ca/15PuzzleOptimalSolver/"
 *         target="_blank">GitHub (full project)</a>
 */
public final class SolverPdbCustomPattern extends AbstractApplication {
  /** The instance of SolverBuilder. */
  private SolverBuilder builder;
  /** The instance of Solver using pattern database. */
  private Solver solverPdb;
  /** The MenuOptions array of default menu. */
  private final MenuOptions[] defaultMenu;

  // Verify the sub-menu.
  static {
    if (!verifySubmenuOrdering(MenuPdbPattern.values())) {
      throw new RuntimeException(MenuPdbPattern.class.getName() + " - check distint key setting");
    }
  }

  /**
   * Create SolverPdbCustomPattern application object.
   *
   * @throws IOException any unexpected IOException
   */
  public SolverPdbCustomPattern() throws IOException {
    super();
    builder = new SolverBuilder(ApplicationMode.CONSOLE, true, getTimeoutLimit());
    builder.setReference(getRefConnection());
    solverPdb = builder.createSolverPdb(HeuristicOptions.PD663, 0);
    defaultMenu = new MenuOptions[] {MenuOptions.CHANGE_HEURISTIC, MenuOptions.SWITCH_VERSION,
        MenuOptions.CREATE_PUZZLE, MenuOptions.KEY_IN_PUZZLE};
  }

  /**
   * Display the main menu and take action, return the Board object to main function.
   *
   * @return Board object to main function
   * @throws Exception any unexpected IOException
   */
  private Board menuMain() throws Exception {
    boolean[] mainList = createMenuList(defaultMenu);
    if (solverPdb.isTimerOn()) {
      mainList[MenuOptions.TIMEOUT_LIMIT.getIndex()] = true;
    }
    if (solverPdb.getHeuristic() == HeuristicOptions.PD78) {
      mainList[MenuOptions.TIMEOUT_FEATURE.getIndex()] = true;
    }
    String optionStr = menuOption(mainList, solverPdb);

    while (true) {
      if (scanner.hasNextInt()) {
        return keyInBoard();
      }
      char choice = scanner.next().toUpperCase().charAt(0);
      if (choice == 'Q') {
        appExit();
      } else if (mainList[MenuOptions.CHANGE_HEURISTIC.getIndex()] && choice == 'C') {
        solverPdb = menuChangeSolver(builder, solverPdb, MenuPdbPattern.class, true);
        //resumeVersion(solverPdb, inUseVersion);
        mainList[1] = false;
        if (solverPdb.getHeuristic() == HeuristicOptions.PD78) {
          mainList[MenuOptions.TIMEOUT_FEATURE.getIndex()] = true;
        } else {
          mainList[MenuOptions.TIMEOUT_FEATURE.getIndex()] = false;
        }
        if (solverPdb.isTimerOn()) {
          mainList[MenuOptions.TIMEOUT_LIMIT.getIndex()] = true;
        }
        optionStr = menuOption(mainList, solverPdb);
      } else if (mainList[MenuOptions.SWITCH_VERSION.getIndex()] && choice == 'V') {
        builder.setVersion(flipVersion(solverPdb));
        mainList[MenuOptions.SWITCH_VERSION.getIndex()] = false;
        optionStr = menuOption(mainList, solverPdb);
      } else if (mainList[MenuOptions.TIMEOUT_FEATURE.getIndex()] && choice == 'O') {
        if (solverPdb.getHeuristic() != HeuristicOptions.PD78) {
          throw new Exception();
        }
        solverPdb.setTimerOn(!solverPdb.isTimerOn());
        if (solverPdb.isTimerOn()) {
          mainList[MenuOptions.TIMEOUT_LIMIT.getIndex()] = true;
        }
        mainList[MenuOptions.TIMEOUT_FEATURE.getIndex()] = false;
        optionStr = menuOption(mainList, solverPdb);
      } else if (mainList[MenuOptions.TIMEOUT_LIMIT.getIndex()] && choice == 'T'
            && solverPdb.isTimerOn()) {
        final int limitPdb78 = 120;
        final int limitAll = 60;
        int limit = limitAll;
        if (solverPdb.getHeuristic() == HeuristicOptions.PD78) {
          limit = limitPdb78;
        }
        changeTimeout(solverPdb, limit);
        builder.setTimeoutLimit(getTimeoutLimit());
        mainList[MenuOptions.TIMEOUT_LIMIT.getIndex()] = false;
        optionStr = menuOption(mainList, solverPdb);
      }  else {
        Board board = createBoard(choice);
        if (board != null) {
          return board;
        }
        System.out.println(optionStr);
      }
    }
  }

  @Override
  public void run() throws Exception {
    System.out.println("Allow user to try on more preset patterns"
        + " or enter a user defined custom pattern.\n");

    //resumeVersion(solverPdb, inUseVersion);
    solverPdb.setTimeoutLimit(getTimeoutLimit());
    solverPdb.printDescription();

    Board board = null;

    while (true) {
      if (isRequestedRemote()) {
        resetRemoteConnection(solverPdb);
      }

      board = menuMain();
      solverPdb.printHeader(true);
      System.out.println(board);

      if (board.isSolvable()) {
        solverPdb.findOptimalPath(board);

        if (solverPdb.isSearchTimeout()) {
          System.out.println("Search terminated after " + getTimeoutLimit() + "s.");
        } else {
          System.out.println("Minimum number of moves = "
              + solverPdb.moves() + ", search time use "
              + solverPdb.searchTime() + " seconds.");
        }
      } else {
        System.out.println("The board is unsolvable, try again!\n");
      }
      System.out.println();

      try {
        if (solverPdb.isNewReference()) {
          getRefConnection().updateLastSearch(solverPdb);
        }
      } catch (RemoteException ex) {
        // standalone
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
  private enum MenuPdbPattern implements SubmenuHeuristic {
    /** Pattern database use 555 pattern. */
    PATTERN_DB_555(1, PatternOptions.Pattern_555,
        "If data file not exists, it takes about 15s to generate."),
    /** Pattern database use 663 pattern. */
    PATTERN_DB_663(2, PatternOptions.Pattern_663,
        "If data file not exists, it takes about 2 minutes to generate."),
    /** Pattern database use 78 pattern. */
    PATTERN_DB_78(3, PatternOptions.Pattern_78,
        "If data file not exists, it takes about 2.5-3 hours to generate.\n"
            + "       Also equire minimum 2gig memory -Xms2g to run.\n"
            + "       If you are not sure, please choose default '0'."),
    /** Pattern database use user defined pattern. */
    PATTERN_DB_CUSTOM(4, PatternOptions.Pattern_Custom,
        "size 5 or less - < 5s, size 6 - 1 min, size 7 - 10 mins");

    /** The variable of order number. */
    private int order;
    /** The variable of PatternOptions. */
    private PatternOptions ptnOption;
    /** The variable of custom message. */
    private String message;

    /**
     * Initialize the heuristic menu option.
     *
     * @param order the given menu order
     * @param ptnOption the given PatternOption
     * @param message the given custom message
     */
    MenuPdbPattern(final int order, final PatternOptions ptnOption, final String message) {
      this.order = order;
      this.ptnOption = ptnOption;
      this.message = message;
    }

    @Override
    public int getOrder() {
      return order;
    }

    @Override
    public HeuristicOptions getHeuristic() {
      return ptnOption.getHeuristic();
    }

    @Override
    public PatternOptions getPtnOption() {
      return ptnOption;
    }

    @Override
    public boolean getWdFlag() {
      return false;
    }

    @Override
    public String getClassName() {
      throw new UnsupportedOperationException();
    }

    @Override
    public String getMessage() {
      return message;
    }
  }
}
