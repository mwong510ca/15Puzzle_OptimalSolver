package mwong.myprojects.fifteenpuzzle.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

import mwong.myprojects.fifteenpuzzle.solver.SmartSolver;
import mwong.myprojects.fifteenpuzzle.solver.ai.ReferenceAdapter;
import mwong.myprojects.fifteenpuzzle.solver.ai.ReferenceBoard;
import mwong.myprojects.fifteenpuzzle.solver.ai.ReferenceMoves;
import mwong.myprojects.fifteenpuzzle.solver.ai.ReferenceRemote;
import mwong.myprojects.fifteenpuzzle.solver.components.Board;
import mwong.myprojects.fifteenpuzzle.solver.components.Direction;

/**
 * DBRemoteImpl implements DBMainExtRemote interface that 
 * the RMI engine looks to for the stub. In other words the DataWrapAdapter
 * methods (local) are wrapped with these remote methods so the calling 
 * routine can treat these remote methods as is they are local.
 */
public class ReferenceRemoteImpl extends UnicastRemoteObject implements ReferenceRemote {
    private static final long serialVersionUID = 17195273121L;
    private static ReferenceRemote refObject;
    
    /**
     * Constructor that takes the path of the data file as parameter
     * that creates the database instance.
     *
     * @param filePath the path to the data file.
     * @throws RemoteException Indicates the remote connection
     * is lost.
     * @throws IOException if the data file cannot be written, magic 
     * @throws FileNotFoundException if the data file cannot be found.
     * cookie mismatched or the file is corrupted.
     */
    public ReferenceRemoteImpl() 
            throws RemoteException, IOException, FileNotFoundException {
        refObject = new ReferenceAdapter();
    }

	@Override
	public HashMap<ReferenceBoard, ReferenceMoves> getActiveMap() throws RemoteException {
		return refObject.getActiveMap();
	}

	@Override
	public int getCutoffSetting() throws RemoteException {
		return refObject.getCutoffSetting();
	}

	@Override
	public double getCutoffLimit() throws RemoteException {
		return refObject.getCutoffLimit();
	}

	@Override
	public void updatePending() throws RemoteException {
		refObject.updatePending();
	}

	@Override
	public void updatePending(SmartSolver inSolver) throws RemoteException {
		updatePending();
	}

	@Override
	public boolean addBoard(SmartSolver inSolver) throws RemoteException {
		// should not be call by remote version
		return false;
	}

	@Override
	public boolean addBoard(Board board, byte steps, Direction[] solution) throws RemoteException {
		return refObject.addBoard(board, steps, solution);
	}

	@Override
	public boolean addBoard(Board board, byte steps, Direction[] solution, SmartSolver inSolver) throws RemoteException {
		return addBoard(board, steps, solution);
	}

	@Override
	public boolean updateLastSearch(SmartSolver inSolver) throws RemoteException {
		// should not be call by remote version
		return false;
	}

	@Override
	public boolean updateLastSearch(Board board) throws RemoteException {
		return refObject.updateLastSearch(board);
	}

	@Override
	public boolean updateLastSearch(Board board, SmartSolver inSolver) throws RemoteException {
		return updateLastSearch(board);
	}
}
