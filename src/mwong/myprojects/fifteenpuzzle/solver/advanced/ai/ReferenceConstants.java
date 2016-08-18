package mwong.myprojects.fifteenpuzzle.solver.advanced.ai;

import mwong.myprojects.fifteenpuzzle.solver.components.PuzzleConstants;

public class ReferenceConstants {
	// ReferenceBoard 
    private static final int PUZZLE_SIZE = PuzzleConstants.getSize();
	private static final byte[] ROTATE_90_POS =
        { 12, 8, 4, 0, 13, 9, 5, 1, 14, 10, 6, 2, 15, 11, 7, 3};
    private static final byte[] ROTATE_180_POS =
        { 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0};
    private static final byte[] REFERENCE_LOOKUP =
        { 0, 1, 3, 0, 3, 2, 2, 1, 3, 2, 2, 3, 0, 1, 1, 0};
    private static final byte[] REFERENCE_GROUP =
        { 2, 2, 1, 1, 2, 2, 1, 1, 3, 3, 0, 0, 3, 3, 0, 0};
    private static final boolean SYMMETRY = true;
        
    // ReferenceMoves
    private static final byte[] STATUS_BIT = {1, 2, 4, 8};
    private static final byte STATUS_COMPLETED = 15;
    // store as short in 16 bits, 2 bits per move
    private static final byte NUM_PARTIAL_MOVES = 8;
    
	/**
	 * @return the rotate90Pos
	 */
	public static final byte[] getRotate90Pos() {
		return ROTATE_90_POS;
	}
	/**
	 * @return the rotate180Pos
	 */
	public static final byte[] getRotate180Pos() {
		return ROTATE_180_POS;
	}
	/**
	 * @return the referenceLookup
	 */
	public static final byte[] getReferenceLookup() {
		return REFERENCE_LOOKUP;
	}
	/**
	 * @return the referenceLookup
	 */
	public static final byte getReferenceLookup(int zeroPos) {
		return REFERENCE_LOOKUP[zeroPos];
	}
	/**
	 * @return the referenceGroup
	 */
	public static final byte[] getReferenceGroup() {
		return REFERENCE_GROUP;
	}
	/**
	 * @return the referenceGroup
	 */
	public static final byte getReferenceGroup(int zeroPos) {
		return REFERENCE_GROUP[zeroPos];
	}
	/**
	 * @return the symmetry
	 */
	public static final boolean isSymmetry() {
		return SYMMETRY;
	}
	/**
	 * @return the puzzleSize
	 */
	public static final int getPuzzleSize() {
		return PUZZLE_SIZE;
	}
	/**
	 * @return the status
	 */
	public static final byte[] getStatusBit() {
		return STATUS_BIT;
	}
	/**
	 * @return the statusComplete
	 */
	public static final byte getStatusCompleted() {
		return STATUS_COMPLETED;
	}
	/**
	 * @return the numPartialMoves
	 */
	public static final byte getNumPartialMoves() {
		return NUM_PARTIAL_MOVES;
	}
}