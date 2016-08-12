/****************************************************************************
 *  @author   Meisze Wong
 *            www.linkedin.com/pub/macy-wong/46/550/37b/
 *
 *  Compilation  : javac AdvancedAccumulator.java
 *  Dependencies : AdvancedBoard.java, AdvancedMoves.java, Board.java,
 *                 SolverInterface.java, SolverPD.java
 *
 *  A data type of collection of reference boards.  It analysis each
 *  board's actual number of moves, first 8 moves to goal state, and a
 *  conversion set for reverse estimate (use reference board as goal state).
 *
 ****************************************************************************/

package mwong.myprojects.fifteenpuzzle.solver.components;

import mwong.myprojects.fifteenpuzzle.solver.SolverInterface;
import mwong.myprojects.fifteenpuzzle.solver.SolverInterface.HeuristicType;
import mwong.myprojects.fifteenpuzzle.solver.SolverPD;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

public class AdvancedAccumulator {
    private static final String directory = "database";
    private static final String seperator = System.getProperty("file.separator");
    private static final String filePath = directory + seperator + "advanced_accumulator.db";

    private static final int defaultCutoffLimit = 10;
    private static final double defaultCutoffBuffer = 0.95;
    private static final int puzzleSize = Board.getSize();
    private static HashMap<AdvancedBoard, AdvancedMoves> defaultMap =
            new HashMap<AdvancedBoard, AdvancedMoves>();

    private HashMap<AdvancedBoard, AdvancedMoves> activeMap;
    private int cutoffSetting;
    private double cutoffLimit;
    private boolean fileReady = false;
    private final boolean isSymmetry = AdvancedMoves.isSymmetry();

    // selected reference baords for default setting, total 40 after generation.
    private static final byte[][][] PRESET_BOARDS = {
            {{ 0, 15,  8,  3, 12, 11,  7,  4, 14, 10,  6,  5,  9, 13,  2,  1}, {0,  70}},
            {{ 6,  5,  9, 13,  2,  1, 10, 14,  3,  7,  0, 15,  4,  8, 12, 11}, {10, 72}},
            {{ 0, 12,  8,  4, 15, 11,  7,  3, 14, 10,  6,  2, 13,  9,  5,  1}, {0,  72}},
            {{ 6,  5, 14, 13,  2,  1, 10,  9,  8,  7,  0, 15,  4,  3, 12, 11}, {10, 70}},
            {{ 0,  5,  9, 13,  2,  1, 10, 14,  3,  7, 11, 15,  4,  8, 12,  6}, {0,  72}},
            {{ 0, 12,  7,  4, 15, 11,  8,  3, 10, 14,  6,  2, 13,  9,  5,  1}, {0,  70}},
            {{ 0, 15,  8,  7, 12, 11,  4,  3, 14, 13,  6,  5, 10,  9,  2,  1}, {0,  72}},
            {{11, 12,  8,  3, 15,  0,  7,  4, 14, 10,  6,  5,  9, 13,  2,  1}, {5,  66}},
            {{ 1,  5,  9, 13,  2,  6, 10, 14,  3,  7, 11, 15,  4,  8, 12,  0}, {15, 72}},
            {{ 0, 15,  8,  4, 12, 11,  7,  5, 14, 10,  6,  3, 13,  2,  9,  1}, {0,  70}},
            {{ 1, 10, 14, 13,  7,  6,  5,  9,  8,  2, 11, 15,  4,  3, 12,  0}, {15, 72}},
            {{ 0, 12,  8,  7, 15, 11,  4,  3, 14, 13,  6,  2, 10,  9,  5,  1}, {0,  72}},
            {{ 6,  5, 14, 13,  2,  1, 10,  9,  8,  7, 11, 12,  4,  3, 15,  0}, {15, 70}},
            {{ 0,  5,  9, 13,  2,  6, 10, 14,  3,  7,  1, 15,  4,  8, 12, 11}, {0,  72}},
            {{ 6,  5, 13,  9,  2,  1, 10, 14,  4,  7, 11, 12,  3,  8, 15,  0}, {15, 68}},
            {{ 6,  5,  9, 13,  2,  1, 10, 14,  3,  7, 11, 12,  4,  8, 15,  0}, {15, 70}},
            {{11, 15,  8,  3, 12,  0,  7,  4, 14, 10,  6,  2,  9, 13,  5,  1}, {5,  66}},
            {{ 1, 10,  9, 13,  7,  0,  5, 14,  3,  2,  6, 15,  4,  8, 12, 11}, {5,  70}},

            {{ 0, 15,  9, 13, 11, 12, 10, 14,  3,  7,  6,  2,  4,  8,  5,  1}, { 0, 80}},
            {{ 0, 12,  9, 13, 15, 11, 10, 14,  8,  3,  6,  2,  4,  7,  5,  1}, { 0, 80}},
            {{ 0, 12,  9, 13, 15, 11, 10, 14,  7,  8,  6,  2,  4,  3,  5,  1}, { 0, 80}},
            {{ 0, 12,  9, 13, 15,  8, 10, 14,  11, 7,  6,  2,  4,  3,  5,  1}, { 0, 80}},
            {{ 0, 12,  9, 13, 15, 11, 10, 14,  3,  7,  5,  6,  4,  8,  2,  1}, { 0, 80}},
            {{ 0, 12,  9, 13, 15, 11, 10, 14,  7,  8,  5,  6,  4,  3,  2,  1}, { 0, 80}},
            {{ 0, 12,  9, 13, 15, 11, 10, 14,  3,  7,  6,  2,  4,  8,  5,  1}, { 0, 80}},
            {{ 0, 12,  9, 13, 15, 11, 14, 10,  3,  8,  6,  2,  4,  7,  5,  1}, { 0, 80}},
            {{ 0, 12, 10, 13, 15, 11,  9, 14,  7,  3,  6,  2,  4,  8,  5,  1}, { 0, 80}},
            {{ 0, 12, 14, 13, 15, 11,  9, 10,  8,  3,  6,  2,  4,  7,  5,  1}, { 0, 80}},
            {{ 0, 12, 10, 13, 15, 11, 14,  9,  7,  8,  6,  2,  4,  3,  5,  1}, { 0, 80}},
            {{ 0, 15,  8, 13, 12, 11,  9, 10, 14,  3,  6,  2,  4,  7,  5,  1}, { 0, 78}},
            {{11, 15,  9, 13, 12,  0, 10, 14,  3,  7,  6,  2,  4,  8,  5,  1}, { 5, 78}},
            {{ 0, 12,  5, 13, 15,  6, 10,  9,  2,  7, 11, 14,  4,  3,  8,  1}, { 0, 78}},
            {{ 0, 12,  8, 13, 15, 11,  7,  9, 14, 10,  6,  2,  4,  3,  5,  1}, { 0, 78}},
            {{ 0, 14, 15, 13,  8, 11, 10,  5, 12,  7,  6,  9,  4,  2,  3,  1}, { 0, 78}}
    };

    static {
        if (defaultMap.size() == 0) {
            for (byte[][] preset : PRESET_BOARDS) {
                AdvancedBoard advBoard = new AdvancedBoard(new Board(preset[0]));
                AdvancedMoves advMoves = new AdvancedMoves(preset[1][0], preset[1][1]);
                defaultMap.put(advBoard, advMoves);

                if (preset[1][0] == 5) {
                    byte[] tiles = preset[0].clone();
                    tiles[5] = tiles[6];
                    tiles[6] = 0;
                    advBoard = new AdvancedBoard(new Board(tiles));
                    advMoves = new AdvancedMoves((byte) 6, (byte) (preset[1][1] - 1));
                    defaultMap.put(advBoard, advMoves);
                }

                if (preset[1][0] == 10) {
                    byte[] tiles = preset[0].clone();
                    tiles[10] = tiles[6];
                    tiles[6] = 0;
                    advBoard = new AdvancedBoard(new Board(tiles));
                    advMoves = new AdvancedMoves((byte) 6, (byte) (preset[1][1] - 1));
                    defaultMap.put(advBoard, advMoves);
                }
            }
        }
    }

    /**
     * Initializes A object.  Load the stored boards from file or use default set.
     */
    public AdvancedAccumulator() {
        try {
            activeMap = new HashMap<AdvancedBoard, AdvancedMoves>();
            System.out.println("Load data and system update - archived hard board. "
                    + "Please wait.");
            loadFile();
            System.out.println();
        } catch (IOException ex) {
            System.out.println("Default setting : cutoff archive limit - "
                    + defaultCutoffLimit);
            cutoffSetting = defaultCutoffLimit;
            cutoffLimit = cutoffSetting * defaultCutoffBuffer;

            activeMap = new HashMap<AdvancedBoard, AdvancedMoves>();
            for (Entry<AdvancedBoard, AdvancedMoves> entry : defaultMap.entrySet()) {
                activeMap.put(entry.getKey(), entry.getValue());
            }
            createFile();
        }
        updateData(createSolver());
    }

    /**
     * Returns a HashMap of default set of reference boards.
     *
     * @return HashMap of default set of reference boards
     */
    public static final HashMap<AdvancedBoard, AdvancedMoves> getDefaultMap() {
        return defaultMap;
    }

    /**
     * Returns a HashMap of collection of reference boards.
     *
     * @return HashMap of collection of reference boards
     */
    public final HashMap<AdvancedBoard, AdvancedMoves> getActiveMap() {
        return activeMap;
    }

    /**
     * Returns an integer of cutoff setting.
     *
     * @return integer of cutoff setting
     */
    public int getCutoffSetting() {
        return cutoffSetting;
    }

    /**
     * Returns a double of cutoff limit (95% of cutoff setting).
     *
     * @return double of cutoff limit
     */
    public double getCutoffLimit() {
        return cutoffLimit;
    }

    // load the reference boards from file
    private void loadFile() throws IOException {
        FileInputStream fin = new FileInputStream(filePath);
        FileChannel inChannel = fin.getChannel();
        ByteBuffer buffer = inChannel.map(FileChannel.MapMode.READ_ONLY, 0, inChannel.size());

        cutoffSetting = buffer.getInt();
        cutoffLimit = cutoffSetting * defaultCutoffBuffer;

        while (buffer.remaining() >= 34) {
            AdvancedBoard advBoard = null;

            long transformKey = buffer.getLong();
            byte group = buffer.get();
            int hash1 = buffer.getInt();
            int hash2 = buffer.getInt();
            int hashcode = buffer.getInt();
            advBoard = new AdvancedBoard(transformKey, group, hash1, hash2, hashcode);

            byte[] moves = new byte[4];
            buffer.get(moves);
            short[] initMoves = new short[4];
            for (int i = 0; i < 4; i++) {
                initMoves[i] = buffer.getShort();
            }
            byte status = buffer.get();
            if (activeMap.containsKey(advBoard)) {
                AdvancedMoves advMoves = activeMap.get(advBoard);
                advMoves.updateMoves(moves, initMoves, status);
            } else {
                AdvancedMoves advMoves = new AdvancedMoves(moves, initMoves, status);
                activeMap.put(advBoard, advMoves);
            }
        }

        if (buffer.remaining() > 0) {
            fin.close();
            throw new IOException("Data file error - advanced_accumulator.db");
        }
        fin.close();
        fileReady = true;
    }

    // create the new file to store the reference boards.
    private void createFile() {
        fileReady = false;
        if (!(new File(directory)).exists()) {
            (new File(directory)).mkdir();
        }

        if ((new File(filePath)).exists()) {
            (new File(filePath)).delete();
        }

        FileOutputStream fout;
        FileChannel outChannel;
        ByteBuffer buffer;

        try {
            fout = new FileOutputStream(filePath);
            outChannel = fout.getChannel();
            buffer = ByteBuffer.allocateDirect(8);
            buffer.putInt(cutoffSetting);
            buffer.flip();
            outChannel.write(buffer);
            outChannel.close();
            fout.close();
            fileReady = true;
        } catch (IOException ex) {
            System.err.println("System error : unable to save file.");
        }
    }

    // append a reference board with moves and partial solutions to file.
    private void add2file(AdvancedBoard advBoard, AdvancedMoves advMoves) {
        if (!fileReady) {
            createFile();
        }

        FileOutputStream fout;
        FileChannel outChannel;
        try {
            fout = new FileOutputStream(filePath, true);
            outChannel = fout.getChannel();

            ByteBuffer buffer = ByteBuffer.allocateDirect(34);
            long key = 0L;
            for (int val : advBoard.tilesTransform) {
                key <<= 4;
                key |= val;
            }
            buffer.putLong(key);                        //  8
            buffer.put(advBoard.group);                 //  1
            buffer.putInt(advBoard.hash1);              //  4
            buffer.putInt(advBoard.hash2);              //  4
            buffer.putInt(advBoard.hashcode);           //  4
            buffer.put(advMoves.moves);                 //  4
            for (short move : advMoves.initMoves) {     //  8 (2x4)
                buffer.putShort(move);
            }
            buffer.put(advMoves.status);                //  1
            buffer.flip();
            outChannel.write(buffer);

            outChannel.close();
            fout.close();
        } catch (IOException ex) {
            System.err.println("System error : write file error.");
        }
    }

    // create and return a SolverPD object.
    SolverPD createSolver() {
        try {
            SolverPD solver = new SolverPD(PDPresetPatterns.Pattern_78);
            solver.setAdvancedAccumulator(this);
            solver.messageSwitch(SolverInterface.SWITCH_OFF);
            solver.timeoutSwitch(SolverInterface.SWITCH_OFF);
            solver.advPrioritySwitch(SolverInterface.SWITCH_ON);
            return solver;
        } catch (OutOfMemoryError ex) {
            return null;
        }
    }

    // verify the given solver is SolverPD object using pattern database 7-8
    private boolean validateSolver(SolverInterface inSolver) {
        if (inSolver != null && inSolver.getHeuristicType() == HeuristicType.PD78) {
            return true;
        }
        return false;
    }

    // verify the given solver is using pattern database 7-8, scan the full
    // collection, if the reference board is not verified, verify it now.
    void updateData(SolverInterface inSolver) {
        SolverInterface solver = inSolver;
        if (inSolver == null || !validateSolver(inSolver)) {
            solver = createSolver();
        }
        if (solver == null) {
            System.out.println("System update failed - not enough memory.");
            return;
        }
        updateAll((SolverPD) solver);
    }

    /**
     *  Verify the given solver is using pattern database 7-8, scan the full
     *  collection, if the reference board is not verified, verify it now.
     *
     *  @param inSolver the SolverInterface object in use
     */
    public void updatePending(SolverInterface inSolver) {
        if (inSolver == null || !validateSolver(inSolver)) {
            return;
        }
        updateAll((SolverPD) inSolver);
    }

    // scan the full collection, if the reference board is not verified, verify it now.
    private void updateAll(SolverPD solverPD) {
        assert solverPD instanceof SolverPD : "updateAll did not recevie SolverPD object";
        final boolean backupAdvPriority = solverPD.getPriorityFlag();
        final boolean backupMessageFlag = solverPD.getMessageFlag();
        final boolean backupTimeoutFlag = solverPD.getTimeoutFlag();
        solverPD.timeoutSwitch(SolverInterface.SWITCH_OFF);
        solverPD.messageSwitch(SolverInterface.SWITCH_OFF);
        solverPD.advPrioritySwitch(SolverInterface.SWITCH_ON);

        for (Entry<AdvancedBoard, AdvancedMoves> entry : activeMap.entrySet()) {
            AdvancedMoves advMoves = entry.getValue();
            if (advMoves.isCompleted()) {
                continue;
            }
            AdvancedBoard advBoard = entry.getKey();
            advMoves.updateSolutions(advBoard, solverPD);
            add2file(advBoard, advMoves);
        }
        solverPD.advPrioritySwitch(backupAdvPriority);
        solverPD.messageSwitch(backupMessageFlag);
        solverPD.timeoutSwitch(backupTimeoutFlag);
    }

    /**
     * If the given solver using pattern database 7-8, and it takes
     * over the cutoff limit solve the puzzle with advanced estimate;
     * add to reference boards collection.
     *
     * @param inSolver the SolverInterface object in use
     */
    public boolean addBoard(SolverInterface inSolver) {
        return addBoard(inSolver, false);
    }

    // add a reference board in collection, allow bypass mininum
    // cutoff limit requirement
    boolean addBoard(SolverInterface inSolver, boolean bypass) {
        if (activeMap == null) {
            return false;
        }
        if (inSolver == null || !validateSolver(inSolver)) {
            return false;
        }
        if (!bypass && inSolver.searchTime() < getCutoffLimit()) {
            return false;
        }
        SolverPD solverPD = (SolverPD) inSolver;
        Board board = solverPD.lastSearchBoard();
        Direction[] solution = solverPD.solution().clone();

        if (!bypass && !solverPD.getPriorityFlag()) {
            int heuristicOrg = solverPD.heuristicOrg(board);
            int heuristicAdv = solverPD.heuristicAdv(board);
            if (heuristicOrg != heuristicAdv) {
                return false;
            }
        }

        final boolean backupAdvPriority = solverPD.getPriorityFlag();
        final boolean backupMessageFlag = solverPD.getMessageFlag();
        final boolean backupTimeoutFlag = solverPD.getTimeoutFlag();
        solverPD.timeoutSwitch(SolverInterface.SWITCH_OFF);
        solverPD.messageSwitch(SolverInterface.SWITCH_OFF);
        solverPD.advPrioritySwitch(SolverInterface.SWITCH_ON);

        AdvancedBoard advBoard = new AdvancedBoard(board);
        byte lookup = AdvancedBoard.getLookupKey(board.getZero1d());
        int group = AdvancedBoard.getGroup(board.getZero1d());

        if (activeMap.containsKey(advBoard)) {
            AdvancedMoves advMoves = activeMap.get(advBoard);
            if (group == 3) {
                advMoves.updateSolution(lookup, solverPD.moves(), solution, isSymmetry);
            } else {
                advMoves.updateSolution(lookup, solverPD.moves(), solution, !isSymmetry);
            }
            add2file(advBoard, advMoves);
            inSolver.advPrioritySwitch(backupAdvPriority);
            inSolver.messageSwitch(backupMessageFlag);
            inSolver.timeoutSwitch(backupTimeoutFlag);
            return true;
        }

        AdvancedBoard advBoardSym = null;
        if (group == 0 || group == 2) {
            advBoardSym = new AdvancedBoard(new Board(board.getTilesSym()));
        }
        if (activeMap.containsKey(advBoardSym)) {
            AdvancedMoves advMoves = activeMap.get(advBoardSym);
            if (lookup == 1) {
                lookup = 3;
            } else if (lookup == 3) {
                lookup = 1;
            }
            advMoves.updateSolution(lookup, solverPD.moves(), solution, isSymmetry);
            add2file(advBoardSym, advMoves);
            inSolver.advPrioritySwitch(backupAdvPriority);
            inSolver.messageSwitch(backupMessageFlag);
            inSolver.timeoutSwitch(backupTimeoutFlag);
            return true;
        }

        AdvancedMoves advMoves = new AdvancedMoves(board.getZero1d(), solverPD.moves());
        if (group == 3) {
            advMoves.updateSolution(lookup, solverPD.moves(), solution, isSymmetry);
        } else {
            advMoves.updateSolution(lookup, solverPD.moves(), solution, !isSymmetry);
        }
        activeMap.put(advBoard, advMoves);
        add2file(advBoard, advMoves);
        inSolver.advPrioritySwitch(backupAdvPriority);
        inSolver.messageSwitch(backupMessageFlag);
        inSolver.timeoutSwitch(backupTimeoutFlag);
        return true;
    }

    /**
     * If the solver is SolverPD object and last search board in activeMap
     * that need to verify; verify the full set and return true.
     *
     * @param inSolver the given SolverIntegerface
     * @return boolean if last search board in activeMap has been verified.
     */
    public boolean updateLastSearch(SolverInterface inSolver) {
        if (activeMap == null) {
            return false;
        }
        if (inSolver == null || !validateSolver(inSolver)) {
            return false;
        }
        if (inSolver.isSearchTimeout() || inSolver.searchTime() < cutoffLimit) {
            return false;
        }

        SolverPD solverPD = (SolverPD) inSolver;
        final boolean backupAdvPriority = solverPD.getPriorityFlag();
        final boolean backupMessageFlag = solverPD.getMessageFlag();
        final boolean backupTimeoutFlag = solverPD.getTimeoutFlag();
        solverPD.timeoutSwitch(SolverInterface.SWITCH_OFF);
        solverPD.messageSwitch(SolverInterface.SWITCH_OFF);
        solverPD.advPrioritySwitch(SolverInterface.SWITCH_ON);

        Board board = solverPD.lastSearchBoard();
        AdvancedBoard advBoard = new AdvancedBoard(board);
        int group = AdvancedBoard.getGroup(board.getZero1d());

        if (activeMap.containsKey(advBoard)) {
            AdvancedMoves advMoves = activeMap.get(advBoard);
            if (!advMoves.isCompleted()) {
                advMoves.updateSolutions(advBoard, solverPD);
                add2file(advBoard, advMoves);
            }
            inSolver.advPrioritySwitch(backupAdvPriority);
            inSolver.messageSwitch(backupMessageFlag);
            inSolver.timeoutSwitch(backupTimeoutFlag);
            return true;
        }

        AdvancedBoard advBoardSym = null;
        if (group == 0 || group == 2) {
            advBoardSym = new AdvancedBoard(new Board(board.getTilesSym()));
        }
        if (activeMap.containsKey(advBoardSym)) {
            AdvancedMoves advMoves = activeMap.get(advBoardSym);
            if (!advMoves.isCompleted()) {
                advMoves.updateSolutions(advBoardSym, solverPD);
                add2file(advBoardSym, advMoves);
            }
            inSolver.advPrioritySwitch(backupAdvPriority);
            inSolver.messageSwitch(backupMessageFlag);
            inSolver.timeoutSwitch(backupTimeoutFlag);
            return true;
        }

        inSolver.advPrioritySwitch(backupAdvPriority);
        inSolver.messageSwitch(backupMessageFlag);
        inSolver.timeoutSwitch(backupTimeoutFlag);
        return false;
    }

    // remove the given board from reference boards collection if exists, except
    // default reference boards.
    void removeBoard(Board board) {
        AdvancedBoard advBoard = new AdvancedBoard(board);
        if (defaultMap.containsKey(advBoard)) {
            System.out.println("It's not allow to remove a default board.");
            return;
        }
        if (activeMap.containsKey(advBoard)) {
            activeMap.remove(advBoard);
        }
    }

    // print the current status of reference boards collection.
    void printStatus() {
        System.out.println("Data file size: " + (new File(filePath).length())
                + " saved at " + new Date((new File(filePath)).lastModified()));
        System.out.println("Boards takes over " + cutoffSetting + "s will store in file.");
        System.out.println(activeMap.size() + " of boards stored in data file.\n");
    }

    // print all reference boards and it's components.
    void printAllBoards() {
        for (Entry<AdvancedBoard, AdvancedMoves> entry : activeMap.entrySet()) {
            AdvancedBoard advBoard = entry.getKey();
            for (int i = 0; i < puzzleSize; i++) {
                System.out.print(advBoard.getTiles()[i] + " ");
            }
            System.out.println();
            for (int i = 0; i < puzzleSize; i++) {
                System.out.print(advBoard.tilesTransform[i] + " ");
            }
            AdvancedMoves advMoves = entry.getValue();
            System.out.println("\t" + Integer.toBinaryString(advMoves.status));
            for (int i = 0; i < 4; i++) {
                System.out.println(advMoves.moves[i] + "\t" + advMoves.initMoves[i]
                        + "\t" + Arrays.toString(advMoves.getInitialMoves(i, !isSymmetry)));
            }
        }
        System.out.println();
    }

    // change the cutoff setting with the given integer in second, range from 1 to 10 and
    // save a new copy if stand alone or off network
    void setCutoffArchive(int cutoff) {
        if (cutoff < 1) {
            System.out.println(cutoff + " below minimum cutoff limit 1.0s - stop and no change");
            return;
        } else if (cutoff > 10) {
            System.out.println(cutoff + " above maximum cutoff limit 10.0s - stop and no change");
            return;
        } else if (cutoff == cutoffSetting) {
            System.out.println("Same cutoff limit - no change.");
            return;
        }
        cutoffSetting = cutoff;
        cutoffLimit = cutoffSetting * defaultCutoffBuffer;

        System.out.println("Cutoff archive limit changed to " + cutoffSetting
                + " seconds, existing archive boards will remain as is.");
        refreshFile();
    }

    // TODO - RMI : Connection off only
    // save all reference board in a new copy
    void refreshFile() {
        System.out.println("Please make sure connection is off to prcessed:");
        System.out.println("Enter 'Y' to continue, other to quit.");

        createFile();
        for (Entry<AdvancedBoard, AdvancedMoves> entry : activeMap.entrySet()) {
            add2file(entry.getKey(), entry.getValue());
        }
    }

    // reset the reference boards collection to default setting and save a new copy
    // if stand alone or off network
    void reset() {
        System.out.println("Reset to initial setup, all stored boards will removed.");
        System.out.println("Cutoff archive limit reset to " + defaultCutoffLimit + "s");
        cutoffSetting = defaultCutoffLimit;
        cutoffLimit = cutoffSetting * defaultCutoffBuffer;

        HashMap<AdvancedBoard, AdvancedMoves> oldMap = activeMap;
        activeMap = new HashMap<AdvancedBoard, AdvancedMoves>();
        for (AdvancedBoard advBoard : defaultMap.keySet()) {
            activeMap.put(advBoard, oldMap.get(advBoard));
        }

        System.out.println("Reset completed");
        refreshFile();
    }
}
