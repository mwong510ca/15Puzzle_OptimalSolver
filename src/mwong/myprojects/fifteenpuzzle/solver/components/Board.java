package mwong.myprojects.fifteenpuzzle.solver.components;

import java.util.ArrayList;
import java.util.Random;

/**
 * Board is the data type of 15 puzzle.  It take 16 numbers of the puzzle or
 * generate the random board at difficulty level.  It verify the solvable state.
 * It also generate a new board after the shift.
 *
 * <p>Dependencies : PuzzleConstants.java
 *
 * @author Meisze Wong
 *         www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public class Board {
    private static final byte SIZE = PuzzleConstants.getSize();
    private static final byte ROW_SIZE = PuzzleConstants.getRowSize();
    private static final int GOAL_KEY1 = PuzzleConstants.getGoalKey1();
    private static final int GOAL_KEY2 = PuzzleConstants.getGoalKey2();

    private boolean isSolvable;
    private boolean isIdenticalSymmetry;
    private int zeroX;
    private int zeroY;
    private int hashcode;
    private int hashKey1;
    private int hashKey2;
    private byte[] tiles;
    private byte[] tilesSym;
    private int[] validMoves;

    /**
     * Initializes a Board object, generate a random board.
     */
    public Board() {
        this(PuzzleDifficultyLevel.RANDOM);
    }

    /**
     * Initializes a Board object, generate a random board
     * of given difficult level.
     *
     * @param level the given difficulty level
     */
    public Board(PuzzleDifficultyLevel level) {
        if (level == PuzzleDifficultyLevel.RANDOM) {
            generateRandomBoard();
        } else {
            generateBoard(level);
        }
        setAdvancedProperties();
    }

    /**
     * Initializes a Board object of a given 16 tiles byte array.
     *
     * @param blocks the byte array of 16 tiles
     */
    public Board(byte [] blocks) {
        setBasicPriorities(blocks);
        setAdvancedProperties();
    }

    // use by shift() from a solvable Board object
    // initializes a Board object for internal use
    private Board(int zeroX, int zeroY, byte[] tiles) {
        isSolvable = true;
        this.zeroX = zeroX;
        this.zeroY = zeroY;
        this.tiles = tiles;
        setAdvancedProperties();
    }

    // loop over the board and determine if the board is solvable
    // and initializes zero position
    private void setBasicPriorities(byte [] blocks) {
        isSolvable = true;
        tiles = new byte[SIZE];
        System.arraycopy(blocks, 0, tiles, 0, SIZE);

        // invert distance
        int invertH = 0;
        for (int i = 0; i < SIZE; i++) {
            int value = tiles[i];
            if (value == 0) {
                zeroX = (byte) (i % ROW_SIZE);
                zeroY = (byte) (i / ROW_SIZE);
            } else {
                for (int j = i + 1; j < SIZE; j++) {
                    if (blocks[j] > 0 && value > blocks[j])  {
                        invertH++;
                    }
                }
            }
        }

        if (zeroY % 2 == 1 && invertH % 2 == 1) {
            isSolvable = false;
        } else if (zeroY % 2 == 0 && invertH % 2 == 0) {
            isSolvable = false;
        }
    }

    // initializes the hashcode, symmetry conversion tiles and verify valid moves
    // with symmetry reduction
    private void setAdvancedProperties() {
        for (int i = 0; i < SIZE / 2; i++) {
            hashKey1 <<= 4;
            hashKey1 |= tiles[i];
        }
        for (int i = SIZE / 2; i < SIZE; i++) {
            hashKey2 <<= 4;
            hashKey2 |= tiles[i];
        }
        hashcode = hashKey1 * (hashKey2 + 0x1111);

        tilesSym = PuzzleConstants.tiles2sym(tiles);

        validMoves = new int[4];
        if (zeroX < ROW_SIZE - 1) {
            validMoves[Direction.RIGHT.getValue()] = 1;
        }
        if (zeroY < ROW_SIZE - 1) {
            validMoves[Direction.DOWN.getValue()] = 1;
        }
        if (zeroX > 0) {
            validMoves[Direction.LEFT.getValue()] = 1;
        }
        if (zeroY > 0) {
            validMoves[Direction.UP.getValue()] = 1;
        }

        isIdenticalSymmetry = true;
        for (int i = SIZE - 1; i > -1; i--) {
            if (tiles[i] != tilesSym[i]) {
                isIdenticalSymmetry = false;
                break;
            }
        }
        if (isIdenticalSymmetry) {
            validMoves[Direction.DOWN.getValue()] = 0;
            validMoves[Direction.UP.getValue()] = 0;
        }
    }

    // generate a solvable random board with a given difficulty level
    private void generateBoard(PuzzleDifficultyLevel level) {
        if (level == PuzzleDifficultyLevel.MODERATE) {
            int estimate = 0;
            while (estimate < 20 || estimate > 45) {
                generateRandomBoard();
                estimate = heuristic();
            }
        } else {
            byte [] blocks = new byte[SIZE];
            int zero = 15;

            while (true) {
                System.arraycopy(PuzzleConstants.getGoalTiles(), 0, blocks, 0, SIZE);
                zero = 15;
                if (level == PuzzleDifficultyLevel.HARD) {
                    int rand = new Random().nextInt(5);
                    if (rand == 0) {
                        if (PuzzleProperties.getHardZero15Size() > 0) {
                            rand = new Random().nextInt(PuzzleProperties.getHardZero15Size());
                            System.arraycopy(PuzzleProperties.getHardZero15(rand),
                                    0, blocks, 0, SIZE);
                        }
                    } else {
                        if (PuzzleProperties.getHardZero0Size() > 0) {
                            rand = new Random().nextInt(PuzzleProperties.getHardZero0Size());
                            System.arraycopy(PuzzleProperties.getHardZero0(rand),
                                    0, blocks, 0, SIZE);
                            zero = 0;
                        }
                    }
                }

                int shuffle = new Random().nextInt(100);
                int count = 0;
                while (count < shuffle) {
                    int dir = new Random().nextInt(4);
                    if (dir == 0 && zero % 4 < 3) {
                        blocks[zero] = blocks[zero + 1];
                        blocks[zero + 1] = 0;
                        zero = zero + 1;
                    } else if (dir == 1 && zero % 4 > 0) {
                        blocks[zero] = blocks[zero - 1];
                        blocks[zero - 1] = 0;
                        zero = zero - 1;
                    } else if (dir == 2 && zero > 3) {
                        blocks[zero] = blocks[zero - 4];
                        blocks[zero - 4] = 0;
                        zero = zero - 4;
                    } else if (dir == 3 && zero < 12) {
                        blocks[zero] = blocks[zero + 4];
                        blocks[zero + 4] = 0;
                        zero = zero + 4;
                    }
                    count++;
                }

                if (isGoal(blocks, zero)) {
                    continue;
                }

                tiles = new byte[SIZE];
                System.arraycopy(blocks, 0, tiles, 0, SIZE);
                if (level == PuzzleDifficultyLevel.HARD && heuristic() > 40) {
                    break;
                } else if (level == PuzzleDifficultyLevel.EASY && heuristic() < 25) {
                    break;
                }
            }

            isSolvable = true;
            zeroX = (byte) (zero % ROW_SIZE);
            zeroY = (byte) (zero / ROW_SIZE);
        }
    }

    // generate a solvable random board using Knuth Shuffle
    private void generateRandomBoard() {
        Random random = new Random();
        byte [] blocks = new byte[SIZE];
        int count = 1;
        while (count < SIZE) {
            int rand = random.nextInt(count + 1);
            blocks[count] = blocks[rand];
            blocks[rand] = (byte) (count++);
        }

        setBasicPriorities(blocks);
        // if the random board is not solvable, swap a pair of tiles.
        if (!isSolvable) {
            if (zeroY == 0)  {
                byte temp = tiles[4];
                tiles[4] = tiles[5];
                tiles[5] = temp;
            } else {
                byte temp = tiles[0];
                tiles[0] = tiles[1];
                tiles[1] = temp;
            }
            isSolvable = true;
        }
    }

    /**
     * Returns a Board object after it shift one move with the given direction.
     *
     * @param dir the given Direction of move
     * @return Board object after it shift one move with the given direction
     */
    public Board shift(Direction dir) {
        if (!isSolvable) {
            return null;
        }

        int orgX = zeroX;
        int orgY = zeroY;
        byte[] movedTiles = new byte[SIZE];
        System.arraycopy(tiles, 0, movedTiles, 0, SIZE);
        int zeroPos = orgY * ROW_SIZE + orgX;
        switch (dir) {
            // space RIGHT, tile LEFT
            case RIGHT:
                if (orgX == ROW_SIZE - 1) {
                    return null;
                }
                orgX++;
                movedTiles[zeroPos] = tiles[zeroPos + 1];
                movedTiles[zeroPos + 1] = 0;
                return new Board(orgX, orgY, movedTiles);
            // space DOWN, tile UP
            case DOWN:
                if (orgY == ROW_SIZE - 1) {
                    return null;
                }
                orgY++;
                movedTiles[zeroPos] = tiles[zeroPos + ROW_SIZE];
                movedTiles[zeroPos + ROW_SIZE] = 0;
                return new Board(orgX, orgY, movedTiles);
            // space LEFT, tile RIGHT
            case LEFT:
                if (orgX == 0) {
                    return null;
                }
                orgX--;
                movedTiles[zeroPos] = tiles[zeroPos - 1];
                movedTiles[zeroPos - 1] = 0;
                return new Board(orgX, orgY, movedTiles);
            // space UP, tile DOWN
            case UP:
                if (orgY == 0) {
                    return null;
                }
                orgY--;
                movedTiles[zeroPos] = tiles[zeroPos - ROW_SIZE];
                movedTiles[zeroPos - ROW_SIZE] = 0;
                return new Board(orgX, orgY, movedTiles);
            default:
                return null;
        }
    }

    /**
     * Returns a ArrayList of all neighbor boards with symmetry reduction.
     *
     * @return a ArrayList of all neighbor boards with symmetry reduction
     */
    public ArrayList<Board> neighbors() {
        ArrayList<Board> list = new ArrayList<Board>();
        Board nextShift;
        nextShift = shift(Direction.RIGHT);
        if (nextShift != null) {
            list.add(nextShift);
        }
        if (!isIdenticalSymmetry) {
            nextShift = shift(Direction.DOWN);
            if (nextShift != null) {
                list.add(nextShift);
            }
        }
        nextShift = shift(Direction.LEFT);
        if (nextShift != null) {
            list.add(nextShift);
        }
        if (!isIdenticalSymmetry) {
            nextShift = shift(Direction.UP);
            if (nextShift != null) {
                list.add(nextShift);
            }
        }
        return list;
    }

    // returns the heuristic using Manhattan Distance
    private int heuristic() {
        int manhattan = 0;
        int value;
        int base = 0;
        for (int row = 0; row < ROW_SIZE; row++) {
            for (int col = 0; col < ROW_SIZE; col++) {
                value = tiles[base + col];
                if (value != 0) {
                    manhattan += Math.abs((value - 1) % ROW_SIZE - col);
                    manhattan += Math.abs((((value - 1)
                            - (value - 1) % ROW_SIZE) / ROW_SIZE) - row);
                }
            }
            base += ROW_SIZE;
        }
        return manhattan;
    }

    /**
     * Returns the boolean represent this board is the goal board.
     *
     * @return boolean represent this board is the goal board
     */
    public boolean isGoal() {
        if (hashKey1 == GOAL_KEY1 && hashKey2 == GOAL_KEY2) {
            return true;
        }
        return false;
    }

    // check if given values is goal state
    private boolean isGoal(byte[] blocks, int zero) {
        if (zero % ROW_SIZE != ROW_SIZE - 1 || zero / ROW_SIZE != ROW_SIZE - 1) {
            return false;
        } else {
            int idx = 0;
            while (idx < SIZE - 1) {
                if (blocks[idx++] != idx) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Returns the column index of zero position.
     *
     * @return integer represent the column index of zero space
     */
    public int getZeroX() {
        return zeroX;
    }

    /**
     * Returns the row index of zero position.
     *
     * @return integer represent the row index of zero space
     */
    public int getZeroY() {
        return zeroY;
    }

    /**
     * Returns the index of zero position as 1d array.
     *
     * @return integer represent the index of zero space as 1d array
     */
    public byte getZero1d() {
        return (byte) (zeroY * ROW_SIZE + zeroX);
    }

    /**
     * Returns the byte array of board tiles.
     *
     * @return byte array of board tiles
     */
    public final byte [] getTiles() {
        return tiles;
    }

    /**
     * Returns the byte array of board's symmetry tiles.
     *
     * @return byte array of board's symmetry tiles
     */
    public final byte[] getTilesSym() {
        return tilesSym;
    }

    /**
     * Returns the integer array of represent the valid moves.
     *
     * @return integer array of represent the valid moves
     */
    public final int[] getValidMoves() {
        return validMoves;
    }

    /**
     * Returns the boolean represent this board is solvable.
     *
     * @return boolean represent this board is solvable
     */
    public boolean isSolvable() {
        return isSolvable;
    }

    /**
     * Returns the boolean represent this board is a symmtry board.
     *
     * @return boolean represent this board is a symmtry board
     */
    public boolean isIdenticalSymmetry() {
        return isIdenticalSymmetry;
    }

    /**
     * Returns a string representation of the board, 4 rows with 4 numbers.
     *
     * @return a string representation of the board, 4 rows with 4 numbers
     */
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < SIZE; i++) {
            str.append(String.format("%2d ", tiles[i]));
            if (i % ROW_SIZE == ROW_SIZE - 1) {
                str.append("\n");
            }
        }
        return str.toString();
    }

    /**
     * Returns an integer of hashcode.
     *
     * @return an integer of hashcode
     */
    @Override
    public int hashCode() {
        return hashcode;
    }

    /**
     * Returns an boolean represents the given object is equal.
     *
     * @param obj the given Object
     * @return an boolean represents the given object is equal
     */
    @Override
    public boolean equals(Object obj) {
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        Board that = (Board) obj;
        if (this.hashcode != that.hashcode) {
            return false;
        }
        if (this.hashKey1 == that.hashKey1 && this.hashKey2 == that.hashKey2) {
            return true;
        }
        return false;
    }
}
