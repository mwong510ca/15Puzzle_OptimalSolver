package mwong.myprojects.fifteenpuzzle.solver.ai;

import mwong.myprojects.fifteenpuzzle.solver.components.Board;

import java.io.IOException;
import java.io.Serializable;

/**
 * ReferenceBoard is the data type of stored board of reference collection.
 *
 * <p>Dependencies : Board.java, ReferenceConstants.java
 *
 * @author   Meisze Wong
 *           www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public class ReferenceBoard implements Serializable {
    private static final long serialVersionUID = 17195273121L;
    private final int puzzleSize;
    private final byte[] referenceLookup;
    private final byte[] referenceGroup;
    private final byte[] rotate90pos;
    private final byte[] rotate180pos;

    byte[] tilesTransform;
    int hashcode;
    int hash1;
    int hash2;
    byte group;

    // load all constants.
    private ReferenceBoard() {
        puzzleSize = ReferenceConstants.getPuzzleSize();
        referenceLookup = ReferenceConstants.getReferenceLookup();
        referenceGroup = ReferenceConstants.getReferenceGroup();
        rotate90pos = ReferenceConstants.getRotate90Pos();
        rotate180pos = ReferenceConstants.getRotate180Pos();
    }

    /**
     * Initializes a ReferenceBoard object, take a board object,
     * shift the space to corner, and generate a conversion keys
     * in byte array to transform to goal state.
     *
     * @param initial the given board object
     */
    public ReferenceBoard(Board initial) {
        this();
        byte[] tiles = new byte[puzzleSize];
        System.arraycopy(initial.getTiles(), 0, tiles, 0, puzzleSize);
        group = referenceGroup[initial.getZero1d()];
        byte lookup = referenceLookup[initial.getZero1d()];
        if (group == 3) {
            group = 1;
            System.arraycopy(initial.getTilesSym(), 0, tiles, 0, puzzleSize);
        }

        byte[] tilesRotate = new byte[puzzleSize];
        if (group == 0) {
            if (lookup > 2) {
                tiles[11] = tiles[10];
                tiles[10] = 0;
            }
            if (lookup > 1) {
                tiles[10] = tiles[14];
                tiles[14] = 0;
            }
            if (lookup > 0) {
                tiles[14] = tiles[15];
                tiles[15] = 0;
            }
            System.arraycopy(tiles, 0, tilesRotate, 0, puzzleSize);
        } else if (group == 1) {
            if (lookup > 2) {
                tiles[2] = tiles[6];
                tiles[6] = 0;
            }
            if (lookup > 1) {
                tiles[6] = tiles[7];
                tiles[7] = 0;
            }
            if (lookup > 0) {
                tiles[7] = tiles[3];
                tiles[3] = 0;
            }
            for (int i = 0; i < puzzleSize; i++) {
                tilesRotate[i] = tiles[rotate90pos[i]];
            }
        } else if (group == 2) {
            if (lookup > 2) {
                tiles[4] = tiles[5];
                tiles[5] = 0;
            }
            if (lookup > 1) {
                tiles[5] = tiles[1];
                tiles[1] = 0;
            }
            if (lookup > 0) {
                tiles[1] = tiles[0];
                tiles[0] = 0;
            }
            for (int i = 0; i < puzzleSize; i++) {
                tilesRotate[i] = tiles[rotate180pos[i]];
            }
        } else {
            throw new IllegalArgumentException("Invalid group : " + group);
        }

        tilesTransform = new byte[puzzleSize];
        for (int i = 1; i < puzzleSize; i++) {
            tilesTransform[tilesRotate[i - 1]] = (byte) i;
        }
        setHashcode(tiles);
    }

    // use by AdvancedAccumulator loadFile function
    // initializes a BoardAdvanced object with stored variables, restore the
    // conversion key into byte array
    ReferenceBoard(long transformKey, byte group, int hash1, int hash2, int hashcode)
            throws IOException {
        this();
        tilesTransform = new byte[puzzleSize];
        int pos = puzzleSize;
        boolean[] visited = new boolean[puzzleSize];
        while (pos > 0) {
            pos--;
            byte key = (byte) (transformKey & 0x0F);
            if (visited[key]) {
                throw new IOException("Data file error - advanced_accumulator.db");
            }
            tilesTransform[pos] = key;
            visited[key] = true;
            transformKey >>= 4;
        }
        this.group = group;
        this.hash1 = hash1;
        this.hash2 = hash2;
        this.hashcode = hashcode;
    }

    @Override
    public int hashCode() {
        return hashcode;
    }

    @Override
    public boolean equals(Object obj) {
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

    // initializes the hashcode of the object
    private void setHashcode(byte[] tiles) {
        hashcode = 0;
        for (int i = 0; i < puzzleSize / 2; i++) {
            hash1 <<= 4;
            hash1 |= tiles[i];
        }
        for (int i = puzzleSize / 2; i < puzzleSize; i++) {
            hash2 <<= 4;
            hash2 |= tiles[i];
        }
        hashcode = hash1 * (hash2 + 0x1111);
    }

    // restores and returns the reference board in byte array
    byte[] getTiles() {
        byte[] tiles = new byte[puzzleSize];
        int value = hash1;
        int pos = puzzleSize / 2;
        while (pos > 0) {
            pos--;
            byte key = (byte) (value & 0x0F);
            tiles[pos] = key;
            value >>= 4;
        }
        value = hash2;
        pos = puzzleSize;
        while (pos > puzzleSize / 2) {
            pos--;
            byte key = (byte) (value & 0x0F);
            tiles[pos] = key;
            value >>= 4;
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
    public byte[] transformer(byte[] blocks) {
        byte[] transTiles = new byte[puzzleSize];
        for (int pos = 0; pos < 16; pos++) {
            transTiles[pos] = tilesTransform[blocks[pos]];
        }

        if (group == 0) {
            return transTiles;
        }

        byte[] rotateTiles = new byte[puzzleSize];
        if (group == 1) {
            for (byte i = 0; i < puzzleSize; i++) {
                rotateTiles[i] = transTiles[rotate90pos[i]];
            }
        } else if (group == 2) {
            for (int i = 0; i < puzzleSize; i++) {
                rotateTiles[i] = transTiles[rotate180pos[i]];
            }
        } else {
            throw new IllegalArgumentException("Invalid group : " + group);
        }
        return rotateTiles;
    }

    @Override
    public String toString() {
        String str = "";
        for (int i = 0; i < puzzleSize; i++) {
            str += getTiles()[i] + " ";
        }
        str += "\n";
        for (int i = 0; i < puzzleSize; i++) {
            str += tilesTransform[i] + " ";
        }
        return str;
    }
}

