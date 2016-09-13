package mwong.myprojects.fifteenpuzzle.solver.ai;

import mwong.myprojects.fifteenpuzzle.solver.SmartSolver;
import mwong.myprojects.fifteenpuzzle.solver.components.Board;
import mwong.myprojects.fifteenpuzzle.solver.components.Direction;

import java.rmi.RemoteException;
import java.util.HashMap;

/**
 * Reference is the interface of stored board of reference collection
 * for 15 puzzle optimal solver advanced version.
 *
 * <p>Dependencies : SmartSolver.java
 *
 * @author Meisze Wong
 *         www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public interface Reference {
    /**
     * Returns a HashMap of collection of reference boards.
     *
     * @return HashMap of collection of reference boards
     */
    HashMap<ReferenceBoard, ReferenceMoves> getActiveMap();

    /**
     * Returns an integer of cutoff setting.
     *
     * @return integer of cutoff setting
     */
    int getCutoffSetting();

    /**
     * Returns a double of cutoff limit (minus buffer setting).
     *
     * @return double of cutoff limit
     */
    double getCutoffLimit();

    /**
     * Verify the given solver is using pattern database 7-8, scan the full
     * collection, if the reference board is not verified, verify it now.
     *
     * @throws RemoteException throw exception when connection lost
     */
    void updatePending() throws RemoteException;

    /**
     * Verify the given solver is using pattern database 7-8, scan the full
     * collection, if the reference board is not verified, verify it now.
     *
     * @param inSolver the SolverInterface object in use
     * @throws RemoteException throw exception when connection lost
     */
    void updatePending(SmartSolver inSolver) throws RemoteException;

    /**
     * If the given solver using pattern database 7-8, and it takes
     * over the cutoff limit solve the puzzle with advanced estimate;
     * add to reference boards collection.
     *
     * @param inSolver the SolverInterface object in use
     * @throws RemoteException throw exception when connection lost
     */
    boolean addBoard(SmartSolver inSolver) throws RemoteException;

    /**
     * If the given solver using pattern database 7-8, and it takes
     * over the cutoff limit solve the puzzle with advanced estimate;
     * add to reference boards collection.
     *
     * @param board the given board object
     * @param steps the byte value of number of moves
     * @param solution the Direction array of moves
     */
    boolean addBoard(Board board, byte steps, Direction[] solution);

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
    boolean addBoard(Board board, byte steps, Direction[] solution, SmartSolver inSolver);

    /**
     * If the solver is SolverPD object and last search board in activeMap
     * that need to verify; verify the full set and return true.
     *
     * @param inSolver the given SolverIntegerface
     * @return boolean if last search board in activeMap has been verified.
     * @throws RemoteException throw exception when connection lost
     */
    boolean updateLastSearch(SmartSolver inSolver) throws RemoteException;

    /**
     * If the solver is SolverPD object and last search board in activeMap
     * that need to verify; verify the full set and return true.
     *
     * @param board the given Board object
     * @return boolean if last search board in activeMap has been verified.
     * @throws RemoteException throw exception when connection lost
     */
    boolean updateLastSearch(Board board) throws RemoteException;

    /**
     * If the solver is SolverPD object and last search board in activeMap
     * that need to verify; verify the full set and return true.
     *
     * @param board the given Board object
     * @param inSolver the given SolverIntegerface
     * @return boolean if last search board in activeMap has been verified.
     */
    boolean updateLastSearch(Board board, SmartSolver inSolver);
}
