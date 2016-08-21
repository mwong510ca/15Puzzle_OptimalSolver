package mwong.myprojects.fifteenpuzzle.solver.advanced.ai;

import mwong.myprojects.fifteenpuzzle.solver.HeuristicOptions;
import mwong.myprojects.fifteenpuzzle.solver.SolverConstants;
import mwong.myprojects.fifteenpuzzle.solver.components.PuzzleConstants;

/**
 * ReferenceConstants contains all constant variables of reference collection.
 *
 * @author   Meisze Wong
 *           www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public class ReferenceConstants {
    private static final String CORE_SOLVER_CLASS_NAME = "SmartSolverPD";
    private static final HeuristicOptions CORE_HEURISTIC = HeuristicOptions.PD78;
    private static final boolean ON_SWITCH = SolverConstants.isOnSwitch();

    // Constants for ReferenceBoard
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

    // Constants for ReferenceMoves
    private static final byte[] STATUS_BIT = {1, 2, 4, 8};
    private static final byte STATUS_COMPLETED = 15;
    // store as short in 16 bits, 2 bits per move
    private static final byte NUM_PARTIAL_MOVES = 8;

    /**
     * Returns the String of class name of Solver for ReferenceAccumulator.
     *
     * @eturns the String of class name of Solver for ReferenceAccumulator
     */
    static final String getCoreSolverClassName() {
        return CORE_SOLVER_CLASS_NAME;
    }

    /**
     * Returns the HeuristicOption that ReferenceAccumulator being use.
     *
     * @return HeuristicOption that ReferenceAccumulator being use
     */
    static final HeuristicOptions getCoreHeuristic() {
        return CORE_HEURISTIC;
    }

    /**
     * Returns the byte array of symmetry tiles conversion.
     *
     * @return byte array of symmetry value
     */
    static final boolean isOnSwitch() {
        return ON_SWITCH;
    }

    /**
     * Return the byte array of rotate 90 degree clockwise conversion.
     *
     * @return byte array of rotate 90 degree clockwise conversion
     */
    public static final byte[] getRotate90Pos() {
        return ROTATE_90_POS;
    }

    /**
     * Return the byte array of rotate 180 degree conversion.
     *
     * @return byte array of rotate 180 degree clockwise conversion
     */
    public static final byte[] getRotate180Pos() {
        return ROTATE_180_POS;
    }

    /**
     * Return the byte array of reference lookup key.
     *
     * @return byte array of reference lookup key
     */
    public static final byte[] getReferenceLookup() {
        return REFERENCE_LOOKUP;
    }

    /**
     * Return the byte value of reference lookup key of the given zero position.
     *
     * @param zeroPos the given zero position
     * @return byte value of reference lookup key of the given zero position
     */
    public static final byte getReferenceLookup(int zeroPos) {
        return REFERENCE_LOOKUP[zeroPos];
    }

    /**
     * Return the byte array of reference group.
     *
     * @return byte array of reference group
     */
    public static final byte[] getReferenceGroup() {
        return REFERENCE_GROUP;
    }

    /**
     * Return the byte value of reference group of the given zero position.
     *
     * @param zeroPos the given zero position
     * @return byte value of reference group of the given zero position
     */
    public static final byte getReferenceGroup(int zeroPos) {
        return REFERENCE_GROUP[zeroPos];
    }

    /**
     * Returns the boolean value represents the symmetry conversion.
     *
     * @return boolean value represents the symmetry conversion
     */
    public static final boolean isSymmetry() {
        return SYMMETRY;
    }

    /**
     * Returns the integer value of the puzzle size.
     *
     * @return integer value of the puzzle size
     */
    public static final int getPuzzleSize() {
        return PUZZLE_SIZE;
    }

    /**
     * Returns the byte array of status in bits.
     *
     * @return byte array of status in bits
     */
    public static final byte[] getStatusBit() {
        return STATUS_BIT;
    }

    /**
     * Returns the byte value represent the status of completion.
     *
     * @return byte value represent the status of completion
     */
    public static final byte getStatusCompleted() {
        return STATUS_COMPLETED;
    }

    /**
     * Returns the byte value of number of partial moves has stored.
     *
     * @return byte value of number of partial moves
     */
    public static final byte getNumPartialMoves() {
        return NUM_PARTIAL_MOVES;
    }
}