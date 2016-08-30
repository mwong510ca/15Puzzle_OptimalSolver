package mwong.myprojects.fifteenpuzzle.solver.advanced.ai;

import mwong.myprojects.fifteenpuzzle.solver.HeuristicOptions;
import mwong.myprojects.fifteenpuzzle.solver.Solver;
import mwong.myprojects.fifteenpuzzle.solver.advanced.SmartSolverPdb;
import mwong.myprojects.fifteenpuzzle.solver.components.Board;
import mwong.myprojects.fifteenpuzzle.solver.components.Direction;
import mwong.myprojects.fifteenpuzzle.solver.components.FileProperties;
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

/**
 * ReferenceBoard is the data type of stored board of reference collection.
 * It analysis each board's actual number of moves, first 8 moves to goal state,
 * and a conversion set for reverse estimate (use reference board as goal state).
 *
 * <p>Dependencies : Board.java, Direction.java, FileProperties.java, HeuristicOptions.java,
 *                   PatternOptions.java, ReferenceBoard.java, ReferenceConstants.java,
 *                   ReferenceMoves.java, ReferenceProperties.java, SmartSolverPD.java, Solver.java
 *
 * @author   Meisze Wong
 *           www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public class ReferenceAccumulator {
    private final String directory;
    private final String filepath;
    private final String coreSolverClassName;
    private final HeuristicOptions coreHeuristic;
    private final boolean symmetry;
    private final boolean onSwitch;
    private final boolean offSwitch;

    private HashMap<ReferenceBoard, ReferenceMoves> referenceMap;
    private HashMap<ReferenceBoard, ReferenceMoves> defaultMap;
    private int cutoffSetting;
    private double cutoffLimit;
    private boolean fileReady = false;

    /**
     * Initializes ReferenceBoard object.  Load the stored boards from file or use default set.
     */
    public ReferenceAccumulator() {
        directory = FileProperties.getDirectory();
        filepath = FileProperties.getFilepathReference();
        coreSolverClassName = ReferenceConstants.getCoreSolverClassName();
        coreHeuristic = ReferenceConstants.getCoreHeuristic();
        symmetry = ReferenceConstants.isSymmetry();
        onSwitch = ReferenceConstants.isOnSwitch();
        offSwitch = !onSwitch;

        loadDefault();
        try {
            referenceMap = new HashMap<ReferenceBoard, ReferenceMoves>();
            System.out.println("Load data and system update - archived hard board. "
                    + "Please wait.");
            loadFile();
            System.out.println();
        } catch (IOException ex) {
            reset();
        }
        updateData(createSolver());
        refreshFile();
    }

    // load the default set
    private void loadDefault() {
        defaultMap = new HashMap<ReferenceBoard, ReferenceMoves>();
        for (byte[][] preset : ReferenceProperties.getDefaultBoards()) {
            ReferenceBoard advBoard = new ReferenceBoard(new Board(preset[0]));
            ReferenceMoves advMoves = new ReferenceMoves(preset[1][0], preset[1][1]);
            defaultMap.put(advBoard, advMoves);

            if (preset[1][0] == 5) {
                byte[] tiles = preset[0].clone();
                tiles[5] = tiles[6];
                tiles[6] = 0;
                advBoard = new ReferenceBoard(new Board(tiles));
                advMoves = new ReferenceMoves((byte) 6, (byte) (preset[1][1] - 1));
                defaultMap.put(advBoard, advMoves);
            }

            if (preset[1][0] == 10) {
                byte[] tiles = preset[0].clone();
                tiles[10] = tiles[6];
                tiles[6] = 0;
                advBoard = new ReferenceBoard(new Board(tiles));
                advMoves = new ReferenceMoves((byte) 6, (byte) (preset[1][1] - 1));
                defaultMap.put(advBoard, advMoves);
            }
        }
    }

    // reset to default set
    void reset() {
        cutoffSetting = ReferenceProperties.getDefaultCutoffLimit();
        System.out.println("Default setting : cutoff archive limit - "
                + cutoffSetting);
        cutoffLimit = cutoffSetting * ReferenceProperties.getDefaultCutoffBuffer();
        referenceMap = new HashMap<ReferenceBoard, ReferenceMoves>();
        for (Entry<ReferenceBoard, ReferenceMoves> entry : defaultMap.entrySet()) {
            referenceMap.put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Returns a HashMap of collection of reference boards.
     *
     * @return HashMap of collection of reference boards
     */
    public final HashMap<ReferenceBoard, ReferenceMoves> getActiveMap() {
        if (referenceMap == null) {
            return defaultMap;
        }
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
        FileInputStream fin = new FileInputStream(filepath);
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

        if ((new File(filepath)).exists()) {
            (new File(filepath)).delete();
        }

        FileOutputStream fout;
        FileChannel outChannel;
        ByteBuffer buffer;

        try {
            fout = new FileOutputStream(filepath);
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
            fout = new FileOutputStream(filepath, true);
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

    // create and return a SmartSolverPD object.
    SmartSolverPdb createSolver() {
        try {
            SmartSolverPdb solver = new SmartSolverPdb(PatternOptions.Pattern_78, this);
            solver.messageSwitch(offSwitch);
            solver.timeoutSwitch(offSwitch);
            solver.versionSwitch(onSwitch);
            return solver;
        } catch (OutOfMemoryError ex) {
            return null;
        }
    }

    // verify the given solver is SmartSolverPD object using pattern database 7-8
    /**
     * Returns the boolean value of the given solver is the valid for ReferenceAccumulator.
     *
     * @param inSolver the giver concrete instance of Solver interface.
     * @return boolean value of the given solver is the valid for ReferenceAccumulator
     */
    public boolean validateSolver(Solver inSolver) {
        if (inSolver == null) {
            return false;
        }
        if (!inSolver.getClass().getSimpleName().equals(coreSolverClassName)) {
            return false;
        }
        if (inSolver.getHeuristicOptions() != coreHeuristic) {
            return false;
        }
        return true;
    }

    // verify the given solver is using pattern database 7-8, scan the full
    // collection, if the reference board is not verified, verify it now.
    void updateData(Solver inSolver) {
        Solver solver = inSolver;
        if (!validateSolver(inSolver)) {
            solver = createSolver();
        }
        if (solver == null) {
            System.out.println("System update failed - not enough memory.");
            return;
        }
        updateAll((SmartSolverPdb) solver);
    }

    /**
     *  Verify the given solver is using pattern database 7-8, scan the full
     *  collection, if the reference board is not verified, verify it now.
     *
     *  @param inSolver the SolverInterface object in use
     */
    public void updatePending(Solver inSolver) {
        if (!validateSolver(inSolver)) {
            return;
        }
        updateAll((SmartSolverPdb) inSolver);
    }

    // scan the full collection, if the reference board is not verified, verify it now.
    private void updateAll(SmartSolverPdb solverPD) {
        assert solverPD instanceof SmartSolverPdb : "updateAll without SmartSolverPD object";
        final boolean backupAdvPriority = solverPD.getInUseVersionFlag();
        final boolean backupMessageFlag = solverPD.getMessageFlag();
        final boolean backupTimeoutFlag = solverPD.getTimeoutFlag();
        solverPD.timeoutSwitch(offSwitch);
        solverPD.messageSwitch(offSwitch);
        solverPD.versionSwitch(onSwitch);

        for (Entry<ReferenceBoard, ReferenceMoves> entry : referenceMap.entrySet()) {
            ReferenceMoves advMoves = entry.getValue();
            if (advMoves.isCompleted()) {
                continue;
            }
            ReferenceBoard advBoard = entry.getKey();
            advMoves.updateSolutions(advBoard, solverPD);
            add2file(advBoard, advMoves);
        }
        solverPD.versionSwitch(backupAdvPriority);
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
    public boolean addBoard(Solver inSolver) {
        return addBoard(inSolver, false);
    }

    // add a reference board in collection, allow bypass mininum
    // cutoff limit requirement
    boolean addBoard(Solver inSolver, boolean bypass) {
        if (referenceMap == null) {
            return false;
        }
        if (!validateSolver(inSolver)) {
            return false;
        }
        if (!bypass && inSolver.searchTime() < getCutoffLimit()) {
            return false;
        }
        SmartSolverPdb solverPD = (SmartSolverPdb) inSolver;
        Board board = solverPD.lastSearchBoard();
        Direction[] solution = solverPD.solution().clone();

        if (!bypass && !solverPD.getInUseVersionFlag()) {
            int heuristicOrg = solverPD.heuristicStandard(board);
            int heuristicAdv = solverPD.heuristicAdvanced(board);
            if (heuristicOrg != heuristicAdv) {
                return false;
            }
        }

        final boolean backupAdvPriority = solverPD.getInUseVersionFlag();
        final boolean backupMessageFlag = solverPD.getMessageFlag();
        final boolean backupTimeoutFlag = solverPD.getTimeoutFlag();
        solverPD.timeoutSwitch(offSwitch);
        solverPD.messageSwitch(offSwitch);
        solverPD.versionSwitch(onSwitch);

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
            inSolver.versionSwitch(backupAdvPriority);
            inSolver.messageSwitch(backupMessageFlag);
            inSolver.timeoutSwitch(backupTimeoutFlag);
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
            inSolver.versionSwitch(backupAdvPriority);
            inSolver.messageSwitch(backupMessageFlag);
            inSolver.timeoutSwitch(backupTimeoutFlag);
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
        inSolver.versionSwitch(backupAdvPriority);
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
    public boolean updateLastSearch(Solver inSolver) {
        if (referenceMap == null) {
            return false;
        }
        if (!validateSolver(inSolver)) {
            return false;
        }
        if (inSolver.isSearchTimeout() || inSolver.searchTime() < cutoffLimit) {
            return false;
        }

        SmartSolverPdb solverPD = (SmartSolverPdb) inSolver;

        final boolean backupAdvPriority = solverPD.getInUseVersionFlag();
        final boolean backupMessageFlag = solverPD.getMessageFlag();
        final boolean backupTimeoutFlag = solverPD.getTimeoutFlag();
        solverPD.timeoutSwitch(offSwitch);
        solverPD.messageSwitch(offSwitch);
        solverPD.versionSwitch(onSwitch);

        Board board = solverPD.lastSearchBoard();
        ReferenceBoard advBoard = new ReferenceBoard(board);
        int group = ReferenceConstants.getReferenceGroup(board.getZero1d());

        if (referenceMap.containsKey(advBoard)) {
            ReferenceMoves advMoves = referenceMap.get(advBoard);
            if (!advMoves.isCompleted()) {
                advMoves.updateSolutions(advBoard, solverPD);
                add2file(advBoard, advMoves);
            }

            inSolver.versionSwitch(backupAdvPriority);
            inSolver.messageSwitch(backupMessageFlag);
            inSolver.timeoutSwitch(backupTimeoutFlag);
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
            inSolver.versionSwitch(backupAdvPriority);
            inSolver.messageSwitch(backupMessageFlag);
            inSolver.timeoutSwitch(backupTimeoutFlag);
            return true;
        }
        inSolver.versionSwitch(backupAdvPriority);
        inSolver.messageSwitch(backupMessageFlag);
        inSolver.timeoutSwitch(backupTimeoutFlag);
        return false;
    }

    // remove the given board from reference boards collection if exists, except
    // default reference boards.
    void removeBoard(Board board) {
        ReferenceBoard advBoard = new ReferenceBoard(board);
        if (defaultMap.containsKey(advBoard)) {
            return;
        }
        if (referenceMap.containsKey(advBoard)) {
            referenceMap.remove(advBoard);
        }
    }

    // print the current status of reference boards collection.
    void printStatus() {
        System.out.println("Data file size: " + (new File(filepath).length())
                + " saved at " + new Date((new File(filepath)).lastModified()));
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
        //System.out.println("Please make sure connection is off to prcessed:");
        //System.out.println("Enter 'Y' to continue, other to quit.");

        createFile();
        for (Entry<ReferenceBoard, ReferenceMoves> entry : referenceMap.entrySet()) {
            add2file(entry.getKey(), entry.getValue());
        }
    }
}
