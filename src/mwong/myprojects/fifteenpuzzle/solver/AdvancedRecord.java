package mwong.myprojects.fifteenpuzzle.solver;

import mwong.myprojects.fifteenpuzzle.solver.components.Direction;

/**
 * AdvancedRecord is the data type store the number of moves and partial solution.
 *
 * <p>Dependencies : Direction.java
 *
 * @author Meisze Wong
 *         www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public class AdvancedRecord {
    private byte estimate;
    private Direction[] partialMoves;
    private boolean hasPartialMoves;

    AdvancedRecord(byte estimate) {
        this.estimate = estimate;
        hasPartialMoves = false;
        partialMoves = null;
    }

    AdvancedRecord(byte estimate, Direction[] partialMoves) {
        this.estimate = estimate;
        hasPartialMoves = true;
        this.partialMoves = partialMoves;
    }

    /**
     * Returns the byte values of number of moves.
     *
     * @return byte values of number of moves
     */
    final byte getEstimate() {
        return estimate;
    }

    /**
     * Returns the Directions array of partial solution from record.
     *
     * @return Directions array of partial solution
     */
    final Direction[] getPartialMoves() {
        return partialMoves;
    }

    /**
     * Returns the boolean represent record has partial solution.
     *
     * @return boolean represent record has partial solution
     */
    public final boolean hasPartialMoves() {
        return hasPartialMoves;
    }
}