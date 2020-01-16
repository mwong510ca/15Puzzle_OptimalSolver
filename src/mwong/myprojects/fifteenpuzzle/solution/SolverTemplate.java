package mwong.myprojects.fifteenpuzzle.solution;

import java.rmi.RemoteException;
import java.util.Arrays;

import mwong.myprojects.fifteenpuzzle.puzzle.Board;
import mwong.myprojects.fifteenpuzzle.puzzle.HeuristicOptions;
import mwong.myprojects.fifteenpuzzle.solution.ai.ReferenceRecorder;
import mwong.myprojects.fifteenpuzzle.solution.ai.ReferenceRemote;

/**
 * AbstractSolver is the abstract class extends Solver Interface of 15 puzzle
 * that has the following variables and methods.
 *
 * <p>Dependencies : Board.java, HeuristicOptions.java, ReferenceRemote.java,
 * Stopwatch.java
 *
 * @author <a href="http://www.linkedin.com/pub/macy-wong/46/550/37b/"
 *            target="_blank">Meisze Wong (linkedin)</a>
 * @see <a href="http://www.github.com/mwong510ca/15PuzzleOptimalSolver/"
 *         target="_blank">GitHub (full project)</a>
 */
abstract class SolverTemplate implements Solver {
  // constants
  /** Puzzle size.
   *  @see mwong.myprojects.fifteenpuzzle.puzzle.PuzzleConstants#SIZE */
  static final int PUZZLE_SIZE = SolverConstants.getPuzzleSize();
  /** Row size.
   *  @see mwong.myprojects.fifteenpuzzle.puzzle.PuzzleConstants#ROW_SIZE */
  static final int ROW_SIZE = SolverConstants.getRowSize();
  /** Number of moves or directions.
   *  @see mwong.myprojects.fifteenpuzzle.puzzle.PuzzleConstants#DIRECTION_SIZE */
  static final int DIR_SIZE = SolverConstants.getDirectionSize();
  /** Maximum moves to solve the puzzle.
   *  @see mwong.myprojects.fifteenpuzzle.puzzle.PuzzleConstants#MAX_MOVES */
  static final int MAX_MOVE = SolverConstants.getMaxMoves();
  /** The rotation reset value.
   *  @see Rotation#RESET */
  static final int RESET_VAL = Rotation.RESET.getValue();
  /** The rotation clockwise value.
   *  @see Rotation#CLOCKWISE */
  static final int CW_VAL = Rotation.CLOCKWISE.getValue();
  /** The rotation counterclockwise value.
   *  @see Rotation#COUNTERCLOCKWISE */
  static final int CCW_VAL = Rotation.COUNTERCLOCKWISE.getValue();
  /** The clockwise half cycle chain. */
  static final int CW_HALF_CYCLE;
  /** The clockwise half cycle chain bits, 10 bits (5 sequences x 2 bits). */
  static final int CW_HALF_BITS = 0x03FF;
  /** The counterclockwise half cycle chain. */
  static final int CCW_HALF_CYCLE;
  /** The counterclockwise half cycle chain bits, 8 bits (4 sequences x 2 bits). */
  static final int CCW_HALF_BITS = 0x00FF;
  /** Mirror reflection, value conversion table.
   *  @see mwong.myprojects.fifteenpuzzle.puzzle.PuzzleConstants#MIRROR_VALUE */
  static final byte[] MIRROR_VAL_TABLE = SolverConstants.getMirrorValue();
  /** Mirror reflection, position conversion table.
   *  @see mwong.myprojects.fifteenpuzzle.puzzle.PuzzleConstants#MIRROR_POSITION */
  static final byte[] MIRROR_POS_TABLE = SolverConstants.getMirrorPosition();
  /** The variable of direction move right.
   *  @see mwong.myprojects.fifteenpuzzle.puzzle.Board.Move#RIGHT */
  static final Board.Move MOVE_RT = Board.Move.RIGHT;
  /** The variable of direction move left.
   *  @see mwong.myprojects.fifteenpuzzle.puzzle.Board.Move#LEFT */
  static final Board.Move MOVE_LT = Board.Move.LEFT;
  /** The variable of direction move down.
   *  @see mwong.myprojects.fifteenpuzzle.puzzle.Board.Move#DOWN */
  static final Board.Move MOVE_DN = Board.Move.DOWN;
  /** The variable of direction move up.
   *  @see mwong.myprojects.fifteenpuzzle.puzzle.Board.Move#UP */
  static final Board.Move MOVE_UP = Board.Move.UP;
  /** The end of search indicator.
   *  @see SolverConstants#getEndOfSearch() */
  static final byte END_OF_SEARCH = SolverConstants.getEndOfSearch();
  /** The number of partial solution of moves of reference collection.
   *  @see mwong.myprojects.fifteenpuzzle.solution.ai.ReferenceConstants#NUM_PARTIAL_MOVES */
  private static final byte NUM_PARTIAL_MOVES = SolverConstants.getNumPartialMoves();
  /** The maximum count of initial DFS search to determine starting order for optimal search. */
  static final int DFS_REVIEW_LIMIT = 10000;
  /** The minimum basic priority value to determine boost priority. */
  private static final int BOOST_PRIORITY_CUTOFF;

  // setting
  /** The solver type when the object initiated. */
  private SolverVersion solverType;
  /** The solver version currently in use. */
  private SolverVersion inUseVersion;
  /** The heuristic function of the object. */
  private HeuristicOptions inUseHeuristic;
  /** The console status printing feature. */
  private boolean statusSetting;
  /** The search timer feature. */
  private boolean timerSetting;
  /** The time out limit setting. */
  private int timeoutLimit;
  /** The instance of ReferenceRemote connection, for solver. */
  private ReferenceRemote refConnection;
  /** The instance of ReferenceRecorder, for administrative tool. */
  private ReferenceRecorder refRecorder;
  /** The double value of reference cutoff from reference collection object. */
  private double refCutoffLimit;
  /** The instance of SupplementaryEstimator object for optimum search. */
  private SupplementaryEstimator estimator;
  /** The byte array of a copy of pattern database pattern if applicable. */
  private byte[] inUsePdbPtn;

  // board related
  /** A copy of last board, store the initial values.  */
  Board lastBoard;
  /** The integer value of x-coordinate of zero space. */
  private int initZeroX;
  /** The integer value of y-coordinate of zero space. */
  private int initZeroY;
  /** The byte array of original tiles. */
  private byte[] initTiles;
  /** The byte array of mirror reflection tiles. */
  private byte[] initTilesMirror;
  //search related
  /** The integer value of x-coordinate of zero space for searching. */
  int zeroX;
  /** The integer value of x-coordinate of zero space for searching. */
  int zeroY;
  /** The byte array of copy of tiles for searching. */
  byte[] tiles;
  /** The byte array of mirror reflection tiles for searching. */
  byte[] tilesMirror;
  /** The integer value of original priority of heuristic function. */
  int priorityBasis;
  /** The integer value of boost priority with reference collection. */
  int priorityBoost;
  /** The instance of Stopwatch for searching. */
  Stopwatch stopwatch;

  // search results
  /** The boolean value represent the puzzle is solvable. */
  boolean isSolvable;
  /** The boolean value represent the puzzle has found a solution. */
  boolean solved;
  /** The boolean value represent the search process terminated due to timeout. */
  boolean searchTimeout;
  /** The boolean value represent the search process terminated. */
  boolean terminated;
  /** The integer value represents the search depth when search process terminated. */
  int searchDepth;
  /** The integer value represents the search node count when search process terminated. */
  int searchNodeCount;
  /** The integer value represents the search node count from previous depth. */
  int searchCountBase;
  /** The double value represents the search time. */
  double searchTime;
  /** The integer array of last search depth to determine the starting order for on going search. */
  int[] lastDepthSummary;
  /** The byte value of number of steps to reach the goal state. */
  byte steps;
  /** The integer value of node counter per depth. */
  int idaCount;
  /** The Board.Move array of final solution if applicable. */
  Board.Move[] solutionMove;
  /** The boolean value represents the board has added to reference collection after search. */
  boolean flagNewReference;
  /** A Board copy of last search board. */
  Board lastSearchBoard;

  // load constants
  static {
    int temp = RESET_VAL;
    final int halfCycle = 5;  // 6 turns => 5 sequences
    // continuous 5 clockwise move
    for (int i = 0; i < halfCycle; i++) {
      temp = (temp << 2) | CW_VAL;
    }
    CW_HALF_CYCLE = temp;
    temp = RESET_VAL;
    // continuous 4 counter clockwise move
    for (int i = 0; i < halfCycle - 1; i++) {
      temp = (temp << 2) | CCW_VAL;
    }
    CCW_HALF_CYCLE = temp;
    final double percent40 = 0.4;
    BOOST_PRIORITY_CUTOFF = (int) (MAX_MOVE * percent40); // 40% of maximum moves
  }

  /**
   * Initialize the SolverTemplate object.
   *
   * @param option the mandatory HeuristicOption
   */
  SolverTemplate(final HeuristicOptions option) {
    timerSetting = true;
    inUseHeuristic = option;
    lastBoard = SolverConstants.getGoalBoard();
    tiles = new byte[PUZZLE_SIZE];
    tilesMirror = new byte[PUZZLE_SIZE];
  }

  /**
   * Review the given board and save the original heuristic.
   *
   * @param board the given board
   */
  abstract void setPriorityBasis(Board board);

  /**
   * Depth first search with starting order review, will not exists the given limit.
   *
   * @param limit the upper limit of the search.
   */
  abstract void dfsStartingOrder(int limit);

  // ----- solver settings -----

  @Override
  public final void setReferenceConnection(final ReferenceRemote inRefConnection) {
    resumePrimeSolver();
    if (inRefConnection == null) {
      return;
    }
    if (refRecorder != null) {
      throw new ReferenceConflictException(
          "setReferenceConnection error - ReferenceRecorder in use");
    }

    try {
      if (inRefConnection.getActiveMap() == null) {
        System.err.println("Attention: Reference board collection unavailable."
            + " Advanced estimate will use standard estimate.");
        resumePrimeSolver();
      } else {
        upgradeOptimumSolver(inRefConnection);
      }
    } catch (RemoteException ex) {
      System.err.println(this.getClass().getSimpleName()
          + " - Attention: Server connection failed. Resume to standard version.\n");
      resumePrimeSolver();
    }
  }

  /**
   * Set the ReferenceRecorder with the given object for administrative tool.
   * Cannot use with ReferenceRemote connection for puzzle solver.
   *
   * @param inRecorder set the ReferenceRecorder with the given object
   */
  final void setReferenceRecorder(final ReferenceRecorder inRecorder) {
    if (refConnection != null) {
      throw new ReferenceConflictException(
          "setReferenceRecorder error - ReferenceConnection in use");
    }

    solverType = SolverVersion.OPTIMUM;
    inUseVersion = SolverVersion.OPTIMUM;
    this.refRecorder = inRecorder;
    refCutoffLimit = inRecorder.getCutoffLimit();
    if (estimator == null) {
      this.estimator = new SupplementaryEstimator();
    }
  }

  @Override
  public final ReferenceRemote getReference() {
    return refConnection;
  }

  /**
   * Return double represents the cutoff limit from the reference collection object.
   *
   * @return double represents the cutoff limit from the reference collection object
   */
  final double getRefCutoffLimit() {
    return refCutoffLimit;
  }

  /**
   * Return SupplementaryEstimator object currently stored and using.
   *
   * @return SupplementaryEstimator object currently stored and using
   */
  final SupplementaryEstimator getEstimator() {
    return estimator;
  }

  /**
   * Change to Prime version.
   */
  final void resumePrimeSolver() {
    solverType = SolverVersion.PRIME;
    inUseVersion = SolverVersion.PRIME;
    refConnection = null;
    refCutoffLimit = Double.MAX_VALUE;
  }

  /**
   * Change to Optimum version.
   *
   * @param inRefConnection refConnection
   * @throws RemoteException RemoteException
   */
  private void upgradeOptimumSolver(final ReferenceRemote inRefConnection) throws RemoteException {
    solverType = SolverVersion.OPTIMUM;
    inUseVersion = SolverVersion.OPTIMUM;
    this.refConnection = inRefConnection;
    refCutoffLimit = refConnection.getCutoffLimit();
    if (estimator == null) {
      this.estimator = new SupplementaryEstimator();
    }
  }

  @Override
  public final boolean shiftPrime() {
    if (inUseVersion.isOptimum()) {
      inUseVersion = SolverVersion.PRIME;
      return true;
    } else {
      return false;
    }
  }

  @Override
  public final boolean shiftOptimum() {
    if (solverType.isOptimum() && inUseVersion.isPrime()) {
      inUseVersion = SolverVersion.OPTIMUM;
      return true;
    } else {
      return false;
    }
  }

  @Override
  public final SolverVersion getVersion() {
    return inUseVersion;
  }

  @Override
  public final HeuristicOptions getHeuristic() {
    return inUseHeuristic;
  }

  /**
   * Save a copy of pattern of pattern database.
   *
   * @param inUsePdbPtn the given byte array of pattern.
   */
  final void setInUsePdbPtn(final byte[] inUsePdbPtn) {
    this.inUsePdbPtn = inUsePdbPtn;
  }

  @Override
  public final byte[] getInUsePdbPtn() {
    if (inUsePdbPtn == null) {
      throw new UnsupportedOperationException("getInUsePdbPtn - "
    + inUseHeuristic + " is not pattern database");
    }
    return inUsePdbPtn.clone();
  }

  @Override
  public final void printDescription() {
    String str = "15 puzzle solver using " + inUseHeuristic.getDescription();
    if (this.getClass().getSimpleName().equals("SolverPdbWd")) {
      str += " + " + HeuristicOptions.WD.getDescription();
    }
    System.out.println(str);

    if (solverType.isOptimum() && estimator != null) {
      if (inUseVersion == SolverVersion.OPTIMUM) {
        System.out.println("Optimize version - initial estimate use the goal state and "
            + "stored reference boards.");
      } else {
        System.out.println("Original version - initial estimate use the goal state only.");
      }
    }
    printPattern();
  }

  @Override
  public final void printHeader() {
    printHeader(false);
  }

  @Override
  public final void printHeader(final boolean printPattern) {
    String str = inUseHeuristic.getDescription();
    if (this.getClass().getSimpleName().equals("SolverPdbWd")) {
      str += " + " + HeuristicOptions.WD.getDescription();
    }
    str += " (" + inUseVersion + " version)";
    if (isTimerOn()) {
      str += " will timeout at " + timeoutLimit + "s:";
    } else {
      str += " never timeout until soultion found:";
    }
    System.out.println(str);

    if (printPattern) {
      printPattern();
    }
  }

  /**
   * Print the pattern database pattern if applicable.
   */
  private void printPattern() {
    if (inUsePdbPtn != null) {
      System.out.print("Pattern in use" + " : ");
      int ct = 0;
      for (int group : inUsePdbPtn) {
        ct++;
        System.out.print(group + " ");
        if (ct % ROW_SIZE == 0) {
          if (ct < PUZZLE_SIZE) {
            System.out.print("\n                 ");
          } else {
            System.out.println();
          }
        }
      }
      System.out.println();
    }
  }

  @Override
  public final void setStatusOn(final boolean flag) {
    statusSetting = flag;
  }

  @Override
  public final boolean isStatusOn() {
    return statusSetting;
  }

  @Override
  public final void setTimerOn(final boolean flag) {
    if (inUseHeuristic == HeuristicOptions.PD78) {
      timerSetting = flag;
    } else {
      throw new UnsupportedOperationException();
    }
  }

  @Override
  public final boolean isTimerOn() {
    return timerSetting;
  }

  @Override
  public final void setTimeoutLimit(final int seconds) {
    timeoutLimit = seconds;
  }

  @Override
  public final int getTimeoutLimit() {
    return timeoutLimit;
  }

  // ----- heuristic and solve the puzzle -----

  /**
   * The boolean value determine next clockwise turn is valid.
   *
   * @param chain the sequence of clockwise turns, maximum 5.
   * @return boolean value determine next clockwise turn is valid
   */
  final boolean isValidClockwise(final int chain) {
    return !((chain & CW_HALF_BITS) == CW_HALF_CYCLE);
  }

  /**
   * The boolean value determine next counterclockwise turn is valid.
   *
   * @param chain the sequence of clockwise turns, maximum 4.
   * @return boolean value determine next counterclockwise turn is valid
   */
  final boolean isValidCounterClockwise(final int chain) {
    return !((chain & CCW_HALF_BITS) == CCW_HALF_CYCLE);
  }

  /**
   * Returns the boolean value represent the tiles is not symmetry by
   * comparing the tiles with mirror reflection.
   *
   * @return boolean value represent the tiles is not symmetry.
   */
  final boolean isNotSymmetry() {
    for (int i = PUZZLE_SIZE - 1; i > 0; i--) {
      if (tiles[i] != tilesMirror[i]) {
        return true;
      }
    }
    assert checkMirrorInSync() : "Incorrect sync mirror tiles.";
    return false;
  }

  /**
   * Returns the boolean value represents the mirror reflection is valid. (Assertion tool)
   *
   * @return boolean value represents the mirror reflection is valid
   */
  private boolean checkMirrorInSync() {
    byte[] temp = tiles2mirror(tiles);
    for (int i = PUZZLE_SIZE - 1; i > 0; i--) {
      if (temp[i] != tilesMirror[i]) {
        return false;
      }
    }
    return true;
  }

  /** Reset and clear variables from previous search results. */
  final void clearHistory() {
    isSolvable = true;
    solved = false;
    searchTimeout = false;
    terminated = false;
    searchTime = 0.0;
    searchDepth = 0;
    searchNodeCount = 0;
    searchCountBase = 0;
    lastDepthSummary = new int[DIR_SIZE * 2];
    solutionMove = new Board.Move[MAX_MOVE + 1];
    solutionMove[0] = Board.Move.NONE;
    steps = 0;
    stopwatch = new Stopwatch();
  }

  /**
   * Initialize the given board object.
   *
   * @param board the given board object
   */
  private void initialize(final Board board) {
    lastBoard = board;
    initZeroX = board.getZeroX();
    initZeroY = board.getZeroY();
    initTiles = board.getTiles();
    initTilesMirror = board.getTilesMirror();
    priorityBasis = 0;
    priorityBoost = -1;

    // duplicate copy for child classes
    System.arraycopy(initTiles, 0, tiles, 0, PUZZLE_SIZE);
    System.arraycopy(initTilesMirror, 0, tilesMirror, 0, PUZZLE_SIZE);
    zeroX = initZeroX;
    zeroY = initZeroY;
    flagNewReference = false;
  }

  /**
   * Return the byte array of mirror conversion.
   *
   * @param original the byte array of given tiles
   * @return byte array of mirror reflection
   */
  private byte[] tiles2mirror(final byte[] original) {
    return SolverConstants.tiles2mirror(original);
  }

  @Override
  public final int heuristic(final Board board) {
    return heuristic(board, getVersion(), SolverAction.REVIEW);
  }

  /**
   * Return integer value of heuristic based on condition, initialize board if needed.
   *
   * @param board the given Board object
   * @param version the given version of heuristic request
   * @param action the usage of this heuristic value
   * @return integer value of heuristic based on condition
   */
  final int heuristic(final Board board, final SolverVersion version,
      final SolverAction action) {
    if (board == null) {
      throw new IllegalArgumentException("Board is null");
    }
    if (!board.isSolvable()) {
      return -1;
    }

    assert solverType.isOptimum() || solverType == inUseVersion
        : "Version setting mismatch";

    if (!board.equals(lastBoard) || action.isSearch()) {
      initialize(board);
      setPriorityBasis(board);
    }

    if (version.isPrime()) {
      return priorityBasis;
    } else if (action.isReview() && priorityBoost != -1) {
      return priorityBoost;
    }

    setPriorityOptimum(board, action);
    return priorityBoost;
  }

  @Override
  public final int heuristicBasis(final Board board) {
    if (board.equals(lastBoard) && priorityBasis > 0) {
      return priorityBasis;
    }
    return heuristic(board, SolverVersion.PRIME, SolverAction.REVIEW);
  }

  @Override
  public final int heuristicBoost(final Board board) {
    if (solverType.isPrime()) {
      throw new UnsupportedOperationException("heuristicBoost - in use version is " + inUseVersion);
    }
    if (board.equals(lastBoard) && priorityBoost > 0) {
      return priorityBoost;
    }
    return heuristic(board, SolverVersion.OPTIMUM, SolverAction.REVIEW);
  }

  /**
   * Set boost priority for optimum version with given board or type of search.
   *
   * @param board the given Board object
   * @param action the usage of this heuristic value
   */
  private void setPriorityOptimum(final Board board, final SolverAction action) {
    SupplementaryData record = referenceContains(board);
    if (record != null) {
      priorityBoost = record.getEstimate();
      if (record.hasPartialMoves() && action.isSearch()) {
        solutionMove = record.getPartialMoves();
      }
      return;
    }

    if (priorityBoost > -1) {
      return;
    }

    priorityBoost = priorityBasis;
    if (priorityBoost < BOOST_PRIORITY_CUTOFF) {
      return;
    }
    priorityBoost = inverseEstimate(board, priorityBoost);
    if (priorityBoost - priorityBasis < 0) {
      assert false : "unexpected error - negative number setPriorityOptimum";
    } else if ((priorityBoost - priorityBasis) % 2 != 0) {
      priorityBoost++;
    }
  }

  /**
   * Returns the SupplementaryData data type contains number of move and partial solution
   * if the given board in reference collection.
   *
   * @param board the given board object
   * @return SupplementaryData data type of the given board.  Return null if board is not
   *         in reference collection.
   */
  SupplementaryData referenceContains(final Board board) {
    try {
      if (refRecorder != null) {
        return estimator.referenceContains(board, refRecorder.getActiveMap());
      }
      return estimator.referenceContains(board, refConnection.getActiveMap());
    } catch (RemoteException e) {
      resumePrimeSolver();
      return null;
    }
  }

  /**
   * Returns the integer value of best inverse estimate based on reference collections.
   *
   * @param board the given board object
   * @param priority the basic priority of the given board
   * @return integer value of best inverse estimate based on reference collections
   */
  private int inverseEstimate(final Board board, final int priority) {
    try {
      if (refRecorder != null) {
        return estimator.inverseEstimate(board, priority, refRecorder.getActiveMap());
      }
      return estimator.inverseEstimate(board, priority, refConnection.getActiveMap());
    } catch (RemoteException e) {
      resumePrimeSolver();
      return priority;
    }
  }

  @Override
  public void findOptimalPath(final Board board) {
    if (board == null) {
      throw new IllegalArgumentException("Board is null");
    }

    assert inUseHeuristic == HeuristicOptions.PD78 || timerSetting
        : "Timeout feature much turn on for all solvers except pdb 78.";

    if (timerSetting) {
      final int maxTimeout = 300;
      assert timeoutLimit > 0 && timeoutLimit <= maxTimeout
          : "Timeout limit : " + timeoutLimit + " out of range 1 second to 5 minutes";
    }

    stopwatch = new Stopwatch();
    stopwatch.stop();
    stopwatch.reset();
    int limit = -1;
    if (board.isSolvable()) {
      clearHistory();
      if (board.isGoal()) {
        solved = true;
        terminated = true;
      } else {
        stopwatch.start();
        resetDepthSummary(board);
        limit = heuristic(board, inUseVersion, SolverAction.SEARCH);
        assert limit > 0 : "Board must be solvable and is not the goal state.";
        idaStar(limit);
        if (solved) {
          assert board.checkSolution(steps, solutionMove)
              : "Not reach goal state, steps " + steps;
        } else if (!searchTimeout) {
          assert false : "Neither solver nor timeout.";
        }
      }
    } else {
      isSolvable = false;
    }
    searchTime = stopwatch.currentTime();
    stopwatch = null;
  }

  /**
   * Solve the puzzle using interactive deepening A* algorithm.  Start from the initial
   * limit, increment 2 at a time up to maximum 80 until solution found.
   *
   * @param initLimit the initial maximum allowance of solution moves
   */
  final void idaStar(final int initLimit) {
    int limit = initLimit;
    searchCountBase = 0;
    if (solutionMove[1] != null) {
      forwardSearch(limit);
      return;
    }

    if (getVersion().isOptimum()) {
      int countDir = 0;
      for (int i = 0; i < DIR_SIZE; i++) {
        if (lastDepthSummary[i + DIR_SIZE] > 0) {
          countDir++;
        }
      }

      // quick scan for advanced priority, determine the start order for optimization
      if (countDir > 1) {
        int startLimit = priorityBasis;
        while (startLimit < limit) {
          idaCount = 0;
          searchDepth = startLimit;
          dfsStartingOrder(startLimit);
          startLimit += 2;
          boolean overload = false;
          for (int i = DIR_SIZE; i < DIR_SIZE * 2; i++) {
            if (lastDepthSummary[i] > DFS_REVIEW_LIMIT) {
              overload = true;
              break;
            }
          }
          if (overload) {
            break;
          }
        }
      }
    }

    while (limit <= MAX_MOVE) {
      idaCount = 0;
      if (isStatusOn()) {
        System.out.print("ida limit " + limit);
      }
      searchDepth = limit;
      dfsStartingOrder(limit);
      searchCountBase += idaCount;
      searchNodeCount = searchCountBase;

      if (searchTimeout) {
        if (isStatusOn()) {
          System.out.printf("\tNodes : %-15s timeout\n", Integer.toString(idaCount));
        }
        return;
      } else {
        if (isStatusOn()) {
          System.out.printf("\tNodes : %-15s " + stopwatch.currentTime() + "s\n",
              Integer.toString(idaCount));
        }

        if (solved) {
          lastSearchBoard = lastBoard;
          if (inUseHeuristic == HeuristicOptions.PD78 && searchTime > refCutoffLimit
              && (inUseVersion == SolverVersion.OPTIMUM || (heuristicBasis(lastBoard)
                  == heuristicBoost(lastBoard)))) {
            try {
              if (refConnection != null) {
                flagNewReference = refConnection.addBoard(lastBoard, steps, solutionMove);
              }
              if (refRecorder != null) {
                flagNewReference = refRecorder.addBoard(lastBoard, steps, solutionMove);
              }
              if (flagNewReference) {
                priorityBoost = -1;
              }
            } catch (RemoteException e) {
              resumePrimeSolver();
            }
          }
          return;
        }
      }
      limit += 2;
    }
  }

  /**
   * Skip the first 8 moves from stored record then solve the remaining puzzle using
   * depth first search with exact number of steps of optimal solution.
   *
   * @param limit the initial maximum allowance of solution moves
   */
  final void forwardSearch(final int limit) {
    Board.Move[] partialSolution = solutionMove;
    Board board = new Board(initTiles);
    for (int i = 1; i < NUM_PARTIAL_MOVES; i++) {
      board = board.shift(partialSolution[i]);
      assert board != null : i + "board is null" + Arrays.toString(solutionMove)
      + (new Board(initTiles));
    }
    clearHistory();
    heuristic(board, SolverVersion.PRIME, SolverAction.SEARCH);
    updateDepthSummary(partialSolution[NUM_PARTIAL_MOVES]);

    idaCount = NUM_PARTIAL_MOVES;
    if (isStatusOn()) {
      System.out.print("ida limit " + limit);
    }
    searchDepth = limit;
    dfsStartingOrder(limit - NUM_PARTIAL_MOVES + 1);
    searchNodeCount = idaCount;

    if (solved) {
      System.arraycopy(solutionMove, 2, partialSolution, NUM_PARTIAL_MOVES + 1,
          limit - NUM_PARTIAL_MOVES);
      solutionMove = partialSolution;
    }
    steps = (byte) limit;

    if (isStatusOn()) {
      if (searchTimeout) {
        System.out.printf("\tNodes : %-15s timeout\n", Integer.toString(searchNodeCount));
      } else {
        System.out.printf("\tNodes : %-15s " + stopwatch.currentTime() + "s\n",
            Integer.toString(searchNodeCount));
      }
    }
  }

  /**
   * Reset lastDepthSummary from the given board object.
   *
   * @param board the given board object
   */
  final void resetDepthSummary(final Board board) {
    lastDepthSummary = new int[DIR_SIZE * 2];
    int[] validMoves = board.getValidMoves();
    for (int i = 0; i < DIR_SIZE; i++) {
      if (validMoves[i] == 0) {
        lastDepthSummary[i] = END_OF_SEARCH;
      } else {
        lastDepthSummary[i + DIR_SIZE] = 1;
      }
    }
  }

  /**
   * Change the lastDepthSummary to the given Direction only for the remaining
   * search in Optimum version.
   *
   * @param dir the given Board.Move direction
   */
  private void updateDepthSummary(final Board.Move dir) {
    lastDepthSummary = new int[DIR_SIZE * 2];
    int dirValue = dir.getValue();
    for (int i = 0; i < DIR_SIZE; i++) {
      if (i == dirValue) {
        lastDepthSummary[i + DIR_SIZE] = 1;
      } else {
        lastDepthSummary[i] = END_OF_SEARCH;
      }
    }
  }

  /**
   * Swap a pair of tiles.
   *
   * @param zeroPos the integer of current zero position
   * @param nextPos the integer of next position to swap
   */
  final void swap(final int zeroPos, final int nextPos) {
    tiles[zeroPos] = tiles[nextPos];
    tiles[nextPos] = 0;
  }

  /**
   * Swap a pair of tiles and a pair of mirror tiles.
   *
   * @param zeroPos the integer of current zero position
   * @param nextPos the integer of next position to swap
   * @param zeroMirror the integer of current zero position of mirror reflection
   * @param nextMirror the integer of next position to swap of mirror reflection
   */
  final void swap(final int zeroPos, final int nextPos, final int zeroMirror,
      final int nextMirror) {
    swap(zeroPos, nextPos);
    tilesMirror[zeroMirror] = tilesMirror[nextMirror];
    tilesMirror[nextMirror] = 0;
  }

  /**
   * Stop the timer, update number of steps when reached goal state.
   *
   * @param cost the given cost at time of search reached goal state
   * @return integer value of END_OF_SEARCH factor
   */
  final int goalReached(final int cost) {
    stopwatch.stop();
    steps = (byte) cost;
    solved = true;
    terminated = true;
    assert lastBoard != null && lastBoard.checkSolution(steps, solutionMove)
        : "Not reach goal state, steps " + steps;
    return END_OF_SEARCH;
  }

  // ----- search results -----

  @Override
  public final boolean isSolvable() {
    return isSolvable;
  }

  @Override
  public final boolean isSearchTimeout() {
    if (!isSolvable) {
      return false;
    }
    return searchTimeout;
  }

  @Override
  public final int searchDepth() {
    if (!isSolvable) {
      return -1;
    }
    return searchDepth;
  }

  @Override
  public final int searchNodeCount() {
    if (!isSolvable) {
      return -1;
    }
    return searchNodeCount;
  }

  @Override
  public final double searchTime() {
    return searchTime;
  }

  @Override
  public final byte moves() {
    if (!isSolvable) {
      return -1;
    }
    if (searchTimeout) {
      return -1;
    }
    return steps;
  }

  @Override
  public final Board.Move[] solution() {
    if (!isSolvable) {
      return null;
    }
    if (searchTimeout) {
      return null;
    }
    return solutionMove;
  }

  @Override
  public final Board lastSearchBoard() {
    if (inUseHeuristic == HeuristicOptions.PD78) {
      return lastSearchBoard;
    }
    throw new UnsupportedOperationException();
  }

  @Override
  public final boolean isNewReference() {
    if (inUseHeuristic == HeuristicOptions.PD78) {
      return flagNewReference;
    }
    return false;
  }

  @Override
  public final void clearNewReference() {
    flagNewReference = false;
  }

  /**
   * Rotation direction between two consecutive moves.
   *
   * @author <a href="http://www.linkedin.com/pub/macy-wong/46/550/37b/"
   *            target="_blank">Meisze Wong (linkedin)</a>
   */
  public enum Rotation {
    /**
     * Two consecutive moves in same directions.
     */
    RESET(0),
    /**
     * Two consecutive moves are clockwise.
     */
    CLOCKWISE(1),
    /**
     * Two consecutive moves are counterclockwise.
     */
    COUNTERCLOCKWISE(2);

    /** The value of rotation. */
    private final int val;

    /**
     * Initializes a Rotation type.
     *
     * @param val the preset value
     */
    Rotation(final int val) {
      this.val = val;
    }

    /**
     * Returns the value of current rotation.
     *
     * @return value of current rotation
     */
    public int getValue() {
      return val;
    }
  }

  /**
   * SolverAction the action choice of heuristic lookup.
   *
   * @author <a href="http://www.linkedin.com/pub/macy-wong/46/550/37b/"
   *            target="_blank">Meisze Wong (linkedin)</a>
   */
  public enum SolverAction {
    /**
     * Solver action review for heuristic lookup.
     */
    REVIEW,

    /**
     * Solver version search for heuristic during search in progress.
     */
    SEARCH;

    /**
     * Returns the boolean represent the instance object is REVIEW.
     *
     * @return boolean represent the instance object is REVIEW
     */
    public boolean isReview() {
      return this == REVIEW;
    }

    /**
     * Returns the boolean represent the instance object is SEARCH.
     *
     * @return boolean represent the instance object is SEARCH
     */
    public boolean isSearch() {
      return this == SEARCH;
    }
  }

  /**
   * ReferenceConflictException is the custom runtime exception.
   *
   * @author <a href="http://www.linkedin.com/pub/macy-wong/46/550/37b/"
   *            target="_blank">Meisze Wong (linkedin)</a>
   */
  private final class ReferenceConflictException extends RuntimeException {
    /**
     * Auto generated serial version ID.
     */
    private static final long serialVersionUID = 2692369565206551559L;

    /**
     * Initialize ReferenceConflictException custom exception.
     *
     * @param message the String of error message
     */
    private ReferenceConflictException(final String message) {
      super(message);
    }
  }
}

