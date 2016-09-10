package mwong.myprojects.fifteenpuzzle.console;

import mwong.myprojects.fifteenpuzzle.solver.SolverConstants;

/**
 * ApplicationConstants contains all constant variables for any console application.
 *
 * <p>Dependencies : SolverConstants.java
 *
 * @author Meisze Wong
 *         www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public class ApplicationConstants {
    private static final boolean TAG_LINEAR_CONFLICT = SolverConstants.isTagLinearConflict();
    private static final boolean TAG_ADVANCED = SolverConstants.isTagAdvanced();
    private static final boolean TIMEOUT_ON = SolverConstants.isOnSwitch();
    private static final boolean MESSAGE_ON = SolverConstants.isOnSwitch();
    private static final byte PUZZLE_SIZE = SolverConstants.getPuzzleSize();

    /**
     * Returns the boolean value of the LinearConflict in use.
     *
     * @return boolean value of the LinearConflict in use
     */
    public static final boolean isTagLinearConflict() {
        return TAG_LINEAR_CONFLICT;
    }

    /**
     * Returns the boolean value of the Advanced version in use.
     *
     * @return boolean value of Advanced version in use
     */
    static final boolean isTagAdvanced() {
        return TAG_ADVANCED;
    }

    /**
     * Returns the boolean value of the timeout feature in use.
     *
     * @return boolean value of timeout feature in use
     */
    public static final boolean isTimeoutOn() {
        return TIMEOUT_ON;
    }

    /**
     * Returns the boolean value of the print message feature in use.
     *
     * @return boolean value of the print message feature in use
     */
    public static final boolean isMessageOn() {
        return MESSAGE_ON;
    }

    /**
     * Returns the byte value of the puzzle size.
     *
     * @return byte value of  puzzle size
     */
    public static final byte getPuzzleSize() {
        return PUZZLE_SIZE;
    }
}