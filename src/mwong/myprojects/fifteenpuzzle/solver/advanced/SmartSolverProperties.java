package mwong.myprojects.fifteenpuzzle.solver.advanced;

import mwong.myprojects.fifteenpuzzle.solver.advanced.ai.ReferenceProperties;;

public class SmartSolverProperties {
	private static final byte[] REFERENCE_LOOKUP = ReferenceProperties.getReferenceLookup();
    private static final byte[] REFERENCE_GROUP = ReferenceProperties.getReferenceGroup();
    private static final boolean SYMMETRY = ReferenceProperties.isSymmetry();
    private static final byte NUM_PARTIAL_MOVES = 8;
    private static final byte REFERENCE_CUTOFF = 8;
    
	/**
	 * @return the referenceLookup
	 */
	static final byte[] getReferenceLookup() {
		return REFERENCE_LOOKUP;
	}
	/**
	 * @return the referenceLookup
	 */
	static final byte getReferenceLookup(int zeroPos) {
		return REFERENCE_LOOKUP[zeroPos];
	}
	/**
	 * @return the referenceGroup
	 */
	static final byte[] getReferenceGroup() {
		return REFERENCE_GROUP;
	}
	/**
	 * @return the referenceGroup
	 */
	static final byte getReferenceGroup(int zeroPos) {
		return REFERENCE_GROUP[zeroPos];
	}
	/**
	 * @return the symmetry
	 */
	static final boolean isSymmetry() {
		return SYMMETRY;
	}
	/**
	 * @return the numPartialMoves
	 */
	static final byte getNumPartialMoves() {
		return NUM_PARTIAL_MOVES;
	}
	/**
	 * @return the referenceCutoff
	 */
	static final byte getReferenceCutoff() {
		return REFERENCE_CUTOFF;
	}
}