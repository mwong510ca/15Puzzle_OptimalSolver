package mwong.myprojects.fifteenpuzzle.solution.ai;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import mwong.myprojects.fifteenpuzzle.FileProperties;
import mwong.myprojects.fifteenpuzzle.puzzle.Board;
import mwong.myprojects.fifteenpuzzle.puzzle.HeuristicOptions;
import mwong.myprojects.fifteenpuzzle.solution.Solver;
import mwong.myprojects.fifteenpuzzle.solution.SolverPdb78;
import mwong.myprojects.fifteenpuzzle.solution.Solver.ApplicationMode;
import mwong.myprojects.fifteenpuzzle.solution.SolverBuilder;

/**
 * ReferenceLog implements Reference interface of the reference collections.
 * It has full features of load the storage, add or remove a board, change setting,
 * reset the collection, etc.
 *
 * <p>Dependencies : FileProperties.java, Board.java, HeuristicOptions.java,
 *                   Solver.java, SolverPdb78.java, SolverBuilder.java
 *
 * @author <a href="http://www.linkedin.com/pub/macy-wong/46/550/37b/"
 *            target="_blank">Meisze Wong (linkedin)</a>
 * @see <a href="http://www.github.com/mwong510ca/15PuzzleOptimalSolver/"
 *         target="_blank">GitHub (full project)</a>
 */
public class ReferenceLog implements Reference {
  /** The String of directory of data files. */
  static final String DIRECTORY = FileProperties.getDirectory();
  /** The String of file path of data files. */
  static final String FILEPATH = FileProperties.getFilepathReference();
  /** The buffer size per record.
   *  @see ReferenceConstants#BUFFER_SIZE_PER_RECORD */
  static final int BUFFER_SIZE_PER_RECORD = ReferenceConstants.getBufferSizePerRecord();
  /** Constant number, 100.0. */
  static final double HUNDRED = 100.0;
  /** The byte value of mirror flip group.
   *  @see ReferenceConstants#MIRROR_FLIP_GROUP */
  static final byte MIRROR_FLIP_GROUP = ReferenceConstants.getMirrorFlipGroup();
  /** The bit size of tile value. (0 - 15)
   *  @see mwong.myprojects.fifteenpuzzle.puzzle.PuzzleConstants#TILE_BIT_SIZE */
  static final int TILE_BIT_SIZE = ReferenceConstants.getTileBitSize();
  /** The number of lookups, split in 4 groups, 4 lookup per group.
   *  @see ReferenceConstants#NUM_LOOKUPS*/
  static final int NUM_LOOKUPS = ReferenceConstants.getNumLookups();

  /** The HashMap of reference collections. */
  HashMap<ReferenceBoard, ReferenceMoves> referenceMap;
  /** The HashMap of default boards. These boards never remove from collections.
   *  @see ReferenceProperties#DEFAULT_BOARDS */
  HashMap<ReferenceBoard, ReferenceMoves> defaultMap;
  /** The integer of cutoff setting. */
  int cutoffSetting;
  /** The double value of cutoff setting with buffer percentage off. */
  double cutoffLimit;
  /** The boolean value represents the data file is ready to use. */
  boolean fileReady = false;
  /** The boolean value represents a copy of local solver is ready to use. */
  boolean solverReady = false;
  /** The instance of SolverPdb78 object. */
  SolverPdb78 localSolver = null;
  /** The ConnectionType in use. */
  ConnectionType connectionTypeInUse;

  /**
   * Initializes ReferenceLog object. Load the stored collection from file.
   * Use default setting if not available.
   *
   * @param connectionType the choice connectionType
   */
  public ReferenceLog(final ConnectionType connectionType) {
    this.connectionTypeInUse = connectionType;
    try {
      referenceMap = new HashMap<ReferenceBoard, ReferenceMoves>();
      loadFile();
    } catch (IOException ex) {
      reset();
    }
  }

  /**
   * Load the reference collection from file.
   *
   * @throws IOException any IOExcpetion
   */
  private void loadFile() throws IOException {
    FileInputStream fin = new FileInputStream(FILEPATH);
    FileChannel inChannel = fin.getChannel();
    ByteBuffer buffer = inChannel.map(FileChannel.MapMode.READ_ONLY, 0, inChannel.size());

    cutoffSetting = buffer.getInt();
    int cutoffBuffer = ReferenceProperties.getCutoffBuffer();
    cutoffLimit = cutoffSetting * ((HUNDRED - cutoffBuffer) / HUNDRED);

    while (buffer.remaining() >= BUFFER_SIZE_PER_RECORD) {
      ReferenceBoard advBoard = null;

      long transformKey = buffer.getLong();
      byte group = buffer.get();
      int hash1 = buffer.getInt();
      int hash2 = buffer.getInt();
      int hashcode = buffer.getInt();
      advBoard = new ReferenceBoard(transformKey, group, hash1, hash2, hashcode);

      byte[] moves = new byte[NUM_LOOKUPS];
      buffer.get(moves);
      short[] initMoves = new short[NUM_LOOKUPS];
      for (int i = 0; i < NUM_LOOKUPS; i++) {
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

  /**
   * Load the default reference collection.
   */
  final void loadDefault() {
    defaultMap = new HashMap<ReferenceBoard, ReferenceMoves>();
    for (byte[][] preset : ReferenceProperties.getDefaultBoards()) {
      ReferenceBoard advBoard = new ReferenceBoard(new Board(preset[0]));
      ReferenceMoves advMoves = new ReferenceMoves(preset[1][0], preset[1][1]);
      defaultMap.put(advBoard, advMoves);
    }
  }

  /**
   * Clear default reference collection.
   */
  final void clearDefault() {
    defaultMap = null;
  }

  /**
   * Reset to reference collection to default setting.
   */
  final void reset() {
    cutoffSetting = ReferenceProperties.getDefaultCutoffLimit();
    System.out.println("Default setting : cutoff archive limit - "
        + cutoffSetting);
    int cutoffBuffer = ReferenceProperties.getCutoffBuffer();
    cutoffLimit = cutoffSetting * ((HUNDRED - cutoffBuffer) / HUNDRED);

    loadDefault();
    referenceMap = new HashMap<ReferenceBoard, ReferenceMoves>();
    for (Entry<ReferenceBoard, ReferenceMoves> entry : defaultMap.entrySet()) {
      referenceMap.put(entry.getKey(), entry.getValue());
    }
    clearDefault();
  }

  @Override
  public final HashMap<ReferenceBoard, ReferenceMoves> getActiveMap() throws NullPointerException {
    if (referenceMap == null) {
      reset();
    }
    if (referenceMap == null || referenceMap.isEmpty()) {
      throw new NullPointerException("getActiveMap is empyt");
    }
    return referenceMap;
  }

  @Override
  public final ConnectionType getConnectionTypeInUse() {
    return connectionTypeInUse;
  }

  @Override
  public final double getCutoffLimit() {
    return cutoffLimit;
  }

  @Override
  public final boolean loadSolver(final Solver copySolver) {
    if (solverReady) {
      return false;
    }
    if (copySolver.getHeuristic() != HeuristicOptions.PD78) {
      System.out.println("error 1");
      return false;
    }
    SolverBuilder builder = new SolverBuilder(ApplicationMode.SYSTEM);
    builder.setReference(copySolver.getReference());
    localSolver = builder.duplicateSolverPdb78(copySolver);
    solverReady = true;
    return solverReady;
  }

  @Override
  public final boolean hasSolver78() {
    return localSolver != null;
  }

  @Override
  public final SolverPdb78 getSolver78() {
    if (hasSolver78()) {
      return localSolver;
    } else {
      throw new UnsupportedOperationException();
    }
  }

  @Override
  public final boolean containsBoard(final Board board) {
    if (referenceMap == null) {
      System.err.println("empty map");
      return false;
    }

    ReferenceBoard advBoard = new ReferenceBoard(board);
    int group = ReferenceConstants.getReferenceGroup(board.getZero1d());
    if (referenceMap.containsKey(advBoard)) {
      return true;
    }

    if (group == 0 || group == 2) {
      advBoard = new ReferenceBoard(new Board(board.getTilesMirror()));
      if (referenceMap.containsKey(advBoard)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Append a reference board with moves and partial solutions to file.
   *
   * @param advBoard the given ReferenceBoard from function call
   * @param advMoves the given ReferenceMoves from function call
   */
  synchronized void add2file(final ReferenceBoard advBoard, final ReferenceMoves advMoves) {
    if (!fileReady) {
      System.err.println("System error : file system is not ready.");
      return;
    }
    if (!(new File(FILEPATH)).exists()) {
      System.err.println("System error : " + FILEPATH + " is missing.");
      return;
    }

    FileOutputStream fout;
    FileChannel outChannel;
    try {
      fout = new FileOutputStream(FILEPATH, true);
      outChannel = fout.getChannel();

      ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE_PER_RECORD);
      long key = 0L;
      for (int val : advBoard.getTilesTransform()) {
        key <<= TILE_BIT_SIZE;
        key |= val;
      }
      buffer.putLong(key);                       // 8
      buffer.put(advBoard.getGroup());           // 1
      buffer.putInt(advBoard.getHash1());        // 4
      buffer.putInt(advBoard.getHash2());        // 4
      buffer.putInt(advBoard.hashCode());        // 4
      buffer.put(advMoves.getMoves());           // 4
      for (short move : advMoves.getInitMoves()) {   // 8 (2x4)
        buffer.putShort(move);
      }
      buffer.put(advMoves.getStatus());          // 1
      buffer.flip();
      outChannel.write(buffer);

      outChannel.close();
      fout.close();
    } catch (IOException ex) {
      System.err.println("System error : write file error - " + FILEPATH);
    }
  }

  @Override
  public final boolean addBoard(final Board board, final byte steps, final Board.Move[] solution) {
    if (referenceMap == null) {
      System.err.println("empty map");
      return false;
    }

    ReferenceBoard advBoard = new ReferenceBoard(board);
    byte lookup = ReferenceConstants.getReferenceLookup(board.getZero1d());
    int group = ReferenceConstants.getReferenceGroup(board.getZero1d());

    if (referenceMap.containsKey(advBoard)) {
      ReferenceMoves advMoves = referenceMap.get(advBoard);
      if (group == MIRROR_FLIP_GROUP) {
        advMoves.updateSolution(lookup, steps, solution, true);
      } else {
        advMoves.updateSolution(lookup, steps, solution, false);
      }
      add2file(advBoard, advMoves);
      return true;
    }

    ReferenceBoard advBoardMirror = null;
    if (group == 0 || group == 2) {
      advBoardMirror = new ReferenceBoard(new Board(board.getTilesMirror()));
    }

    if (referenceMap.containsKey(advBoardMirror)) {
      ReferenceMoves advMoves = referenceMap.get(advBoardMirror);
      if (lookup == 1) {
        lookup = MIRROR_FLIP_GROUP;
      } else if (lookup == MIRROR_FLIP_GROUP) {
        lookup = 1;
      }
      advMoves.updateSolution(lookup, steps, solution, true);
      add2file(advBoardMirror, advMoves);
      return true;
    }

    ReferenceMoves advMoves = new ReferenceMoves(board.getZero1d(), steps);
    if (group == MIRROR_FLIP_GROUP) {
      advMoves.updateSolution(lookup, steps, solution, true);
    } else {
      advMoves.updateSolution(lookup, steps, solution, false);
    }
    referenceMap.put(advBoard, advMoves);
    add2file(advBoard, advMoves);
    return true;
  }

  @Override
  public final boolean updateLastSearch(final Solver copySolver) {
    return updateLastSearch(copySolver, copySolver.lastSearchBoard());
  }

  @Override
  public final boolean updateLastSearch(final Solver copySolver, final Board board) {
    if (referenceMap == null) {
      System.out.println("error 3");
      return false;
    }

    if (board == null) {
      System.out.println("error 4");
    }

    if (solverReady || loadSolver(copySolver)) {
      ReferenceBoard advBoard = new ReferenceBoard(copySolver.lastSearchBoard());
      int group = ReferenceConstants.getReferenceGroup(board.getZero1d());

      if (referenceMap.containsKey(advBoard)) {
        ReferenceMoves advMoves = referenceMap.get(advBoard);
        if (!advMoves.isCompleted()) {
          System.out.println("System update, please wait.");
          advMoves.updateSolutions(advBoard, localSolver);
          add2file(advBoard, advMoves);
        }
        copySolver.clearNewReference();
        return true;
      }

      ReferenceBoard advBoardMirror = null;
      if (group == 0 || group == 2) {
        advBoardMirror = new ReferenceBoard(new Board(board.getTilesMirror()));
      }

      if (referenceMap.containsKey(advBoardMirror)) {
        ReferenceMoves advMoves = referenceMap.get(advBoardMirror);
        if (!advMoves.isCompleted()) {
          System.out.println("System update, please wait.");
          advMoves.updateSolutions(advBoardMirror, localSolver);
          add2file(advBoardMirror, advMoves);
        }
        copySolver.clearNewReference();
        return true;
      }
    }
    return false;
  }

  @Override
  public final void updatePending() {
    if (referenceMap == null) {
      return;
    }
    if (!solverReady) {
      return;
    }

    for (Entry<ReferenceBoard, ReferenceMoves> entry : referenceMap.entrySet()) {
      ReferenceMoves advMoves = entry.getValue();
      if (advMoves.isCompleted()) {
        continue;
      }
      ReferenceBoard advBoard = entry.getKey();
      advMoves.updateSolutions(advBoard, localSolver);
      add2file(advBoard, advMoves);
    }
  }

  @Override
  public final void updatePending(final Solver copySolver) {
    if (solverReady || loadSolver(copySolver)) {
      updatePending();
    }
  }

  @Override
  public final void printStatus() {
    System.out.println("Data file size: " + (new File(FILEPATH).length())
        + " saved at " + new Date((new File(FILEPATH)).lastModified()));
    System.out.println("Boards takes over " + cutoffSetting + "s will store in file.");
    System.out.println(referenceMap.size() + " of boards stored in data file.");
    System.out.println("The cutoff limit with buffer: " + cutoffLimit + "\n");
  }

  @Override
  public final void printAllBoards() {
    int min = Integer.MAX_VALUE;
    int max = Integer.MIN_VALUE;

    if (defaultMap == null) {
      loadDefault();
    }

    for (Entry<ReferenceBoard, ReferenceMoves> entry : referenceMap.entrySet()) {
      if (!defaultMap.containsKey(entry.getKey())) {
        byte[] moves = entry.getValue().getMoves();
        for (byte move : moves) {
          if (move < min) {
            min = move;
          }
          if (move > max) {
            max = move;
          }
        }
        System.out.println(entry.getKey());
        System.out.println(entry.getValue());
      }
    }

    System.out.print("range from : " + min + " " + max + "\n");
  }
}
