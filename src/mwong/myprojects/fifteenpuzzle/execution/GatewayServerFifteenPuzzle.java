package mwong.myprojects.fifteenpuzzle.execution;

import java.io.IOException;
import java.rmi.RemoteException;

import mwong.myprojects.fifteenpuzzle.PropertiesCache;
import mwong.myprojects.fifteenpuzzle.puzzle.Board;
import mwong.myprojects.fifteenpuzzle.puzzle.HeuristicOptions;
import mwong.myprojects.fifteenpuzzle.puzzle.Board.DifficultyLevel;
import mwong.myprojects.fifteenpuzzle.solution.Solver;
import mwong.myprojects.fifteenpuzzle.solution.SolverBuilder;
import mwong.myprojects.fifteenpuzzle.solution.SolverConstants;
import mwong.myprojects.fifteenpuzzle.solution.Solver.ApplicationMode;
import mwong.myprojects.fifteenpuzzle.solution.SolverQuick;
import mwong.myprojects.fifteenpuzzle.solution.ai.ReferenceFactory;
import mwong.myprojects.fifteenpuzzle.solution.ai.ReferenceRemote;
import py4j.GatewayServer;

/**
 * GatewayServerFifteenPuzzle for pyqt5 GUI front end to connect to 15 puzzle solvers.
 * It use standalone reference collection.
 *
 * <p>Dependencies : PropertiesCache.java, Board.java, HeuristicOptions.java, SolverConstants.java,
 *                   Solver.java, SolverBuilder.java, SolverQuick.java, UniversalSolverFactory.java,
 *                   ReferenceFactory.java, ReferenceRemote.java, GatewayServer.jar
 *
 * @author <a href="http://www.linkedin.com/pub/macy-wong/46/550/37b/"
 *            target="_blank">Meisze Wong (linkedin)</a>
 * @see <a href="http://www.github.com/mwong510ca/15PuzzleOptimalSolver/"
 *         target="_blank">GitHub (full project)</a>
 */
public final class GatewayServerFifteenPuzzle {
  /** py4j gateway server port range lower bound. */
  private static final int PORT_LO_LIMIT = 25335;
  /** py4j gateway server port range upper bound. */
  private static final int PORT_HI_LIMIT = 65535;
  /** Default timeout limit is 10 seconds.
   *  @see mwong.myprojects.fifteenpuzzle.PropertiesCache */
  private static final int DEFAULT_TIMEOUT_LIMIT = 10;
  /** Initial timeout setting to default value when property file or field not available.
   *  @see mwong.myprojects.fifteenpuzzle.PropertiesCache */
  private int timeoutLimit;
  /** Solver variable using Manhattan distance. */
  private Solver solverMd;
  /** Solver variable using Manhattan distance with linear conflict. */
  private Solver solverMdLc;
  /** Solver variable using walking distance. */
  private Solver solverWd;
  /** Solver variable using walking distance and Manhattan distance. */
  private Solver solverWdMd;
  /** Solver variable using pattern database 555 and walking distance. */
  private Solver solverPdbWd555;
  /** Solver variable using pattern database 663 and walking distance. */
  private Solver solverPdbWd663;
  /** Solver variable using pattern database 78. */
  private Solver solverPdb78;
  /** QuickSolver variable provide non optimal solution within a second. */
  private SolverQuick quickSolver;

  /**
   * Initializes the GatewayServerFifteenPuzzle object.
   *
   * @throws IOException I/O exceptions
   * @throws RemoteException remote exceptions
   */
  public GatewayServerFifteenPuzzle() throws RemoteException, IOException {
    ReferenceRemote refConnection = (new ReferenceFactory()).getReferenceLocal();

    // load timeout setting from property file, use initial setting if not available
    timeoutLimit = DEFAULT_TIMEOUT_LIMIT;
    if (PropertiesCache.getInstance().containsKey("guiTimeoutLimit")) {
      try {
        timeoutLimit = Integer.parseInt(PropertiesCache.getInstance().getProperty(
            "guiTimeoutLimit"));
      } catch (NumberFormatException ex) {
        // do nothing
      }
    }

    SolverBuilder builder = new SolverBuilder(ApplicationMode.GUI, timeoutLimit);
    builder.setReference(refConnection);

    solverMd = builder.createSolver(HeuristicOptions.MD);
    solverMdLc = builder.createSolver(HeuristicOptions.MDLC);
    solverWd = builder.createSolver(HeuristicOptions.WD);
    solverWdMd = builder.createSolver(HeuristicOptions.WDMD);
    solverPdbWd555 = builder.createSolver(HeuristicOptions.PD555, true);
    solverPdbWd663 = builder.createSolver(HeuristicOptions.PD663, true);
    solverPdb78 = builder.createSolver(HeuristicOptions.PD78);
    quickSolver = new SolverQuick();
  }

  /**
   * Returns true, dummy method represents the connection success.
   *
   * @return boolean represents the connection success, always return true
   */
  public boolean isConnected() {
    return true;
  }

  /**
   * Returns the Board object at goal state.
   *
   * @return returns the Board object at goal state
   */
  public Board getGoal() {
    return SolverConstants.getGoalBoard();
  }

  /**
   * Returns the Board object of solvable random puzzle.
   *
   * @return returns the Board object of solvable random puzzle
   */
  public Board getRandom() {
    return new Board();
  }

  /**
   * Returns the Board object of solvable puzzle with easy level.
   *
   * @return returns the Board object of solvable puzzle with easy level
   */
  public Board getEasy() {
    return new Board(DifficultyLevel.EASY);
  }

  /**
   * Returns the Board object of solvable puzzle with moderate level.
   *
   * @return returns the Board object of solvable puzzle with moderate level
   */
  public Board getModerate() {
    return new Board(DifficultyLevel.MODERATE);
  }

  /**
   * Returns the Board object of solvable puzzle with hard level.
   *
   * @return returns the Board object of solvable puzzle with hard level
   */
  public Board getHard() {
    return new Board(DifficultyLevel.HARD);
  }

  /**
   * Returns the Board object from the given byte array.
   *
   * @param block byte array of 16 numbers from 0 - 15 represent the puzzle layout
   * @return returns the Board object from the given byte array
   */
  public Board getBoard(final byte[] block) {
    return new Board(block);
  }

  /**
   * Returns the Solver object using pattern database 78 heuristic function.
   *
   * @return returns the Solver object using pattern database 78
   */
  public Solver getSolverPd78() {
    return solverPdb78;
  }

  /**
   * Returns the Solver object using the combination of pattern database 663 and
   * walking distance heuristic functions.
   *
   * @return returns the Solver object using pattern database 663 and walking distance
   */
  public Solver getSolverPdWd663() {
    return solverPdbWd663;
  }

  /**
   * Returns the Solver object using the combination of pattern database 555 and
   * walking distance heuristic functions.
   *
   * @return returns the Solver object using pattern database 78 and walking distance
   */
  public Solver getSolverPdWd555() {
    return solverPdbWd555;
  }

  /**
   * Returns the Solver object using the combination of walking distance and
   * Manhattan distance heuristic functions.
   *
   * @return returns the Solver object using walking distance and Manhattan distance
   */
  public Solver getSolverWdMd() {
    return solverWdMd;
  }

  /**
   * Returns the Solver object using walking distance heuristic function.
   *
   * @return returns the Solver object using walking distance
   */
  public Solver getSolverWd() {
    return solverWd;
  }

  /**
   * Returns the Solver object using Manhattan distance with linear conflict heuristic function.
   *
   * @return returns the Solver object using Manhattan distance with linear conflict
   */
  public Solver getSolverMdLc() {
    return solverMdLc;
  }

  /**
   * Returns the Solver object using Manhattan distance heuristic function.
   *
   * @return returns the Solver object using  Manhattan distance
   */
  public Solver getSolverMd() {
    return solverMd;
  }

  /**
   * Returns the QuickSolver object provide non optimal solution in a second.
   *
   * @return returns the QuickSolver object provide non optimal solution
   */
  public SolverQuick getQuickSolver() {
    return quickSolver;
  }

  /**
   * Returns the integer represents the initial timeout setting.
   *
   * @return returns the integer represents the timeout limit
   */
  public int getTimeoutLimit() {
    return timeoutLimit;
  }

  /**
   * Main application to start the gateway server.
   *
   * @param args standard argument main function
   * @throws IOException I/O exceptions
   * @throws RemoteException remote exceptions
   */
  public static void main(final String[] args) throws RemoteException, IOException {
    int port = Integer.parseUnsignedInt(args[0]);
    if (port < PORT_LO_LIMIT || port > PORT_HI_LIMIT) {
      throw new IllegalArgumentException("invalid port : " + port);
    }
    GatewayServer gatewayServer = new GatewayServer(new GatewayServerFifteenPuzzle(), port);
    gatewayServer.start();
    System.out.println("Gateway server for 15 puzzle started using port " + port);
  }
}
