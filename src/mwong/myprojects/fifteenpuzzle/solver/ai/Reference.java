package mwong.myprojects.fifteenpuzzle.solver.ai;

import mwong.myprojects.fifteenpuzzle.solver.SmartSolver;
import mwong.myprojects.fifteenpuzzle.solver.components.Board;
import mwong.myprojects.fifteenpuzzle.solver.components.Direction;

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
     * @param inSolver the SolverInterface object in use
     */
    void updatePending();

    /**
     * Verify the given solver is using pattern database 7-8, scan the full
     * collection, if the reference board is not verified, verify it now.
     *
     * @param inSolver the SolverInterface object in use
     */
    void updatePending(SmartSolver inSolver);

    /**
     * If the given solver using pattern database 7-8, and it takes
     * over the cutoff limit solve the puzzle with advanced estimate;
     * add to reference boards collection.
     *
     * @param inSolver the SolverInterface object in use
     */
    boolean addBoard(SmartSolver inSolver);

    /**
     * If the given solver using pattern database 7-8, and it takes
     * over the cutoff limit solve the puzzle with advanced estimate;
     * add to reference boards collection.
     *
     * @param inSolver the SolverInterface object in use
     */
    boolean addBoard(Board board, byte steps, Direction[] solution);

    /**
     * If the given solver using pattern database 7-8, and it takes
     * over the cutoff limit solve the puzzle with advanced estimate;
     * add to reference boards collection.
     *
     * @param inSolver the SolverInterface object in use
     */
    boolean addBoard(Board board, byte steps, Direction[] solution, SmartSolver inSolver);

    /**
     * If the solver is SolverPD object and last search board in activeMap
     * that need to verify; verify the full set and return true.
     *
     * @param inSolver the given SolverIntegerface
     * @return boolean if last search board in activeMap has been verified.
     */
    boolean updateLastSearch(SmartSolver inSolver);
    /**
     * If the solver is SolverPD object and last search board in activeMap
     * that need to verify; verify the full set and return true.
     *
     * @param inSolver the given SolverIntegerface
     * @return boolean if last search board in activeMap has been verified.
     */
    boolean updateLastSearch(Board board);
    /**
     * If the solver is SolverPD object and last search board in activeMap
     * that need to verify; verify the full set and return true.
     *
     * @param inSolver the given SolverIntegerface
     * @return boolean if last search board in activeMap has been verified.
     */
    boolean updateLastSearch(Board board, SmartSolver inSolver);
}
