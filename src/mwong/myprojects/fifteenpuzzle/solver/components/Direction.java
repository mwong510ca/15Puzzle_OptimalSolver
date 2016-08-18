package mwong.myprojects.fifteenpuzzle.solver.components;

/**
 * Directions that can be used.
 * <li>{@link #RIGHT}</li>
 * <li>{@link #DOWN}</li>
 * <li>{@link #LEFT}</li>
 * <li>{@link #UP}</li>
 * <li>{@link #NONE}</li>
 *
 * @author   Meisze Wong
 *           www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public enum Direction {
    /**
     *  Direction Right.
     */
    RIGHT(0),

    /**
     *  Direction Down.
     */
    DOWN(1),

    /**
     *  Direction Left.
     */
    LEFT(2),

    /**
     *  Direction Up.
     */
    UP(3),

    /**
     *  Direction None.
     */
    NONE(-1);

    private final int val;

    /**
     * Initializes a Direction reference type.
     */
    Direction(int val) {
        this.val = val;
    }

    /**
     *  Returns the value of current direction.
     *
     *  @return value of current direction
     */
    public int getValue() {
        return val;
    }

    /**
     *  Returns the opposite direction of current direction.
     *
     *  @return direction is the opposite of current direction
     */
    public Direction oppositeDirection() {
        switch (this) {
            case RIGHT : return LEFT;
            case DOWN : return UP;
            case LEFT : return RIGHT;
            case UP : return DOWN;
            default : return NONE;
        }
    }

    /**
     *  Returns the symmetry direction of current direction.
     *
     *  @return direction is the symmetry of current direction
     */
    public Direction symmetryDirection() {
        switch (this) {
            case RIGHT : return DOWN;
            case DOWN : return RIGHT;
            case LEFT : return UP;
            case UP : return LEFT;
            default : return NONE;
        }
    }
}

