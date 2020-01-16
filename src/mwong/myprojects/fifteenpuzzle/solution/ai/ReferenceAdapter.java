package mwong.myprojects.fifteenpuzzle.solution.ai;

import java.rmi.RemoteException;
import java.util.HashMap;

import mwong.myprojects.fifteenpuzzle.puzzle.Board;
import mwong.myprojects.fifteenpuzzle.solution.Solver;
import mwong.myprojects.fifteenpuzzle.solution.SolverPdb78;
import mwong.myprojects.fifteenpuzzle.solution.ai.Reference.ConnectionType;

/**
 * ReferenceAdapter implements ReferenceRemote interface. This class applied adapter pattern on
 * the ReferenceLog.class. It will be called by the ReferenceFactory.class to determine the
 * local or remote connection. Remote connection will load ReferenceRemoteImpl object from server.
 * It provides the same functionality as ReferenceLog.class with any type of connections.
 *
 * <p>Dependencies : Board.java, Solver.java, Solverpdb78.java, Reference.java
 *
 * @author <a href="http://www.linkedin.com/pub/macy-wong/46/550/37b/"
 *            target="_blank">Meisze Wong (linkedin)</a>
 * @see <a href="http://www.github.com/mwong510ca/15PuzzleOptimalSolver/"
 *         target="_blank">GitHub (full project)</a>
 */
public final class ReferenceAdapter implements ReferenceRemote {
  /** The instance of Reference object. */
  private Reference refObject;

  /**
   * Initialize ReferenceAdapter object with given connection type.
   *
   * @param type the given connection type
   * @throws RemoteException the remote exception
   */
  public ReferenceAdapter(final ConnectionType type) throws RemoteException {
    refObject = new ReferenceLog(type);
  }

  @Override
  public HashMap<ReferenceBoard, ReferenceMoves> getActiveMap() throws RemoteException {
    return refObject.getActiveMap();
  }

  @Override
  public ConnectionType getConnectionTypeInUse() {
    return refObject.getConnectionTypeInUse();
  }

  @Override
  public double getCutoffLimit() {
    return refObject.getCutoffLimit();
  }

  @Override
  public boolean loadSolver(final Solver copySolver) {
    return refObject.loadSolver(copySolver);
  }

  @Override
  public boolean hasSolver78() {
    return refObject.hasSolver78();
  }

  @Override
  public SolverPdb78 getSolver78() {
    return refObject.getSolver78();
  }

  @Override
  public boolean containsBoard(final Board board) {
    return refObject.containsBoard(board);
  }

  @Override
  public boolean addBoard(final Board board, final byte steps, final Board.Move[] solution)
      throws RemoteException {
    return refObject.addBoard(board, steps, solution);
  }

  @Override
  public boolean updateLastSearch(final Solver copySolver) throws RemoteException {
    return refObject.updateLastSearch(copySolver);
  }

  @Override
  public boolean updateLastSearch(final Solver copySolver, final Board board)
      throws RemoteException {
    return refObject.updateLastSearch(copySolver, board);
  }

  @Override
  public void updatePending() throws RemoteException {
    refObject.updatePending();
  }

  @Override
  public void updatePending(final Solver copySolver) throws RemoteException {
    refObject.updatePending(copySolver);
  }

  @Override
  public void printStatus() {
    refObject.printStatus();
  }

  @Override
  public void printAllBoards() {
    refObject.printAllBoards();
  }
}
