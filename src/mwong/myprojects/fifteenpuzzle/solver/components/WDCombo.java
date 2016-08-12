/****************************************************************************
 *  @author   Meisze Wong
 *            www.linkedin.com/pub/macy-wong/46/550/37b/
 *
 *  Compilation: javac WDCombo.java
 *  Dependencies: Board.java
 *
 *  A immutable data type of generate walking distance for the 15 puzzle
 *
 ****************************************************************************/

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

public class WDCombo {
    private static final String directory = "database";
    private static final String seperator = System.getProperty("file.separator");
    private static final String filePath = directory + seperator + "WalkingDistance.db";
    private static final int rowSize = Board.getRowSize();
    private static final int keySize = 55;
    private static final int patternSize = 24964;
    private static final int[] priorKey = {0, 0x0E00, 0x0FC0, 0x0FF8};
    private static final int[] afterKay = {0x01FF, 0x003F, 0x0007, 0};
    private static final int[] partialPattern = {0x00000FFF, 0x00FC0000, 0x0000003F, 0x00FFF000};

    private HashMap<Integer, Integer> rowKeys;  // 4 * 3 bits
    private HashMap<Integer, Integer> ptnKeys;  // 4 * 6 bits + 4 bits of zero row index
    private byte [] pattern;
    private int [] ptnLink;

    /**
     * Initializes the WDCombo object using default pattern.
     */
    public WDCombo() {
        loadData();
    }

    // load the walking distance in file
    private void loadData() {
        rowKeys = new HashMap<Integer, Integer>();
        ptnKeys = new HashMap<Integer, Integer>();
        pattern = new byte[patternSize];
        ptnLink = new int[patternSize * rowSize * 2];

        try (FileInputStream fin = new FileInputStream(filePath);
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
            saveData();
        }
    }

    // save the walking distance in file
    private void saveData() {
        if (!(new File(directory)).exists()) {
            (new File(directory)).mkdir();
        }
        if ((new File(filePath)).exists()) {
            (new File(filePath)).delete();
        }

        try (FileOutputStream fout = new FileOutputStream(filePath);
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
            if ((new File(filePath)).exists()) {
                (new File(filePath)).delete();
            }
        }
    }

    // generate all keys for the walking distance
    private int [] genKeys() {
        HashSet<Integer> set = new HashSet<Integer>();
        HashSet<int[]> next = new HashSet<int[]>();
        rowKeys = new HashMap<Integer, Integer>();
        int [] rowKeys2combo = new int[keySize];

        // starts with 0004, 0040, 0400, 4000
        int counter = 0;
        int key;
        for (int i = 0; i < rowSize; i++) {
            int [] temp = new int[rowSize];
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
            for (int[] item : expand) {
                for (int i = 0; i < rowSize; i++) {
                    if (item[i] > 0) {
                        for (int j = 0; j < rowSize; j++) {
                            if (i != j) {
                                int [] temp = item.clone();
                                temp[i] = item[i] - 1;
                                temp[j] = item[j] + 1;
                                key = rowCombo2Key(temp);
                                if (!set.contains(key)) {
                                    rowKeys2combo[counter] = key;
                                    rowKeys.put(key, counter++);
                                    set.add(key);
                                    next.add(temp);
                                }
                            }
                        }
                    }
                }
            }
        }

        int split = counter;

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
            for (int[] item : expand) {
                for (int i = 0; i < rowSize; i++) {
                    if (item[i] > 0) {
                        for (int j = 0; j < rowSize; j++) {
                            if (i != j) {
                                int [] temp = item.clone();
                                temp[i] = item[i] - 1;
                                temp[j] = item[j] + 1;
                                key = rowCombo2Key(temp);
                                if (!set.contains(key)) {
                                    rowKeys2combo[counter] = key;
                                    rowKeys.put(key, counter++);
                                    set.add(key);
                                    next.add(temp);
                                }
                            }
                        }
                    }
                }
            }
        }
        return genKeyLink(split, rowKeys2combo);
    }

    // generate all key link for the walking distance
    private int [] genKeyLink(int split, int [] rowKeys2combo) {
        int [] rowKeyLink = new int[keySize * rowSize];
        for (int i = 0; i < split; i++) {
            int combo = rowKeys2combo[i];

            for (int j = 0; j < rowSize; j++) {
                int shift = (rowSize - j - 1) * 3;
                int self = ((combo >> shift) & 0x0007);
                if (self > 0) {
                    self = (self - 1) << shift;
                    int nextKey = (combo & priorKey[j]) | (combo & afterKay[j]) | self;
                    rowKeyLink[i * rowSize + j] = rowKeys.get(nextKey);
                } else {
                    rowKeyLink[i * rowSize + j] = -1;
                }
            }
        }

        for (int i = split; i < keySize; i++) {
            for (int j = 0; j < rowSize; j++) {
                int combo = rowKeys2combo[i];
                int shift = (rowSize - j - 1) * 3;
                int nextKey = (combo & priorKey[j]) | (combo & afterKay[j])
                        | ((((combo >> shift) & 0x0007) + 1) << shift);
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

        /* starts with 4000  // 6 bits each
                       0400
                       0040
                       0003 | 3 (4 bits for zero row index) */
        int initPtn = 0;
        for (int i = 0; i < rowSize - 1; i++) {
            int key = rowSize << ((rowSize - i - 1) * 3);
            initPtn = (initPtn << 6) | rowKeys.get(key);
        }
        initPtn = (initPtn << 6) | rowKeys.get(rowSize - 1);
        initPtn = (initPtn << 4) | (rowSize - 1);
        int ctPtn = 0;
        byte moves = 0;
        int[] ptnKeys2combo = new int[patternSize];

        ptnKeys2combo[ctPtn] = initPtn;
        ptnKeys.put(initPtn, ctPtn);
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
                int ptnCombo = currPtn >> 4;
                int zeroRow = currPtn & 0x000F;
                int zeroIdx = getRowKey(ptnCombo, zeroRow);

                // space down, tile up
                if (zeroRow < rowSize - 1) {
                    int lowerIdx = getRowKey(ptnCombo, zeroRow + 1);
                    for (int j = 0; j < rowSize; j++) {
                        if (rowKeyLink[lowerIdx * rowSize + j] != -1) {
                            int newPtn = 0;
                            int pairKeys = (rowKeyLink[zeroIdx * rowSize + j] << 6)
                                    | rowKeyLink[lowerIdx * rowSize + j];
                            assert (rowKeyLink[lowerIdx * rowSize + j] == -1
                                    | rowKeyLink[zeroIdx * rowSize + j] == -1)
                                : "rowKeyLink negative value, space down";

                            switch (zeroRow) {
                                case 0:
                                    newPtn = (pairKeys << 12) | (ptnCombo & partialPattern[0]);
                                    break;
                                case 1:
                                    newPtn = (ptnCombo & partialPattern[1]) | (pairKeys << 6)
                                            | (ptnCombo & partialPattern[2]);
                                    break;
                                case 2:
                                    newPtn = (ptnCombo & partialPattern[3]) | pairKeys;
                                    break;
                                default:
                                    System.err.println("ERROR");
                            }

                            newPtn = (newPtn << 4) | (zeroRow + 1);
                            if (ptnKeys.containsKey(newPtn)) {
                                ptnLink[i * rowSize * 2 + j * 2] = ptnKeys.get(newPtn);
                            } else {
                                ptnKeys2combo[ctPtn] = newPtn;
                                ptnKeys.put(newPtn, ctPtn);
                                pattern[ctPtn] = moves;
                                ptnLink[i * rowSize * 2 + j * 2] = ctPtn++;
                                loop = true;
                                end2++;
                            }
                        } else {
                            ptnLink[i * rowSize * 2 + j * 2] = -1;
                        }
                    }
                } else {
                    ptnLink[i * rowSize * 2] = -1;
                    ptnLink[i * rowSize * 2 + 2] = -1;
                    ptnLink[i * rowSize * 2 + 4] = -1;
                    ptnLink[i * rowSize * 2 + 6] = -1;
                }

                // space up, tile down
                if (zeroRow > 0) {
                    int upperIdx = getRowKey(ptnCombo, zeroRow - 1);
                    for (int j = 0; j < rowSize; j++) {
                        if (rowKeyLink[upperIdx * rowSize + j] != -1) {
                            int newPtn = 0;
                            int pairKeys = (rowKeyLink[upperIdx * rowSize + j] << 6)
                                    | rowKeyLink[zeroIdx * rowSize + j];
                            assert (rowKeyLink[upperIdx * rowSize + j] == -1
                                    | rowKeyLink[zeroIdx * rowSize + j] == -1)
                                : "rowKeyLink negative value, space up";

                            switch (zeroRow) {
                                case 1:
                                    newPtn = (ptnCombo & partialPattern[0]) | (pairKeys << 12);
                                    break;
                                case 2:
                                    newPtn = (ptnCombo & partialPattern[1]) | (pairKeys << 6)
                                            | (ptnCombo & partialPattern[2]);
                                    break;
                                case 3:
                                    newPtn = (ptnCombo & partialPattern[3]) | pairKeys;
                                    break;
                                default:
                                    System.err.println("ERROR");
                            }
                            newPtn = (newPtn << 4) | (zeroRow - 1);
                            if (ptnKeys.containsKey(newPtn)) {
                                ptnLink[i * rowSize * 2 + j * 2 + 1] = ptnKeys.get(newPtn);
                            } else {
                                ptnKeys2combo[ctPtn] = newPtn;
                                ptnKeys.put(newPtn, ctPtn);
                                pattern[ctPtn] = moves;
                                ptnLink[i * rowSize * 2 + j * 2 + 1] = ctPtn++;
                                loop = true;
                                end2++;
                            }
                        } else {
                            ptnLink[i * rowSize * 2 + j * 2 + 1] = -1;
                        }
                    }
                } else {
                    ptnLink[i * rowSize * 2 + 1] = -1;
                    ptnLink[i * rowSize * 2 + 3] = -1;
                    ptnLink[i * rowSize * 2 + 5] = -1;
                    ptnLink[i * rowSize * 2 + 7] = -1;
                }
            }
        }
    }

    /**
     *  Returns the number of walking distance pattern.
     *
     *  @return number of walking distance pattern
     */
    public static int getPatternSize() {
        return patternSize;
    }

    /**
     *  Returns the number of compress key per line.
     *
     *  @return number of walking distance pattern
     */
    public static int getKeySize() {
        return keySize;
    }

    /**
     * Returns the string of the file path of the walking distance.
     *
     * @return string of the file path of the walking distance
     */
    public static String getFilePath() {
        return filePath;
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
