/****************************************************************************
 *  @author   Meisze Wong
 *            www.linkedin.com/pub/macy-wong/46/550/37b/
 *
 *  Compilation  : javac SolverInterface.java
 *  Dependencies : Board.java, Direction.java, AdvancedAccumulator.java,
 *                 AdvancedMoves.java
 *
 *  SolverInterface class of 15 puzzle that has the following constants and methods.
 *
 ****************************************************************************/

package mwong.myprojects.fifteenpuzzle.solver;

import mwong.myprojects.fifteenpuzzle.solver.components.Board;

public interface Solver extends SolverStandard {
    /**
     *  Set the advanced priority with the given flag.
     *
     *  @param flag the boolean represent the active status of advanced priority
     */
    void advPrioritySwitch(boolean flag);

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
