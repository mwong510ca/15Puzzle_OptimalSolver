package mwong.myprojects.fifteenpuzzle.solution.ai;

import java.rmi.UnexpectedException;
import java.util.HashMap;

import mwong.myprojects.fifteenpuzzle.puzzle.Board;
import mwong.myprojects.fifteenpuzzle.solution.Solver;
import mwong.myprojects.fifteenpuzzle.solution.SolverPdb78;

/**
 * Reference is the interface of stored board of reference collection
 * for 15 puzzle optimal solver advanced version.
 *
 * <p>Dependencies : Board.java, Solver.java, SolverPdb78.java
 *
 * @author <a href="http://www.linkedin.com/pub/macy-wong/46/550/37b/"
 *            target="_blank">Meisze Wong (linkedin)</a>
 * @see <a href="http://www.github.com/mwong510ca/15PuzzleOptimalSolver/"
 *         target="_blank">GitHub (full project)</a>
 */
public interface Reference {
  /**
   * Returns a HashMap of collection of reference boards.
   *
   * @return HashMap of collection of reference boards
   * @throws UnexpectedException an unexpected reference collection error
   */
  HashMap<ReferenceBoard, ReferenceMoves> getActiveMap() throws UnexpectedException;

  /**
   * Return the connection type currently using.
   *
   * @return ConnectionType currently in use
   */
  ConnectionType getConnectionTypeInUse();

  /**
   * Returns a double of cutoff limit (minus buffer percentage).
   *
   * @return double of cutoff limit
   */
  double getCutoffLimit();

  /**
   * If the local solver not exists, use the data set from the given solver to create a new one.
   *
   * @param copySolver the given Solver object should be using pattern database 7-8.
   * @return boolean represents create local solver success
   */
  boolean loadSolver(Solver copySolver);

  /**
   * Returns boolean represents the object contains a solver.
   *
   *  @return boolean represents the object contains a solver
   */
  boolean hasSolver78();

  /**
   * Return the local solver object.
   *
   * @return SolverPdb78 the local solver object
   */
  SolverPdb78 getSolver78();

  /**
   * Returns the boolean value represents the given Board is the reference board in collection.
   *
   * @param board the given Board object
   * @return boolean value represents the given Board in reference collection
   */
  boolean containsBoard(Board board);

  /**
   * Add the given board and solutions to reference collection.
   *
   * @param board the given board object
   * @param steps the byte value of number of moves
   * @param solution the Direction array of moves
   * @return boolean represents add to collection success
   */
  boolean addBoard(Board board, byte steps, Board.Move[] solution);

   /**
   * Update the latest search results if the board exists in reference collection.
   * Returns true if the given solver using pattern database 7-8 and search time exists
   * the cutoff limit.
   *
   * @param copySolver the given Solver object
   * @return boolean if last search board in activeMap has been verified.
   */
  boolean updateLastSearch(Solver copySolver);

  /**
   * Update the latest search results if the board exists in reference collection.
   * Returns true if the given solver using pattern database 7-8.
   *
   * @param copySolver the given Solver object
   * @param board the given board
   * @return boolean if last search board in activeMap has been verified.
   */
  boolean updateLastSearch(Solver copySolver, Board board);

  /**
   * If local solver exists, update all incomplete reference board in collection.
   */
  void updatePending();

  /**
   * Load the given solver, update all incomplete reference board in collection.
   *
   * @param copySolver object
   */
  void updatePending(Solver copySolver);

  /**
   * Print the current status of reference boards collection.
   */
  void printStatus();

  /**
   * Print all reference boards and it's components.
   */
  void printAllBoards();

  /**
   * ConnectionType is the enum type determine of connection.
   *
   * @author <a href="http://www.linkedin.com/pub/macy-wong/46/550/37b/"
   *            target="_blank">Meisze Wong (linkedin)</a>
   */
  enum ConnectionType {
    /** Remote server connection. */
    REMOTESERVER,
    /** Standalone local connection. */
    STANDALONE,
    /** Disabled, not in use. */
    DISABLED;

    @Override
    public String toString() {
      switch (this) {
        case REMOTESERVER:
          return "Remote server - remote connection in use.";
        case STANDALONE:
          return "Standalone - local object in use.";
        case DISABLED:
          return "Disabled - not in use.";
        default:
          break;
      }
      return null;
    }
  }
}
