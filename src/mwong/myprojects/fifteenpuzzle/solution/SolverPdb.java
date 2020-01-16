package mwong.myprojects.fifteenpuzzle.solution;

import mwong.myprojects.fifteenpuzzle.puzzle.Board;
import mwong.myprojects.fifteenpuzzle.puzzle.HeuristicOptions;
import mwong.myprojects.fifteenpuzzle.puzzle.PatternDatabase;
import mwong.myprojects.fifteenpuzzle.puzzle.PatternOptions;

/**
 *  SolverPdb extends SolverSetting, is the 15 puzzle optimal solver using Pattern Database.
 *  Choice of preset pattern or user defined custom pattern up to size 7. It takes a Board
 *  object of the puzzle and solve it with IDA*.
 *
 *  <p>Dependencies : Board.java, HeuristicOptions.java, PatternDatabase.java, PatternOptions.java,
 *                    Solver.java, SolverBuilder.java, SolverSetting.java
 *
 * @author <a href="http://www.linkedin.com/pub/macy-wong/46/550/37b/"
 *            target="_blank">Meisze Wong (linkedin)</a>
 * @see <a href="http://www.github.com/mwong510ca/15PuzzleOptimalSolver/"
 *         target="_blank">GitHub (full project)</a>
 */
class SolverPdb extends SolverSetup {
  /** The integer of initial pattern database value of the board. */
  private int initPdbValReg = 0;
  /** The integer of initial pattern database value of the board mirror reflection. */
  private int initPdbValMirror = 0;

  /**
   * Initializes SolverPdb object using existing Solver object and type of application.
   *
   * @param inSolver the given Solver object must using pattern database 78
   * @param appMode the applicationMode
   */
  SolverPdb(final Solver inSolver, final ApplicationMode appMode) {
    super(inSolver.getHeuristic());
    if (inSolver.getHeuristic() != HeuristicOptions.PD78) {
      throw new IllegalArgumentException("Given solver is not using pattern database 78.");
    }
    if (appMode == ApplicationMode.GUI) {
      throw new UnsupportedOperationException(
          "Pdb78 duplicate solver for console application only.");
    }
    loadPdbComponents(inSolver);
  }

  /**
   * Initializes SolverPdb object using given preset pattern and type of application.
   *
   * @param presetPattern the given preset pattern type
   * @param appMode the applicationMode
   */
  SolverPdb(final PatternOptions presetPattern, final ApplicationMode appMode) {
    this(presetPattern, 0, appMode);
  }

  /**
   * Initializes SmartSoSolverPdblverPdb object with choice of given preset pattern and
   * type of application.
   *
   * @param presetPattern the given preset pattern type
   * @param choice the number of preset pattern option
   * @param appMode the applicationMode
   */
  SolverPdb(final PatternOptions presetPattern, final int choice, final ApplicationMode appMode) {
    super(presetPattern.getHeuristic());
    PatternDatabase pdb = new PatternDatabase(presetPattern, choice, appMode);
    loadPdbComponents(pdb, appMode);
    pdb = null;
    setInUsePdbPtn(presetPattern.getPattern(choice));
  }

  /**
   * Initializes SolverPdb object with user defined custom pattern and type of application.
   *
   * @param customPattern byte array of user defined custom pattern
   * @param appMode the applicationMode
   */
  SolverPdb(final byte[] customPattern, final ApplicationMode appMode) {
    super(HeuristicOptions.PDCustom);
    if (appMode != ApplicationMode.CONSOLE) {
      throw new UnsupportedOperationException();
    }
    PatternDatabase pdb = new PatternDatabase(customPattern);
    loadPdbComponents(pdb, appMode);
    pdb = null;
    setInUsePdbPtn(customPattern);
  }

  @Override
  final void setPriorityBasis(final Board board) throws IllegalArgumentException {
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
  final void dfsStartingOrder(final int limit) {
    dfsStartingOrder(zeroX, zeroY, limit, initPdbValReg, initPdbValMirror);
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
    int[] orgCopy = new int[pdbComboSize];
    System.arraycopy(pdbCombo, 0, orgCopy, 0, pdbComboSize);

    int[] estimate1stMove = new int[DIR_SIZE * 2];
    System.arraycopy(lastDepthSummary, 0, estimate1stMove, 0, DIR_SIZE * 2);

    int estimate = limit;
    while (!terminated && estimate != END_OF_SEARCH) {
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

      if (!terminated && estimate < END_OF_SEARCH) {
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

    boolean pass = true;
    if (zeroPos == zeroMirror) {
      pass = isNotSymmetryPdb();
    }

    // hard code different order to next moves base on the current move
    int priority = estimate;

    int[] orgCopy = new int[pdbComboSize];
    System.arraycopy(pdbCombo, 0, orgCopy, 0, pdbCombo.length);

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
          if (orgY > 0 && isValidCounterClockwise(chain)) {
            priority = Math.min(priority, shiftUp(orgX, orgY, zeroPos, zeroMirror,
                cost, limit, valReg, valMirror, orgCopy, chain << 2 | CCW_VAL));
          }
          // DOWN
          if (orgY < ROW_SIZE - 1 && isValidClockwise(chain)) {
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
          if (orgX > 0 && isValidClockwise(chain)) {
            priority = Math.min(priority, shiftLeft(orgX, orgY, zeroPos, zeroMirror,
                cost, limit, valReg, valMirror, orgCopy, chain << 2 | CW_VAL));
          }
          // RIGHT
          if (orgX < ROW_SIZE - 1 && isValidCounterClockwise(chain)) {
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
          if (orgY < ROW_SIZE - 1 &&  isValidCounterClockwise(chain)) {
            priority = Math.min(priority, shiftDown(orgX, orgY, zeroPos, zeroMirror,
                cost, limit, valReg, valMirror, orgCopy, chain << 2 | CCW_VAL));
          }
          // UP
          if (orgY > 0 && isValidClockwise(chain)) {
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
          if (orgX < ROW_SIZE - 1 && isValidClockwise(chain)) {
            priority = Math.min(priority, shiftRight(orgX, orgY, zeroPos, zeroMirror,
                cost, limit, valReg, valMirror, orgCopy, chain << 2 | CW_VAL));
          }
          // LEFT
          if (orgX > 0 && isValidCounterClockwise(chain)) {
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
}
