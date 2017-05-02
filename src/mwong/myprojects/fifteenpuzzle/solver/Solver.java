package mwong.myprojects.fifteenpuzzle.solver;

import mwong.myprojects.fifteenpuzzle.solver.components.Board;
import mwong.myprojects.fifteenpuzzle.solver.components.Direction;

/**
 * SolverStandard is the interface class that has the basic methods of any
 * 15 puzzle solver.
 *
 * <p>Dependencies : Board.java, Direction.java
 *
 * @author Meisze Wong
 *         www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public interface Solver {
    // ----- solver setting -----
    /**
     * Return HeuristicOptions of object instance that currently using.
     *
     * @return HeuristicOptions of solver
     */
    HeuristicOptions getHeuristicOptions();

    /**
     * Print solver description.
     */
    void printDescription();

    /**
     * Set the message feature with the given flag.
     *
     * @param flag the boolean represent the ON/OFF message feature
     */
    void messageSwitch(boolean flag);

    /**
     * Set the timeout feature with the given flag.
     *
     * @param flag the boolean represent the ON/OFF timeout feature
     */
    void timeoutSwitch(boolean flag);

    /**
     * Set the timeout limit with the given value in seconds.
     *
     * @param seconds the integer represent the timeout limit in seconds
     */
    void setTimeoutLimit(int seconds);

    /**
     * Returns the boolean represents the timeout feature is in use.
     *
     * @return the boolean represents the timeout feature is in use
     */
    boolean isFlagTimeout();

    /**
     * Returns integer of timeout setting.
     *
     * @return integer of timeout setting
     */
    int getSearchTimeoutLimit();

    // ----- heuristic and solve the puzzle -----
    /**
     * Returns the heuristic value of the given board.
     *
     * @param board the initial puzzle Board object to solve
     * @return byte value of the heuristic value of the given board
     */
    byte heuristic(Board board);

    /**
     * Find the optimal path to goal state if the given board is solvable.
     *
     * @param board the initial puzzle Board object to solve
     */
    void findOptimalPath(Board board);

    // ----- search results -----

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
    int searchDepth();

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
     * Returns the String Directions of each move to the goal state.
     *
     * @return String of Directions of each move to the goal state
     */
    String solutionString();

    /**
     * Returns the String Directions of each move to the goal state.
     *
     * @return String of Directions of each move to the goal state
     */
    String solutionQtString();
}
