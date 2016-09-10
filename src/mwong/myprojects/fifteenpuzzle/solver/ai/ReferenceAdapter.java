package mwong.myprojects.fifteenpuzzle.solver.ai;

import mwong.myprojects.fifteenpuzzle.solver.SmartSolver;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

/**
 * ReferenceAdapter implements ReferenceRemote interface.  This class applied adapter pattern on
 * the ReferenceAccumulator.class.  It will be called by the ReferenceFactory.class to determine
 * the local or remote connection.  It provides the same functionality ad ReferenceAccumulator.class
 * with any type of connections.
 *
 * <p>Dependencies : ReferenceAccumulator.java, ReferenceBaord.java, ReferenceMoves.java,
 *                   referenceRemote.java, SmartSolver.java
 *
 * @author Meisze Wong
 *         www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public class ReferenceAdapter extends UnicastRemoteObject implements ReferenceRemote {
    private static final long serialVersionUID = 17195273121L;
    private Reference refObject;

    // initialize the ReferenceAdapter object
    public ReferenceAdapter() throws RemoteException {
        refObject = new ReferenceAccumulator();
    }

    // initialize the ReferenceAdapter object with the given ReferenceAccumulator object
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
     * Verify the given solver is using pattern database 7-8, scan the full
     * collection, if the reference board is not verified, verify it now.
     *
     * @param inSolver the SolverInterface object in use
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
}
