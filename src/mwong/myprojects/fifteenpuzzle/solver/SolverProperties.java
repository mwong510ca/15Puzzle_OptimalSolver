package mwong.myprojects.fifteenpuzzle.solver;

import mwong.myprojects.fifteenpuzzle.solver.components.PuzzleProperties;

public class SolverProperties {
    private static final byte PUZZLE_SIZE = PuzzleProperties.getSize();
    private static final byte ROW_SIZE = PuzzleProperties.getRowSize();
    private static final byte MAX_MOVES = PuzzleProperties.getMaxMoves();
    private static final byte END_OF_SEARCH = (byte) (MAX_MOVES + 1);
    private static final int DEFAULT_TIMEOUT_LIMIT = 10;
    private static final boolean ON_SWITCH = true;
    private static final boolean TAG_ADVANCED = true;
    private static final boolean TAG_SEARCH = true;
    private static final boolean TAG_LINEAR_CONFLICT = true;
    private static final byte[] SYMMETRY_POS =
            PuzzleProperties.getSymmetryPos();
    private static final byte[] SYMMETRY_VAL =
            PuzzleProperties.getSymmetryVal();
    
    /**
     * Return the puzzleSize.
     *
     * @return the puzzleSize
     */
    public static final byte getPuzzleSize() {
        return PUZZLE_SIZE;
    }

    /**
     * Return the rowSize.
     *
     * @return the rowSize
     */
    public static final byte getRowSize() {
        return ROW_SIZE;
    }

    /**
     * Return the maxMoves.
     *
     * @return the maxMoves
     */
    public static final byte getMaxMoves() {
        return MAX_MOVES;
    }

    /**
     * Return the endOfSearch.
     *
     * @return the endOfSearch
     */
    public static final byte getEndOfSearch() {
        return END_OF_SEARCH;
    }

    /**
     * Return the defaultTimeoutLimit.
     *
     * @return the defaultTimeoutLimit
     */
    public static final int getDefaultTimeoutLimit() {
        return DEFAULT_TIMEOUT_LIMIT;
    }

    /**
     * Return the onSwitch.
     *
     * @return the onSwitch
     */
    public static final boolean isOnSwitch() {
        return ON_SWITCH;
    }

    /**
	 * @return the tagAdvanced
	 */
	public static final boolean isTagAdvanced() {
		return TAG_ADVANCED;
	}

	/**
     * Return the tagSearch.
     *
     * @return the tagSearch
     */
    public static final boolean isTagSearch() {
        return TAG_SEARCH;
    }

    /**
	 * @return the tagLinearConflict
	 */
	public static final boolean isTagLinearConflict() {
		return TAG_LINEAR_CONFLICT;
	}

	/**
     * Return the symmetryPos.
     *
     * @return the symmetryPos
     */
    public static final byte[] getSymmetryPos() {
        return SYMMETRY_POS;
    }

    /**
     * Return the symmetryVal.
     *
     * @return the symmetryVal
     */
    public static final byte[] getSymmetryVal() {
        return SYMMETRY_VAL;
    }
}