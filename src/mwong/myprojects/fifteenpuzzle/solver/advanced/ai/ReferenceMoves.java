/****************************************************************************
 *  @author   Meisze Wong
 *            www.linkedin.com/pub/macy-wong/46/550/37b/
 *
 *  Compilation  : javac AdvancedMoves.java
 *  Dependencies : Board.java, SolverPD.java
 *
 *  A data type of preset moves and partial solution of reference board.
 *
 ****************************************************************************/

package mwong.myprojects.fifteenpuzzle.solver.advanced.ai;

import mwong.myprojects.fifteenpuzzle.solver.components.Board;
import mwong.myprojects.fifteenpuzzle.solver.components.Direction;
import mwong.myprojects.fifteenpuzzle.solver.standard.SolverPD;

public class ReferenceMoves {
	private final byte[] statusBit;
	private final byte statusCompleted;
	private final int numPartialMoves;
	private final boolean symmetry;
	
    byte[] moves = new byte[4];
    short[] initMoves = new short[4];
    byte status = 0;

    private ReferenceMoves() {
    	statusBit = ReferenceProperties.getStatusBit();
    	statusCompleted = ReferenceProperties.getStatusCompleted();
    	numPartialMoves = ReferenceProperties.getNumPartialMoves();
    	symmetry = ReferenceProperties.isSymmetry();
    }
    
    // initializes AdvancedMoves object with given zero position with given
    // unverified estimate and no partial solution
    ReferenceMoves(byte zeroPos, byte steps) {
    	this();
        int refLookup = ReferenceProperties.getReferenceLookup(zeroPos);
        moves[refLookup] = steps;
        int count = 1;
        while (refLookup - count > -1) {
            moves[refLookup - count] = (byte) (steps - count);
            count++;
        }
        count = 1;
        while (refLookup + count < 4) {
            moves[refLookup + count] = (byte) (steps - count);
            count++;
        }
    }

    // initializes AdvancedMoves object with stored variables
    ReferenceMoves(byte[] moves, short[] initMoves, byte status) {
    	this();
        this.moves = moves.clone();
        this.initMoves = initMoves.clone();
        this.status = status;
    }

    // while AdvancedMoves object already exists, update unverified moves and
    // partial solutions only with given values.
    void updateMoves(byte[] steps, short[] initMoves, byte status2) {
        status |= status2;
        for (int lookup = 0; lookup < 4; lookup++) {
            if (moves[lookup] < steps[lookup]) {
                moves[lookup] = steps[lookup];
                this.initMoves[lookup] = initMoves[lookup];
            } else if (this.initMoves[lookup] == 0) {
                this.initMoves[lookup] = initMoves[lookup];
            }
        }
    }

    // update moves and partial solution at the given lookup key
    void updateSolution(byte lookup, byte steps, Direction[] solution, boolean symmetry) {
        status |= statusBit[lookup];
        moves[lookup] = steps;
        initMoves[lookup] = initialMoves2value(solution, symmetry);
    }

    // update a full set of moves and partial solutions with a given reference board
    // and a solverPD object
    void updateSolutions(ReferenceBoard advBoard, SolverPD solver) {
    	assert solver != null : "SolverPD is null";
        byte group = advBoard.group;
        byte[] blocks = advBoard.getTiles().clone();
        for (int lookup = 0; lookup < 4; lookup++) {
            if ((status & statusBit[lookup]) == 0) {
                Board board = new Board(blocks);
                solver.findOptimalPath(board, moves[lookup]);
                assert solver.solution() != null : "No solution from updateSolutions function";
                moves[lookup] = solver.moves();
                initMoves[lookup] = initialMoves2value(solver.solution(), !symmetry);
            }
            shiftOne(blocks, group, lookup);
        }
        status = statusCompleted;
    }

    // convert the first 8 directions from the given array into short value
    private short initialMoves2value(Direction[] dir, boolean isSymmetry) {
        short value = 0;
        for (int i = numPartialMoves; i > 1; i--) {
            if (isSymmetry) {
                value |= dir[i].symmetryDirection().getValue();
            } else {
                value |= dir[i].getValue();
            }
            value <<= 2;
        }
        if (isSymmetry) {
            value |= dir[1].symmetryDirection().getValue();
        } else {
            value |= dir[1].getValue();
        }
        return value;
    }

    /**
     * Returns the Direction arrays of the given lookup key.  Restore the
     * solution key for the first 8 moves in Direction array.
     *
     * @param lookup the give lookup key of the reference board
     * @param isSymmetry the type of restored directions
     * @return the Direction arrays of the given lookup key
     */
    public Direction[] getInitialMoves(int lookup, boolean isSymmetry) {
        short value = initMoves[lookup];
        Direction[] movesDir = new Direction[numPartialMoves];
        for (int i = 0; i < numPartialMoves; i++) {
            int dir = value & 0x03;
            if (dir == 0) {
                movesDir[i] = Direction.RIGHT;
            } else if (dir == 1) {
                movesDir[i] = Direction.DOWN;
            } else if (dir == 2) {
                movesDir[i] = Direction.LEFT;
            } else if (dir == 3) {
                movesDir[i] = Direction.UP;
            }
            value >>>= 2;
        }

        if (isSymmetry) {
            for (int i = 0; i < numPartialMoves; i++) {
                movesDir[i] = movesDir[i].symmetryDirection();
            }
        }
        return movesDir;
    }

    /**
     * Returns the boolean represents the given lookup key has partial solution.
     *
     * @param lookup the give lookup key of the reference board
     * @return boolean represents the given lookup key has partial solution
     */
    public boolean hasInitialMoves(int lookup) {
        return initMoves[lookup] != 0;
    }

    // shift the given blocks to next lookup position
    private void shiftOne(byte[] blocks, int group, int lookup) {
        if (group == 2) {
            if (lookup == 0) {
                blocks[0] = blocks[1];
                blocks[1] = 0;
            } else if (lookup == 1) {
                blocks[1] = blocks[5];
                blocks[5] = 0;
            } else if (lookup == 2) {
                blocks[5] = blocks[4];
                blocks[4] = 0;
            }
        } else if (group == 0) {
            if (lookup == 0) {
                blocks[15] = blocks[14];
                blocks[14] = 0;
            } else if (lookup == 1) {
                blocks[14] = blocks[10];
                blocks[10] = 0;
            } else if (lookup == 2) {
                blocks[10] = blocks[11];
                blocks[11] = 0;
            }
        } else if (group == 1) {
            if (lookup == 0) {
                blocks[3] = blocks[7];
                blocks[7] = 0;
            } else if (lookup == 1) {
                blocks[7] = blocks[6];
                blocks[6] = 0;
            } else if (lookup == 2) {
                blocks[6] = blocks[2];
                blocks[2] = 0;
            }
        }
    }

    /**
     * Return the boolean represents the full set of moves and partial solutions
     * has been verified.
     *
     * @return boolean represents the full set has been verified
     */
    public boolean isCompleted() {
        return status == statusCompleted;
    }

    /**
     * Return the byte of estimate of the reference board.
     *
     * @return byte of estimate of the reference board
     */
    public byte getEstimate() {
        return moves[0];
    }

    /**
     * Return the byte of estimate of the given lookup position of reference board.
     *
     * @param lookup the give lookup key of the reference board
     * @return byte of estimate of the lookup position of reference board
     */
    public byte getEstimate(int lookup) {
        return moves[lookup];
    }
}
