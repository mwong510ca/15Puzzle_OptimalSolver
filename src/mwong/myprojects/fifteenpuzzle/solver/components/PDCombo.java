/****************************************************************************
 *  @author   Meisze Wong
 *            www.linkedin.com/pub/macy-wong/46/550/37b/
 *
 *  Compilation: javac PDCombo.java
 *  Execution:   java -d64 -Xms2g -Xmx4g PDCombo
 *  Dependencies: Stopwatch.java, Direction.java, Board.java,
 *                PDElement.java, PDPresetPatterns.java
 *
 *  A immutable data type of generate additive pattern database for any group
 *  size between 2 to 8.
 *  Remarks: group size of 8 takes 2.5-3 hours and require at least 2 gigabytes
 *           -Xms2g
 *
 ****************************************************************************/

package mwong.myprojects.fifteenpuzzle.solver.components;

import mwong.myprojects.utilities.Stopwatch;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;

public class PDCombo {
    private static final PDPresetPatterns defaultPattern = PDPresetPatterns.Pattern_663;
    private static final String directory = "database";
    private static final String seperator = System.getProperty("file.separator");
    private static final int[] bitPos16 = PDElement.getBitPos16();
    private static final byte[] bitOrder8
        = {(byte) (1 << 7), 1 << 6, 1 << 5, 1 << 4, 1 << 3, 1 << 2, 1 << 1, 1};
    private static final int puzzleSize = Board.getSize();
    private static final int rowSize = Board.getRowSize();
    private static final int maxGroupSize = PDElement.getMaxGroupSize();
    private byte[] patternGroups;
    // for each pattern group : key size x format size
    private byte[][] patterns;
    private byte[] val2ptnKey;
    private byte[] val2ptnOrder;
    private byte[][] ptnKey2val;

    /**
     * Initializes the PDCombo object using default pattern.
     */
    public PDCombo() {
        this(defaultPattern);
    }

    /**
     * Initializes the PDCombo object using default pattern.
     *
     * @param type the given PresetPatterns type
     */
    public PDCombo(PDPresetPatterns type) {
        // default option is 0
        this(type, 0);
    }

    /**
     * Initializes the PDCombo object using the given preset pattern.
     *
     * @param type the given PresetPatterns type
     * @param choice the integer of pattern option in PresetPatterns
     */
    public PDCombo(PDPresetPatterns type, int choice) {
        if (!type.isValidPattern(choice)) {
            System.out.println("Invalid pattern option : " + choice);
            System.out.println("It will use the default pattern ("
                    + defaultPattern.getType() + " pattern)");
            type = defaultPattern;
            choice = 0;
        }
        loadData(type, choice);
    }

    /**
     * Initializes the PDCombo object using the given custom pattern.
     * will not save in data file
     *
     * @param pattern the byte array of user defined pattern
     */
    public PDCombo(byte [] pattern) {
        createPattern(pattern, null);
    }

    // load the pattern database from file if exists
    // otherwise, create a new set and save in file
    private void loadData(PDPresetPatterns type, int choice) {
        String filepath = directory + seperator + type.getFilename(choice);
        try (FileInputStream fin = new FileInputStream(filepath);
                FileChannel inChannel = fin.getChannel();
                ) {
            ByteBuffer buf = inChannel.map(FileChannel.MapMode.READ_ONLY, 0, inChannel.size());
            int szPuzzle = Board.getSize();
            int numPatterns = buf.get();
            patternGroups = new byte[numPatterns];
            val2ptnKey = new byte[szPuzzle];
            val2ptnOrder = new byte[szPuzzle];

            buf.get(patternGroups);
            buf.get(val2ptnKey);
            buf.get(val2ptnOrder);

            patterns = new byte [numPatterns][];
            for (int i = 0; i < numPatterns; i++) {
                int sizeKeys = PDElement.getKeySize(patternGroups[i]);
                int sizeFmts = PDElement.getFormatSize(patternGroups[i]);

                patterns[i] = new byte[sizeKeys * sizeFmts];
                buf.get(patterns[i]);
            }
        } catch (BufferUnderflowException | IOException ex) {
            if (type == PDPresetPatterns.Pattern_78) {
                System.out.println("Warning: Please make sure increase minimum memory to -Xms2g");
                System.out.println("         and it takes ~ 2.5-3 hours to generate 78 pattern.");
            }
            createPattern(type.getPattern(choice), type.getElements());
            saveData(filepath);
        }
    }

    // save the pattern database in file
    private void saveData(String filepath) {
        System.out.println("Saving a local copy ...");
        if (!(new File(directory)).exists()) {
            (new File(directory)).mkdir();
        }
        if (!(new File(filepath)).exists()) {
            (new File(filepath)).delete();
        }

        try (FileOutputStream fout = new FileOutputStream(filepath);
                FileChannel outChannel = fout.getChannel();) {
            int numPatterns = patternGroups.length;
            ByteBuffer buffer = ByteBuffer.allocateDirect(numPatterns + 33);
            buffer.put((byte) patternGroups.length);   // 1 byte
            buffer.put(patternGroups);                 // number of Patterns
            buffer.put(val2ptnKey);                    // 16 bytes
            buffer.put(val2ptnOrder);                  // 16 bytes
            buffer.flip();
            outChannel.write(buffer);

            for (int i = 0; i < numPatterns; i++) {
                int sizeKeys = PDElement.getKeySize(patternGroups[i]);
                int sizeFmts = PDElement.getFormatSize(patternGroups[i]);

                buffer = ByteBuffer.allocateDirect(sizeKeys * sizeFmts);
                buffer.put(patterns[i]);
                buffer.flip();
                outChannel.write(buffer);
            }
            System.out.println("PD15Combo - save data set in file succeeded.");
        } catch (BufferUnderflowException | IOException ex) {
            System.out.println("PD15Combo - save data set in file failed");
            if ((new File(filepath)).exists()) {
                (new File(filepath)).delete();
            }
        }
    }

    // validate the pattern format, and generate the additive pattern database
    private void createPattern(byte [] pattern, boolean [] elementGroups) {
        // validate the pattern format
        if (elementGroups == null) {
            if (pattern.length != 16) {
                System.err.println("Invalid pattern - size != 16");
                throw new IllegalArgumentException();
            }
            if (pattern[15] != 0) {
                System.err.println("Invalid pattern - pattern[15] != 0");
                throw new IllegalArgumentException();
            }
            if (pattern[14] < 2 || pattern[14] > 8) {
                System.err.println("Invalid pattern - pattern[14] num of groups");
                throw new IllegalArgumentException();
            }
        }

        // analysis the pattern format, store the relations of original tile value,
        // element components and pattern order in local storage
        int numOfPatterns = pattern[14];
        patternGroups = new byte[numOfPatterns];
        int [] ptnFormat = new int[numOfPatterns];

        for (int i = 0; i < puzzleSize - 1; i++) {
            if (pattern[i] < 1 || pattern[i] > numOfPatterns) {
                System.err.println("Invalid pattern - pattern[" + i + "]");
            }
            int group = pattern[i] - 1;
            patternGroups[group]++;

            for (int j = 0; j < numOfPatterns; j++) {
                ptnFormat[j] = ptnFormat[j] << 1;
                if (j == group) {
                    ptnFormat[j] |= 1;
                }
            }
        }

        patterns = new byte[numOfPatterns][];
        val2ptnKey = new byte[puzzleSize];
        val2ptnOrder = new byte[puzzleSize];
        ptnKey2val = new byte[pattern[14]][];
        val2ptnKey[0] = -1;
        val2ptnOrder[0] = -1;
        int [] ctGroup = new int[numOfPatterns];
        for (int i = 0; i < puzzleSize - 1; i++) {
            val2ptnOrder[i + 1] = (byte) (pattern[i] - 1);
            ctGroup[pattern[i] - 1]++;
        }
        for (int i = 0; i < numOfPatterns; i++) {
            ptnKey2val[i] = new byte[ctGroup[i]];

            // check runtime memory for pattern 8, stop if less than 1.6 GB.
            if (ctGroup[i] == maxGroupSize) {
                int mb = 1024 * 1024;
                if (Runtime.getRuntime().maxMemory() / mb < 1600) {
                    System.out.println("Not enough estimate memory : "
                            + (Runtime.getRuntime().maxMemory() / mb / 1000.0)
                            + "GB < 1.6GB for pattern of 8");
                    System.out.println("Please increase runtime memory (java -Xmx2g)"
                            + " and try again!");
                    System.exit(0);
                }
            }
        }

        for (int i = 0; i < numOfPatterns; i++) {
            int count2 = 0;
            for (byte j = 1; j < puzzleSize; j++) {
                if (val2ptnOrder[j] == i) {
                    ptnKey2val[i][count2] = j;
                    val2ptnKey[j] = (byte) count2++;
                }
            }
        }

        // create PD15Element object if using in additive pattern
        if (elementGroups == null) {
            elementGroups = new boolean [maxGroupSize + 1];
            for (byte group : patternGroups) {
                elementGroups[group] = true;
            }
        }
        PDElement pd15e = new PDElement(elementGroups, PDElement.Mode.Generator);

        // create each additive pattern
        for (int i = 0; i < patternGroups.length; i++) {
            // shift 1 bit to left for zero at position 15, lower right corner
            ptnFormat[i] <<= 1;
            if (patternGroups[i] == maxGroupSize) {
                genPatternByte(i, ptnFormat[i], pd15e);
            } else {
                genPatternShort(i, patternGroups[i], ptnFormat[i], pd15e);
            }
        }
        System.out.println("PD15Combo - generate additive pattern database completed");
    }

    // use by additive pattern with 8 tiles (8 spaces for zeroes), collect actual zeroes
    // and pass in as byte value, move all zero spaces freely until it reach the tile
    // return 16 bits short represents a set of final moves that stop by a tile only
    private short freeMoveByte(byte zeroOrder, int fmt) {
        short initMoves = 0;
        boolean [] next = new boolean[puzzleSize];
        int order = 0;
        for (int zeroPos = 0; zeroPos < puzzleSize; zeroPos++) {
            if ((fmt & bitPos16[zeroPos]) > 0) {
                continue;
            }
            if ((zeroOrder & bitOrder8[order]) != 0) {
                initMoves |= (short) bitPos16[zeroPos];
                next[zeroPos] = true;
            }
            order++;
        }
        return freeMove(initMoves, fmt, next);
    }

    // use by additive pattern with 2 - 7 tiles (9 - 14 spaces for zeroes), collect
    // actual zeroes and pass in as integer value, move all zero spaces freely until
    // it reach the tile return 16 bits short represents a set of final moves that
    // stop by a tile only
    private short freeMoveShort(int zeroPos, int fmt) {
        short initMoves = 0;
        boolean [] next = new boolean[puzzleSize];
        for (int i = 0; i < puzzleSize; i++) {
            if ((fmt & bitPos16[i]) > 0) {
                continue;
            }
            if ((zeroPos & bitPos16[i]) != 0) {
                initMoves |= (short) bitPos16[i];
                next[i] = true;
            }
        }
        return freeMove(initMoves, fmt, next);
    }

    // return 16 bits short represents a set of final moves that stop by a tile only
    private short freeMove(short initMoves, int fmt, boolean [] next) {
        boolean flag;
        short validMoves = initMoves;
        do {
            flag = false;
            for (int i = 0; i < puzzleSize; i++) {
                if (next[i]) {
                    if (i % 4 < 3 && (fmt & bitPos16[i + 1]) == 0
                            && (validMoves & bitPos16[i + 1]) == 0) {
                        validMoves |= (short) bitPos16[i + 1];
                        next[i + 1] = true;
                        flag = true;
                    }
                    if (i + rowSize < 16 && (fmt & bitPos16[i + rowSize]) == 0
                            && (validMoves & bitPos16[i + rowSize]) == 0) {
                        validMoves |= (short) bitPos16[i + rowSize];
                        next[i + rowSize] = true;
                        flag = true;
                    }
                    if (i % 4 > 0 && (fmt & bitPos16[i - 1]) == 0
                            && (validMoves & bitPos16[i - 1]) == 0) {
                        validMoves |= (short) bitPos16[i - 1];
                        next[i - 1] = true;
                        flag = true;
                    }
                    if (i - 4 > -1 && (fmt & bitPos16[i - rowSize]) == 0
                            && (validMoves & bitPos16[i - rowSize]) == 0) {
                        validMoves |= (short) bitPos16[i - rowSize];
                        next[i - rowSize] = true;
                        flag = true;
                    }
                    next[i] = false;
                }
            }
        } while (flag);
        return validMoves;
    }

    // return the position of empty slot from left to right in the format base on
    // the given index of zero
    // eg. format: 0000111000011110, zero index: 7, return 4 (the 5th zero in format
    //                    ^
    //             from left to right)
    private int zeroIdx2Pos(int zeroIdx, int fmt) {
        int pos = 0;
        for (int i = 0; i < zeroIdx; i++) {
            if ((fmt & bitPos16[i]) == 0) {
                pos++;
            }
        }
        return pos;
    }

    
    private String num2string(int num) {
    	String str = Integer.toString(num);
    	while (str.length() < 15) {
    		str += " ";
    	}
    	return str;
    }

    // generate the additive pattern of 8 tiles, use 8 bits byte (1 zero space
    // plus 7 tile locations) to record each move during the expansion
    private void genPatternByte(int order, int orgFmt, PDElement pd15e) {
        int group = 8;
        int sizeKey = PDElement.getKeySize(group);
        int sizeFmt = PDElement.getFormatSize(group);
        int sizeShift = PDElement.getShiftMaxX2(group);

        patterns[order] = new byte[sizeKey * sizeFmt];
        HashMap<Integer, Integer> formats = pd15e.getFormats();
        int [] formats2combo = pd15e.getFormatCombo(group);
        int [][] moveSet = pd15e.getLinkFormatComboSet(group);
        int [] shiftSet = pd15e.getKeyShiftSet(group);

        System.out.print("Screen additive pattern " + (order + 1) + " : (");
        for (int i = 0; i < puzzleSize - 1; i++) {
            if ((orgFmt & bitPos16[i]) == 0) {
                System.out.print("x ");
            } else {
                System.out.print((i + 1) + " ");
            }
        }
        int orgKeyIdx = 0;
        int orgFmtIdx = formats.get(orgFmt);
        Stopwatch stopwatch = new Stopwatch();
        System.out.println("0) at " + stopwatch.currentTime() + "s");

        // additive pattern of 8 tiles
        byte [] currMove = new byte[sizeKey * sizeFmt];
        currMove[orgKeyIdx * sizeFmt + orgFmtIdx]
                |= bitOrder8[zeroIdx2Pos(puzzleSize - 1, orgFmt)];
        patterns[order][orgKeyIdx * sizeFmt + orgFmtIdx] = 1;
        int pending = sizeKey * sizeFmt - 1;
        int remaining = pending;
        int step = 1;
        int counter = 0;

        while (pending > 0) {
            // a 8 bits byte represent 8 order of zero spaces of format combo
            byte [] nextMove = new byte[sizeKey * sizeFmt];
            for (int k = 0; k < sizeKey; k++) {
                for (int f = 0; f < sizeFmt; f++) {
                    int fmt = formats2combo[f];
                    if (currMove[k * sizeFmt + f] == 0) {
                        continue;
                    }

                    counter++;
                    short freeMove = freeMoveByte(currMove[k * sizeFmt + f], fmt);

                    for (int zeorPos = 0; zeorPos < puzzleSize; zeorPos++) {
                        if ((fmt & bitPos16[zeorPos]) > 0) {
                            continue;
                        }

                        if ((freeMove & bitPos16[zeorPos]) > 0) {
                            ArrayList<Integer> neighbors = new ArrayList<Integer>();
                            if (zeorPos - 4 >= 0 && (fmt & bitPos16[zeorPos - 4]) > 0) {
                                neighbors.add(zeorPos - 4);
                                neighbors.add(Direction.UP.getValue());
                            }
                            if (zeorPos % rowSize > 0 && (fmt & bitPos16[zeorPos - 1]) > 0) {
                                neighbors.add(zeorPos - 1);
                                neighbors.add(Direction.LEFT.getValue());
                            }
                            if (zeorPos % rowSize < rowSize - 1
                                    && (fmt & bitPos16[zeorPos + 1]) > 0) {
                                neighbors.add(zeorPos + 1);
                                neighbors.add(Direction.RIGHT.getValue());
                            }
                            if (zeorPos + 4 < puzzleSize && (fmt & bitPos16[zeorPos + 4]) > 0) {
                                neighbors.add(zeorPos + 4);
                                neighbors.add(Direction.DOWN.getValue());
                            }

                            if (neighbors.isEmpty()) {
                                continue;
                            }

                            int pos = 0;
                            int tileOrder = 0;
                            for (int i = 0; i < neighbors.size(); i += 2) {
                                int tile = neighbors.get(i);
                                int dirValue = neighbors.get(i + 1);
                                while (pos < tile) {
                                    if ((fmt & bitPos16[pos]) > 0) {
                                        tileOrder++;
                                    }
                                    pos++;
                                }

                                if (tileOrder == group) {
                                    break;
                                }

                                if (moveSet[f][tileOrder * 4 + dirValue] > 0) {
                                    int nextFmt = moveSet[f][tileOrder * 4 + dirValue]
                                            >> 4;
                                    int nextFmtIdx = formats.get(nextFmt);
                                    int rotKey = (moveSet[f][tileOrder * 4 + dirValue]
                                            & 0x0F);

                                    if (rotKey == 0) {
                                        if (patterns[order][k * sizeFmt + nextFmtIdx] == 0) {
                                            patterns[order][k * sizeFmt + nextFmtIdx] = (byte) step;
                                            pending--;
                                        }
                                        nextMove[k * sizeFmt + nextFmtIdx]
                                                |= bitOrder8[zeroIdx2Pos(tile, nextFmt)];
                                    } else {
                                        int nextKey = shiftSet[k * group * sizeShift
                                                               + tileOrder * sizeShift
                                                               + rotKey - 1];
                                        if (patterns[order][nextKey * sizeFmt + nextFmtIdx] == 0) {
                                            patterns[order][nextKey * sizeFmt + nextFmtIdx]
                                                    = (byte) step;
                                            pending--;
                                        }
                                        nextMove[nextKey * sizeFmt + nextFmtIdx]
                                                |= bitOrder8[zeroIdx2Pos(tile, nextFmt)];
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            System.out.println("moves : " + step + "\t count : " + num2string(remaining - pending)
    		+ " scanned : " + num2string(counter) + " ended at " + stopwatch.currentTime() + "s");
            step++;
            currMove = nextMove;
            remaining = pending;
            counter = 0;
        }
        System.out.println();
        patterns[order][orgKeyIdx * sizeFmt + orgFmtIdx] = 0;
    }

    // generate the additive pattern of 2 to 7 tiles, use 16 bits short (1 zero space
    // plus 8 to 13 tile spaces) to record each move during the expansion
    private void genPatternShort(int order, int group, int orgFmt, PDElement pd15e) {
        int sizeKey = PDElement.getKeySize(group);
        int sizeFmt = PDElement.getFormatSize(group);
        int sizeShift = PDElement.getShiftMaxX2(group);

        patterns[order] = new byte[sizeKey * sizeFmt];
        HashMap<Integer, Integer> formats = pd15e.getFormats();
        int [] formats2combo = pd15e.getFormatCombo(group);
        int [][] moveSet = pd15e.getLinkFormatComboSet(group);
        int [] shiftSet = pd15e.getKeyShiftSet(group);

        System.out.print("Screen additive pattern " + (order + 1) + " : (");
        for (int i = 0; i < puzzleSize - 1; i++) {
            if ((orgFmt & bitPos16[i]) == 0) {
                System.out.print("x ");
            } else {
                System.out.print((i + 1) + " ");
            }
        }
        int orgKeyIdx = 0;
        int orgFmtIdx = formats.get(orgFmt);
        Stopwatch stopwatch = new Stopwatch();
        System.out.println("0) at " + stopwatch.currentTime() + "s");

        // a 16 bits short represent 16 position of the board for zero space
        short [] currMove = new short[sizeKey * sizeFmt];
        currMove[orgKeyIdx * sizeFmt + orgFmtIdx] = freeMoveShort(puzzleSize - 1, orgFmt);
        patterns[order][orgKeyIdx * sizeFmt + orgFmtIdx] = 1;
        int pending = sizeKey * sizeFmt - 1;
        int remaining = pending;
        int step = 1;
        int counter = 0;

        while (pending > 0) {
            short [] nextMove = new short[sizeKey * sizeFmt];
            for (int k = 0; k < sizeKey; k++) {
                for (int f = 0; f < sizeFmt; f++) {
                    int fmt = formats2combo[f];
                    if (currMove[k * sizeFmt + f] == 0) {
                        continue;
                    }

                    counter++;
                    short freeMove = freeMoveShort(currMove[k * sizeFmt + f], fmt);

                    for (int zeorPos = 0; zeorPos < puzzleSize; zeorPos++) {
                        if ((fmt & bitPos16[zeorPos]) > 0) {
                            continue;
                        }

                        if ((freeMove & bitPos16[zeorPos]) > 0) {
                            ArrayList<Integer> neighbors = new ArrayList<Integer>();
                            if (zeorPos - 4 >= 0 && (fmt & bitPos16[zeorPos - 4]) > 0) {
                                neighbors.add(zeorPos - 4);
                                neighbors.add(Direction.UP.getValue());
                            }
                            if (zeorPos % rowSize > 0 && (fmt & bitPos16[zeorPos - 1]) > 0) {
                                neighbors.add(zeorPos - 1);
                                neighbors.add(Direction.LEFT.getValue());
                            }
                            if (zeorPos % rowSize < rowSize - 1
                                    && (fmt & bitPos16[zeorPos + 1]) > 0) {
                                neighbors.add(zeorPos + 1);
                                neighbors.add(Direction.RIGHT.getValue());
                            }
                            if (zeorPos + 4 < puzzleSize && (fmt & bitPos16[zeorPos + 4]) > 0) {
                                neighbors.add(zeorPos + 4);
                                neighbors.add(Direction.DOWN.getValue());
                            }

                            if (neighbors.isEmpty()) {
                                continue;
                            }

                            int pos = 0;
                            int tileOrder = 0;
                            for (int i = 0; i < neighbors.size(); i += 2) {
                                int tile = neighbors.get(i);
                                int dirValue = neighbors.get(i + 1);
                                while (pos < tile) {
                                    if ((fmt & bitPos16[pos]) > 0) {
                                        tileOrder++;
                                    }
                                    pos++;
                                }

                                if (tileOrder == group) {
                                    break;
                                }

                                if (moveSet[f][tileOrder * 4 + dirValue] > 0) {
                                    int nextFmt = moveSet[f][tileOrder * 4 + dirValue]
                                            >> 4;
                                    int nextFmtIdx = formats.get(nextFmt);
                                    int rotKey = (moveSet[f][tileOrder * 4 + dirValue]
                                            & 0x0F);

                                    if (rotKey == 0) {
                                        if (patterns[order][k * sizeFmt + nextFmtIdx] == 0) {
                                            patterns[order][k * sizeFmt + nextFmtIdx] = (byte) step;
                                            pending--;
                                        }
                                        nextMove[k * sizeFmt + nextFmtIdx] |= bitPos16[tile];
                                    } else {
                                        int nextKey = shiftSet[k * group * sizeShift
                                                               + tileOrder * sizeShift
                                                               + rotKey - 1];
                                        if (patterns[order][nextKey * sizeFmt + nextFmtIdx] == 0) {
                                            patterns[order][nextKey * sizeFmt + nextFmtIdx]
                                                    = (byte) step;
                                            pending--;
                                        }
                                        nextMove[nextKey * sizeFmt + nextFmtIdx] |= bitPos16[tile];
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            System.out.println("moves : " + step + "\t count : " + num2string(remaining - pending)
    		+ " scanned : " + num2string(counter) + " ended at " + stopwatch.currentTime() + "s");
            step++;
            currMove = nextMove;
            remaining = pending;
            counter = 0;
        }
        System.out.println();
        patterns[order][orgKeyIdx * sizeFmt + orgFmtIdx] = 0;
    }

    /**
     *  Returns the byte array of pattern group size.
     *
     *  @return  byte array of pattern group size
     */
    public final byte[] getPatternGroups() {
        return patternGroups;
    }

    /**
     *  Returns the byte array of conversion set of tile values to detached element keys.
     *
     *  @return byte array of conversion set of tile values to detached element keys
     */
    public final byte[] getVal2ptnKey() {
        return val2ptnKey;
    }

    /**
     *  Returns the byte array of conversion set of tile values to pattern order.
     *
     *  @return byte array of conversion set of tile values to pattern order
     */
    public final byte[] getVal2ptnOrder() {
        return val2ptnOrder;
    }

    /**
     *  Returns the conversion set of tile values to pattern group of additive pattern.
     *
     *  @return a integer array of conversion set of tile values to element keys
     */
    public final byte[][] getPatternSet() {
        return patterns;
    }

    /**
     * Unit Test.
     *
     * @param args Standard argument main function
     */
    public static void main(String [] args) {
        PDCombo pd = new PDCombo(PDPresetPatterns.Pattern_555);
        pd = new PDCombo(PDPresetPatterns.Pattern_663);
        System.out.println(pd.patternGroups.length);
        pd = new PDCombo(PDPresetPatterns.Pattern_78);
        System.out.println(pd.patternGroups.length);
    }
}
