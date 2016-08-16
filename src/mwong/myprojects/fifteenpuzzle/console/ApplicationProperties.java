package mwong.myprojects.fifteenpuzzle.console;

import mwong.myprojects.fifteenpuzzle.solver.SolverProperties;
import mwong.myprojects.fifteenpuzzle.solver.standard.SolverMD;

public class ApplicationProperties {
	private static final boolean TAG_LINEAR_CONFLICT = SolverMD.isTagLinearConflict();
	private static final boolean TAG_ADVANCED = SolverProperties.isTagAdvanced();
	private static final boolean TIMEOUT_ON = SolverProperties.isOnSwitch();
	private static final boolean MESSAGE_ON = SolverProperties.isOnSwitch();
	private static final byte PUZZLE_SIZE = SolverProperties.getPuzzleSize();
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