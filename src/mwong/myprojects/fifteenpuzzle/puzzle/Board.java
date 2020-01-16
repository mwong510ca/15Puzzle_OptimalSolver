package mwong.myprojects.fifteenpuzzle.puzzle;

import java.util.Arrays;
import java.util.Random;

/**
 * Board is the data type of 15 puzzle. It take 16 numbers of the puzzle or
 * generate the random board at difficulty level. It verify the solvable state.
 * It also generate a new board after the shift.
 *
 * <p>Dependencies : PuzzleConstants.java, PuzzleProperties.java
 *
 * @author <a href="http://www.linkedin.com/pub/macy-wong/46/550/37b/"
 *            target="_blank">Meisze Wong (linkedin)</a>
 * @see <a href="http://www.github.com/mwong510ca/15PuzzleOptimalSolver/"
 *         target="_blank">GitHub (full project)</a>
 */
public final class Board {
  /** Puzzle size.
   *  @see PuzzleConstants#SIZE */
  private static final byte SIZE = PuzzleConstants.getSize();
  /** Puzzle row size.
   *  @see PuzzleConstants#ROW_SIZE */
  private static final byte ROW_SIZE = PuzzleConstants.getRowSize();
  /** Number of direction or moves.
   *  @see PuzzleConstants#DIRECTION_SIZE */
  private static final byte DIR_SIZE = PuzzleConstants.getDirectionSize();
  /** Split the puzzle in half, compress first half to key 1.
   *  @see PuzzleConstants#GOAL_KEY1 */
  private static final int GOAL_KEY1 = PuzzleConstants.getGoalKey1();
  /** Split the puzzle in half, compress second half to key 2.
   *  @see PuzzleConstants#GOAL_KEY2 */
  private static final int GOAL_KEY2 = PuzzleConstants.getGoalKey2();
  /** Space or zero at position 15.
   *  @see PuzzleConstants#GOAL_ZERO_INDEX */
  private static final int ZERO_IDX_15 = PuzzleConstants.getGoalZeroIdx();
  /** Easy difficulty, range from 5 to 20. */
  private static final int MIN_RANGE_EASY = 5;
  /** Moderate difficulty, range from 21 to 45. */
  private static final int MIN_RANGE_MODERATE = 21;
  /** Hard difficulty, range above 45. */
  private static final int MIN_RANGE_HARD = 46;
  /** HardBoards instance for a copy of initial hard boards. */
  private static final HardBoards HARD_BOARDS_INSTANCE = new HardBoards();

  /** The isSolvable variable determine the board is solvable. */
  private boolean isSolvable;
  /** The variable determine the tiles and mirror reflection is the same. */
  private boolean isIdenticalSymmetry;
  /** The x-coordinate of zero position. */
  private int zeroX;
  /** The y-coordinate of zero position. */
  private int zeroY;
  /** The hashcode variable. */
  private int hashcode;
  /** The hash key 1. */
  private int hashKey1;
  /** The hash key 2. */
  private int hashKey2;
  /** The byte array of tiles. */
  private byte[] tiles;
  /** The byte array of mirror reflection. */
  private byte[] tilesMirror;
  /** The integer array represents vaild move factor of 4 directions. */
  private int[] validMoves;

  /**
   * Initializes a Board object, generate a random board.
   */
  public Board() {
    this(DifficultyLevel.RANDOM);
  }

  /**
   * Initializes a Board object, generate a random board of given difficult level.
   *
   * @param level the given difficulty level
   */
  public Board(final DifficultyLevel level) {
    if (level == DifficultyLevel.RANDOM) {
      generateRandomBoard();
    } else {
      generateBoard(level);
    }
    setMoreProperties();
  }

  /**
   * Initializes a Board object of a given 16 tiles byte array.
   *
   * @param blocks the byte array of 16 tiles
   */
  public Board(final byte[] blocks) {
    setPriorities(blocks);
    setMoreProperties();
  }

  /**
   * Private constructor initializes a new Board object from shift() after a move.
   *
   * @param zeroX the x-coordinate of zero space
   * @param zeroY the y-coordinate of zero space
   * @param tiles the given byte array of tiles
   */
  private Board(final int zeroX, final int zeroY, final byte[] tiles) {
    isSolvable = true;
    this.zeroX = zeroX;
    this.zeroY = zeroY;
    this.tiles = tiles;
    setMoreProperties();
  }

  /**
   * Set the basic properties, review the blocks and determine if the board is solvable and
   * initializes zero position.
   *
   * @param blocks the given array of blocks.
   */
  private void setPriorities(final byte[] blocks) {
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
          if (blocks[j] > 0 && value > blocks[j]) {
            invertH++;
          }
        }
      }
    }

    if (zeroY < 0 || invertH < 0) {
      assert false : "unexpected error - negative number";
    } else if (zeroY % 2 != 0 && invertH % 2 != 0) {
      isSolvable = false;
    } else if (zeroY % 2 == 0 && invertH % 2 == 0) {
      isSolvable = false;
    }
  }

  // Initializes the hashcode, mirror conversion tiles and verify valid moves
  // with symmetry reduction
  /**
   * Set properties include initializes the hashcode, mirror conversion tiles and verify valid moves
   * with symmetry reduction.
   */
  private void setMoreProperties() {
    final int bitShift = 4; // Numbers are 0 - 15, 4 bits
    final int constant = 0x1111;
    for (int i = 0; i < SIZE / 2; i++) {
      hashKey1 <<= bitShift;
      hashKey1 |= tiles[i];
    }
    for (int i = SIZE / 2; i < SIZE; i++) {
      hashKey2 <<= bitShift;
      hashKey2 |= tiles[i];
    }
    hashcode = hashKey1 * (hashKey2 + constant);

    tilesMirror = PuzzleConstants.tiles2mirror(tiles);

    validMoves = new int[DIR_SIZE];
    if (zeroX < ROW_SIZE - 1) {
      validMoves[Move.RIGHT.getValue()] = 1;
    }
    if (zeroY < ROW_SIZE - 1) {
      validMoves[Move.DOWN.getValue()] = 1;
    }
    if (zeroX > 0) {
      validMoves[Move.LEFT.getValue()] = 1;
    }
    if (zeroY > 0) {
      validMoves[Move.UP.getValue()] = 1;
    }

    boolean bypass = true;
    for (int i = SIZE - 1; i > -1; i--) {
      if (tiles[i] != tilesMirror[i]) {
        bypass = false;
        break;
      }
    }
    if (bypass) {
      validMoves[Move.DOWN.getValue()] = 0;
      validMoves[Move.UP.getValue()] = 0;
    }
  }

  /**
   * Generate a solvable random board with a given difficulty level.
   * EASY     - Manhattan distance between 5 to 20
   * MODERATE - Manhattan distance between 21 to 45
   * HARD     - Manhattan distance over 45
   * RANDON   - any random puzzle
   *
   * @param level the given difficulty level assoicate with the new board
   */
  private void generateBoard(final DifficultyLevel level) {
    if (level == DifficultyLevel.MODERATE) {
      int estimate = 0;
      while (estimate < MIN_RANGE_MODERATE || estimate >= MIN_RANGE_HARD) {
        generateRandomBoard();
        estimate = heuristic();
      }
    } else {
      byte[] blocks = new byte[SIZE];
      int zero = ZERO_IDX_15;

      while (true) {
        if (level == DifficultyLevel.HARD) {
          // Split random case 20% vs 80% for the initial blocks
          final int split5 = 5;
          int rand = new Random().nextInt(split5);
          if (rand == 0) {
            System.arraycopy(HARD_BOARDS_INSTANCE.getRandomHardZero15(), 0, blocks, 0, SIZE);
            zero = ZERO_IDX_15;
          } else {
            System.arraycopy(HARD_BOARDS_INSTANCE.getRandomHardZero0(), 0, blocks, 0, SIZE);
            zero = 0;
          }
        } else {
          System.arraycopy(PuzzleConstants.getGoalTiles(), 0, blocks, 0, SIZE);
          zero = ZERO_IDX_15;
        }

        // Shuffle the blocks at a random number (10 - 99);
        final int range = 90;
        final int ten = 10;
        int shuffle = (new Random().nextInt(range)) + ten;
        int count = 0;
        while (count < shuffle) {
          int dir = new Random().nextInt(DIR_SIZE);
          if (dir == Move.RIGHT.getValue() && zero % ROW_SIZE < ROW_SIZE - 1) {
            blocks[zero] = blocks[zero + 1];
            blocks[zero + 1] = 0;
            zero = zero + 1;
          } else if (dir == Move.LEFT.getValue() && zero % ROW_SIZE > 0) {
            blocks[zero] = blocks[zero - 1];
            blocks[zero - 1] = 0;
            zero = zero - 1;
          } else if (dir == Move.DOWN.getValue() && zero / ROW_SIZE < ROW_SIZE - 1) {
            blocks[zero] = blocks[zero + ROW_SIZE];
            blocks[zero + ROW_SIZE] = 0;
            zero = zero + ROW_SIZE;
          } else if (dir == Move.UP.getValue() && zero / ROW_SIZE > 0) {
            blocks[zero] = blocks[zero - ROW_SIZE];
            blocks[zero - ROW_SIZE] = 0;
            zero = zero - ROW_SIZE;
          }
          count++;
        }

        if (isGoal(blocks, zero)) {
          continue;
        }

        tiles = new byte[SIZE];
        System.arraycopy(blocks, 0, tiles, 0, SIZE);
        if (level == DifficultyLevel.HARD && heuristic() >= MIN_RANGE_HARD) {
          break;
        } else if (level == DifficultyLevel.EASY && heuristic() >= MIN_RANGE_EASY
            && heuristic() < MIN_RANGE_MODERATE) {
          break;
        }
      }

      isSolvable = true;
      zeroX = (byte) (zero % ROW_SIZE);
      zeroY = (byte) (zero / ROW_SIZE);
    }
  }

  /**
   * Generate a solvable random board using Knuth Shuffle.
   */
  private void generateRandomBoard() {
    Random random = new Random();
    byte[] blocks = new byte[SIZE];
    int count = 1;
    while (count < SIZE) {
      int rand = random.nextInt(count + 1);
      blocks[count] = blocks[rand];
      blocks[rand] = (byte) (count++);
    }

    setPriorities(blocks);
    // if the random board is not solvable, swap a pair of tiles.
    if (!isSolvable) {
      if (zeroY == 0) {
        byte temp = tiles[0 + ROW_SIZE];
        tiles[0 + ROW_SIZE] = tiles[1 + ROW_SIZE];
        tiles[1 + ROW_SIZE] = temp;
      } else {
        byte temp = tiles[0];
        tiles[0] = tiles[1];
        tiles[1] = temp;
      }
      isSolvable = true;
    }
  }

  /**
   * Returns a Board object after it shift one move of the given direction.
   *
   * @param dir the given Direction of move
   * @return Board object after it shift one move of the given direction
   */
  public Board shift(final Move dir) {
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
   * Returns the heuristic using Manhattan Distance.
   *
   * @return the heuristic using Manhattan Distance
   */
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

  /**
   * Returns the boolean value represent the given blocks is goal state.
   *
   * @param blocks the given byte array of blocks
   * @param zero the position of zero space
   * @return boolean value represent the given blocks is goal state
   */
  private boolean isGoal(final byte[] blocks, final int zero) {
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
   * Check the initial board reach the goal state after apply the solution moves.
   *
   * @param steps the number of solution moves
   * @param solution the array list of solution moves
   * @return boolean represent the final board is the goal state after apply the solution
   */
  public boolean checkSolution(final int steps, final Board.Move[] solution) {
    if (!isSolvable) {
      return false;
    }
    Board board = new Board(tiles);
    int count = 0;
    Board.Move dir = Board.Move.NONE;
    while (count < steps) {
      if (++count <= steps) {
        dir = solution[count];
        if (board == null) {
          System.err.println(steps + "\t" + Arrays.toString(solution));
          System.err.println("stop at " + count);
          System.err.println(this);
          return false;
        }
        board = board.shift(dir);
      }
    }
    if (!board.isGoal()) {
      System.err.println(board);
      return false;
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
   * Returns the index of zero position as one dimension array.
   *
   * @return integer represent the index of zero space as one dimension array
   */
  public byte getZero1d() {
    return (byte) (zeroY * ROW_SIZE + zeroX);
  }

  /**
   * Returns the byte array of board tiles.
   *
   * @return byte array of board tiles
   */
  public byte[] getTiles() {
    return tiles.clone();
  }

  /**
   * Returns the byte array of board's mirror reflection tiles.
   *
   * @return byte array of board's mirror reflection tiles
   */
  public byte[] getTilesMirror() {
    return tilesMirror.clone();
  }

  /**
   * Returns the integer array of represent the valid moves.
   *
   * @return integer array of represent the valid moves
   */
  public int[] getValidMoves() {
    return validMoves.clone();
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
   * Returns the boolean represent the tiles and mirror reflection is the same.
   *
   * @return boolean represent the tiles and mirror reflection is the same
   */
  public boolean isIdenticalSymmetry() {
    return isIdenticalSymmetry;
  }

  /**
   * Returns a string representation of the board, 4 by 4 layout.
   *
   * @return a string representation of the board, 4 by 4 layout
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
  public boolean equals(final Object obj) {
    if (obj == null || this.getClass() != obj.getClass()) {
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

  /**
   * DifficultyLevel is enum type that can be used.
   *
   * @author <a href="http://www.linkedin.com/pub/macy-wong/46/550/37b/"
   *            target="_blank">Meisze Wong (linkedin)</a>
   */
  public enum DifficultyLevel {
    /**
     * Difficult level easy.
     */
    EASY,

    /**
     * Difficult level moderate.
     */
    MODERATE,

    /**
     * Difficult level hard.
     */
    HARD,

    /**
     * Difficult level random.
     */
    RANDOM;

    @Override
    public String toString() {
      switch (this) {
        case EASY : return "easy";
        case MODERATE  : return "moderate ";
        case HARD  : return "hard ";
        default    : return "any ";
      }
    }
  }

  /**
   * Move is enum type that can be used.
   *
   * @author <a href="http://www.linkedin.com/pub/macy-wong/46/550/37b/"
   *            target="_blank">Meisze Wong (linkedin)</a>
   */
  public enum Move {
    /**
     * Direction Right.
     */
    RIGHT(0),

    /**
     * Direction Down.
     */
    DOWN(1),

    /**
     * Direction Left.
     */
    LEFT(2),

    /**
     * Direction Up.
     */
    UP(3),

    /**
     * Direction None.
     */
    NONE(-1);

    /** Value of move. */
    private final int val;

    /**
     * Initializes a Direction reference type.
     *
     * @param val the direction code
     */
    Move(final int val) {
      this.val = val;
    }

    /**
     * Returns the value of current direction.
     *
     * @return value of current direction
     */
    public int getValue() {
      return val;
    }

    /**
     * Returns the backward of current direction.
     *
     * @return direction is the backward of current direction
     */
    public Move rollbackDirection() {
      switch (this) {
        case RIGHT : return LEFT;
        case DOWN  : return UP;
        case LEFT  : return RIGHT;
        case UP    : return DOWN;
        default    : return NONE;
      }
    }

    /**
     * Returns the mirror reflection of current direction.
     *
     * @return direction the mirror reflection of current direction
     */
    public Move mirrorDirection() {
      switch (this) {
        case RIGHT : return DOWN;
        case DOWN  : return RIGHT;
        case LEFT  : return UP;
        case UP    : return LEFT;
        default    : return NONE;
      }
    }

    @Override
    public String toString() {
      switch (this) {
        case RIGHT : return "RIGHT";
        case DOWN  : return "DOWN ";
        case LEFT  : return "LEFT ";
        case UP    : return "UP   ";
        default    : return "NONE ";
      }
    }
  }

  /**
   * HardBoards contains the preset hard boards of 15 puzzle.  These boards
   * are use for generate random board.
   *
   * @author <a href="http://www.linkedin.com/pub/macy-wong/46/550/37b/"
   *            target="_blank">Meisze Wong (linkedin)</a>
   */
  private static final class HardBoards {
    /** a set of predefined hard boards with space at upper left corner. */
    private static final byte[][] ZERO_AT_0 = {
      {0, 12, 9, 13, 15, 11, 10, 14, 3, 7, 6, 2, 4, 8, 5, 1},
      {0, 12, 9, 13, 15, 11, 10, 14, 3, 7, 2, 5, 4, 8, 6, 1},
      {0, 12, 10, 13, 15, 11, 14, 9, 3, 7, 2, 5, 4, 8, 6, 1},
      {0, 12, 14, 13, 15, 11, 9, 10, 3, 7, 6, 2, 4, 8, 5, 1},
      {0, 12, 10, 13, 15, 11, 14, 9, 3, 7, 6, 2, 4, 8, 5, 1},
      {0, 12, 11, 13, 15, 14, 10, 9, 3, 7, 6, 2, 4, 8, 5, 1},
      {0, 12, 10, 13, 15, 11, 9, 14, 7, 3, 6, 2, 4, 8, 5, 1},
      {0, 12, 9, 13, 15, 11, 14, 10, 3, 8, 6, 2, 4, 7, 5, 1},
      {0, 12, 9, 13, 15, 11, 10, 14, 8, 3, 6, 2, 4, 7, 5, 1},
      {0, 12, 14, 13, 15, 11, 9, 10, 8, 3, 6, 2, 4, 7, 5, 1},
      {0, 12, 9, 13, 15, 11, 10, 14, 7, 8, 6, 2, 4, 3, 5, 1},
      {0, 12, 10, 13, 15, 11, 14, 9, 7, 8, 6, 2, 4, 3, 5, 1},
      {0, 12, 9, 13, 15, 8, 10, 14, 11, 7, 6, 2, 4, 3, 5, 1},
      {0, 12, 9, 13, 15, 11, 10, 14, 3, 7, 5, 6, 4, 8, 2, 1},
      {0, 12, 9, 13, 15, 11, 10, 14, 7, 8, 5, 6, 4, 3, 2, 1},
      {0, 15, 8, 3, 12, 11, 7, 4, 14, 10, 6, 5, 9, 13, 2, 1},
      {0, 12, 14, 4, 15, 11, 7, 3, 8, 10, 6, 5, 13, 9, 2, 1},
      {0, 12, 7, 3, 15, 11, 8, 4, 10, 14, 6, 2, 9, 13, 5, 1},
      {0, 12, 7, 4, 15, 11, 8, 3, 10, 14, 6, 2, 13, 9, 5, 1},
      {0, 12, 8, 3, 15, 11, 10, 4, 14, 7, 6, 5, 9, 13, 2, 1},
      {0, 12, 8, 3, 15, 11, 7, 4, 14, 10, 6, 2, 9, 13, 5, 1},
      {0, 12, 8, 4, 15, 11, 7, 3, 14, 10, 6, 2, 13, 9, 5, 1},
      {0, 12, 8, 7, 15, 11, 4, 3, 14, 13, 6, 2, 10, 9, 5, 1},
      {0, 15, 4, 10, 12, 11, 8, 3, 13, 14, 6, 2, 7, 9, 5, 1},
      {0, 15, 7, 8, 12, 11, 4, 3, 10, 13, 6, 5, 14, 9, 2, 1},
      {0, 15, 8, 10, 12, 11, 4, 3, 14, 13, 6, 2, 7, 9, 5, 1},
      {0, 15, 8, 3, 12, 11, 10, 4, 14, 7, 6, 2, 9, 13, 5, 1},
      {0, 15, 8, 4, 12, 11, 7, 5, 14, 10, 6, 3, 13, 2, 9, 1},
      {0, 15, 8, 7, 12, 11, 4, 3, 14, 13, 6, 5, 10, 9, 2, 1},
      {0, 2, 9, 13, 5, 1, 10, 14, 3, 7, 6, 15, 4, 8, 12, 11},
      {0, 5, 9, 13, 2, 1, 10, 14, 3, 7, 11, 15, 4, 8, 12, 6},
      {0, 5, 9, 13, 2, 6, 10, 14, 3, 7, 1, 15, 4, 8, 12, 11},
      {0, 5, 9, 14, 2, 6, 10, 13, 3, 7, 1, 15, 8, 4, 12, 11}
    };

    /** a set of predefined hard boards with space at lower right corner. */
    private static final byte[][] ZERO_AT_15 = {
      {1, 10, 14, 13, 7, 6, 5, 9, 8, 2, 11, 15, 4, 3, 12, 0},
      {1, 10, 9, 13, 7, 6, 5, 14, 3, 2, 11, 15, 4, 8, 12, 0},
      {1, 5, 14, 13, 2, 6, 10, 9, 8, 7, 11, 15, 4, 3, 12, 0},
      {1, 5, 9, 13, 2, 6, 10, 14, 3, 7, 11, 15, 4, 8, 12, 0},
      {6, 5, 13, 9, 2, 1, 10, 14, 4, 7, 11, 12, 3, 8, 15, 0},
      {6, 5, 14, 13, 2, 1, 10, 9, 8, 7, 11, 12, 4, 3, 15, 0},
      {6, 5, 9, 13, 2, 1, 10, 14, 3, 7, 11, 12, 4, 8, 15, 0},
      {6, 5, 9, 14, 2, 1, 10, 13, 3, 7, 11, 12, 8, 4, 15, 0}
    };

    /**
     * Returns the byte array of preset hard puzzle with zero position 0 randomly.
     *
     * @return byte array of preset hard puzzle with zero position 0 randomly
     */
    private byte[] getRandomHardZero0() {
      int index = new Random().nextInt(ZERO_AT_0.length);
      return ZERO_AT_0[index];
    }

    /**
     * Returns the byte array of preset hard puzzle with zero position 15 randomly.
     *
     * @return byte array of preset hard puzzle with zero position 15 randomly
     */
    private byte[] getRandomHardZero15() {
      int index = new Random().nextInt(ZERO_AT_15.length);
      return ZERO_AT_15[index];
    }
  }
}
