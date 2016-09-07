package mwong.myprojects.fifteenpuzzle.solver.ai;

import mwong.myprojects.fifteenpuzzle.solver.SmartSolver;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

/**
 * ReferenceBoard is the data type of stored board of reference collection.
 * It analysis each board's actual number of moves, first 8 moves to goal state,
 * and a conversion set for reverse estimate (use reference board as goal state).
 *
 * <p>Dependencies : Board.java, Direction.java, FileProperties.java, HeuristicOptions.java,
 *                   PatternOptions.java, ReferenceBoard.java, ReferenceConstants.java,
 *                   ReferenceMoves.java, ReferenceProperties.java, SmartSolverPD.java, Solver.java
 *
 * @author   Meisze Wong
 *           www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public class ReferenceAdapter extends UnicastRemoteObject implements ReferenceRemote {
    private static final long serialVersionUID = 17195273121L;
    private Reference refObject;

    public ReferenceAdapter() throws RemoteException {
        refObject = new ReferenceAccumulator();
    }

    public ReferenceAdapter(ReferenceAccumulator refAccumulator) throws RemoteException {
        refObject = refAccumulator;
    }

    /**
     * Returns a HashMap of collection of reference boards.
     *
     * @return HashMap of collection of reference boards
     */
    public HashMap<ReferenceBoard, ReferenceMoves> getActiveMap() {
        return refObject.getActiveMap();
    }

    /**
     * Returns an integer of cutoff setting.
     *
     * @return integer of cutoff setting
     */
    public int getCutoffSetting() {
        return refObject.getCutoffSetting();
    }

    /**
     * Returns a double of cutoff limit (95% of cutoff setting).
     *
     * @return double of cutoff limit
     */
    public double getCutoffLimit() {
        return refObject.getCutoffLimit();
    }

    /**
     *  Verify the given solver is using pattern database 7-8, scan the full
     *  collection, if the reference board is not verified, verify it now.
     *
     *  @param inSolver the SolverInterface object in use
     */
    public void updatePending(SmartSolver inSolver) throws RemoteException {
        refObject.updatePending(inSolver);
    }

    /**
     * If the given solver using pattern database 7-8, and it takes
     * over the cutoff limit solve the puzzle with advanced estimate;
     * add to reference boards collection.
     *
     * @param inSolver the SolverInterface object in use
     */
    public boolean addBoard(SmartSolver inSolver) throws RemoteException {
        return refObject.addBoard(inSolver);
    }

    /**
     * If the solver is SolverPD object and last search board in activeMap
     * that need to verify; verify the full set and return true.
     *
     * @param inSolver the given SolverIntegerface
     * @return boolean if last search board in activeMap has been verified.
     */
    public boolean updateLastSearch(SmartSolver inSolver) throws RemoteException {
        return refObject.updateLastSearch(inSolver);
    }

    @Override
    public boolean validateSolver(SmartSolver inSolver) throws RemoteException {
        return refObject.validateSolver(inSolver);
    }
}
