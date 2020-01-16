package mwong.myprojects.fifteenpuzzle.solution;

import mwong.myprojects.fifteenpuzzle.puzzle.Board;
import mwong.myprojects.fifteenpuzzle.puzzle.HeuristicOptions;
import mwong.myprojects.fifteenpuzzle.solution.ai.ReferenceRemote;

/**
 * Solver is the interface class that has all general methods for setup
 * and search results. It apply to any heuristic of the 15 puzzle solver.
 *
 * <p>Dependencies : Board.java, HeuristicOptions.java, ReferenceRemote.java
 *
 * @author <a href="http://www.linkedin.com/pub/macy-wong/46/550/37b/"
 *            target="_blank">Meisze Wong (linkedin)</a>
 * @see <a href="http://www.github.com/mwong510ca/15PuzzleOptimalSolver/"
 *         target="_blank">GitHub (full project)</a>
 */
public interface Solver {
  // ----- setting options -----
  /**
   * Set the ReferenceRemote connection with the given connection for puzzle solvers.
   * Cannot use with ReferenceRecorder for administrative tool.
   *
   * @param refConnection set the ReferenceRemote with the given connection
   */
  void setReferenceConnection(ReferenceRemote refConnection);

  /**
   * Return ReferenceRemote object currently stored and using.
   *
   * @return ReferenceRemote object currently stored and using
   */
  ReferenceRemote getReference();

  /**
   * Use the Prime version with original heuristic only.
   *
   * @return boolean true if version has change as requested.
   *                 false no changes has been made, already using Prime version
   */
  boolean shiftPrime();

  /**
   * Use the Optimum version with boost heuristic based on the stored boards
   * with actual number of moves.
   *
   * @return boolean true if version has change as requested.
   *                 false Optimum version not available.
   */
  boolean shiftOptimum();

  /**
   * Return SolverVersion of object instance that currently using.
   *
   * @return SolverVersion of solver
   */
  SolverVersion getVersion();

  /**
   * Return HeuristicOptions of object instance that currently using.
   *
   * @return HeuristicOptions of solver
   */
  HeuristicOptions getHeuristic();

  /**
   * Return the byte array of pattern for pattern database.
   *
   * @return byte array of pattern for pattern database.
   */
  byte[] getInUsePdbPtn();

  /**
   * Print solver description.
   */
  void printDescription();

  /**
   * Print solver header.
   */
  void printHeader();

  /**
   * Print solver header.
   *
   * @param printPattern boolean flag to print pattern detail if pattern database.
   */
  void printHeader(boolean printPattern);

  /**
   * Set search status on and print the message while the search in progress.
   *
   * @param flag the boolean represent the status setting
   */
  void setStatusOn(boolean flag);

  /**
   * Return boolean represents the print searching status message feature is on.
   *
   * @return boolean represents the print searching status message feature is on
   */
  boolean isStatusOn();

  /**
   * Set search timer on that will terminate the searching when timeout.
   *
   * @param flag the boolean represent the timer setting
   */
  void setTimerOn(boolean flag);

  /**
   * Return boolean represents the timer feature is on.
   *o
   * @return boolean represents the timer feature is on
   */
  boolean isTimerOn();

  /**
   * Set the timeout limit with the given value in seconds.
   *
   * @param seconds the integer represent the timeout limit in seconds
   */
  void setTimeoutLimit(int seconds);

  /**
   * Returns integer of timeout setting.
   *
   * @return integer of timeout setting
   */
  int getTimeoutLimit();

  // ----- heuristic, solve the puzzle and result -----
  /**
   * Returns the heuristic value of the given board.
   *
   * @param board the initial puzzle Board object to solve
   * @return byte value of the heuristic value of the given board
   */
  int heuristic(Board board);

  /**
   * Returns the original heuristic value of the given board.
   *
   * @param board the initial puzzle Board object to solve
   * @return byte value of the original heuristic value of the given board
   */
  int heuristicBasis(Board board);

  /**
   * Returns the boost heuristic value of the given board.
   *
   * @param board the initial puzzle Board object to solve
   * @return byte value of the boost heuristic value of the given board
   */
  int heuristicBoost(Board board);

  /**
   * Find the optimal path to goal state if the given board is solvable.
   *
   * @param board the initial puzzle Board object to solve
   */
  void findOptimalPath(Board board);

  // ----- search results -----

  /**
   * Determine the given board is solvable from the search result.
   *
   * @return boolean represent solvable from search result
   */
  boolean isSolvable();

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
   * Returns the integer value of total number of nodes generated during
   * the search.
   *
   * @return integer value of total number of nodes generated during
   *         the search
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
  Board.Move[] solution();

  /**
   * Returns the board object of last search.
   *
   * @return board object of last search
   */
  Board lastSearchBoard();

  /**
   * Return boolean represent recent search board has added to reference collection.
   *
   * @return boolean represent recent search board has added to reference collection
   */
  boolean isNewReference();

  /**
   * Clear new reference flag.
   */
  void clearNewReference();

  /**
   * SolverVersion the version of 15 puzzle solver.
   *
   * @author <a href="http://www.linkedin.com/pub/macy-wong/46/550/37b/"
   *            target="_blank">Meisze Wong (linkedin)</a>
   */
  enum SolverVersion {
    /**
     * Solver version prime with enhancements only.
     */
    PRIME,

    /**
     * Solver version ultimate with boost estimate from stored boards.
     */
    OPTIMUM;

    /**
      * Returns the boolean represent the instance object is ULTIMATE.
      *
      * @return boolean represent the instance object is ULTIMATE
      */
    public boolean isOptimum() {
      return this == OPTIMUM;
    }

    /**
     * Returns the boolean represent the instance object is PRIME.
     *
     * @return boolean represent the instance object is PRIME
     */
    public boolean isPrime() {
      return this == PRIME;
    }

    @Override
    public String toString() {
      if (this == PRIME) {
        return "Prime";
      }
      return "Optimum";
    }
  }

  /**
   * ApplicationMode determine the type of application .
   *
   * @author <a href="http://www.linkedin.com/pub/macy-wong/46/550/37b/"
   *            target="_blank">Meisze Wong (linkedin)</a>
   */
  enum ApplicationMode {
    /**
     * Application mode for console application.
     */
    CONSOLE,

    /**
     * Application mode for GUI application.
     */
    GUI,

    /**
     * Application mode for SYSTEM application.
     */
    SYSTEM
  }
}
