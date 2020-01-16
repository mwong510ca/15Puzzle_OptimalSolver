package mwong.myprojects.fifteenpuzzle.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

import mwong.myprojects.fifteenpuzzle.puzzle.Board;
import mwong.myprojects.fifteenpuzzle.solution.Solver;
import mwong.myprojects.fifteenpuzzle.solution.SolverPdb78;
import mwong.myprojects.fifteenpuzzle.solution.ai.Reference.ConnectionType;
import mwong.myprojects.fifteenpuzzle.solution.ai.ReferenceAdapter;
import mwong.myprojects.fifteenpuzzle.solution.ai.ReferenceBoard;
import mwong.myprojects.fifteenpuzzle.solution.ai.ReferenceMoves;
import mwong.myprojects.fifteenpuzzle.solution.ai.ReferenceRemote;

/**
 * ReferenceRemoteImpl implements the remote connection with referenceAdapter object.
 *
 * <p>Dependencies : Board.java, Solver.java, Reference.java, ReferenceAdapter.java,
 *                   ReferenceBoard.java, ReferenceMoves.java, ReferenceRemote.java,
 *                   SolverPdb78.java
 *
 * @author <a href="http://www.linkedin.com/pub/macy-wong/46/550/37b/"
 *            target="_blank">Meisze Wong (linkedin)</a>
 * @see <a href="http://www.github.com/mwong510ca/15PuzzleOptimalSolver/"
 *         target="_blank">GitHub (full project)</a>
 */
public final class ReferenceRemoteImpl extends UnicastRemoteObject implements ReferenceRemote {
  private static final long serialVersionUID = 17195273121L;
  /** The ReferenceRemote instance. */
  private static ReferenceRemote refObject;

  /**
   * Implement the reference object with remote connection.
   *
   * @throws RemoteException RemoteException
   * @throws FileNotFoundException FileNotFoundException
   * @throws IOException IOException
   */
  public ReferenceRemoteImpl()
      throws RemoteException, FileNotFoundException, IOException {
    refObject = new ReferenceAdapter(ConnectionType.REMOTESERVER);
  }

  @Override
  public HashMap<ReferenceBoard, ReferenceMoves> getActiveMap() throws RemoteException {
    return refObject.getActiveMap();
  }

  @Override
  public ConnectionType getConnectionTypeInUse() throws RemoteException {
    return refObject.getConnectionTypeInUse();
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
  public boolean containsBoard(final Board board) throws RemoteException {
    return refObject.containsBoard(board);
  }

  @Override
  public boolean addBoard(final Board board, final byte steps, final Board.Move[] solution)
      throws RemoteException {
    return refObject.addBoard(board, steps, solution);
  }

  @Override
  public boolean loadSolver(final Solver copySolver) throws RemoteException {
    return refObject.loadSolver(copySolver);
  }

  @Override
  public boolean hasSolver78() throws RemoteException {
    return refObject.hasSolver78();
  }

  @Override
  public SolverPdb78 getSolver78() throws RemoteException {
    return refObject.getSolver78();
  }

  @Override
  public double getCutoffLimit() throws RemoteException {
    return refObject.getCutoffLimit();
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
  public void printStatus() throws RemoteException {
    refObject.printStatus();
  }

  @Override
  public void printAllBoards() throws RemoteException {
    refObject.printAllBoards();
  }
}
