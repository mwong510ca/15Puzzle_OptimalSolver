package mwong.myprojects.fifteenpuzzle.solution;

import java.rmi.RemoteException;

import mwong.myprojects.fifteenpuzzle.puzzle.Board;
import mwong.myprojects.fifteenpuzzle.puzzle.HeuristicOptions;
import mwong.myprojects.fifteenpuzzle.puzzle.PatternDatabase;
import mwong.myprojects.fifteenpuzzle.puzzle.PatternOptions;

/**
 * SolverPdb78Enh extends SolverSetting, is the 15 puzzle optimal solver using preset
 * Pattern Database 78.  It allow to solve the puzzle with choice of enhancement level.
 * It takes a Board object of the puzzle and solve it with IDA*.
 *
 * <p>Dependencies : Board.java, SolverBuilder.java, PatternDatabase.java, PatternOptions.java
 *                   SolverBuilder.java, SolverConstnats.java, Stopwatch.java and
 *                   SupplementaryEstimator.java
 *
 * @author <a href="http://www.linkedin.com/pub/macy-wong/46/550/37b/"
 *            target="_blank">Meisze Wong (linkedin)</a>
 * @see <a href="http://www.github.com/mwong510ca/15PuzzleOptimalSolver/"
 *         target="_blank">GitHub (full project)</a>
 */
public final class SolverPdb78Enh extends SolverSetup {
  /** The variable of enhancement size. */
  private static final int ENHANCEMENT_SIZE = Level.values().length;
  /** The boolean array of enhancements. */
  private boolean[] enhancement;
  /** The integer of initial pattern database value of the board. */
  private int initPdbValReg = 0;
  /** The integer of initial pattern database value of the board mirror reflection. */
  private int initPdbValMirror = 0;

  /**
   *  Initializes SolverPdb78Enh object using preset pattern database 78.
   *
   * @param appMode the applicationMode
   */
  SolverPdb78Enh(final ApplicationMode appMode) {
    super(HeuristicOptions.PD78);
    if (appMode != ApplicationMode.CONSOLE) {
      throw new UnsupportedOperationException("Pdb split version for console application only.");
    }
    PatternDatabase pdb = new PatternDatabase(PatternOptions.Pattern_78, 0, appMode);
    loadPdbComponents(pdb, appMode);
    pdb = null;
    enhancement = new boolean[ENHANCEMENT_SIZE];
    setInUsePdbPtn(PatternOptions.Pattern_78.getPattern(0));
  }

  /**
   * Set the enhancement level, shift solver version if needed.
   *
   * @param level the given enhancement level.
   */
  private void setEnhancement(final Level level) {
    int val = level.getLevel();
    for (int i = 1; i <= val; i++) {
      enhancement[i] = true;
    }
    for (int i = val + 1; i < ENHANCEMENT_SIZE; i++) {
      enhancement[i] = false;
    }
  }

  /**
   * Returns boolean represents the given board has partial solution
   * in reference collection.
   *
   * @param  board the given Board object.
   * @return boolean represents the given board has partial solution
   *         in reference collection
   */
  public boolean hasPartialSolution(final Board board) {
    SupplementaryEstimator estimator = getEstimator();
    try {
      return estimator.hasPartialSolution(board, getReference().getActiveMap());
    } catch (Exception e) {
      return false;
    }
  }

  @Override
  public void findOptimalPath(final Board board) {
    throw new UnsupportedOperationException("findOptimalPath(board) disabled, "
        + "use findOptimalPath(board, level) instead");
  }

  /**
   * Find the optimal path to goal state if the given board is solvable, solve
   * the puzzle with choice of enhancement level.
   *
   * @param board the initial puzzle Board object to solve
   * @param level the given enhancement level
   */
  public void findOptimalPath(final Board board, final Level level) {
    if (board == null) {
      throw new IllegalArgumentException("Board is null");
    }

    setEnhancement(level);
    stopwatch = new Stopwatch();
    stopwatch.stop();
    stopwatch.reset();
    int limit = -1;
    if (board.isSolvable()) {
      clearHistory();
      if (board.isGoal()) {
        solved = true;
        terminated = true;
      } else {
        stopwatch.start();
        resetDepthSummary(board);
        if (enhancement[Level.BOOST_ESTIMATE.getLevel()]) {
          limit = heuristic(board, SolverVersion.OPTIMUM, SolverAction.SEARCH);
        } else {
          limit = heuristic(board, SolverVersion.PRIME, SolverAction.SEARCH);
        }
        assert limit > 0 : "Board must be solvable and is not the goal state.";
        idaStarEnh(limit);
      }
    } else {
      isSolvable = false;
    }
    searchTime = stopwatch.currentTime();
    stopwatch = null;
  }

  /**
   * Solve the puzzle using interactive deepening A* algorithm.  Start from the initial
   * limit, increment 2 at a time up to maximum 80 until solution found.
   *
   * @param initLimit the initial maximum allowance of solution moves
   */
  private void idaStarEnh(final int initLimit) {
    lastSearchBoard = new Board(tiles);
    flagNewReference = false;

    searchCountBase = 0;
    int limit = initLimit;
    if (enhancement[Level.OPTIMUM.getLevel()] && solutionMove[1] != null) {
      forwardSearch(limit);
      return;
    }

    //System.out.print("ida star " + limit + "\t");
    if (enhancement[Level.PRIME.getLevel()]) {
      int countDir = 0;
      for (int i = 0; i < DIR_SIZE; i++) {
        if (lastDepthSummary[i + DIR_SIZE] > 0) {
          countDir++;
        }
      }

      // quick scan for advanced priority, determine the start order for optimization
      if (countDir > 1) {
        int startLimit = priorityBasis;
        while (startLimit < limit) {
          idaCount = 0;
          dfsStartingOrder(startLimit);
          startLimit += 2;
          boolean overload = false;
          for (int i = DIR_SIZE; i < DIR_SIZE * 2; i++) {
            if (lastDepthSummary[i] > DFS_REVIEW_LIMIT) {
              overload = true;
              break;
            }
          }
          if (overload) {
            break;
          }
        }
      }
    }

    while (limit <= MAX_MOVE) {
      idaCount = 0;
      if (isStatusOn()) {
        System.out.println("ida limit " + limit);
      }
      dfsStartingOrder(limit);
      searchCountBase += idaCount;
      searchNodeCount = searchCountBase;

      if (searchTimeout) {
        searchDepth = limit;
        if (isStatusOn()) {
          System.out.printf("\tNodes : %-15s timeout\n", Integer.toString(idaCount));
        }
        return;
      } else {
        if (isStatusOn()) {
          System.out.printf("\tNodes : %-15s " + stopwatch.currentTime() + "s\n",
              Integer.toString(idaCount));
        }
        if (solved) {
          if (enhancement[Level.PRIME.getLevel()] && searchTime > getRefCutoffLimit()) {
            if (enhancement[Level.BOOST_ESTIMATE.getLevel()]
                || (heuristicBasis(lastBoard) == heuristicBoost(lastBoard))) {
              // update cached advanced priority if added to reference collection
              try {
                flagNewReference = getReference().addBoard(lastBoard, steps, solutionMove);
                if (flagNewReference) {
                  priorityBoost = -1;
                }
              } catch (RemoteException ex) {
                System.err.println("\n" + this.getClass().getSimpleName()
                    + " - Remote connection lost."
                    + "  Remaining process resume to standard version.\n");
                resumePrimeSolver();
              }
            }
          }
          return;
        }
      }
      limit += 2;
    }
  }

  @Override
  void setPriorityBasis(final Board board) throws IllegalArgumentException {
    if (!board.equals(lastBoard)) {
      throw new IllegalArgumentException("must initialize before call setPriorityBasis");
    }

    transPdbCombo();
    initPdbValReg = 0;
    initPdbValMirror = 0;
    for (int i = groupSize; i < groupSizeX2; i++) {
      initPdbValReg += initPdbCombo[i];
      initPdbValMirror += initPdbCombo[i + groupSizeX2];
    }
    priorityBasis = Math.max(initPdbValReg, initPdbValMirror);
  }

  @Override
  void dfsStartingOrder(final int limit) {
    if (enhancement[Level.PRIME.getLevel()]) {
      dfsStartingOrder(zeroX, zeroY, limit, initPdbValReg, initPdbValMirror);
    } else {
      dfsFixedOrder(zeroX, zeroY, limit, initPdbValReg, initPdbValMirror);
    }
  }

  /**
   * Recursive depth first search until it reach the goal state, reach the limit or timeout,
   * the least estimate and node counts will be use to determine the starting order of next search.
   *
   * @param orgX the x-coordinate of zero space
   * @param orgY the y-coordinate of zero space
   * @param limit the maximum search limit
   * @param valReg the initial pattern value of tiles
   * @param valMirror the initial pattern value of tiles with mirror reflection
   */
  private void dfsStartingOrder(final int orgX, final int orgY, final int limit, final int valReg,
      final int valMirror) {
    int zeroPos = orgY * ROW_SIZE + orgX;
    int zeroMirror = MIRROR_POS_TABLE[zeroPos];
    int[] estimate1stMove = new int[DIR_SIZE * 2];
    System.arraycopy(lastDepthSummary, 0, estimate1stMove, 0, DIR_SIZE * 2);
    int[] orgCopy = new int[pdbComboSize];
    System.arraycopy(pdbCombo, 0, orgCopy, 0, pdbComboSize);

    int estimate = limit;
    while (estimate != END_OF_SEARCH) {
      int firstMoveIdx = -1;
      int nodeCount = Integer.MAX_VALUE;

      estimate = END_OF_SEARCH;
      for (int i = 0; i < DIR_SIZE; i++) {
        if (estimate1stMove[i] == END_OF_SEARCH) {
          continue;
        } else if (lastDepthSummary[i] < estimate) {
          estimate = estimate1stMove[i];
          nodeCount = lastDepthSummary[i + DIR_SIZE];
          firstMoveIdx = i;
        } else if (lastDepthSummary[i] == estimate
              && lastDepthSummary[i + DIR_SIZE] < nodeCount) {
          nodeCount = lastDepthSummary[i + DIR_SIZE];
          firstMoveIdx = i;
        }
      }

      if (estimate < END_OF_SEARCH) {
        int startCounter = idaCount++;

        switch (Board.Move.values()[firstMoveIdx]) {
          case RIGHT:
            lastDepthSummary[firstMoveIdx] = shiftRight(orgX, orgY, zeroPos, zeroMirror,
                1, limit, valReg, valMirror, orgCopy, RESET_VAL);
            break;
          case DOWN:
            lastDepthSummary[firstMoveIdx] = shiftDown(orgX, orgY, zeroPos, zeroMirror,
                1, limit, valReg, valMirror, orgCopy, RESET_VAL);
            break;
          case LEFT:
            lastDepthSummary[firstMoveIdx] = shiftLeft(orgX, orgY, zeroPos, zeroMirror,
                1, limit, valReg, valMirror, orgCopy, RESET_VAL);
            break;
          case UP:
            lastDepthSummary[firstMoveIdx] = shiftUp(orgX, orgY, zeroPos, zeroMirror,
                1, limit, valReg, valMirror, orgCopy, RESET_VAL);
            break;
          default:
            assert false : "Error: starting order switch statement";
        }

        lastDepthSummary[firstMoveIdx + DIR_SIZE] = idaCount - startCounter;
        estimate1stMove[firstMoveIdx] = END_OF_SEARCH;
      }
    }
  }

  /**
   * Recursive depth first search with fixed starting order until it reach the goal state,
   * reach the limit or timeout.
   *
   * @param orgX the x-coordinate of zero space
   * @param orgY the y-coordinate of zero space
   * @param limit the maximum search limit
   * @param valReg the initial pattern value of tiles
   * @param valMirror the initial pattern value of tiles with mirror reflection
   */
  private void dfsFixedOrder(final int orgX, final int orgY, final int limit, final int valReg,
      final int valMirror) {
    int zeroPos = orgY * ROW_SIZE + orgX;
    int zeroMirror = MIRROR_POS_TABLE[zeroPos];
    int[] orgCopy = new int[pdbComboSize];
    System.arraycopy(pdbCombo, 0, orgCopy, 0, pdbComboSize);

    boolean pass = true;
    if (enhancement[1]) {
      //pass = pdbNotSymmetry(keys);
      pass = isNotSymmetryPdb();
    }
    //System.out.println(limit + " " + pass);

    if (orgX < ROW_SIZE - 1) {
      shiftRight(orgX, orgY, zeroPos, zeroMirror, 1, limit, valReg, valMirror, orgCopy, RESET_VAL);
    }
    if (pass && orgY < ROW_SIZE - 1) {
      shiftDown(orgX, orgY, zeroPos, zeroMirror, 1, limit, valReg, valMirror, orgCopy, RESET_VAL);
    }
    if (orgX > 0) {
      shiftLeft(orgX, orgY, zeroPos, zeroMirror, 1, limit, valReg, valMirror, orgCopy, RESET_VAL);
    }
    if (pass && orgY > 0) {
      shiftUp(orgX, orgY, zeroPos, zeroMirror, 1, limit, valReg, valMirror, orgCopy, RESET_VAL);
    }
  }

  /**
   * The boolean value determine next clockwise turn is valid, trigger by
   * enhancement level 2.
   *
   * @param chain the sequence of clockwise turns, maximum 5.
   * @return boolean value determine next clockwise turn is valid
   */
  private boolean verifyClockwise(final int chain) {
    if (enhancement[2]) {
      return !((chain & CW_HALF_BITS) == CW_HALF_CYCLE);
    }
    return true;
  }

  /**
   * The boolean value determine next counterclockwise turn is valid, trigger by
   * enhancement level 2.
   *
   * @param chain the sequence of clockwise turns, maximum 4.
   * @return  boolean value determine next counterclockwise turn is valid
   */
  private boolean verifyCounterClockwise(final int chain) {
    if (enhancement[2]) {
      return !((chain & CCW_HALF_BITS) == CCW_HALF_CYCLE);
    }
    return true;
  }

  /**
   * Recursive depth first search until it reach the goal state or timeout limit.  Returns the
   * best estimate up to search limit.
   *
   * @param orgX the x-coordinate of zero space of current state
   * @param orgY the y-coordinate of zero space of current state
   * @param zeroPos the one dimension of zero index of current state
   * @param zeroMirror the one dimension of zero index of mirror reflection of current state
   * @param valReg pattern value of tiles  of current state
   * @param valMirror the pattern value of tiles with mirror reflection of current state
   * @param cost the number of move of current state
   * @param limit the remaining search limit allowance
   * @param estimate the best estimate of current state
   * @param chain the compress code of rotation sequence
   * @param currMove the current Board.Move direction
   * @return integer value of best estimate from depth first search up to search limit
   */
  private int dfsNext(final int orgX, final int orgY, final int zeroPos, final int zeroMirror,
      final int valReg, final int valMirror, final int cost, final int limit, final int estimate,
      final int chain, final Board.Move currMove) {
    idaCount++;
    if (terminated) {
      return END_OF_SEARCH;
    }
    if (isTimerOn() && stopwatch.currentTime() > getTimeoutLimit()) {
      stopwatch.stop();
      searchTimeout = true;
      terminated = true;
      return END_OF_SEARCH;
    }

    assert stopwatch.isActive() : "stopwatch is not running.";

    int[] orgCopy = new int[pdbComboSize];
    System.arraycopy(pdbCombo, 0, orgCopy, 0, pdbComboSize);

    boolean pass = true;
    if (enhancement[1]) {
      pass = isNotSymmetryPdb();
    }

    // hard code different order to next moves base on the current move
    int priority = estimate;

    // hard code different order to next moves base on the current move       
    switch (currMove) {
      case RIGHT:
        // RIGHT
        if (orgX < ROW_SIZE - 1) {
          priority = Math.min(priority, shiftRight(orgX, orgY, zeroPos, zeroMirror,
              cost, limit, valReg, valMirror, orgCopy, RESET_VAL));
        }
        if (pass) {
          // UP
          if (orgY > 0 && verifyCounterClockwise(chain)) {
            priority = Math.min(priority, shiftUp(orgX, orgY, zeroPos, zeroMirror,
                cost, limit, valReg, valMirror, orgCopy, chain << 2 | CCW_VAL));
          }
          // DOWN
          if (orgY < ROW_SIZE - 1 && verifyClockwise(chain)) {
            priority = Math.min(priority, shiftDown(orgX, orgY, zeroPos, zeroMirror,
                cost, limit, valReg, valMirror, orgCopy, chain << 2 | CW_VAL));
          }
        }
        break;
      case DOWN:
        // DOWN
        if (orgY < ROW_SIZE - 1) {
          priority = Math.min(priority, shiftDown(orgX, orgY, zeroPos, zeroMirror,
              cost, limit, valReg, valMirror, orgCopy, RESET_VAL));
        }
        if (pass) {
          // LEFT
          if (orgX > 0 && verifyClockwise(chain)) {
            priority = Math.min(priority, shiftLeft(orgX, orgY, zeroPos, zeroMirror,
                cost, limit, valReg, valMirror, orgCopy, chain << 2 | CW_VAL));
          }
          // RIGHT
          if (orgX < ROW_SIZE - 1 && verifyCounterClockwise(chain)) {
            priority = Math.min(priority, shiftRight(orgX, orgY, zeroPos, zeroMirror,
                cost, limit, valReg, valMirror, orgCopy, chain << 2 | CCW_VAL));
          }
        }
        break;
      case LEFT:
        // LEFT
        if (orgX > 0) {
          priority = Math.min(priority, shiftLeft(orgX, orgY, zeroPos, zeroMirror,
              cost, limit, valReg, valMirror, orgCopy, RESET_VAL));
        }
        if (pass) {
          // DOWN
          if (orgY < ROW_SIZE - 1 && verifyCounterClockwise(chain)) {
            priority = Math.min(priority, shiftDown(orgX, orgY, zeroPos, zeroMirror,
                cost, limit, valReg, valMirror, orgCopy, chain << 2 | CCW_VAL));
          }
          // UP
          if (orgY > 0 && verifyClockwise(chain)) {
            priority = Math.min(priority, shiftUp(orgX, orgY, zeroPos, zeroMirror,
                cost, limit, valReg, valMirror, orgCopy, chain << 2 | CW_VAL));
          }
        }
        break;
      case UP:
        // UP
        if (orgY > 0) {
          priority = Math.min(priority, shiftUp(orgX, orgY, zeroPos, zeroMirror,
              cost, limit, valReg, valMirror, orgCopy, RESET_VAL));
        }
        if (pass) {
          // RIGHT
          if (orgX < ROW_SIZE - 1 && verifyClockwise(chain)) {
            priority = Math.min(priority, shiftRight(orgX, orgY, zeroPos, zeroMirror,
                cost, limit, valReg, valMirror, orgCopy, chain << 2 | CW_VAL));
          }
          // LEFT
          if (orgX > 0 && verifyCounterClockwise(chain)) {
            priority = Math.min(priority, shiftLeft(orgX, orgY, zeroPos, zeroMirror,
                cost, limit, valReg, valMirror, orgCopy, chain << 2 | CCW_VAL));
          }
        }
        break;
      default:
        assert false : "Error: recursive DFS switch statement";
    }
    return priority;
  }

  /**
   * Shift the space to right. Evaluate new estimate, limit, and search time.  Continue
   * to next move if applicable and return the best estimate.
   *
   * @param orgX the x-coordinate of zero space of current state
   * @param orgY the y-coordinate of zero space of current state
   * @param zeroPos the one dimension of zero index of current state
   * @param zeroMirror the one dimension of zero index of mirror reflection of current state
   * @param cost the number of move of current state
   * @param limit the remaining search limit allowance
   * @param valReg pattern value of tiles  of current state
   * @param valMirror the pattern value of tiles with mirror reflection of current state
   * @param orgCopy the integer array of current pattern keys and values for rollback
   * @param chain the compress code of rotation sequence
   * @return integer value of best estimate from depth first search up to search limit
   */
  private int shiftRight(final int orgX, final int orgY, final int zeroPos, final int zeroMirror,
      final int cost, final int limit, final int valReg, final int valMirror, final int[] orgCopy,
      final int chain) {
    if (terminated) {
      return END_OF_SEARCH;
    }
    searchNodeCount = searchCountBase + idaCount;
    searchTime = stopwatch.currentTime();

    int nextPos = zeroPos + 1;
    int nextMirror = zeroMirror + ROW_SIZE;
    int value = tiles[nextPos];
    int regPtnOrder = val2ptnOrder[value];  // regCombo == regPtnOrder
    int mirrorPtnOrder = val2ptnOrder[MIRROR_VAL_TABLE[value]];

    pdbShift(zeroPos, regPtnOrder, regPtnOrder, zeroMirror, mirrorPtnOrder,
        mirrorComboLookup[mirrorPtnOrder], 0);
    int updatePdValReg = updatePdbValue(valReg, regPtnOrder, regPtnOrder);
    int updatePdValMirror = updatePdbValue(valMirror, mirrorPtnOrder,
        mirrorComboLookup[mirrorPtnOrder]);
    int priority = Math.max(updatePdValReg, updatePdValMirror);

    if (priority < limit) {
      solutionMove[cost] = MOVE_RT;
      if (priority == 0) {
        return goalReached(cost);
      }

      swap(zeroPos, nextPos);
      priority = dfsNext(orgX + 1, orgY, nextPos, nextMirror, updatePdValReg, updatePdValMirror,
          cost + 1, limit - 1, priority, chain, MOVE_RT);
      swap(nextPos, zeroPos);
    }
    rollbackPdbCombo(regPtnOrder, mirrorPtnOrder, orgCopy);
    return priority;
  }

  /**
   * Shift the space to down. Evaluate new estimate, limit, and search time.  Continue
   * to next move if applicable and return the best estimate.
   *
   * @param orgX the x-coordinate of zero space of current state
   * @param orgY the y-coordinate of zero space of current state
   * @param zeroPos the one dimension of zero index of current state
   * @param zeroMirror the one dimension of zero index of mirror reflection of current state
   * @param cost the number of move of current state
   * @param limit the remaining search limit allowance
   * @param valReg pattern value of tiles  of current state
   * @param valMirror the pattern value of tiles with mirror reflection of current state
   * @param orgCopy the integer array of current pattern keys and values for rollback
   * @param chain the compress code of rotation sequence
   * @return integer value of best estimate from depth first search up to search limit
   */
  private int shiftDown(final int orgX, final int orgY, final int zeroPos, final int zeroMirror,
      final int cost, final int limit, final int valReg, final int valMirror, final int[] orgCopy,
      final int chain) {
    if (terminated) {
      return END_OF_SEARCH;
    }

    int nextPos = zeroPos + ROW_SIZE;
    int nextMirror = zeroMirror + 1;
    int value = tiles[nextPos];
    int regPtnOrder = val2ptnOrder[value];
    int mirrorPtnOrder = val2ptnOrder[MIRROR_VAL_TABLE[value]];

    pdbShift(zeroMirror, mirrorPtnOrder, mirrorComboLookup[mirrorPtnOrder],
        zeroPos, regPtnOrder, regPtnOrder, 0);
    int updatePdValReg = updatePdbValue(valReg, regPtnOrder, regPtnOrder);
    int updatePdValMirror = updatePdbValue(valMirror, mirrorPtnOrder,
        mirrorComboLookup[mirrorPtnOrder]);
    int priority = Math.max(updatePdValReg, updatePdValMirror);

    if (priority < limit) {
      solutionMove[cost] = MOVE_DN;
      if (priority == 0) {
        return goalReached(cost);
      }

      swap(zeroPos, nextPos);
      priority = dfsNext(orgX, orgY + 1, nextPos, nextMirror,
          updatePdValReg, updatePdValMirror,
          cost + 1, limit - 1, priority, chain, MOVE_DN);
      swap(nextPos, zeroPos);
    }
    rollbackPdbCombo(regPtnOrder, mirrorPtnOrder, orgCopy);
    return priority;
  }

  /**
   * Shift the space to left. Evaluate new estimate, limit, and search time.  Continue
   * to next move if applicable and return the best estimate.
   *
   * @param orgX the x-coordinate of zero space of current state
   * @param orgY the y-coordinate of zero space of current state
   * @param zeroPos the one dimension of zero index of current state
   * @param zeroMirror the one dimension of zero index of mirror reflection of current state
   * @param cost the number of move of current state
   * @param limit the remaining search limit allowance
   * @param valReg pattern value of tiles  of current state
   * @param valMirror the pattern value of tiles with mirror reflection of current state
   * @param orgCopy the integer array of current pattern keys and values for rollback
   * @param chain the compress code of rotation sequence
   * @return integer value of best estimate from depth first search up to search limit
   */
  private int shiftLeft(final int orgX, final int orgY, final int zeroPos, final int zeroMirror,
      final int cost, final int limit, final int valReg, final int valMirror, final int[] orgCopy,
      final int chain) {
    if (terminated) {
      return END_OF_SEARCH;
    }

    int nextPos = zeroPos - 1;
    int nextMirror = zeroMirror - ROW_SIZE;
    int value = tiles[nextPos];
    int regPtnOrder = val2ptnOrder[value];
    int mirrorPtnOrder = val2ptnOrder[MIRROR_VAL_TABLE[value]];

    pdbShift(zeroPos, regPtnOrder, regPtnOrder, zeroMirror, mirrorPtnOrder,
        mirrorComboLookup[mirrorPtnOrder], PDB_REVERSE_OFFSET);
    int updatePdValReg = updatePdbValue(valReg, regPtnOrder, regPtnOrder);
    int updatePdValMirror = updatePdbValue(valMirror, mirrorPtnOrder,
        mirrorComboLookup[mirrorPtnOrder]);
    int priority = Math.max(updatePdValReg, updatePdValMirror);

    if (priority < limit) {
      solutionMove[cost] = MOVE_LT;
      swap(zeroPos, nextPos);
      priority = dfsNext(orgX - 1, orgY, nextPos, nextMirror,
          updatePdValReg, updatePdValMirror,
          cost + 1, limit - 1, priority, chain, MOVE_LT);
      swap(nextPos, zeroPos);
    }
    rollbackPdbCombo(regPtnOrder, mirrorPtnOrder, orgCopy);
    return priority;
  }

  /**
   * Shift the space to up. Evaluate new estimate, limit, and search time.  Continue
   * to next move if applicable and return the best estimate.
   *
   * @param orgX the x-coordinate of zero space of current state
   * @param orgY the y-coordinate of zero space of current state
   * @param zeroPos the one dimension of zero index of current state
   * @param zeroMirror the one dimension of zero index of mirror reflection of current state
   * @param cost the number of move of current state
   * @param limit the remaining search limit allowance
   * @param valReg pattern value of tiles  of current state
   * @param valMirror the pattern value of tiles with mirror reflection of current state
   * @param orgCopy the integer array of current pattern keys and values for rollback
   * @param chain the compress code of rotation sequence
   * @return integer value of best estimate from depth first search up to search limit
   */
  private int shiftUp(final int orgX, final int orgY, final int zeroPos, final int zeroMirror,
      final int cost, final int limit, final int valReg, final int valMirror, final int[] orgCopy,
      final int chain) {
    if (terminated) {
      return END_OF_SEARCH;
    }

    int nextPos = zeroPos - ROW_SIZE;
    int nextMirror = zeroMirror - 1;
    int value = tiles[nextPos];
    int regPtnOrder = val2ptnOrder[value];
    int mirrorPtnOrder = val2ptnOrder[MIRROR_VAL_TABLE[value]];

    pdbShift(zeroMirror, mirrorPtnOrder, mirrorComboLookup[mirrorPtnOrder],
        zeroPos, regPtnOrder, regPtnOrder, PDB_REVERSE_OFFSET);
    int updatePdValReg = updatePdbValue(valReg, regPtnOrder, regPtnOrder);
    int updatePdValMirror = updatePdbValue(valMirror, mirrorPtnOrder,
        mirrorComboLookup[mirrorPtnOrder]);
    int priority = Math.max(updatePdValReg, updatePdValMirror);

    if (priority < limit) {
      solutionMove[cost] = MOVE_UP;
      swap(zeroPos, nextPos);
      priority = dfsNext(orgX, orgY - 1, nextPos, nextMirror,
          updatePdValReg, updatePdValMirror,
          cost + 1, limit - 1, priority, chain, MOVE_UP);
      swap(nextPos, zeroPos);
    }
    rollbackPdbCombo(regPtnOrder, mirrorPtnOrder, orgCopy);
    return priority;
  }

  /**
   * Level the preset additive pattern database can be use.
   *
   * @author <a href="http://www.linkedin.com/pub/macy-wong/46/550/37b/"
   *            target="_blank">Meisze Wong (linkedin)</a>
   */
  public enum Level {
    /**
     *  NONE - level 0, no enhancement.
     */
    NONE(0),
    /**
     *  SYMMETRY_REDUCTION - level 1, added symmetry reduction.
     */
    SYMMETRY_REDUCTION(1),
    /**
     *  CIRCULAR_RECUCTION - level 2, added circular reduction.
     */
    CIRCULAR_RECUCTION(2),
    /**
     *  PRIME - level 3, added starting order detection.  Last
     *          non reference enhancement.  Same as prime version.
     */
    PRIME(3),
    /**
     *  BOOST_ESTIMATE - level 4, boost initial estimate based on
     *                   reference collections.
     */
    BOOST_ESTIMATE(4),
    /**
     *  OPTIMUM - level 5.  Last enhancement, same as optimum version.
     *            Also use stored partial solution in reference collections.
     */
    OPTIMUM(5);

    /** The integer value of level, also use as index of boolean array. */
    private final int level;

    /**
     * Initializes a Level reference type.
     *
     * @param level the enhancement level
     */
    Level(final int level) {
      this.level = level;
    }

    /**
     * Returns the value of current level.
     *
     * @return value of current level
     */
    public int getLevel() {
      return level;
    }
  }
}
