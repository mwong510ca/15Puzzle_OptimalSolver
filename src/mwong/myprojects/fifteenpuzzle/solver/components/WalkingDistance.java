package mwong.myprojects.fifteenpuzzle.solver.components;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

/**
 * WalkingDistane provides a set of link and a set of heuristic values of Walking Distance.
 * It either load from storage or generate a new set if local file not exists.
 *
 * <p>Dependencies : PuzzleConstants.java
 *
 * @author   Meisze Wong
 *           www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public class WalkingDistance {
    private final int rowSize;
    private final int keySize;
    private final int patternSize;
    private final int[] priorKey;
    private final int[] afterKey;
    private final int[] partialPattern;

    private HashMap<Integer, Integer> rowKeys;  // 4 * 3 bits
    private HashMap<Integer, Integer> ptnKeys;  // 4 * 6 bits + 4 bits of zero row index
    private byte [] pattern;
    private int [] ptnLink;

    /**
     * Initializes the WalkingDistance object.
     */
    public WalkingDistance() {
        rowSize = PuzzleConstants.getRowSize();
        keySize = 55;
        patternSize = 24964;
        priorKey = new int[] {0, 0x0E00, 0x0FC0, 0x0FF8};
        afterKey = new int[] {0x01FF, 0x003F, 0x0007, 0};
        partialPattern = new int[] {0x00000FFF, 0x00FC0000, 0x0000003F, 0x00FFF000};
        loadData();
    }

    // load the walking distance in file
    private void loadData() {
        rowKeys = new HashMap<Integer, Integer>();
        ptnKeys = new HashMap<Integer, Integer>();
        pattern = new byte[patternSize];
        ptnLink = new int[patternSize * rowSize * 2];

        String filepath = FileProperties.getFilepathWD();
        try (FileInputStream fin = new FileInputStream(filepath);
                FileChannel inChannel = fin.getChannel();) {
            ByteBuffer buf = inChannel.map(FileChannel.MapMode.READ_ONLY, 0, inChannel.size());
            buf.get(pattern);

            for (int i = 0; i < keySize; i++) {
                rowKeys.put(buf.getInt(), buf.getInt());
            }

            for (int i = 0; i < patternSize; i++) {
                ptnKeys.put(buf.getInt(), buf.getInt());
            }

            for (int i = 0; i < ptnLink.length; i++) {
                ptnLink[i] = buf.getInt();
            }
        } catch (BufferUnderflowException | IOException ex) {
            int [] keyLink = genKeys();
            genPattern(keyLink);
            saveData(filepath);
        }
    }

    // save the walking distance in file
    private void saveData(String filepath) {
        String directory = FileProperties.getDirectory();
        if (!(new File(directory)).exists()) {
            (new File(directory)).mkdir();
        }
        if ((new File(filepath)).exists()) {
            (new File(filepath)).delete();
        }

        try (FileOutputStream fout = new FileOutputStream(filepath);
                FileChannel outChannel = fout.getChannel();) {
            ByteBuffer buffer;
            buffer = ByteBuffer.allocateDirect(patternSize);
            buffer.put(pattern);
            buffer.flip();
            outChannel.write(buffer);

            buffer = ByteBuffer.allocateDirect(keySize * 4 * 2);
            for (Entry<Integer, Integer> entry : rowKeys.entrySet()) {
                buffer.putInt(entry.getKey());
                buffer.putInt(entry.getValue());
            }
            buffer.flip();
            outChannel.write(buffer);

            buffer = ByteBuffer.allocateDirect(patternSize * 4 * 2);
            for (Entry<Integer, Integer> entry : ptnKeys.entrySet()) {
                buffer.putInt(entry.getKey());
                buffer.putInt(entry.getValue());
            }
            buffer.flip();
            outChannel.write(buffer);

            buffer = ByteBuffer.allocateDirect(ptnLink.length * 4);
            for (int i = 0; i < ptnLink.length; i++) {
                buffer.putInt(ptnLink[i]);
            }
            buffer.flip();
            outChannel.write(buffer);
        } catch (BufferUnderflowException | IOException ex2) {
            if ((new File(filepath)).exists()) {
                (new File(filepath)).delete();
            }
        }
    }

    // generate all keys for the walking distance
    private int[] genKeys() {
        HashSet<Integer> set = new HashSet<Integer>();
        HashSet<int[]> next = new HashSet<int[]>();
        rowKeys = new HashMap<Integer, Integer>();
        int[] rowKeys2combo = new int[keySize];

        // 1st set starts with 0004, 0040, 0400, 4000
        int counter = 0;
        int key;
        for (int i = 0; i < rowSize; i++) {
            int[] temp = new int[rowSize];
            temp[i] = rowSize;
            key = rowCombo2Key(temp);
            rowKeys2combo[counter] = key;
            rowKeys.put(key, counter++);
            set.add(key);
            next.add(temp);
        }

        while (next.size() > 0) {
            HashSet<int[]> expand = next;
            next = new HashSet<int[]>();
            for (int[] combo : expand) {
                for (int i = 0; i < rowSize; i++) {
                    if (combo[i] > 0) {
                        for (int j = 0; j < rowSize; j++) {
                            if (i != j) {
                                int[] shift = new int[rowSize];
                                System.arraycopy(combo, 0, shift, 0, rowSize);
                                shift[i] = combo[i] - 1;
                                shift[j] = combo[j] + 1;
                                key = rowCombo2Key(shift);
                                if (!set.contains(key)) {
                                    rowKeys2combo[counter] = key;
                                    rowKeys.put(key, counter++);
                                    set.add(key);
                                    next.add(shift);
                                }
                            }
                        }
                    }
                }
            }
        }

        final int splitIdx = counter;

        // 2nd set starts with 0003, 0030, 0300, 3000
        for (int i = 0; i < rowSize; i++) {
            int [] temp = new int[rowSize];
            temp[i] = rowSize - 1;
            key = rowCombo2Key(temp);
            rowKeys2combo[counter] = key;
            rowKeys.put(key, counter++);
            set.add(key);
            next.add(temp);
        }

        while (next.size() > 0) {
            HashSet<int[]> expand = next;
            next = new HashSet<int[]>();
            for (int[] combo : expand) {
                for (int i = 0; i < rowSize; i++) {
                    if (combo[i] > 0) {
                        for (int j = 0; j < rowSize; j++) {
                            if (i != j) {
                                int[] shift = new int[rowSize];
                                System.arraycopy(combo, 0, shift, 0, rowSize);
                                shift[i] = combo[i] - 1;
                                shift[j] = combo[j] + 1;
                                key = rowCombo2Key(shift);
                                if (!set.contains(key)) {
                                    rowKeys2combo[counter] = key;
                                    rowKeys.put(key, counter++);
                                    set.add(key);
                                    next.add(shift);
                                }
                            }
                        }
                    }
                }
            }
        }
        return genKeyLink(splitIdx, rowKeys2combo);
    }

    // generate all key link for the walking distance
    private int [] genKeyLink(int splitIdx, int [] rowKeys2combo) {
        int [] rowKeyLink = new int[keySize * rowSize];
        final int keyBitsSize = 3;

        // shift out from column, from 4 tiles to 3 tiles
        for (int i = 0; i < splitIdx; i++) {
            int combo = rowKeys2combo[i];
            for (int j = 0; j < rowSize; j++) {
                int shiftBits = (rowSize - j - 1) * keyBitsSize;
                int self = ((combo >> shiftBits) & 0x0007);
                if (self > 0) {
                    self = (self - 1) << shiftBits;
                    int nextKey = (combo & priorKey[j]) | (combo & afterKey[j]) | self;
                    rowKeyLink[i * rowSize + j] = rowKeys.get(nextKey);
                } else {
                    // invalid link, empty column
                    rowKeyLink[i * rowSize + j] = -1;
                }
            }
        }

        // shift in to column, from 3 tiles 4 tiles
        for (int i = splitIdx; i < keySize; i++) {
            for (int j = 0; j < rowSize; j++) {
                int combo = rowKeys2combo[i];
                int shiftBits = (rowSize - j - 1) * keyBitsSize;
                int nextKey = (combo & priorKey[j]) | (combo & afterKey[j])
                        | ((((combo >> shiftBits) & 0x0007) + 1) << shiftBits);
                rowKeyLink[i * rowSize + j] = rowKeys.get(nextKey);
            }
        }
        return rowKeyLink;
    }

    // generate all patterns for the walking distance
    private void genPattern(int [] rowKeyLink) {
        ptnKeys = new HashMap<Integer, Integer>();
        pattern = new byte[patternSize];
        ptnLink = new int[patternSize * rowSize * 2];
        final int rowBitsSize = 6;
        final int zeroBitsSize = 4;

        /* starts with 4000  // 6 bits each
                       0400
                       0040
                       0003 | 3 (4 bits for zero row index)
           total 4 x 6 bits + 4 bits = 28 bits for combo */
        int initCombo = 0;
        for (int i = 0; i < rowSize - 1; i++) {
            int key = rowSize << ((rowSize - i - 1) * 3);
            initCombo = (initCombo << rowBitsSize) | rowKeys.get(key);
        }
        initCombo = (initCombo << rowBitsSize) | rowKeys.get(rowSize - 1);
        initCombo = (initCombo << zeroBitsSize) | (rowSize - 1);
        int ctPtn = 0;
        byte moves = 0;
        int[] ptnKeys2combo = new int[patternSize];

        ptnKeys2combo[ctPtn] = initCombo;
        ptnKeys.put(initCombo, ctPtn);
        pattern[ctPtn++] = moves;
        boolean loop = true;
        int top = 0;
        int top2 = 0;
        int end = 1;
        int end2 = 1;

        while (loop) {
            moves++;
            top = top2;
            end = end2;
            top2 = end2;
            loop = false;

            for (int i = top; i < end; i++) {
                int currPtn = ptnKeys2combo[i];
                int ptnCombo = currPtn >> zeroBitsSize;
                int zeroRow = currPtn & 0x000F;
                int zeroIdx = getRowKey(ptnCombo, zeroRow);
                int linkBase = i * rowSize * 2;

                // space down, tile up
                if (zeroRow < rowSize - 1) {
                    int lowerIdx = getRowKey(ptnCombo, zeroRow + 1);
                    for (int j = 0; j < rowSize; j++) {
                        if (rowKeyLink[lowerIdx * rowSize + j] != -1) {
                            int newPtn = 0;
                            int pairKeys = (rowKeyLink[zeroIdx * rowSize + j] << rowBitsSize)
                                    | rowKeyLink[lowerIdx * rowSize + j];
                            //assert (rowKeyLink[lowerIdx * rowSize + j] == -1
                              //      | rowKeyLink[zeroIdx * rowSize + j] == -1)
                                //: "rowKeyLink negative value, space down";

                            switch (zeroRow) {
                                case 0:
                                    newPtn = (pairKeys << 2 * rowBitsSize)
                                            | (ptnCombo & partialPattern[0]);
                                    break;
                                case 1:
                                    newPtn = (ptnCombo & partialPattern[1])
                                            | (pairKeys << rowBitsSize)
                                            | (ptnCombo & partialPattern[2]);
                                    break;
                                case 2:
                                    newPtn = (ptnCombo & partialPattern[3]) | pairKeys;
                                    break;
                                default:
                                    System.err.println("ERROR");
                            }

                            newPtn = (newPtn << zeroBitsSize) | (zeroRow + 1);
                            if (ptnKeys.containsKey(newPtn)) {
                                ptnLink[linkBase + j * 2] = ptnKeys.get(newPtn);
                            } else {
                                ptnKeys2combo[ctPtn] = newPtn;
                                ptnKeys.put(newPtn, ctPtn);
                                pattern[ctPtn] = moves;
                                ptnLink[linkBase + j * 2] = ctPtn++;
                                loop = true;
                                end2++;
                            }
                        } else {
                            ptnLink[linkBase + j * 2] = -1;
                        }
                    }
                } else {
                    ptnLink[linkBase] = -1;
                    ptnLink[linkBase + 2] = -1;
                    ptnLink[linkBase + 4] = -1;
                    ptnLink[linkBase + 6] = -1;
                }

                // space up, tile down
                if (zeroRow > 0) {
                    int upperIdx = getRowKey(ptnCombo, zeroRow - 1);
                    for (int j = 0; j < rowSize; j++) {
                        if (rowKeyLink[upperIdx * rowSize + j] != -1) {
                            int newPtn = 0;
                            int pairKeys = (rowKeyLink[upperIdx * rowSize + j] << rowBitsSize)
                                    | rowKeyLink[zeroIdx * rowSize + j];
                            //assert (rowKeyLink[upperIdx * rowSize + j] == -1
                              //      | rowKeyLink[zeroIdx * rowSize + j] == -1)
                                //: "rowKeyLink negative value, space up";

                            switch (zeroRow) {
                                case 1:
                                    newPtn = (ptnCombo & partialPattern[0])
                                            | (pairKeys << 2 * rowBitsSize);
                                    break;
                                case 2:
                                    newPtn = (ptnCombo & partialPattern[1])
                                            | (pairKeys << rowBitsSize)
                                            | (ptnCombo & partialPattern[2]);
                                    break;
                                case 3:
                                    newPtn = (ptnCombo & partialPattern[3]) | pairKeys;
                                    break;
                                default:
                                    System.err.println("ERROR");
                            }
                            newPtn = (newPtn << zeroBitsSize) | (zeroRow - 1);
                            if (ptnKeys.containsKey(newPtn)) {
                                ptnLink[linkBase + j * 2 + 1] = ptnKeys.get(newPtn);
                            } else {
                                ptnKeys2combo[ctPtn] = newPtn;
                                ptnKeys.put(newPtn, ctPtn);
                                pattern[ctPtn] = moves;
                                ptnLink[linkBase + j * 2 + 1] = ctPtn++;
                                loop = true;
                                end2++;
                            }
                        } else {
                            ptnLink[linkBase + j * 2 + 1] = -1;
                        }
                    }
                } else {
                    ptnLink[linkBase + 1] = -1;
                    ptnLink[linkBase + 3] = -1;
                    ptnLink[linkBase + 5] = -1;
                    ptnLink[linkBase + 7] = -1;
                }
            }
        }
    }

    /**
     * Returns HashMap of compress row Key to key index.
     *
     * @return HashMap of compress row Key to key index
     */
    public final HashMap<Integer, Integer> getRowKeys() {
        return rowKeys;
    }

    /**
     * Returns HashMap of compress a set of row key to pattern index.
     *
     * @return HashMap of compress a set of row key to pattern index
     */
    public final HashMap<Integer, Integer> getPtnKeys() {
        return ptnKeys;
    }

    /**
     * Returns byte array of walking distance pattern.
     *
     * @return byte array of walking distance pattern
     */
    public final byte[] getPattern() {
        return pattern;
    }

    /**
     * Returns integer array of pattern move link set.
     *
     * @return integer array of pattern move link set
     */
    public final int[] getPtnLink() {
        return ptnLink;
    }

    // compress the rowKey set to pattern key
    private int rowCombo2Key(int [] combo) {
        int key = 0;
        for (int i : combo) {
            key = (key << 3) | i;
        }
        return key;
    }

    // extract the row key from the row key set of the given row
    private int getRowKey(int combo, int row) {
        int key = (combo >> ((rowSize - row - 1) * 6)) & 0x003F;
        return key;
    }
}
