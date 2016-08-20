package mwong.myprojects.fifteenpuzzle.solver.advanced;

import mwong.myprojects.fifteenpuzzle.solver.advanced.ai.ReferenceConstants;

/**
 * SmartSolverConstants contains all constant variables for smart solver.
 *
 * <p>Dependencies : ReferenceConstants.java
 *
 * @author   Meisze Wong
 *           www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public class SmartSolverConstants {
    private static final byte[] REFERENCE_LOOKUP = ReferenceConstants.getReferenceLookup();
    private static final byte[] REFERENCE_GROUP = ReferenceConstants.getReferenceGroup();
    private static final boolean SYMMETRY = ReferenceConstants.isSymmetry();
    private static final byte NUM_PARTIAL_MOVES = ReferenceConstants.getNumPartialMoves();
    private static final byte REFERENCE_CUTOFF = 30;

    /**
     * Returns the byte array of the reference lookup key.
     *
     * @return byte array of the reference lookup key
     */
    static final byte[] getReferenceLookup() {
        return REFERENCE_LOOKUP;
    }

    /**
     * Returns the byte value of the reference lookup key of the given zero position.
     *
     * @param zeroPos the given zero position
     * @return byte value of the reference lookup key of the given zero position
     */
    static final byte getReferenceLookup(int zeroPos) {
        return REFERENCE_LOOKUP[zeroPos];
    }

    /**
     * Returns the byte array of the reference group.
     *
     * @return byte array of the reference group
     */
    static final byte[] getReferenceGroup() {
        return REFERENCE_GROUP;
    }

    /**
     * Returns the byte value of the reference group of the given zero position.
     *
     * @param zeroPos the given zero position
     * @return byte value of the reference group of the given zero position
     */
    static final byte getReferenceGroup(int zeroPos) {
        return REFERENCE_GROUP[zeroPos];
    }

    /**
     * Returns the boolean represent the symmetry of the reference board.
     *
     * @return boolean represent the symmetry of the reference board
     */
    static final boolean isSymmetry() {
        return SYMMETRY;
    }

    /**
     * Returns the byte value of the number of partial solution.
     *
     * @return byte value of the number of partial solution
     */
    static final byte getNumPartialMoves() {
        return NUM_PARTIAL_MOVES;
    }

    /**
     * Returns the byte value of the cutoff estimate for advanced search.
     *
     * @return byte value of the cutoff estimate for advanced search
     */
    static final byte getReferenceCutoff() {
        return REFERENCE_CUTOFF;
    }
}