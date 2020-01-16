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
    private static final boolean REMOTE_SERVER = true;
    private static final boolean LINEAR_CONFLICT_ON = SolverConstants.linearConflictOn();
    private static final boolean ADVANCED_VERSION_ON = SolverConstants.advancedVersionOn();
    private static final boolean TIMEOUT_ON = SolverConstants.isOnSwitch();
    private static final boolean MESSAGE_ON = SolverConstants.isOnSwitch();
    private static final byte PUZZLE_SIZE = SolverConstants.getPuzzleSize();

    /**
     * Returns the boolean value of the remote connection in use.
     *
     * @return boolean value of remote connection version in use
     */
    static final boolean remoteServer() {
        return REMOTE_SERVER;
    }

    /**
     * Returns the boolean value of the remote connection in use.
     *
     * @return boolean value of remote connection version in use
     */
    static final boolean standalone() {
        return !REMOTE_SERVER;
    }

    /**
     * Returns the boolean value of the LinearConflict in use.
     *
     * @return boolean value of the LinearConflict in use
     */
    public static final boolean linearConflictOn() {
        return LINEAR_CONFLICT_ON;
    }

    /**
     * Returns the boolean value of the LinearConflict in use.
     *
     * @return boolean value of the LinearConflict in use
     */
    public static final boolean linearConflictOff() {
        return !LINEAR_CONFLICT_ON;
    }

    /**
     * Returns the boolean value of the Advanced version in use.
     *
     * @return boolean value of Advanced version in use
     */
    static final boolean advancedVersion() {
        return ADVANCED_VERSION_ON;
    }

    /**
     * Returns the boolean value of the Advanced version in use.
     *
     * @return boolean value of Advanced version in use
     */
    static final boolean standardVersion() {
        return !ADVANCED_VERSION_ON;
    }

    /**
     * Returns the boolean value of the timeout feature in use.
     *
     * @return boolean value of timeout feature in use
     */
    public static final boolean timeoutOn() {
        return TIMEOUT_ON;
    }

    /**
     * Returns the boolean value of the timeout feature in use.
     *
     * @return boolean value of timeout feature in use
     */
    public static final boolean timeoutOff() {
        return !TIMEOUT_ON;
    }

    /**
     * Returns the boolean value of the print message feature in use.
     *
     * @return boolean value of the print message feature in use
     */
    public static final boolean messageOn() {
        return MESSAGE_ON;
    }

    /**
     * Returns the boolean value of the print message feature in use.
     *
     * @return boolean value of the print message feature in use
     */
    public static final boolean messageOff() {
        return !MESSAGE_ON;
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