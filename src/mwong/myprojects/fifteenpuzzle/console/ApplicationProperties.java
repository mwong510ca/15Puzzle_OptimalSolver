package mwong.myprojects.fifteenpuzzle.console;

import mwong.myprojects.fifteenpuzzle.solver.SolverConstants;

public class ApplicationProperties {
	private static final boolean TAG_LINEAR_CONFLICT = SolverConstants.isTagLinearConflict();
	private static final boolean TAG_ADVANCED = SolverConstants.isTagAdvanced();
	private static final boolean TIMEOUT_ON = SolverConstants.isOnSwitch();
	private static final boolean MESSAGE_ON = SolverConstants.isOnSwitch();
	private static final byte PUZZLE_SIZE = SolverConstants.getPuzzleSize();
	/**
	 * @return the tagLinearConflict
	 */
	public static final boolean isTagLinearConflict() {
		return TAG_LINEAR_CONFLICT;
	}
	/**
	 * @return the tagAdvanced
	 */
	static final boolean isTagAdvanced() {
		return TAG_ADVANCED;
	}
	/**
	 * @return the timeoutOn
	 */
	public static final boolean isTimeoutOn() {
		return TIMEOUT_ON;
	}
	/**
	 * @return the messageOn
	 */
	public static final boolean isMessageOn() {
		return MESSAGE_ON;
	}
	/**
	 * @return the puzzleSize
	 */
	public static final byte getPuzzleSize() {
		return PUZZLE_SIZE;
	}
	
	
}