package mwong.myprojects.fifteenpuzzle.solver;

/**
 * Rotations that can be used.
 * <li>{@link #RST}</li>
 * <li>{@link #CW}</li>
 * <li>{@link #CCW}</li>
 *
 * @author Meisze Wong
 *         www.linkedin.com/pub/macy-wong/46/550/37b/
 */
enum Rotation {
    /**
     * Rotation (RST) Reset.
     */
    RST(0),

    /**
     * Rotation (CW) Clockwise.
     */
    CW(1),

    /**
     * Rotation (CCW) CounterClockwise.
     */
    CCW(2);

    private final int val;

    /**
     * Initializes a Rotation reference type.
     */
    Rotation(int val) {
        this.val = val;
    }

    /**
     * Returns the value of current rotation.
     *
     * @return value of current rotation
     */
    int getValue() {
        return val;
    }
}

