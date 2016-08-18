/****************************************************************************
 *  @author   Meisze Wong
 *            www.linkedin.com/pub/macy-wong/46/550/37b/
 *
 *  Compilation  : javac AdvancedAccumulator.java
 *  Dependencies : ReferenceBoard.java, ReferenceMoves.java, Board.java,
 *                 SolverInterface.java, SolverPD.java
 *
 *  A data type of collection of reference boards.  It analysis each
 *  board's actual number of moves, first 8 moves to goal state, and a
 *  conversion set for reverse estimate (use reference board as goal state).
 *
 ****************************************************************************/

package mwong.myprojects.fifteenpuzzle.solver.advanced.ai;

import mwong.myprojects.fifteenpuzzle.solver.Solver;
import mwong.myprojects.fifteenpuzzle.solver.advanced.SmartSolverPD;
import mwong.myprojects.fifteenpuzzle.solver.HeuristicOptions;
import mwong.myprojects.fifteenpuzzle.solver.components.Board;
import mwong.myprojects.fifteenpuzzle.solver.components.Direction;
import mwong.myprojects.fifteenpuzzle.solver.components.PatternOptions;

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

public class ReferenceAccumulator {
    private final String directory = "database";
    private final String seperator = System.getProperty("file.separator");
    private final String filePath = directory + seperator + "advanced_accumulator.db";

    private HashMap<ReferenceBoard, ReferenceMoves> referenceMap;
    private int cutoffSetting;
    private double cutoffLimit;
    private boolean fileReady = false;
    private final boolean symmetry;

    /**
     * Initializes A object.  Load the stored boards from file or use default set.
     */
    public ReferenceAccumulator() {
    	symmetry = ReferenceConstants.isSymmetry();
        try {
        	referenceMap = new HashMap<ReferenceBoard, ReferenceMoves>();
            System.out.println("Load data and system update - archived hard board. "
                    + "Please wait.");
            loadFile();
            System.out.println();
        } catch (IOException ex) {
            loadDefault();
        }
        updateData(createSolver());
        refreshFile();
    }

    private void loadDefault() {
    	cutoffSetting = ReferenceProperties.getDefaultCutoffLimit();
        System.out.println("Default setting : cutoff archive limit - "
                + cutoffSetting);
        cutoffLimit = cutoffSetting * ReferenceProperties.getDefaultCutoffBuffer();
        referenceMap = new HashMap<ReferenceBoard, ReferenceMoves>();
    	for (byte[][] preset : ReferenceProperties.getDefaultBoards()) {
        	ReferenceBoard advBoard = new ReferenceBoard(new Board(preset[0]));
        	ReferenceMoves advMoves = new ReferenceMoves(preset[1][0], preset[1][1]);
        	referenceMap.put(advBoard, advMoves);

            if (preset[1][0] == 5) {
                byte[] tiles = preset[0].clone();
                tiles[5] = tiles[6];
                tiles[6] = 0;
                advBoard = new ReferenceBoard(new Board(tiles));
                advMoves = new ReferenceMoves((byte) 6, (byte) (preset[1][1] - 1));
                referenceMap.put(advBoard, advMoves);
            }

            if (preset[1][0] == 10) {
                byte[] tiles = preset[0].clone();
                tiles[10] = tiles[6];
                tiles[6] = 0;
                advBoard = new ReferenceBoard(new Board(tiles));
                advMoves = new ReferenceMoves((byte) 6, (byte) (preset[1][1] - 1));
                referenceMap.put(advBoard, advMoves);
            }
        }
    }
    
    /**
     * Returns a HashMap of collection of reference boards.
     *
     * @return HashMap of collection of reference boards
     */
    public final HashMap<ReferenceBoard, ReferenceMoves> getActiveMap() {
        return referenceMap;
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
        cutoffLimit = cutoffSetting * ReferenceProperties.getDefaultCutoffBuffer();

        while (buffer.remaining() >= 34) {
            ReferenceBoard advBoard = null;

            long transformKey = buffer.getLong();
            byte group = buffer.get();
            int hash1 = buffer.getInt();
            int hash2 = buffer.getInt();
            int hashcode = buffer.getInt();
            advBoard = new ReferenceBoard(transformKey, group, hash1, hash2, hashcode);

            byte[] moves = new byte[4];
            buffer.get(moves);
            short[] initMoves = new short[4];
            for (int i = 0; i < 4; i++) {
                initMoves[i] = buffer.getShort();
            }
            byte status = buffer.get();
            if (referenceMap.containsKey(advBoard)) {
                ReferenceMoves advMoves = referenceMap.get(advBoard);
                advMoves.updateMoves(moves, initMoves, status);
            } else {
                ReferenceMoves advMoves = new ReferenceMoves(moves, initMoves, status);
                referenceMap.put(advBoard, advMoves);
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
    private void add2file(ReferenceBoard advBoard, ReferenceMoves advMoves) {
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
    SmartSolverPD createSolver() {
        try {
        	SmartSolverPD solver = new SmartSolverPD(PatternOptions.Pattern_78, this);
            //TODO 
            /*
            solver.setReferencedAccumulator(this);
            solver.messageSwitch(false);
            solver.timeoutSwitch(false);
            solver.advPrioritySwitch(true);
            */
            return solver;
        } catch (OutOfMemoryError ex) {
            return null;
        }
    }

    // verify the given solver is SolverPD object using pattern database 7-8
    private boolean validateSolver(Solver inSolver) {
        if (inSolver != null && inSolver.getHeuristicOptions() == HeuristicOptions.PD78) {
            return true;
        }
        return false;
    }

    // verify the given solver is using pattern database 7-8, scan the full
    // collection, if the reference board is not verified, verify it now.
    void updateData(Solver inSolver) {
        Solver solver = inSolver;
        if (inSolver == null || !validateSolver(inSolver)) {
            solver = createSolver();
        }
        if (solver == null) {
            System.out.println("System update failed - not enough memory.");
            return;
        }
        updateAll((SmartSolverPD) solver);
    }

    /**
     *  Verify the given solver is using pattern database 7-8, scan the full
     *  collection, if the reference board is not verified, verify it now.
     *
     *  @param inSolver the SolverInterface object in use
     */
    public void updatePending(Solver inSolver) {
        if (inSolver == null || !validateSolver(inSolver)) {
            return;
        }
        updateAll((SmartSolverPD) inSolver);
    }

    // scan the full collection, if the reference board is not verified, verify it now.
    private void updateAll(SmartSolverPD solverPD) {
        assert solverPD instanceof SmartSolverPD : "updateAll did not recevie SolverPD object";
    	// TODO
    	/*
        final boolean backupAdvPriority = solverPD.getPriorityFlag();
        final boolean backupMessageFlag = solverPD.getMessageFlag();
        final boolean backupTimeoutFlag = solverPD.getTimeoutFlag();
        solverPD.timeoutSwitch(SWITCH_OFF);
        solverPD.messageSwitch(SWITCH_OFF);
        solverPD.advPrioritySwitch(SWITCH_ON);
		*/
        for (Entry<ReferenceBoard, ReferenceMoves> entry : referenceMap.entrySet()) {
            ReferenceMoves advMoves = entry.getValue();
            if (advMoves.isCompleted()) {
                continue;
            }
            ReferenceBoard advBoard = entry.getKey();
            advMoves.updateSolutions(advBoard, solverPD);
            add2file(advBoard, advMoves);
        }
        /*
        solverPD.advPrioritySwitch(backupAdvPriority);
        solverPD.messageSwitch(backupMessageFlag);
        solverPD.timeoutSwitch(backupTimeoutFlag);
        */
    }

    /**
     * If the given solver using pattern database 7-8, and it takes
     * over the cutoff limit solve the puzzle with advanced estimate;
     * add to reference boards collection.
     *
     * @param inSolver the SolverInterface object in use
     */
    public boolean addBoard(Solver inSolver) {
        return addBoard(inSolver, false);
    }

    // add a reference board in collection, allow bypass mininum
    // cutoff limit requirement
    boolean addBoard(Solver inSolver, boolean bypass) {
        if (referenceMap == null) {
            return false;
        }
        if (inSolver == null || !validateSolver(inSolver)) {
            return false;
        }
        if (!bypass && inSolver.searchTime() < getCutoffLimit()) {
            return false;
        }
        SmartSolverPD solverPD = (SmartSolverPD) inSolver;
        Board board = solverPD.lastSearchBoard();
        Direction[] solution = solverPD.solution().clone();
        //TODO
        /*
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
        solverPD.timeoutSwitch(SWITCH_OFF);
        solverPD.messageSwitch(SWITCH_OFF);
        solverPD.advPrioritySwitch(SWITCH_ON);
		*/
        ReferenceBoard advBoard = new ReferenceBoard(board);
        byte lookup = ReferenceConstants.getReferenceLookup(board.getZero1d());
        int group = ReferenceConstants.getReferenceGroup(board.getZero1d());

        if (referenceMap.containsKey(advBoard)) {
            ReferenceMoves advMoves = referenceMap.get(advBoard);
            if (group == 3) {
                advMoves.updateSolution(lookup, solverPD.moves(), solution, symmetry);
            } else {
                advMoves.updateSolution(lookup, solverPD.moves(), solution, !symmetry);
            }
            add2file(advBoard, advMoves);
            /*
            inSolver.advPrioritySwitch(backupAdvPriority);
            inSolver.messageSwitch(backupMessageFlag);
            inSolver.timeoutSwitch(backupTimeoutFlag);
            */
            return true;
        }

        ReferenceBoard advBoardSym = null;
        if (group == 0 || group == 2) {
            advBoardSym = new ReferenceBoard(new Board(board.getTilesSym()));
        }
        if (referenceMap.containsKey(advBoardSym)) {
            ReferenceMoves advMoves = referenceMap.get(advBoardSym);
            if (lookup == 1) {
                lookup = 3;
            } else if (lookup == 3) {
                lookup = 1;
            }
            advMoves.updateSolution(lookup, solverPD.moves(), solution, symmetry);
            add2file(advBoardSym, advMoves);
            /*
            inSolver.advPrioritySwitch(backupAdvPriority);
            inSolver.messageSwitch(backupMessageFlag);
            inSolver.timeoutSwitch(backupTimeoutFlag);
            */
            return true;
        }

        ReferenceMoves advMoves = new ReferenceMoves(board.getZero1d(), solverPD.moves());
        if (group == 3) {
            advMoves.updateSolution(lookup, solverPD.moves(), solution, symmetry);
        } else {
            advMoves.updateSolution(lookup, solverPD.moves(), solution, !symmetry);
        }
        referenceMap.put(advBoard, advMoves);
        add2file(advBoard, advMoves);
        /*
        inSolver.advPrioritySwitch(backupAdvPriority);
        inSolver.messageSwitch(backupMessageFlag);
        inSolver.timeoutSwitch(backupTimeoutFlag);
        */
        return true;
    }

    /**
     * If the solver is SolverPD object and last search board in activeMap
     * that need to verify; verify the full set and return true.
     *
     * @param inSolver the given SolverIntegerface
     * @return boolean if last search board in activeMap has been verified.
     */
    public boolean updateLastSearch(Solver inSolver) {
        if (referenceMap == null) {
            return false;
        }
        if (inSolver == null || !validateSolver(inSolver)) {
            return false;
        }
        if (inSolver.isSearchTimeout() || inSolver.searchTime() < cutoffLimit) {
            return false;
        }

        SmartSolverPD solverPD = (SmartSolverPD) inSolver;
        /*
        final boolean backupAdvPriority = solverPD.getPriorityFlag();
        final boolean backupMessageFlag = solverPD.getMessageFlag();
        final boolean backupTimeoutFlag = solverPD.getTimeoutFlag();
        solverPD.timeoutSwitch(SWITCH_OFF);
        solverPD.messageSwitch(SWITCH_OFF);
        solverPD.advPrioritySwitch(SWITCH_ON);
		*/
        Board board = solverPD.lastSearchBoard();
        ReferenceBoard advBoard = new ReferenceBoard(board);
        int group = ReferenceConstants.getReferenceGroup(board.getZero1d());

        if (referenceMap.containsKey(advBoard)) {
            ReferenceMoves advMoves = referenceMap.get(advBoard);
            if (!advMoves.isCompleted()) {
                advMoves.updateSolutions(advBoard, solverPD);
                add2file(advBoard, advMoves);
            }
            /*
            inSolver.advPrioritySwitch(backupAdvPriority);
            inSolver.messageSwitch(backupMessageFlag);
            inSolver.timeoutSwitch(backupTimeoutFlag);
            */
            return true;
        }

        ReferenceBoard advBoardSym = null;
        if (group == 0 || group == 2) {
            advBoardSym = new ReferenceBoard(new Board(board.getTilesSym()));
        }
        if (referenceMap.containsKey(advBoardSym)) {
            ReferenceMoves advMoves = referenceMap.get(advBoardSym);
            if (!advMoves.isCompleted()) {
                advMoves.updateSolutions(advBoardSym, solverPD);
                add2file(advBoardSym, advMoves);
            }
            /*
            inSolver.advPrioritySwitch(backupAdvPriority);
            inSolver.messageSwitch(backupMessageFlag);
            inSolver.timeoutSwitch(backupTimeoutFlag);
            */
            return true;
        }
        /*
        inSolver.advPrioritySwitch(backupAdvPriority);
        inSolver.messageSwitch(backupMessageFlag);
        inSolver.timeoutSwitch(backupTimeoutFlag);
        */
        return false;
    }

    // remove the given board from reference boards collection if exists, except
    // default reference boards.
    void removeBoard(Board board) {
        ReferenceBoard advBoard = new ReferenceBoard(board);
        // TODO need fix without defaultMap
        /*
        if (defaultMap.containsKey(advBoard)) {
            System.out.println("It's not allow to remove a default board.");
            return;
        }
        */
        if (referenceMap.containsKey(advBoard)) {
            referenceMap.remove(advBoard);
        }
    }

    // print the current status of reference boards collection.
    void printStatus() {
        System.out.println("Data file size: " + (new File(filePath).length())
                + " saved at " + new Date((new File(filePath)).lastModified()));
        System.out.println("Boards takes over " + cutoffSetting + "s will store in file.");
        System.out.println(referenceMap.size() + " of boards stored in data file.\n");
    }

    // print all reference boards and it's components.
    void printAllBoards() {
    	int puzzleSize = ReferenceConstants.getPuzzleSize();
        for (Entry<ReferenceBoard, ReferenceMoves> entry : referenceMap.entrySet()) {
            ReferenceBoard advBoard = entry.getKey();
            for (int i = 0; i < puzzleSize; i++) {
                System.out.print(advBoard.getTiles()[i] + " ");
            }
            System.out.println();
            for (int i = 0; i < puzzleSize; i++) {
                System.out.print(advBoard.tilesTransform[i] + " ");
            }
            ReferenceMoves advMoves = entry.getValue();
            System.out.println("\t" + Integer.toBinaryString(advMoves.status));
            for (int i = 0; i < 4; i++) {
                System.out.println(advMoves.moves[i] + "\t" + advMoves.initMoves[i]
                        + "\t" + Arrays.toString(advMoves.getInitialMoves(i, !symmetry)));
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
        cutoffLimit = cutoffSetting * ReferenceProperties.getDefaultCutoffBuffer();

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
        for (Entry<ReferenceBoard, ReferenceMoves> entry : referenceMap.entrySet()) {
            add2file(entry.getKey(), entry.getValue());
        }
    }

    // reset the reference boards collection to default setting and save a new copy
    // if stand alone or off network
    void reset() {
        System.out.println("Reset to initial setup, all stored boards will removed.");
        loadDefault();
        System.out.println("Reset completed");
        refreshFile();
    }
}
