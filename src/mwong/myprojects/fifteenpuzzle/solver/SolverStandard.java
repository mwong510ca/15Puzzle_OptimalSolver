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
import mwong.myprojects.fifteenpuzzle.solver.components.Direction;

public interface SolverStandard {
    /**
     *  Return HeuristicType of object instance that implements SolverInterface.
     *
     *  @return HeuristicType of solver
     */
    HeuristicType getHeuristicType();

    /**
     *  Print solver description.
     */
    void printDescription();

    /**
     *  Set the message feature with the given flag.
     *
     *  @param flag the boolean represent the ON/OFF message feature
     */
    void messageSwitch(boolean flag);

    /**
     *  Set the timeout feature with the given flag.
     *
     *  @param flag the boolean represent the ON/OFF timeout feature
     */
    void timeoutSwitch(boolean flag);

    /**
     *  Set the timeout limit with the given value in seconds.
     *
     *  @param seconds the integer represent the timeout limit in seconds
     */
    void setTimeoutLimit(int seconds);

    /**
     *  Find the optimal path to goal state if the given board is solvable.
     *
     *  @param board the initial puzzle Board object to solve
     */
    void findOptimalPath(Board board);

    /**
     * Returns the original heuristic value of the given board.
     *
     * @return byte value of the original heuristic value of the given board
     */
    byte heuristic(Board board);

    /**
     * Returns the boolean value represents the search has timeout.
     *
     * @return boolean value represents the search has timeout
     */
    boolean isSearchTimeout();

    /**
     * Returns the integer value of search depth when the search terminated.
     *
     * @return integer value of search depth when the search terminated
     */
    int searchTerminateAtDepth();

    /**
     * Returns the integer value of total number of nodes generated during the search.
     *
     * @return integer value of total number of nodes generated during the search
     */
    int searchNodeCount();

    /**
     * Returns the double value of total time of search in seconds.
     *
     * @return double value of total time of search in seconds
     */
    double searchTime();

    /**
     * Returns the integer value of minimum moves to the goal state.
     *
     * @return integer value of minimum moves to the goal state
     */
    byte moves();

    /**
     * Returns the array of Directions of each move to the goal state.
     *
     * @return array of Directions of each move to the goal state
     */
    Direction[] solution();

    /**
     * Returns integer of timeout setting.
     *
     * @return integer of timeout setting
     */
    int getSearchTimeoutLimit();

	/**
     * Returns the flagTimeout.
     *
	 * @return the flagTimeout
	 */
    boolean isFlagTimeout();
}
