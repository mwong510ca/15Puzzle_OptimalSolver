package mwong.myprojects.fifteenpuzzle.server;

import mwong.myprojects.fifteenpuzzle.solver.SmartSolver;
import mwong.myprojects.fifteenpuzzle.solver.advanced.SmartSolverPdb;
import mwong.myprojects.fifteenpuzzle.solver.ai.ReferenceAdapter;
import mwong.myprojects.fifteenpuzzle.solver.ai.ReferenceBoard;
import mwong.myprojects.fifteenpuzzle.solver.ai.ReferenceMoves;
import mwong.myprojects.fifteenpuzzle.solver.ai.ReferenceRemote;
import mwong.myprojects.fifteenpuzzle.solver.components.Board;
import mwong.myprojects.fifteenpuzzle.solver.components.Direction;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

/**
 * ReferenceRemoteImpl implements the remote connection with referenceAdapter object.
 *
 * @author Meisze Wong
 *         www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public class ReferenceRemoteImpl extends UnicastRemoteObject implements ReferenceRemote {
    private static final long serialVersionUID = 17195273121L;
    private static ReferenceRemote refObject;

    public ReferenceRemoteImpl()
            throws RemoteException, IOException, FileNotFoundException {
        refObject = new ReferenceAdapter();
    }

    @Override
    public SmartSolverPdb getSolver() throws RemoteException {
        return refObject.getSolver();
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
    public boolean addBoard(Board board, byte steps, Direction[] solution, SmartSolver inSolver)
            throws RemoteException {
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
