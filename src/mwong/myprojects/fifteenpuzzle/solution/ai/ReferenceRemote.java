package mwong.myprojects.fifteenpuzzle.solution.ai;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;

import mwong.myprojects.fifteenpuzzle.puzzle.Board;
import mwong.myprojects.fifteenpuzzle.solution.Solver;
import mwong.myprojects.fifteenpuzzle.solution.SolverPdb78;
import mwong.myprojects.fifteenpuzzle.solution.ai.Reference.ConnectionType;

/**
 * ReferenceRemote is the remote interface of stored board of reference collection.
 * It has the same set of functions as the Reference interface with remote feature.
 *
 * <p>Dependencies : Board.java, Solver.java, SolverPdb78.java
 *
 * @author <a href="http://www.linkedin.com/pub/macy-wong/46/550/37b/"
 *            target="_blank">Meisze Wong (linkedin)</a>
 * @see <a href="http://www.github.com/mwong510ca/15PuzzleOptimalSolver/"
 *         target="_blank">GitHub (full project)</a>
 */
public interface ReferenceRemote extends Remote {
  /**
   * Returns a HashMap of collection of reference boards.
   *
   * @return HashMap of collection of reference boards
   * @throws RemoteException RemoteException
   */
  HashMap<ReferenceBoard, ReferenceMoves> getActiveMap() throws RemoteException;

  /**
   * Return the connection type currently using.
   *
   * @return ConnectionType currently in use
   * @throws RemoteException RemoteException
   */
  ConnectionType getConnectionTypeInUse() throws RemoteException;

  /**
   * Returns a double of cutoff limit (95% of cutoff setting).
   *
   * @return double of cutoff limit
   * @throws RemoteException RemoteException
   */
  double getCutoffLimit() throws RemoteException;

  /**
   * Returns boolean value if load solver to a local SolverPdb78 success.
   *
   * @param copySolver the given solver must use pattern database 78.
   * @return boolean represents load solver success
   * @throws RemoteException RemoteException
   */
  boolean loadSolver(Solver copySolver) throws RemoteException;

  /**
   * Returns boolean represent reference object has a SolverPdb78.
   *
   * @return boolean represent reference object has a SolverPdb78
   * @throws RemoteException RemoteException
   */
  boolean hasSolver78() throws RemoteException;

  /**
   * Return the SolverPdb78 object if exists.
   *
   * @return SolverPdb78 object if exists
   * @throws RemoteException RemoteException
   */
  SolverPdb78 getSolver78() throws RemoteException;

  /**
   * Returns the boolean value represents the given Board is the reference board in collection.
   *
   * @param board the given Board object
   * @return boolean value represents the given Board in reference collection
   * @throws RemoteException RemoteException
   */
  boolean containsBoard(Board board) throws RemoteException;

  /**
   * Add the given board and solutions to reference collection.
   *
   * @param board the given board object
   * @param steps the byte value of number of moves
   * @param solution the Direction array of moves
   * @return boolean represents add to collection success
   * @throws RemoteException RemoteException
   */
  boolean addBoard(Board board, byte steps, Board.Move[] solution) throws RemoteException;

  /**
   * Update the latest search results if the board exists in reference collection.
   * Returns true if the given solver using pattern database 7-8 and search time exists
   * the cutoff limit.
   *
   * @param copySolver the given Solver object
   * @return boolean if last search board in activeMap has been verified.
   * @throws RemoteException throw exception when connection lost
   */
  boolean updateLastSearch(Solver copySolver) throws RemoteException;

  /**
   * Update the latest search results if the board exists in reference collection.
   * Returns true if the given solver using pattern database 7-8.
   *
   * @param copySolver the given Solver object
   * @param board the given board
   * @return boolean if last search board in activeMap has been verified.
   * @throws RemoteException throw exception when connection lost
   */
  boolean updateLastSearch(Solver copySolver, Board board) throws RemoteException;

  /**
   * If local solver exists, update all incomplete reference board in collection.
   *
   * @throws RemoteException throw exception when connection lost
   */
  void updatePending() throws RemoteException;

  /**
   * Load the given solver, update all incomplete reference board in collection.
   *
   * @param copySolver object
   * @throws RemoteException throw exception when connection lost
   */
  void updatePending(Solver copySolver) throws RemoteException;

  /**
   * Print the current status of reference boards collection.
   *
   * @throws RemoteException RemoteException
   */
  void printStatus() throws RemoteException;

  /**
   * Print all reference boards and it's components.
   *
   * @throws RemoteException RemoteException
   */
  void printAllBoards() throws RemoteException;
}
