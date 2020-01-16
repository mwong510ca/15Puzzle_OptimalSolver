package mwong.myprojects.fifteenpuzzle.solution;

import mwong.myprojects.fifteenpuzzle.puzzle.Board;

/**
 * SolverQuick is the 15 puzzle quick solver for non-optimal solution.  Hard coded
 * to solve the top row or left column, the optimal solver will solve the rest.
 *
 * <p>Dependencies : Board.java
 *
 * @author <a href="http://www.linkedin.com/pub/macy-wong/46/550/37b/"
 *            target="_blank">Meisze Wong (linkedin)</a>
 * @see <a href="http://www.github.com/mwong510ca/15PuzzleOptimalSolver/"
 *         target="_blank">GitHub (full project)</a>
 */
public final class SolverQuick {
  /** Puzzle size.
   *  @see mwong.myprojects.fifteenpuzzle.puzzle.PuzzleConstants#SIZE */
  private static final int PUZZLE_SIZE = SolverConstants.getPuzzleSize();
  /** Puzzle row size.
   *  @see mwong.myprojects.fifteenpuzzle.puzzle.PuzzleConstants#ROW_SIZE */
  private static final int ROW_SIZE = SolverConstants.getRowSize();
  /** Mirror reflection, position conversion table.
   *  @see mwong.myprojects.fifteenpuzzle.puzzle.PuzzleConstants#MIRROR_POSITION */
  private static final byte[] MIRROR_POS_TABLE = SolverConstants.getMirrorPosition();
  /** Enum type, move direction right.
   *  @see Board.Move#RIGHT*/
  private static final Board.Move MOVE_R = Board.Move.RIGHT;
  /** Enum type, move direction left.
   *  @see Board.Move#LEFT*/
  private static final Board.Move MOVE_L = Board.Move.LEFT;
  /** Enum type, move direction down.
   *  @see Board.Move#DOWN*/
  private static final Board.Move MOVE_D = Board.Move.DOWN;
  /** Enum type, move direction up.
   *  @see Board.Move#UP*/
  private static final Board.Move MOVE_U = Board.Move.UP;

  /** The x-coordinate of zero position. */
  private int zeroX;
  /** The y-coordinate of zero position. */
  private int zeroY;
  /** The index of zero position. */
  private int zeroPos;
  /** The byte array of tiles. */
  private byte[] tiles;
  /** The isSolvable variable determine the board is solvable. */
  private boolean isSolvable;
  /** The total search node count. */
  private int searchNodeCount;
  /** The total search time. */
  private double searchTime;
  /** The number of steps of solution. */
  private int steps;
  /** The Board.Move array of solution. */
  private Board.Move[] solutionMove;

  /**
   * Initialize SolverQuick object.
   */
  public SolverQuick() {
    // empty constructor
  }

  /**
   * Find the non-optimal path of 15 puzzle.
   *
   * @param board the given puzzle
   */
  public void findPathQuickSearch(final Board board) {
    findPathQuickSearch(board, new SolverBuilder().internalSolverMd());
  }

  /**
   * Find the non-optimal path of 15 puzzle.
   *
   * @param board the given puzzle
   * @param solver the given solver to be use to solver the rest of the puzzle,
   *        it hard code to solve the first row
   */
  public void findPathQuickSearch(final Board board, final Solver solver) {
    searchNodeCount = 0;
    searchTime = 0.0;
    steps = 0;
    isSolvable = true;
    if (!board.isSolvable()) {
      isSolvable = false;
      return;
    }

    final int storageSize = 200;
    solutionMove = new Board.Move[storageSize];
    solutionMove[0] = Board.Move.NONE;
    steps = 0;
    Stopwatch sw = new Stopwatch();
    sw.start();
    int counts = solveFirstRow(board);

    final boolean solverStatus = solver.isStatusOn();
    solver.setStatusOn(false);
    solver.findOptimalPath(new Board(tiles));
    searchTime = sw.currentTime();
    System.arraycopy(solver.solution(), 1, solutionMove, steps + 1, solver.moves());
    searchNodeCount = counts + solver.searchNodeCount();
    steps += solver.moves();
    clearRedundantMoves();
    solver.setStatusOn(solverStatus);
    assert board.checkSolution(steps, solutionMove) : "Failed - not solved at goal state";
  }

  /**
   * Hard code to solve the first row horizontally and vertically (column), pick the partial
   * solution with less moves.
   *
   * @param board the given Board object
   * @return the number node count
   */
  private int solveFirstRow(final Board board) {
    // solve the top row
    tiles = board.getTiles();
    zeroX = board.getZeroX();
    zeroY = board.getZeroY();
    zeroPos = board.getZero1d();

    moveToOrigin(Origin.Tile1);
    moveToOrigin(Origin.Tile2);
    move34();
    for (Origin org : Origin.values()) {
      assert tiles[org.index] == org.value : "Solve first row failed.";
    }

    // copy the top row solution set
    final int steps1 = steps;
    Board.Move[] moves1 = new Board.Move[steps];
    System.arraycopy(solutionMove, 1, moves1, 0, steps1);
    final byte[] tiles1 = tiles.clone();
    final int x1 = zeroX;
    final int y1 = zeroY;
    final int pos1 = zeroPos;

    // solve the left column
    tiles = board.getTilesMirror();
    zeroX = board.getZeroY();
    zeroY = board.getZeroX();
    zeroPos = MIRROR_POS_TABLE[board.getZero1d()];
    steps = 0;

    moveToOrigin(Origin.Tile1);
    moveToOrigin(Origin.Tile2);
    move34();
    for (Origin org : Origin.values()) {
      assert tiles[org.index] == org.value : "Solve first column failed.";
    }

    // pick the solution with less moves
    int steps2 = steps;
    if (steps1 <= steps2) {
      steps = steps1;
      System.arraycopy(moves1, 0, solutionMove, 1, steps1);
      tiles = tiles1;
      zeroX = x1;
      zeroY = y1;
      zeroPos = pos1;
    } else {
      for (int i = 1; i <= steps; i++) {
        solutionMove[i] = solutionMove[i].mirrorDirection();
      }
      tiles = SolverConstants.tiles2mirror(tiles);
      int temp = zeroX;
      zeroX = zeroY;
      zeroY = temp;
      zeroPos = MIRROR_POS_TABLE[zeroPos];
    }
    return steps1 + steps2;
  }

  /**
   * Clear redundant moves from solution.
   */
  private void clearRedundantMoves() {
    int i = 1;
    while (i < steps) {
      if (solutionMove[i] == solutionMove[i + 1].rollbackDirection()) {
        System.arraycopy(solutionMove, i + 2, solutionMove, i, steps - i - 1);
        steps -= 2;
        i--;
      } else {
        i++;
      }
    }
  }

  /**
   * Move the tile to goal position.
   *
   * @param tile the target Origin position and value.
   */
  private void moveToOrigin(final Origin tile) {
    moveToOrigin(tile.index, tile.value);
  }

  /**
   * Move the tile to goal position.
   *
   * @param goalPosition the given target position
   * @param val the given value of target position
   */
  private void moveToOrigin(final int goalPosition, final int val) {
    if (tiles[goalPosition] != val) {
      int valPos = 0;
      for (int i = 1; i < PUZZLE_SIZE; i++) {
        if (tiles[i] == val) {
          valPos = i;
          break;
        }
      }
      int goalX = goalPosition % ROW_SIZE;

      int valX = valPos % ROW_SIZE;
      int valY = valPos / ROW_SIZE;

      if (valX != goalX) {
        moveToCol(goalX, valX, valY);
      }
      if (valY != 0) {
        moveToTop(goalX, valY);
      }
    }
    if (zeroY == 0) {
      shiftDown();
    }
    assert tiles[goalPosition] == val : "moveToOrigin function error.";
  }

  /**
   * Move the tile 3 and tile 4 to goal position.
   */
  private void move34() {
    if (tiles[Origin.Tile3.index] == Origin.Tile3.value
        && tiles[Origin.Tile4.index] == Origin.Tile4.value) {
      return;
    }
    if (conflict43()) {
      return;
    }

    int dist3 = -1;
    int dist4 = -1;
    for (int i = 2; i < PUZZLE_SIZE; i++) {
      if (tiles[i] == Origin.Tile3.value) {
        dist3 = 0;
        dist3 += Math.abs(Origin.Tile4.index - i % ROW_SIZE);
        dist3 += i / ROW_SIZE;
      }
      if (tiles[i] == Origin.Tile4.value) {
        dist4 = 0;
        dist4 += Math.abs(Origin.Tile3.index - i % ROW_SIZE);
        dist4 += i / ROW_SIZE;
      }
      if (dist3 != -1 && dist4 != -1) {
        break;
      }
    }

    if (dist3 <= dist4) {
      moveToOrigin(Origin.Tile4.index, Origin.Tile3.value);
      if (conflict43()) {
        return;
      }
      moveToOrigin(Origin.Tile4.index, Origin.Tile4.value);
      if (conflict43()) {
        return;
      }
    } else {
      moveToOrigin(Origin.Tile3.index, Origin.Tile4.value);
      if (conflict43()) {
        return;
      }
      moveToOrigin(Origin.Tile3.index, Origin.Tile3.value);
      if (conflict43()) {
        return;
      }
    }
  }

  /**
   * Returns the boolean value represents the tiles are conflict 3 and 4,
   * and has been resolved.
   *
   * @return boolean value represents the tiles are conflict 3 and 4 and resolved
   */
  private boolean conflict43() {
    if (tiles[Origin.Tile3.index] == Origin.Tile4.value
        && tiles[Origin.Tile4.index] == Origin.Tile3.value) {
      if (zeroY == 0) {
          assert false : "conflict43() call, zero should not on first row.";
      } else {
        while (zeroX < 2) {
          shiftRight();
        }
        while (zeroX > 2) {
          shiftLeft();
        }

        while (zeroY > 0) {
          shiftUp();
        }
      }

      // shuffle it than solve again
      shiftLeft();
      shiftDown();
      shiftDown();
      shiftDown();
      shiftLeft();
      shiftLeft();
      shiftUp();
      shiftUp();
      shiftRight();
      shiftRight();
      shiftUp();
      move34();
      return true;
    }
    return false;
  }

  /**
   * Move tile to target column, without change the row index.
   *
   * @param goalCol the target column index
   * @param x the current x index of tile
   * @param y the current y index of tile
   */
  private void moveToCol(final int goalCol, final int x, final int y) {
    if (zeroX == x) {
      if (x == ROW_SIZE - 1) {
        shiftLeft();
      } else {
        shiftRight();
      }
    }
    while (zeroX + 1 < x) {
      shiftRight();
    }
    while (zeroX - 1 > x) {
      shiftLeft();
    }
    while (zeroY < y) {
      shiftDown();
    }
    while (zeroY > y) {
      shiftUp();
    }

    int pos = x;
    while (pos < goalCol) {
      tileRight(pos++, y);
    }
    while (pos > goalCol) {
      tileLeft(pos--, y);
    }
  }

  /**
   * Move tile to the top row without change column index.
   *
   * @param x the x-coordinate of the tile
   * @param y the y-coordinate of the tile
   */
  private void moveToTop(final int x, final int y) {
    if (zeroY == y) {
      if (y == ROW_SIZE - 1) {
        shiftUp();
      } else {
        shiftDown();
      }
    }
    while (zeroY + 1 < y) {
      shiftDown();
    }
    while (zeroY - 1 > y) {
      shiftUp();
    }
    if (y == 0) {
      System.err.println("row error " + new Board(tiles));
    }

    while (zeroX < x) {
      shiftRight();
    }
    while (zeroX > x) {
      shiftLeft();
    }

    int pos = y;
    while (pos > 0) {
      tileUp(x, pos--);
    }
  }

  /**
   * Roll the tile up one row.
   *
   * @param x the x-coordinate of the tile
   * @param y the y-coordinate of the tile
   */
  private void tileUp(final int x, final int y) {
    if (zeroX != x) {
      throw new IllegalArgumentException();
    }

    if (zeroY == y - 1) {
      shiftDown();
    } else if (zeroY != y + 1) {
      throw new IllegalArgumentException();
    } else {
      if (x < ROW_SIZE - 1) {
        shiftRight();
        shiftUp();
        shiftUp();
        shiftLeft();
        shiftDown();
      } else {
        shiftLeft();
        shiftUp();
        shiftUp();
        shiftRight();
        shiftDown();
      }
    }
  }

  /**
   * Roll the tile to right one column.
   *
   * @param x the x-coordinate of the tile
   * @param y the y-coordinate of the tile
   */
  private void tileRight(final int x, final int y) {
    if (zeroY != y) {
      throw new IllegalArgumentException();
    }
    if (zeroX == x + 1) {
      shiftLeft();
    } else if (zeroX != x - 1) {
      throw new IllegalArgumentException();
    } else {
      if (y < ROW_SIZE - 1) {
        shiftDown();
        shiftRight();
        shiftRight();
        shiftUp();
        shiftLeft();
      } else {
        shiftUp();
        shiftRight();
        shiftRight();
        shiftDown();
        shiftLeft();
      }
    }
  }

  /**
   * Roll the tile to left one column.
   *
   * @param x the x-coordinate of the tile
   * @param y the y-coordinate of the tile
   */
  private void tileLeft(final int x, final int y) {
    if (zeroY != y) {
      throw new IllegalArgumentException();
    }

    if (zeroX == x - 1) {
      shiftRight();
    } else if (zeroX != x + 1) {
      throw new IllegalArgumentException();
    } else {
      if (y < ROW_SIZE - 1) {
        shiftDown();
        shiftLeft();
        shiftLeft();
        shiftUp();
        shiftRight();
      } else {
        shiftUp();
        shiftLeft();
        shiftLeft();
        shiftDown();
        shiftRight();
      }
    }
  }

  /** Shift the space to right. */
  private void shiftRight() {
    swapZero(zeroPos + 1);
    zeroX++;
    solutionMove[++steps] = MOVE_R;
  }

  /** Shift the space to down. */
  private void shiftDown() {
    swapZero(zeroPos + ROW_SIZE);
    zeroY++;
    solutionMove[++steps] = MOVE_D;
  }

  /** Shift the space to left. */
  private void shiftLeft() {
    swapZero(zeroPos - 1);
    zeroX--;
    solutionMove[++steps] = MOVE_L;
  }

  /** Shift the space to up. */
  private void shiftUp() {
    swapZero(zeroPos - ROW_SIZE);
    zeroY--;
    solutionMove[++steps] = MOVE_U;
  }

  /**
   * Swap the tile with zero.
   *
   * @param nextPos the tile position to be swap with zero space
   */
  private void swapZero(final int nextPos) {
    tiles[zeroPos] = tiles[nextPos];
    tiles[nextPos] = 0;
    zeroPos = nextPos;
  }

  /**
   * Returns the integer value of total search node count.
   *
   * @return integer value of total search node count
   */
  public int searchNodeCount() {
    if (!isSolvable) {
      return -1;
    }
    return searchNodeCount;
  }

  /**
   * Returns the double value of total time of search in seconds.
   *
   * @return double value of total time of search in seconds
   */
  public double searchTime() {
    return searchTime;
  }

  /**
   * Returns the integer value of minimum moves to the goal state.
   *
   * @return integer value of minimum moves to the goal state
   */
  public int moves() {
    if (!isSolvable) {
      return -1;
    }
    return steps;
  }

  /**
   * Returns the array of Directions of each move to the goal state.
   *
   * @return array of Directions of each move to the goal state
   */
  public Board.Move[] solution() {
    if (!isSolvable) {
      return null;
    }
    return solutionMove;
  }

  /**
   * The enum class of origin, position and value.
   *
   * @author <a href="http://www.linkedin.com/pub/macy-wong/46/550/37b/"
   *            target="_blank">Meisze Wong (linkedin)</a>
   */
  private enum Origin {
    /** Tile1 - index 0, tile value 1. */
    Tile1(0, 1),
    /** Tile2 - index 1, tile value 2. */
    Tile2(1, 2),
    /** Tile3 - index 2, tile value 3. */
    Tile3(2, 3),
    /** Tile4 - index 3, tile value 4. */
    Tile4(3, 4);

    /** The position index of Origin. */
    private int index;
    /** The tile value of Origin. */
    private int value;

    /**
     * Initialize Origin with a pair of position and value.
     *
     * @param index the index of Origin
     * @param value tile value of Origin
     */
    Origin(final int index, final int value) {
      this.index = index;
      this.value = value;
    }
  }
}
