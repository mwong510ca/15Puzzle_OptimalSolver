package mwong.myprojects.fifteenpuzzle.console;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import mwong.myprojects.fifteenpuzzle.PropertiesCache;
import mwong.myprojects.fifteenpuzzle.puzzle.Board;
import mwong.myprojects.fifteenpuzzle.puzzle.HeuristicOptions;
import mwong.myprojects.fifteenpuzzle.puzzle.PatternOptions;
import mwong.myprojects.fifteenpuzzle.solution.Solver;
import mwong.myprojects.fifteenpuzzle.solution.SolverBuilder;
import mwong.myprojects.fifteenpuzzle.solution.Solver.SolverVersion;
import mwong.myprojects.fifteenpuzzle.solution.SolverConstants;
import mwong.myprojects.fifteenpuzzle.solution.ai.Reference.ConnectionType;
import mwong.myprojects.fifteenpuzzle.solution.ai.ReferenceFactory;
import mwong.myprojects.fifteenpuzzle.solution.ai.ReferenceRemote;

/**
 * AbstractApplication is the abstract class of console application that has the properties,
 * variables and methods in common.
 *
 * <p>Dependencies : PropertiesCache.java, Board.java, HeuristicOptions.java, PatternOptions.java,
 *                   Solver.java, SolverBuilder.java, SolverConstants.java,
 *                   Reference.java, ReferenceFactory.java
 *                   ReferenceRemote.java,
 *
 * @author <a href="http://www.linkedin.com/pub/macy-wong/46/550/37b/"
 *            target="_blank">Meisze Wong (linkedin)</a>
 * @see <a href="http://www.github.com/mwong510ca/15PuzzleOptimalSolver/"
 *         target="_blank">GitHub (full project)</a>
 */
public abstract class AbstractApplication {
  /** Default display delay 1000 miliseconds. (1 second) */
  private static final int DEFAULT_DISPLAY_DELAY = 1000;
  /** Default timeout limit 10 seconds. */
  private static final int DEFAULT_TIMEOUT_LIMIT = 10;
  /** Maximum timeout allowance 300 seconds. (5 minutes) */
  private static final int MAX_TIMEOUT_LIMIT = 300;
  /** Puzzle size from PuzzleConstants.
   *  @see mwong.myprojects.fifteenpuzzle.puzzle.PuzzleConstants#SIZE */
  static final int PUZZLE_SIZE = SolverConstants.getPuzzleSize();
  /** Row size from PuzzleConstants.
   *  @see mwong.myprojects.fifteenpuzzle.puzzle.PuzzleConstants#ROW_SIZE */
  static final int ROW_SIZE = SolverConstants.getRowSize();

  /** Timeout limit minimum range 1 second. */
  private final int timeoutLowLimit = 1;
  /** Timeout limit maximum range 5 minutes or user defined within 5 minutes. */
  private final int timeoutHighLimit;
  /** Prefer reference connection type when created the object. */
  private ConnectionType connectionTypeRequest;
  /** Display delay in miliseconds if applicable. */
  private int displayDelay;
  /** Reference connection type currently in use. */
  private ConnectionType connectionTypeInUse;
  /** Scanner instance to read command prompt input. */
  Scanner scanner;
  /** The ReferenceRemote instance can be local object or through RMI server. */
  private ReferenceRemote refConnection;
  /** The variable of timeout limit if applicable. */
  private int timeoutLimit;

  /**
   * Static method to verify the sub-menu keys must distinct from 1 to it's size.
   * Returns boolean represents the verification pass.
   *
   * @param values an array of SubmenuHeuristic to be verify.
   * @return boolean represents the verification pass
   */
  static boolean verifySubmenuOrdering(final SubmenuHeuristic[] values) {
    final Set<Integer> numbers = new HashSet<Integer>();
    int min = Integer.MAX_VALUE;
    int max = Integer.MIN_VALUE;

    for (final SubmenuHeuristic item : values) {
      int order = item.getOrder();
      numbers.add(order);
      if (order < min) {
        min = order;
      }
      if (order > max) {
        max = order;
      }
    }

    if (min != 1) {
      return false;
    }
    if (max != values.length) {
      return false;
    }
    return numbers.size() == values.length;
  }

  /**
   * The main method of concrete console application object.
   *
   * @throws Exception any exceptions
   */
  public abstract void run() throws Exception;

  /**
   * Default constructor, use local reference connection and load all default settings.
   *
   * @throws IOException any unexpected IOException
   */
  AbstractApplication() throws IOException {
    this(ConnectionType.STANDALONE);
  }

  /**
   * Default constructor with given connection choice, load all default settings.
   *
   * @param type the given ConnectionType
   * @throws IOException any unexpected IOException
   */
  AbstractApplication(final ConnectionType type) throws IOException {
    this(type, MAX_TIMEOUT_LIMIT);
  }

  /**
   * Default constructor with given maximum timeout limit, use local reference connection
   * and load all default settings.
   *
   * @param hiLimit the user defined timeout upper range, up to 5 minutes.
   * @throws IOException any unexpected IOException
   */
  AbstractApplication(final int hiLimit) throws IOException {
    this(ConnectionType.STANDALONE, hiLimit);
  }

  /**
   * Private constructor, load all settings.
   *
   * @param type the given ConnectionType
   * @param hiLimit the given timeout upper range
   * @throws IOException any unexpected IOException
   */
  private AbstractApplication(final ConnectionType type, final int hiLimit) throws IOException {
    connectionTypeRequest = type;
    scanner = new Scanner(System.in);
    loadReferenceConnection();
    timeoutHighLimit = hiLimit;
    loadDefault();
  }

  /**
   * Initialize local variables, load setting from property file, use default settings
   * if file or property is not available.
   */
  private void loadDefault() {
    displayDelay = DEFAULT_DISPLAY_DELAY;
    timeoutLimit = DEFAULT_TIMEOUT_LIMIT;
    if (PropertiesCache.getInstance().containsKey("solutionDisplayRate")) {
      try {
        int delay = Integer.parseInt(PropertiesCache.getInstance().getProperty(
            "solutionDisplayRate"));
        // range from 0.1 to 5 seconds
        final int lo = 100;  // low limit 0.1 second
        final int hi = 5000; // high limit 5.0 second
        if (delay >= lo && delay <= hi) {
          displayDelay = delay;
        } else {
          System.err.println("Invalid solution display rate setting " + delay
              + ", allow minimum 0.1 second (100) to maximum 5 seconds (5000) only."
              + " Restore to system default 1 second.");
        }
      } catch (NumberFormatException ex) {
        System.err.println("Configuration solution display rate is not an integer,"
            + " restore to system default 1 second.");
      }
    } else {
      assert false : "property - solutionDisplayRate - not found";
    }

    if (PropertiesCache.getInstance().containsKey("solverTimeoutLimit")) {
      try {
        int limit = Integer.parseInt(PropertiesCache.getInstance().getProperty(
            "solverTimeoutLimit"));
        // range from 1 to 300 seconds (up to 5 minutes)
        if (limit >= timeoutLowLimit && limit <= timeoutHighLimit) {
          timeoutLimit = limit;
        } else {
          System.err.print("Invalid timeout limit setting " + limit
              + ", allow minimum " + timeoutLowLimit + " to maximum "
              + timeoutHighLimit + " seconds.");
          if (DEFAULT_TIMEOUT_LIMIT < timeoutLowLimit
              || DEFAULT_TIMEOUT_LIMIT > timeoutHighLimit) {
            final int ten = 10;
            final int min = Math.min(ten, timeoutHighLimit);
            timeoutLimit = min;
          }
          System.err.println(" Restore to system " + timeoutLimit + " seconds.");
        }
      } catch (NumberFormatException ex) {
        System.err.println("Configuration timeout limt is not an integer,"
            + " restore to system default 10 seconds.");
      }
    } else {
      assert false : "property - solverTimeoutLimit - not found";
    }
  }

  /**
   * Connect the reference connection with requested connection type. If connection
   * failed, down grade to next level.  Return the connection in use.
   *
   * @throws IOException any unexpected IOException
   */
  private void loadReferenceConnection() throws IOException {
    connectionTypeInUse = ConnectionType.DISABLED;
    if (connectionTypeRequest == ConnectionType.DISABLED) {
      return;
    }

    if (connectionTypeRequest == ConnectionType.STANDALONE) {
      refConnection = (new ReferenceFactory()).getReferenceLocal();
      connectionTypeInUse = refConnection.getConnectionTypeInUse();
      return;
    }
    resetRemoteConnection();
  }

  /**
   * Test connection type currently in use, down grade it if connection lost.
   * When down grade, also update current solver and solver builder.
   *
   * @throws IOException any unexpected IOException
   */
  private void resetRemoteConnection() throws IOException {
    // Disabled, do nothing.
    if (connectionTypeRequest != ConnectionType.REMOTESERVER) {
      return;
    }

    // If same connection in use.
    try {
      if (connectionTypeInUse == ConnectionType.REMOTESERVER && refConnection != null
          && refConnection.getActiveMap() != null) {
        return;
      }
    } catch (RemoteException e1) {
      // reconnect below.
    }

    try {
      refConnection = (new ReferenceFactory()).getReferenceServer();
      connectionTypeInUse = refConnection.getConnectionTypeInUse();
    } catch (IOException ex) {
      refConnection = (new ReferenceFactory()).getReferenceLocal();
      connectionTypeInUse = refConnection.getConnectionTypeInUse();
    }

    System.out.println(connectionTypeInUse);
    
    if (connectionTypeInUse != connectionTypeRequest) {
      System.out.println("Warning: Connectioon type " + connectionTypeInUse);
    }
    
    if (refConnection != null && refConnection.getActiveMap() != null) {
      return;
    } else {
      throw new NullPointerException("resetRemoteConnection - empty collection " + connectionTypeInUse);
    }
  }

  /**
   * Test connection type currently in use, down grade it if connection lost.
   * When down grade, also update current solver and solver builder.
   *
   * @param solver the given solver for reset reference collection
   * @return boolean represents reference collection testing passed.
   * @throws IOException any unexpected IOException
   */
  final boolean resetRemoteConnection(final Solver solver) throws IOException {
    resetRemoteConnection();
    solver.setReferenceConnection(refConnection);
    return false;
  }

  /**
   * Returns boolean value if application prefer Remote Server connection.
   *
   * @return boolean value if application prefer Remote Server connection
   */
  final boolean isRequestedRemote() {
    return connectionTypeRequest == ConnectionType.REMOTESERVER;
  }

  /**
   * Return connection type currently in use.
   *
   * @return ConnectionType in use
   */
  final ConnectionType getConnectionTypeInUse() {
    return connectionTypeInUse;
  }

  /**
   * Returns the ReferenceRemote connection object currently in use.
   *
   * @return ReferenceRemote connection object currently in use
   */
  final ReferenceRemote getRefConnection() {
    return refConnection;
  }

  /**
   * Print connection type currently in use.
   */
  final void printConnection() {
    System.out.println(connectionTypeInUse + "\n");
  }

  /**
   * Return boolean value represents the given board is the reference board in reference collection.
   *
   * @param board the given Board object
   * @return boolean value represents the given board is the reference board
   * @throws RemoteException any RemoteException
   */
  boolean referenceContains(final Board board) throws RemoteException {
    if (connectionTypeInUse == ConnectionType.DISABLED) {
      return false;
    }
    return refConnection.containsBoard(board);
  }

  /**
   * Given a list of menu selection, print the menu, return a list of options in string.
   *
   * @param selection the boolean array represents the selected options
   * @return the string of choices
   */
  final String menuOption(final boolean[] selection) {
    return menuOption(selection, null, null);
  }

  /**
   * Given a list of menu selection and solver, print the menu, return a list of options in string.
   *
   * @param selection the boolean array represents the selected options
   * @param solver the given solver with solution
   * @return the string of choices
   */
  final String menuOption(final boolean[] selection, final Solver solver) {
    return menuOption(selection, solver, null);
  }

  /**
   * Given a list of menu selection and sub-menu, print the menu, return a list of options
   * in string.
   *
   * @param selection the boolean array represents the selected options
   * @param enumSubmenu the given heuristic menu
   * @param <T> An ExtendedType T
   * @return the string of choices
   */
  final <T extends Enum<T> & SubmenuHeuristic> String menuOption(final boolean[] selection,
      final Class<T> enumSubmenu) {
    return menuOption(selection, null, enumSubmenu);
  }

  /** Print the menu, return a list of options in string.
   *
   * @param selection the boolean array represents the selected options
   * @param solver the given solver with solution
   * @param enumSubmenu the given heuristic menu
   * @param <T> An ExtendedType T
   * @return the string of choices
   */
  final <T extends Enum<T> & SubmenuHeuristic> String menuOption(final boolean[] selection,
      final Solver solver, final Class<T> enumSubmenu) {
    String optionStr = "Enter 'Q'";
    System.out.println("Enter 'Q' - quit the program");
    if (selection[MenuOptions.LIST_MOVES.getIndex()]) {
      if (solver == null || solver.moves() <= 0) {
        throw new NullPointerException("Solver is null or no solution.");
      }
      System.out.println("   'L' - print a direction list of moves");
      optionStr = optionStr + ", 'L'";
    }
    if (selection[MenuOptions.DISPLAY_MOVES.getIndex()]) {
      if (solver == null || solver.moves() <= 0) {
        throw new NullPointerException("Solver is null or no solution.");
      }
      final double thousand = 1000.0;
      System.out.println("   'D' - display each board of moves at "
          + (displayDelay / thousand) + " board per second rate");
      optionStr = optionStr + ", 'D'";
    }
    if (selection[MenuOptions.CHANGE_HEURISTIC.getIndex()]) {
      System.out.println("   'C' - for change your choice of heuristic function");
    }
    if (selection[MenuOptions.HEURISTICH_CHOICE.getIndex()]) {
      System.out.println("Choose your heuristic functions:");
      SubmenuHeuristic[] submenu = enumSubmenu.getEnumConstants();
      Arrays.sort(submenu, Comparator.comparing(SubmenuHeuristic::getOrder));
      boolean firstItem = true;
      for (SubmenuHeuristic item : submenu) {
        String str = "'" + item.getOrder() + "' for " + item.getHeuristic().getDescription();
        if (item.getWdFlag()) {
          str = str + " + Walking Distance";
        }

        if (firstItem) {
          str = "Enter " + str;
          firstItem = false;
        } else {
          str = "      " + str;
        }
        System.out.println(str);
      }
      System.out.println("      '0' no change");
    }
    if (selection[MenuOptions.SWITCH_VERSION.getIndex()] && solver != null) {
      if (solver.getVersion().isOptimum()) {
        System.out.println("   'V' - change initial heuristic estimate from Advanced "
            + "to Standard version");
      } else {
        System.out.println("   'V' - change initial heuristic estimate from Standard "
            + "to Advanced version");
      }
      optionStr = optionStr + ", 'V'";
    }
    if (selection[MenuOptions.TIMEOUT_FEATURE.getIndex()] && solver != null) {
      if (solver.isTimerOn()) {
        System.out.println("   'O' - turn timeout feature off");
      } else {
        System.out.println("   'O' - turn timeout feature on");
      }
      optionStr = optionStr + ", 'O'";
    }
    if (selection[MenuOptions.TIMEOUT_LIMIT.getIndex()]) {
      System.out.println("   'T' - change timeout limit");
      optionStr = optionStr + ", 'T'";
    }
    if (selection[MenuOptions.CREATE_PUZZLE.getIndex()]) {
      System.out.println("   'E' - Easy | 'M' - Moderate | 'H' - Hard | 'R' - Random");
      optionStr = optionStr + ", 'E', 'M', 'H', 'R'";
    }
    if (selection[MenuOptions.KEY_IN_PUZZLE.getIndex()]) {
      System.out.println("   16 numbers from 0 to 15 for the puzzle");
      optionStr = optionStr + " or 16 numbers (0-15)";
    }
    optionStr = optionStr + " :";
    return optionStr;
  }

  /**
   * Create a boolean array represents the given menu options.
   *
   * @param opts Variable Arguments of MenuOptons
   * @return boolean array represents the given menu options
   */
  final boolean[] createMenuList(final MenuOptions... opts) {
    return createMenuList(null, opts);
  }

  /**
   * Create a boolean array represents the given menu options.
   *
   * @param arr an array of MenuOptions
   * @param opts Variable Arguments of MenuOptons
   * @return boolean array represents the given menu options
   */
  final boolean[] createMenuList(final MenuOptions[] arr, final MenuOptions... opts) {
    boolean[] list = new boolean[MenuOptions.values().length];
    if (arr != null) {
      for (MenuOptions opt : arr) {
        list[opt.getIndex()] = true;
      }
    }
    for (MenuOptions opt : opts) {
      list[opt.getIndex()] = true;
    }
    return list;
  }

  /**
   * Create the Board object with user key in entry and return the Board object.
   *
   * @return Board object with custom key in entry
   */
  final Board keyInBoard() {
    byte[] blocks = new byte[PUZZLE_SIZE];
    boolean[] used = new boolean[PUZZLE_SIZE];
    int count = 0;

    while (count < PUZZLE_SIZE) {
      if (!scanner.hasNextInt()) {
        char ch = scanner.next().charAt(0);
        if (ch == 'C' || ch == 'c') {
          return null;
        }
      } else {
        int value = scanner.nextInt();
        if (value < 0 || value >= PUZZLE_SIZE) {
          System.out.print("Invalid number " + value + ".  Try again,");
          System.out.println("or enter 'C' to clear all entries.");
        } else if (used[value]) {
          System.out.print(value + " already entered.  Try again,");
          System.out.println("or enter 'C' to clear all entries.");
        } else {
          blocks[count++] = (byte) value;
          used[value] = true;
        }
      }
    }
    return new Board(blocks);
  }

  /**
   * Create the Board object randomly based on difficulty choice.
   *
   * @param choice the selected difficulty level of puzzle
   * @return Board object randomly based on difficulty input
   */
  final Board createBoard(final char choice) {
    switch (choice) {
      case 'E': case 'e':
        return (new Board(Board.DifficultyLevel.EASY));
      case 'M': case 'm':
        return (new Board(Board.DifficultyLevel.MODERATE));
      case 'H': case 'h':
        return (new Board(Board.DifficultyLevel.HARD));
      case 'R': case 'r':
        return (new Board());
      default: return null;
    }
  }

  /**
   * Return a new solver object with user selection.
   *
   * @param builder the given SolverBuilder object
   * @param inUseSolver the Solver object currently in use
   * @param enumSubmenu the given heuristic menu
   * @param <T> An ExtendedType T
   * @return new solver object with user selection
   */
  final <T extends Enum<T> & SubmenuHeuristic> Solver menuChangeSolver(
      final SolverBuilder builder, final Solver inUseSolver,
      final Class<T> enumSubmenu) {
    return menuChangeSolver(builder, inUseSolver, enumSubmenu, false);
  }

  /** Return a new solver with user selection with pattern database only option.
   *
   * @param builder the given SolverBuilder object
   * @param solver the Solver object currently in use
   * @param enumSubmenu the given heuristic menu
   * @param <T> An ExtendedType T
   * @param pdbOnly the option of heuristic must be pattern database only
   * @return new solver object with user selection
   */
  final <T extends Enum<T> & SubmenuHeuristic> Solver menuChangeSolver(
      final SolverBuilder builder, final Solver solver,
      final Class<T> enumSubmenu, final boolean pdbOnly) {
    boolean[] menuList = createMenuList(MenuOptions.HEURISTICH_CHOICE);
    menuOption(menuList, enumSubmenu);
    SubmenuHeuristic[] submenu = enumSubmenu.getEnumConstants();
    final int menuSize = submenu.length;

    while (true) {
      while (!scanner.hasNextInt()) {
        char ch = scanner.next().charAt(0);
        if (ch == 'q') {
          appExit();
        }
        System.out.println("Enter '1 - " + menuSize
            + "' for heuristic function, '0' no change, 'q' to quit.");
      }
      int choice = scanner.nextInt();

      if (choice == 0) {
        return solver;
      } else {
        for (SubmenuHeuristic item : submenu) {
          if (choice == item.getOrder()) {
            if (pdbOnly) {
              if (item.getHeuristic() == HeuristicOptions.PDCustom) {
                System.out.println("Notes (estimated generation time): " + item.getMessage());

                byte[] inPattern = keyInPattern();
                try {
                  Solver replacement = builder.createSolverPdb(inPattern);
                  if (replacement != null) {
                    return replacement;
                  }
                  break;
                } catch (Exception ex) {
                  System.out.println("Your input pattern is invalid, please try again");
                  break;
                }
              } else {
                int patternIdx = -1;
                System.out.println(item.getPtnOption());
                PatternOptions selectedPattern = item.getPtnOption();
                while (!selectedPattern.isValidPattern(patternIdx)) {
                  System.out.println("Choose your pattern option, enter '0' for default");
                  System.out.println("Notes: " + item.getMessage());
                  while (!scanner.hasNextInt()) {
                    scanner.next();
                  }
                  patternIdx = scanner.nextInt();
                }

                if (matchPattern(solver.getInUsePdbPtn(), selectedPattern.getPattern(patternIdx))) {
                  System.out.println("No Change, currently in use.");
                  return solver;
                }
                Solver replacement =
                    builder.createSolverPdb(item.getHeuristic(), patternIdx);
                if (replacement != null) {
                  replacement.setTimeoutLimit(timeoutLimit);
                  return replacement;
                }
                System.err.println("Unexpected error, no change.");
                return solver;
              }
            } else if (solver.getHeuristic() != item.getHeuristic()
                || !solver.getClass().getSimpleName().equals(item.getClassName())) {
              Solver replacement =
                  builder.createSolver(item.getHeuristic(), item.getWdFlag());
              if (replacement != null) {
                replacement.setTimeoutLimit(timeoutLimit);
                return replacement;
              }
              System.err.println("Unexpected error, no change.");
              return solver;
            } else {
              System.out.println("No Change, currently in use.");
              return solver;
            }
          }
        }
      }
      System.out.println("Enter '1 - " + menuSize
          + "' for heuristic function, '0' no change, 'q' to quit.");
    }
  }

  /**
   * Returns boolean value represents the two pattern is same as current pattern.
   *
   * @param thisPattern the given pattern set 1
   * @param thatPattern the given pattern set 2
   * @return boolean value represents the given pattern is same as current pattern
   */
  private boolean matchPattern(final byte[] thisPattern, final byte[] thatPattern) {
   for (int i = 0; i < PUZZLE_SIZE; i++) {
      if (thisPattern[i] != thatPattern[i]) {
        return false;
      }
    }
    return true;
  }

  /**
   * Return a byte array of user entry custom pattern of 15 puzzle.
   *
   * @return byte array of user entry custom pattern
   */
  private byte[] keyInPattern() {
    byte[] inPattern = new byte[PUZZLE_SIZE];
    final int maxGroup = 7; // Custom pattern maximum group size allow
    System.out.println("Enter 15 numbers (1, 2, 3 ... represent the group"
        + " number) for your pattern, ");
    System.out.println("min group size is 2 and max group " + maxGroup + ", position 15 "
        + " must be the last group, example:");
    System.out.println("    1 1 1 1");
    System.out.println("    2 2 2 2");
    System.out.println("    3 3 3 3");
    System.out.println("    4 4 4 0");

    int count = 0;
    while (count < PUZZLE_SIZE) {
      //System.out.println(count);
      if (!scanner.hasNextInt()) {
        scanner.next();
      } else {
        int value = scanner.nextInt();
        if (value >= 0 && value <= maxGroup) {
          inPattern[count++] = (byte) value;
        }
      }
    }

    System.out.println("\nYou have enter:");
    for (count = 0; count < PUZZLE_SIZE; count++) {
      System.out.print(inPattern[count] + " ");
      if (count % ROW_SIZE == ROW_SIZE - 1) {
        System.out.println();
      }
    }
    System.out.println();

    return inPattern;
  }

  /**
   * Swap the solver version alternatively.
   *
   * @param solver the given solver object
   * @return swap the solver version if applicable
   */
  final SolverVersion flipVersion(final Solver solver) {
    SolverVersion currentVersion = solver.getVersion();
    if (currentVersion.isPrime()) {
      solver.shiftOptimum();
    } else {
      solver.shiftPrime();
    }
    return solver.getVersion();
  }

  /**
   * Returns the boolean value represents the solver's timeout setting has changed success.
   *
   * @param solver the given solver to update timeout limit
   * @return boolean if timeout limit has changed
   */
  final boolean changeTimeout(final Solver solver) {
    return changeTimeout(solver, timeoutHighLimit);
  }

  /**
   * Change the solver's timeout setting.
   *
   * @param solver the given solver to update timeout limit
   * @param reduceLimit the custom defined timeout limit, lover then maximum allowance
   * @return boolean if timeout limit has changed
   */
  final boolean changeTimeout(final Solver solver, final int reduceLimit) {
    int highLimit = timeoutHighLimit;
    if (reduceLimit > 0 && reduceLimit < timeoutHighLimit) {
      highLimit = reduceLimit;
    }

    int limit = 0;
    while (limit < timeoutLowLimit || limit > highLimit) {
      System.out.println("Enter timeout limit in seconds, minimun " + timeoutLowLimit + " seconds"
          + " and maximum " + highLimit + " seconds, 0 for no change:");
      if (!scanner.hasNextInt()) {
        scanner.next();
      } else {
        final int val = scanner.nextInt();
        if (val == 0) {
          return false;
        }
        limit = val;
      }
    }
    timeoutLimit = limit;
    solver.setTimeoutLimit(timeoutLimit);
    return true;
  }

  /**
   * Returns the integer of timeout limit in use.
   *
   * @return integer of timeout limit in use
   */
  final int getTimeoutLimit() {
    return timeoutLimit;
  }

  /**
   * Quit the program with a message.
   */
  final void appExit() {
    System.out.println("Goodbye!");
    System.exit(0);
  }

  /** Print the list of direction of moves to the goal state.
   *
   * @param initial the given Board object
   * @param steps the number of solution steps
   * @param moves the Board.Move array of solution moves
   */
  final void solutionList(final Board initial, final int steps, final Board.Move[] moves) {
    if (!initial.isSolvable()) {
      System.out.println("Insolvable puzzle, no solution.");
    }
    final int ten = 10;
    final int hundred = 100;
    final int lineBreak = 10;
    String str = "";
    for (int i = 1; i <= steps; i++) {
      if (i < ten) {
        str += "  ";
      } else if (i < hundred) {
        str += " ";        
      }
      str += i + " : " + moves[i] + " ";
      if (i % lineBreak == 0 && steps > i) {
        str += "\n";
      }
    }
    System.out.println(str + "\n");
  }

  /**
   * Print all boards of moves to the goal state.
   *
   * @param initial the given Board object
   * @param steps the number of solution steps
   * @param moves the Board.Move array of solution moves
   */
  final void solutionDetail(final Board initial, final int steps, final Board.Move[] moves) {
    int count = 0;
    System.out.print("Step : " + (count));
    System.out.println();
    Board board = initial;
    System.out.println(board);

    Board.Move dir = Board.Move.NONE;
    dir = moves[++count];
    board = board.shift(dir);

    try {
      Thread.sleep(displayDelay);
      while (count <= steps) {
        System.out.print("Step : " + (count));
        System.out.print("\t" + dir);
        System.out.println();
        System.out.println(board);
        if (++count <= steps) {
          dir = moves[count];
          board = board.shift(dir);
          Thread.sleep(displayDelay);
        }
      }
    } catch (InterruptedException ex) {
      while (count <= steps) {
        System.out.print("Step : " + (count));
        System.out.print("\t" + dir);
        System.out.println();
        System.out.println(board);
        if (++count <= steps) {
          dir = moves[count];
          board = board.shift(dir);
        }
      }
    }
  }

  /** An enum type of main menu options. */
  enum MenuOptions {
    /**
     * Quit the application.
     */
    DEFAULT_QUIT(0),
    /**
     * List the heuristic functions for change.
     */
    CHANGE_HEURISTIC(1),
    /**
     * Choose the choice of heuristic functions.
     */
    HEURISTICH_CHOICE(2),
    /**
     * Switch the solver version if applicable.
     */
    SWITCH_VERSION(3),
    /**
     * Turn timeout feature on/off.
     */
    TIMEOUT_FEATURE(4),
    /**
     * Change the timeout limit.
     */
    TIMEOUT_LIMIT(5),
    /**
     * Create the new random puzzle.
     */
    CREATE_PUZZLE(6),
    /**
     * Key in the custom defined puzzle.
     */
    KEY_IN_PUZZLE(7),
    /**
     * After solution found, display a list of moves.
     */
    LIST_MOVES(8),
    /**
     * After solution found, display the boards of step of moves.
     */
    DISPLAY_MOVES(9);

    /** The index variable. */
    private int menuIndex;

    /**
     * Initialize the MenuOptions with given index.
     *
     * @param index the given index value
     */
    MenuOptions(final int index) {
      menuIndex = index;
    }

    /**
     * Returns the integer value of index.
     *
     * @return integer value of index
     */
    int getIndex() {
      return menuIndex;
    }
  }

  /**
   * An interface class of sub-menu.
   * 
   * @author <a href="http://www.linkedin.com/pub/macy-wong/46/550/37b/"
   *            target="_blank">Meisze Wong (linkedin)</a>
   */
  interface SubmenuHeuristic {
    /**
     * Returns the preset order of heuristic choice of sub-menu.
     *
     * @return integer value of preset order of heuristic choice
     */
    int getOrder();
    /**
     * Returns the heuristic choice of sub-menu.
     *
     * @return HeuristicOptions of heuristic choice
     */
    HeuristicOptions getHeuristic();
    /**
     * Returns the pattern database option if applicable.
     *
     * @return PatternOptions pattern database option if applicable
     */
    PatternOptions getPtnOption();
    /**
     * The boolean flag for walking distance requirement.
     *
     * @return boolean value represents the heuristic use walking distance
     */
    boolean getWdFlag();
    /**
     * Returns the simple class name of heuristic choice.
     *
     * @return String of simple class name of heuristic choice
     */
    String getClassName();
    /**
     * Returns the custom message of heuristic choice.
     *
     * @return String of custom message
     */
    String getMessage();
  }
}
