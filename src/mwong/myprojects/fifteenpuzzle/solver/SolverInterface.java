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

import mwong.myprojects.fifteenpuzzle.solver.components.AdvancedAccumulator;
import mwong.myprojects.fifteenpuzzle.solver.components.AdvancedMoves;
import mwong.myprojects.fifteenpuzzle.solver.components.Board;
import mwong.myprojects.fifteenpuzzle.solver.components.Direction;

public interface SolverInterface {
    // HeuristicType is a reference type of Solver's Heuristic function
    enum HeuristicType {
        // Manhattan Distance
        MD("Manhattan Distance"),
        // Manhattan Distance
        MDLC("Manhattan Distance with Linear Conflict"),
        // Walking Distance
        WD("Walking Distance"),
        // Walking Distance
        WDMD("Walking Distance + Manhattan Distance with Linear Conflict"),
        // Additive Pattern 555
        PD555("Additive Pattern Database 555"),
        // Additive Pattern 663
        PD663("Additive Pattern Database 663"),
        // Additive Pattern 78
        PD78("Additive Pattern Database 78"),
        // Additive Pattern 78
        PDCustom("Additive Pattern - user defined custom pattern");

        // initialize solver option
        private String description;
        HeuristicType(String str) {
            description = str;
        }

        /**
         *  Return heuristic function description.
         *
         *  @return heuristic function description
         */
        public String getDescription() {
            return description;
        }
    }

    // ApplicationType is a reference type of console application
    enum ApplicationType {
        Solver, CompareHeuristic, CustomPattern, Stats;
    }

    //implicitly public, static and final
    int BOARD_SIZE = Board.getSize();
    int DEFAULT_TIMEOUT_LIMIT = 10;
    byte ROW_SIZE = Board.getRowSize();
    int MAX_MOVES = Board.getMaxMoves();
    int END_OF_SEARCH = MAX_MOVES + 1;
    byte CUTOFF_ADV_PRIORITY = 30;
    byte ADV_PARTIAL_MOVES = AdvancedMoves.getPartialMoves();
    boolean SWITCH_ON = true;
    boolean SWITCH_OFF = !SWITCH_ON;
    byte[] SYMMETRY_VAL = Board.getSymValConversion();
    byte[] SYMMETRY_POS = Board.getSymPosConversion();

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
     *  Print solver heading of given ApplicationType.
     */
    void printHeading(ApplicationType type);

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
     *  Set the advanced priority with the given flag.
     *
     *  @param flag the boolean represent the active status of advanced priority
     */
    void advPrioritySwitch(boolean flag);

    /**
     *  Set the timeout limit with the given value in seconds.
     *
     *  @param seconds the integer represent the timeout limit in seconds
     */
    void setTimeoutLimit(int seconds);

    /**
     *  Set the AdvancedAccumulator for advanced heuristic.
     *
     *  @param advAccumlator represent the AdvancedAccumulator object
     */
    void setAdvancedAccumulator(AdvancedAccumulator advAccumlator);

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
    byte heuristicOrg(Board board);

    /**
     * Returns the advanced heuristic value of the given board.
     *
     * @return byte value of the advanced heuristic value of the given board
     */
    byte heuristicAdv(Board board);

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
}
