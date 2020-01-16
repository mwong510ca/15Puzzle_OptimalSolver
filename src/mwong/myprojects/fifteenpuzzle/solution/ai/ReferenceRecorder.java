package mwong.myprojects.fifteenpuzzle.solution.ai;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Map.Entry;

import mwong.myprojects.fifteenpuzzle.puzzle.Board;
import mwong.myprojects.fifteenpuzzle.server.ReferenceServerProperties;
import mwong.myprojects.fifteenpuzzle.solution.SolverPdb78;
import mwong.myprojects.fifteenpuzzle.solution.Solver.ApplicationMode;
import mwong.myprojects.fifteenpuzzle.solution.Solver.SolverVersion;
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
public final class ReferenceRecorder extends ReferenceLog {
  /**
   * Initializes ReferenceLog object. Load the stored collection from file.
   * Use default setting if not available.
   */
  ReferenceRecorder() {
    this(false);
  }

  /**
   * Initializes ReferenceLog object. Load the stored collection from file.
   * Use default setting if not available.
   *
   * @param reserved represents the remote port is not in used and reserve for recorder
   */
  ReferenceRecorder(final boolean reserved) {
    super(ConnectionType.STANDALONE);
    if (!reserved && ReferenceServerProperties.isPortInUse()) {
      System.out.println("Warning: Port " + ReferenceServerProperties.getRemotePort()
          + " in use, not recommand to modify reference collection.");
      System.out.println("         Please stop the remote server to continue.\n");
    }
    SolverBuilder builder = new SolverBuilder(ApplicationMode.SYSTEM);
    localSolver = builder.createSolverPdb78(this);
    solverReady = true;
  }

  /**
   * Change the cutoff setting with the given integer in second, range from 1 to 10 and
   * save a new copy.
   *
   * @param cutoff the integer value of cutoff setting
   */
  void setCutoffArchive(final int cutoff) {
    final int maxCutoff = 10;
    if (cutoff < 1) {
      System.out.println(cutoff + " below minimum cutoff limit 1.0s - stop and no change");
      return;
    } else if (cutoff > maxCutoff) {
      System.out.println(cutoff + " above maximum cutoff limit 10.0s - stop and no change");
      return;
    } else if (cutoff == cutoffSetting) {
      System.out.println("Same cutoff limit - no change.");
      return;
    }

    cutoffSetting = cutoff;
    int cutoffBuffer = ReferenceProperties.getCutoffBuffer();
    cutoffLimit = cutoffSetting * ((HUNDRED - cutoffBuffer) / HUNDRED);

    System.out.println("Cutoff archive limit changed to " + cutoffSetting
        + " seconds, existing archive boards will remain as is.");
    refreshFile();
  }

  /**
   * Returns the integer value of the cutoff setting.
   *
   * @return integer value of the cutoff setting
   */
  int getCutoffSetting() {
    return cutoffSetting;
  }

  /**
   * If the given solver using pattern database 7-8, and it takes
   * over the cutoff limit solve the puzzle with advanced estimate;
   * add to reference boards collection.
   *
   * @param inSolver the SolverInterface object in use
   * @return boolean boolean
   */
  boolean addBoard(final SolverPdb78 inSolver) {
    return addBoard(inSolver, false);
  }

  /**
   * Add a reference board in collection, allow bypass minimum
   * cutoff limit requirement.
   *
   * @param inSolver the given SolverPdb78 object, add the latest searched board
   * to reference collection
   * @param bypass the boolean flag to bypass cutoff limit requirement
   * @return boolean represents add to collection success
   */
  boolean addBoard(final SolverPdb78 inSolver, final boolean bypass) {
    if (referenceMap == null) {
      return false;
    }
    if (!solverReady && !loadSolver(inSolver)) {
      return false;
    }
    if (!bypass && inSolver.searchTime() < getCutoffLimit()) {
      return false;
    }

    Board board = inSolver.lastSearchBoard();
    Board.Move[] solution = inSolver.solution().clone();
    byte moves = inSolver.moves();

    if (!bypass && inSolver.getVersion() != SolverVersion.OPTIMUM) {
      int heuristicOrg = localSolver.heuristicBasis(board);
      int heuristicAdv = localSolver.heuristicBoost(board);
      if (heuristicOrg != heuristicAdv) {
        return false;
      }
    }

    ReferenceBoard advBoard = new ReferenceBoard(board);
    byte lookup = ReferenceConstants.getReferenceLookup(board.getZero1d());
    int group = ReferenceConstants.getReferenceGroup(board.getZero1d());

    if (referenceMap.containsKey(advBoard)) {
      ReferenceMoves advMoves = referenceMap.get(advBoard);
      if (group == MIRROR_FLIP_GROUP) {
        advMoves.updateSolution(lookup, moves, solution, true);
      } else {
        advMoves.updateSolution(lookup, moves, solution, false);
      }
      if (bypass && !advMoves.isCompleted()) {
         advMoves.updateSolutions(advBoard, localSolver);
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
      advMoves.updateSolution(lookup, moves, solution, true);
      if (bypass && !advMoves.isCompleted()) {
        advMoves.updateSolutions(advBoardMirror, localSolver);
      }
      add2file(advBoardMirror, advMoves);
      return true;
    }

    ReferenceMoves advMoves = new ReferenceMoves(board.getZero1d(), moves);
    if (group == MIRROR_FLIP_GROUP) {
      advMoves.updateSolution(lookup, moves, solution, true);
    } else {
      advMoves.updateSolution(lookup, moves, solution, false);
    }
    if (bypass && !advMoves.isCompleted()) {
      advMoves.updateSolutions(advBoard, localSolver);
    }
    referenceMap.put(advBoard, advMoves);
    add2file(advBoard, advMoves);
    return true;
  }

  /**
   * Remove the given board from reference boards collection if exists, except
   * default reference boards.
   *
   * @param board the given board to be remove from collection
   */
  void removeBoard(final Board board) {
    if (defaultMap == null) {
      loadDefault();
    }

    ReferenceBoard advBoard = new ReferenceBoard(board);
    if (defaultMap.containsKey(advBoard)) {
      return;
    }

    if (referenceMap.containsKey(advBoard)) {
      referenceMap.remove(advBoard);
    }
    refreshFile();
  }

  /**
   * Verify the given solver is using pattern database 7-8, scan the full
   * collection, if the reference board is not verified, verify it now.
   */
  void updateData() {
    if (localSolver == null) {
      throw new NullPointerException("Solver is null, admin mode.");
    }
    updatePending();
    refreshFile();
  }

  /**
   * Create the new file to store the reference boards.
   */
  private void createFile() {
    fileReady = false;
    if (!(new File(DIRECTORY)).exists()) {
      (new File(DIRECTORY)).mkdir();
    }

    if ((new File(FILEPATH)).exists()) {
      (new File(FILEPATH)).delete();
    }

    FileOutputStream fout;
    FileChannel outChannel;
    ByteBuffer buffer;

    try {
      fout = new FileOutputStream(FILEPATH);
      outChannel = fout.getChannel();
      final int emptyFileSize = 8;
      buffer = ByteBuffer.allocateDirect(emptyFileSize);
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

  /**
   * Save all reference board in a new copy.
   */
 void refreshFile() {
    createFile();
    for (Entry<ReferenceBoard, ReferenceMoves> entry : referenceMap.entrySet()) {
      add2file(entry.getKey(), entry.getValue());
    }
  }
}
