package mwong.myprojects.fifteenpuzzle.solver.ai;

import mwong.myprojects.fifteenpuzzle.solver.SmartSolver;
import mwong.myprojects.fifteenpuzzle.solver.components.Board;
import mwong.myprojects.fifteenpuzzle.solver.components.Direction;

import java.rmi.RemoteException;
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
public class ReferenceAdapter implements ReferenceRemote {
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
     * @throws RemoteException throw exception when connection lost
     */
    public void updatePending() throws RemoteException {
        refObject.updatePending();
    }

    /**
     * Verify the given solver is using pattern database 7-8, scan the full
     * collection, if the reference board is not verified, verify it now.
     *
     * @param inSolver the SolverInterface object in use
     * @throws RemoteException throw exception when connection lost
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
     * @throws RemoteException throw exception when connection lost
     */
    public boolean addBoard(SmartSolver inSolver) throws RemoteException {
        return refObject.addBoard(inSolver);
    }

    /**
     * If the given solver using pattern database 7-8, and it takes
     * over the cutoff limit solve the puzzle with advanced estimate;
     * add to reference boards collection.
     *
     * @param board the given board object
     * @param steps the byte value of number of moves
     * @param solution the Direction array of moves
     */
    public boolean addBoard(Board board, byte steps, Direction[] solution) throws RemoteException {
        return refObject.addBoard(board, steps, solution);
    }

    /**
     * If the given solver using pattern database 7-8, and it takes
     * over the cutoff limit solve the puzzle with advanced estimate;
     * add to reference boards collection.
     *
     * @param board the given board object
     * @param steps the byte value of number of moves
     * @param solution the Direction array of moves
     * @param inSolver the SolverInterface object in use
     */
    public boolean addBoard(Board board, byte steps, Direction[] solution, SmartSolver inSolver)
            throws RemoteException {
        return addBoard(inSolver);
    }

    /**
     * If the solver is SolverPD object and last search board in activeMap
     * that need to verify; verify the full set and return true.
     *
     * @param inSolver the given SolverIntegerface
     * @return boolean if last search board in activeMap has been verified.
     * @throws RemoteException throw exception when connection lost
     */
    public boolean updateLastSearch(SmartSolver inSolver) throws RemoteException {
        return refObject.updateLastSearch(inSolver);
    }

    /**
     * If the solver is SolverPD object and last search board in activeMap
     * that need to verify; verify the full set and return true.
     *
     * @param board the given Board object
     * @return boolean if last search board in activeMap has been verified.
     * @throws RemoteException throw exception when connection lost
     */
    public boolean updateLastSearch(Board board) throws RemoteException {
        return refObject.updateLastSearch(board);
    }

    /**
     * If the solver is SolverPD object and last search board in activeMap
     * that need to verify; verify the full set and return true.
     *
     * @param board the given Board object
     * @param inSolver the given SolverIntegerface
     * @return boolean if last search board in activeMap has been verified.
     * @throws RemoteException throw exception when connection lost
     */
    public boolean updateLastSearch(Board board, SmartSolver inSolver) throws RemoteException {
        return updateLastSearch(inSolver);
    }
}
