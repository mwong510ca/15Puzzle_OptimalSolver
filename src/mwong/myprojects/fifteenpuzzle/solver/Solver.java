package mwong.myprojects.fifteenpuzzle.solver;

import mwong.myprojects.fifteenpuzzle.solver.components.Board;

/**
 * Solver extends SolverStandard Interface as the main interface.  It is the interface class
 * that extend the standard methods for the advanced version of 15 puzzle solvers.
 *
 * <p>Dependencies : Board.java, SolverStandard.java,
 *
 * @author   Meisze Wong
 *           www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public interface Solver extends SolverStandard {
    // ----- solver setting -----
    /**
     *  Set the advanced version with the given flag.
     *
     *  @param flag the boolean represent the active status of in use solver version
     */
    boolean versionSwitch(boolean flag);

    // ----- heuristic -----
    /**
     * Returns the original heuristic value of the given board.
     *
     * @return byte value of the original heuristic value of the given board
     */
    byte heuristicStandard(Board board);

    /**
     * Returns the advanced heuristic value of the given board.
     *
     * @return byte value of the advanced heuristic value of the given board
     */
    byte heuristicAdvanced(Board board);
}
