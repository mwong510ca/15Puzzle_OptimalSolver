package mwong.myprojects.fifteenpuzzle.solution.ai;

import java.io.IOException;
import java.io.Serializable;

import mwong.myprojects.fifteenpuzzle.puzzle.Board;

/**
 * ReferenceBoard is the data type of stored board of reference collection.
 *
 * <p>Dependencies : Board.java, ReferenceConstants.java
 *
 * @author <a href="http://www.linkedin.com/pub/macy-wong/46/550/37b/"
 *            target="_blank">Meisze Wong (linkedin)</a>
 * @see <a href="http://www.github.com/mwong510ca/15PuzzleOptimalSolver/"
 *         target="_blank">GitHub (full project)</a>
 */
public final class ReferenceBoard implements Serializable {
  private static final long serialVersionUID = 17195273121L;
  /** The puzzle size.
   *  @see mwong.myprojects.fifteenpuzzle.puzzle.PuzzleConstants#SIZE */
  private static final int PUZZLE_SIZE = ReferenceConstants.getPuzzleSize();
  /** The byte array of reference lookup of zero space.
   *  @see ReferenceConstants#REFERENCE_LOOKUP */
  private static final byte[] REF_LOOKUP = ReferenceConstants.getReferenceLookup();
  /** The byte array of reference group of zero space.
   *  @see ReferenceConstants#REFERENCE_GROUP */
  private static final byte[] REF_GROUP = ReferenceConstants.getReferenceGroup();
  /** The double byte array combines group with lookup of zero space.
   *  @see ReferenceConstants#GROUP_LOOKUP_POS */
  private static final byte[][] GROUP_LOOKUP_POS = ReferenceConstants.getGroupLookupPos();
  /** The byte value of mirror flip group.
   *  @see ReferenceConstants#MIRROR_FLIP_GROUP */
  private static final byte MIRROR_FLIP_GROUP = ReferenceConstants.getMirrorFlipGroup();
  /** The byte array of 90 degrees rotation conversion.
   *  @see ReferenceConstants#ROTATE_90_POS */
  private static final byte[] ROTATE_90_POS = ReferenceConstants.getRotate90Pos();
  /** The byte array of 180 degrees rotation conversion.
   *  @see ReferenceConstants#ROTATE_180_POS */
  private static final byte[] ROTATE_180_POS = ReferenceConstants.getRotate180Pos();
  /** The byte value of mirror flip group.
   *  @see mwong.myprojects.fifteenpuzzle.puzzle.PuzzleConstants#TILE_BITS */
  private static final int TILE_BITS = ReferenceConstants.getTileBits();
  /** The byte value of mirror flip group.
   *  @see mwong.myprojects.fifteenpuzzle.puzzle.PuzzleConstants#TILE_BIT_SIZE */
  private static final int TILE_BITS_SIZE = ReferenceConstants.getTileBitSize();

  /** The byte array of transform tiles that the zero space at lower right group. */
  private byte[] tilesTransform;
  /** The integer value of hash code. */
  private int hashcode;
  /** The integer value of hash key 1. */
  private int hash1;
  /** The integer value of hash key 2. */
  private int hash2;
  /** The byte value of reference group. */
  private byte group;

  /** private constructor, no instance. */
  private ReferenceBoard() {
    // not called.
  }

  /**
   * Initializes a ReferenceBoard object, take a board object,
   * shift the space to corner, and generate a conversion keys
   * in byte array to transform to goal state.
   *
   * @param initial the given board object
   */
  public ReferenceBoard(final Board initial) {
    this();
    byte[] tiles = new byte[PUZZLE_SIZE];
    System.arraycopy(initial.getTiles(), 0, tiles, 0, PUZZLE_SIZE);
    group = REF_GROUP[initial.getZero1d()];
    byte lookup = REF_LOOKUP[initial.getZero1d()];
    if (group == MIRROR_FLIP_GROUP) {
      group = 1;
      System.arraycopy(initial.getTilesMirror(), 0, tiles, 0, PUZZLE_SIZE);
    }

    while (lookup > 0) {
      tiles[GROUP_LOOKUP_POS[group][lookup]] = tiles[GROUP_LOOKUP_POS[group][--lookup]];
      tiles[GROUP_LOOKUP_POS[group][lookup]] = 0;
    }

    byte[] tilesRotate = new byte[PUZZLE_SIZE];
    if (group == 0) {
      System.arraycopy(tiles, 0, tilesRotate, 0, PUZZLE_SIZE);
    } else if (group == 1) {
      for (int i = 0; i < PUZZLE_SIZE; i++) {
        tilesRotate[i] = tiles[ROTATE_90_POS[i]];
      }
    } else if (group == 2) {
      for (int i = 0; i < PUZZLE_SIZE; i++) {
        tilesRotate[i] = tiles[ROTATE_180_POS[i]];
      }
    } else {
      throw new IllegalArgumentException("Invalid group : " + group);
    }

    tilesTransform = new byte[PUZZLE_SIZE];
    for (int i = 1; i < PUZZLE_SIZE; i++) {
      tilesTransform[tilesRotate[i - 1]] = (byte) i;
    }
    setHashcode(tiles);
  }

  /**
   * Use by ReferenceLog loadFile function, initializes a BoardAdvanced object with stored
   * variables, restore the conversion key into byte array.
   *
   * @param transformKey the given transformKey from data tile
   * @param group the given reference group from data tile
   * @param hash1 the given hash key 1 from data tile
   * @param hash2 the given hash key 2 from data tile
   * @param hashcode the given hash code from data tile
   * @throws IOException any IOException
   */
  ReferenceBoard(final long transformKey, final byte group, final int hash1, final int hash2,
      final int hashcode) throws IOException {
    this();
    tilesTransform = new byte[PUZZLE_SIZE];
    int pos = PUZZLE_SIZE;
    boolean[] visited = new boolean[PUZZLE_SIZE];
    long copyKey = transformKey;
    while (pos > 0) {
      pos--;
      byte key = (byte) (copyKey & TILE_BITS);
      if (visited[key]) {
        throw new IOException("Data file error - advanced_accumulator.db");
      }
      tilesTransform[pos] = key;
      visited[key] = true;
      copyKey >>= TILE_BITS_SIZE;
    }
    this.group = group;
    this.hash1 = hash1;
    this.hash2 = hash2;
    this.hashcode = hashcode;
  }

  /**
   * Initializes the hashcode of the given byte array of tiles.
   *
   * @param tiles the given byte array of tiles
   */
  private void setHashcode(final byte[] tiles) {
    final int constant = 0x1111;
    hashcode = 0;
    for (int i = 0; i < PUZZLE_SIZE / 2; i++) {
      hash1 <<= TILE_BITS_SIZE;
      hash1 |= tiles[i];
    }
    for (int i = PUZZLE_SIZE / 2; i < PUZZLE_SIZE; i++) {
      hash2 <<= TILE_BITS_SIZE;
      hash2 |= tiles[i];
    }
    hashcode = hash1 * (hash2 + constant);
  }

  @Override
  public int hashCode() {
    return hashcode;
  }

  /**
   * Returns the integer value of hash key 1.
   *
   * @return integer value of hash key 1
   */
  public int getHash1() {
    return hash1;
  }

  /**
   * Returns the integer value of hash key 2.
   *
   * @return integer value of hash key 2
   */
  public int getHash2() {
    return hash2;
  }

  /**
   * Restore the hash code to tiles in byte array.
   *
   * @return byte array of tiles.
   */
  public byte[] getTiles() {
    byte[] tiles = new byte[PUZZLE_SIZE];
    int value = hash1;
    int pos = PUZZLE_SIZE / 2;
    while (pos > 0) {
      pos--;
      byte key = (byte) (value & TILE_BITS);
      tiles[pos] = key;
      value >>= TILE_BITS_SIZE;
    }
    value = hash2;
    pos = PUZZLE_SIZE;
    while (pos > PUZZLE_SIZE / 2) {
      pos--;
      byte key = (byte) (value & TILE_BITS);
      tiles[pos] = key;
      value >>= TILE_BITS_SIZE;
    }
    return tiles;
  }

  /**
   * Returns a byte array of tiles after transform the given blocks which
   * use the reference stored board as the goal state.
   *
   * @param blocks a byte array of original tiles
   * @return a byte array of tiles after transformation
   */
  public byte[] transformer(final byte[] blocks) {
    byte[] transTiles = new byte[PUZZLE_SIZE];
    for (int pos = 0; pos < PUZZLE_SIZE; pos++) {
      transTiles[pos] = tilesTransform[blocks[pos]];
    }

    if (group == 0) {
      return transTiles;
    }

    byte[] rotateTiles = new byte[PUZZLE_SIZE];
    if (group == 1) {
      for (byte i = 0; i < PUZZLE_SIZE; i++) {
        rotateTiles[i] = transTiles[ROTATE_90_POS[i]];
      }
    } else if (group == 2) {
      for (int i = 0; i < PUZZLE_SIZE; i++) {
        rotateTiles[i] = transTiles[ROTATE_180_POS[i]];
      }
    } else {
      throw new IllegalArgumentException("Invalid group : " + group);
    }
    return rotateTiles;
  }

  /**
   * Returns the byte array of transformed tiles as goal state.
   *
   * @return byte array of transformed tiles
   */
  public byte[] getTilesTransform() {
    return tilesTransform;
  }

  /**
   * Return the byte value of group number.
   *
   * @return byte value of group number
   */
  public byte getGroup() {
    return group;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == null) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    ReferenceBoard that = (ReferenceBoard) obj;
    if (this.hashcode != that.hashcode) {
      return false;
    }
    if (this.hash1 == that.hash1 && this.hash2 == that.hash2) {
      return true;
    }
    return false;
  }

  @Override
  public String toString() {
    String str = "";
    for (int i = 0; i < PUZZLE_SIZE; i++) {
      str += getTiles()[i] + " ";
    }
    str += "\n";
    for (int i = 0; i < PUZZLE_SIZE; i++) {
      str += tilesTransform[i] + " ";
    }
    return str;
  }
}

