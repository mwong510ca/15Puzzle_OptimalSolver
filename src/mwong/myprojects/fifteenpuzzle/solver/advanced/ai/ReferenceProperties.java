package mwong.myprojects.fifteenpuzzle.solver.advanced.ai;

import mwong.myprojects.fifteenpuzzle.solver.components.PuzzleProperties;

public class ReferenceProperties {
	// ReferenceBoard 
    private static final int PUZZLE_SIZE = PuzzleProperties.getSize();
	private static final byte[] ROTATE_90_POS =
        { 12, 8, 4, 0, 13, 9, 5, 1, 14, 10, 6, 2, 15, 11, 7, 3};
    private static final byte[] ROTATE_180_POS =
        { 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0};
    private static final byte[] REFERENCE_LOOKUP =
        { 0, 1, 3, 0, 3, 2, 2, 1, 3, 2, 2, 3, 0, 1, 1, 0};
    private static final byte[] REFERENCE_GROUP =
        { 2, 2, 1, 1, 2, 2, 1, 1, 3, 3, 0, 0, 3, 3, 0, 0};
    
    // ReferenceMoves
    private static final byte[] STATUS_BIT = {1, 2, 4, 8};
    private static final byte STATUS_COMPLETED = 15;
    // store as short in 16 bits, 2 bits per move
    private static final byte NUM_PARTIAL_MOVES = 8;
    private static final boolean SYMMETRY = true;
    
    //ReferenceAccumulator
    private static final int DEFAULT_CUTOFF_LIMIT = 10;
    private static final double DEFAULT_CUTOFF_BUFFER = 0.95;
    // selected reference boards for default setting, total 40 after generation.
    private static final byte[][][] DEFAULT_BOARDS = { 
            {{ 0, 15,  8,  3, 12, 11,  7,  4, 14, 10,  6,  5,  9, 13,  2,  1}, {0,  70}},
            {{ 6,  5,  9, 13,  2,  1, 10, 14,  3,  7,  0, 15,  4,  8, 12, 11}, {10, 72}},
            {{ 0, 12,  8,  4, 15, 11,  7,  3, 14, 10,  6,  2, 13,  9,  5,  1}, {0,  72}},
            {{ 6,  5, 14, 13,  2,  1, 10,  9,  8,  7,  0, 15,  4,  3, 12, 11}, {10, 70}},
            {{ 0,  5,  9, 13,  2,  1, 10, 14,  3,  7, 11, 15,  4,  8, 12,  6}, {0,  72}},
            {{ 0, 12,  7,  4, 15, 11,  8,  3, 10, 14,  6,  2, 13,  9,  5,  1}, {0,  70}},
            {{ 0, 15,  8,  7, 12, 11,  4,  3, 14, 13,  6,  5, 10,  9,  2,  1}, {0,  72}},
            {{11, 12,  8,  3, 15,  0,  7,  4, 14, 10,  6,  5,  9, 13,  2,  1}, {5,  66}},
            {{ 1,  5,  9, 13,  2,  6, 10, 14,  3,  7, 11, 15,  4,  8, 12,  0}, {15, 72}},
            {{ 0, 15,  8,  4, 12, 11,  7,  5, 14, 10,  6,  3, 13,  2,  9,  1}, {0,  70}},
            {{ 1, 10, 14, 13,  7,  6,  5,  9,  8,  2, 11, 15,  4,  3, 12,  0}, {15, 72}},
            {{ 0, 12,  8,  7, 15, 11,  4,  3, 14, 13,  6,  2, 10,  9,  5,  1}, {0,  72}},
            {{ 6,  5, 14, 13,  2,  1, 10,  9,  8,  7, 11, 12,  4,  3, 15,  0}, {15, 70}},
            {{ 0,  5,  9, 13,  2,  6, 10, 14,  3,  7,  1, 15,  4,  8, 12, 11}, {0,  72}},
            {{ 6,  5, 13,  9,  2,  1, 10, 14,  4,  7, 11, 12,  3,  8, 15,  0}, {15, 68}},
            {{ 6,  5,  9, 13,  2,  1, 10, 14,  3,  7, 11, 12,  4,  8, 15,  0}, {15, 70}},
            {{11, 15,  8,  3, 12,  0,  7,  4, 14, 10,  6,  2,  9, 13,  5,  1}, {5,  66}},
            {{ 1, 10,  9, 13,  7,  0,  5, 14,  3,  2,  6, 15,  4,  8, 12, 11}, {5,  70}},

            {{ 0, 15,  9, 13, 11, 12, 10, 14,  3,  7,  6,  2,  4,  8,  5,  1}, { 0, 80}},
            {{ 0, 12,  9, 13, 15, 11, 10, 14,  8,  3,  6,  2,  4,  7,  5,  1}, { 0, 80}},
            {{ 0, 12,  9, 13, 15, 11, 10, 14,  7,  8,  6,  2,  4,  3,  5,  1}, { 0, 80}},
            {{ 0, 12,  9, 13, 15,  8, 10, 14,  11, 7,  6,  2,  4,  3,  5,  1}, { 0, 80}},
            {{ 0, 12,  9, 13, 15, 11, 10, 14,  3,  7,  5,  6,  4,  8,  2,  1}, { 0, 80}},
            {{ 0, 12,  9, 13, 15, 11, 10, 14,  7,  8,  5,  6,  4,  3,  2,  1}, { 0, 80}},
            {{ 0, 12,  9, 13, 15, 11, 10, 14,  3,  7,  6,  2,  4,  8,  5,  1}, { 0, 80}},
            {{ 0, 12,  9, 13, 15, 11, 14, 10,  3,  8,  6,  2,  4,  7,  5,  1}, { 0, 80}},
            {{ 0, 12, 10, 13, 15, 11,  9, 14,  7,  3,  6,  2,  4,  8,  5,  1}, { 0, 80}},
            {{ 0, 12, 14, 13, 15, 11,  9, 10,  8,  3,  6,  2,  4,  7,  5,  1}, { 0, 80}},
            {{ 0, 12, 10, 13, 15, 11, 14,  9,  7,  8,  6,  2,  4,  3,  5,  1}, { 0, 80}},
            {{ 0, 15,  8, 13, 12, 11,  9, 10, 14,  3,  6,  2,  4,  7,  5,  1}, { 0, 78}},
            {{11, 15,  9, 13, 12,  0, 10, 14,  3,  7,  6,  2,  4,  8,  5,  1}, { 5, 78}},
            {{ 0, 12,  5, 13, 15,  6, 10,  9,  2,  7, 11, 14,  4,  3,  8,  1}, { 0, 78}},
            {{ 0, 12,  8, 13, 15, 11,  7,  9, 14, 10,  6,  2,  4,  3,  5,  1}, { 0, 78}},
            {{ 0, 14, 15, 13,  8, 11, 10,  5, 12,  7,  6,  9,  4,  2,  3,  1}, { 0, 78}}
    };
    
    
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
	/**
	 * @return the symmetry
	 */
	public static final boolean isSymmetry() {
		return SYMMETRY;
	}
	/**
	 * @return the defaultCutoffLimit
	 */
	public static final int getDefaultCutoffLimit() {
		return DEFAULT_CUTOFF_LIMIT;
	}
	/**
	 * @return the defaultCutoffBuffer
	 */
	public static final double getDefaultCutoffBuffer() {
		return DEFAULT_CUTOFF_BUFFER;
	}
	/**
	 * @return the defaultBoards
	 */
	public static final byte[][][] getDefaultBoards() {
		return DEFAULT_BOARDS;
	}
}