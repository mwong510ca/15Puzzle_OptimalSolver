package mwong.myprojects.fifteenpuzzle.solution;

import mwong.myprojects.fifteenpuzzle.puzzle.Board;
import mwong.myprojects.fifteenpuzzle.puzzle.HeuristicOptions;
import mwong.myprojects.fifteenpuzzle.puzzle.WalkingDistance.Arrow;

/**
 * SolverWdMd extends SolverSetup. It is the 15 puzzle optimal solver.
 * It takes a Board object of the puzzle and solve it with IDA* using combination of
 * Walking Distance and Manhattan distance with linear conflict.
 *
 * <p>Dependencies : Board.java, HeuristicOptions.java, WalkingDistance.java
 *
 * @author <a href="http://www.linkedin.com/pub/macy-wong/46/550/37b/"
 *            target="_blank">Meisze Wong (linkedin)</a>
 * @see <a href="http://www.github.com/mwong510ca/15PuzzleOptimalSolver/"
 *         target="_blank">GitHub (full project)</a>
 */
final class SolverWdMd extends SolverSetup {
  /** The variable of initial priority using Manhattan distance with linear conflict. */
  private int mdlc;

  /**
   * Initializes SolverWdMd object.
   *
   * @param appMode the applicationMode
   */
  SolverWdMd(final ApplicationMode appMode) {
    super(HeuristicOptions.WDMD);
    loadWdComponents(appMode);
  }

  @Override
  void setPriorityBasis(final Board board) throws IllegalArgumentException {
    if (!board.equals(lastBoard)) {
      throw new IllegalArgumentException("must initialize before call setPriorityBasis");
    }

    transWdCombo();
    int priority1 = initWdCombo[WD_KEY_ORDER_H_VAL] + initWdCombo[WD_KEY_ORDER_V_VAL];
    mdlc = mdEstimate(true);
    priorityBasis = Math.max(priority1, mdlc);
  }

  @Override
  void dfsStartingOrder(final int limit) {
    dfsStartingOrder(zeroX, zeroY, limit, mdlc, initWdCombo[WD_KEY_ORDER_H_IDX],
        initWdCombo[WD_KEY_ORDER_V_IDX], initWdCombo[WD_KEY_ORDER_H_VAL],
        initWdCombo[WD_KEY_ORDER_V_VAL]);
  }

  /**
   * Recursive depth first search until it reach the goal state, reach the limit or timeout,
   * the least estimate and node counts will be use to determine the starting order of next search.
   *
   * @param orgX the x-coordinate of zero space
   * @param orgY the y-coordinate of zero space
   * @param limit the maximum search limit
   * @param estMdLc the initial priority of Manhattan distance with linear conflict
   * @param idxH the original walking distance horizontal index
   * @param idxV the original walking distance vertical index
   * @param valH the original walking distance horizontal value
   * @param valV the original walking distance vertical value
   */
  private void dfsStartingOrder(final int orgX, final int orgY, final int limit, final int estMdLc,
      final int idxH, final int idxV, final int valH, final int valV) {
    int zeroPos = orgY * ROW_SIZE + orgX;
    int zeroMirror = MIRROR_POS_TABLE[zeroPos];
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

      if (estimate < END_OF_SEARCH) {
        int startCounter = idaCount++;

        switch (Board.Move.values()[firstMoveIdx]) {
          case RIGHT:
            lastDepthSummary[firstMoveIdx] = shiftRight(orgX, orgY, zeroPos, zeroMirror,
                1, limit, estMdLc, idxH, idxV, valH, valV, RESET_VAL);
            break;
          case DOWN:
            lastDepthSummary[firstMoveIdx] = shiftDown(orgX, orgY, zeroPos, zeroMirror,
                1, limit, estMdLc, idxH, idxV, valH, valV, RESET_VAL);
            break;
          case LEFT:
            lastDepthSummary[firstMoveIdx] = shiftLeft(orgX, orgY, zeroPos, zeroMirror,
                1, limit, estMdLc, idxH, idxV, valH, valV, RESET_VAL);
            break;
          case UP:
            lastDepthSummary[firstMoveIdx] = shiftUp(orgX, orgY, zeroPos, zeroMirror,
                1, limit, estMdLc, idxH, idxV, valH, valV, RESET_VAL);
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
   * @param estMdLc the estimate of Manhattan distance with linear conflict of current state
   * @param idxH the walking distance horizontal index of current state
   * @param idxV the walking distance vertical index of current state
   * @param valH the walking distance horizontal value of current state
   * @param valV the walking distance vertical value of current state
   * @param cost the number of move of current state
   * @param limit the remaining search limit allowance
   * @param estimate the best estimate of current state
   * @param chain the compress code of rotation sequence
   * @param currMove the current Board.Move direction
   * @return integer value of best estimate from depth first search up to search limit
   */
  private int dfsNext(final int orgX, final int orgY, final int zeroPos, final int zeroMirror,
      final int estMdLc, final int idxH, final int idxV, final int valH, final int valV,
      final int cost, final int limit, final int estimate, final int chain,
      final Board.Move currMove) {
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
      pass = isNotSymmetry();
    }

    // hard code different order to next moves base on the current move
    int priority = estimate;

    // hard code different order to next moves base on the current move
    switch (currMove) {
      case RIGHT:
        // RIGHT
        if (orgX < ROW_SIZE - 1) {
          priority = Math.min(priority, shiftRight(orgX, orgY, zeroPos, zeroMirror,
              cost, limit, estMdLc, idxH, idxV, valH, valV, RESET_VAL));
        }
        if (pass) {
          // UP
          if (orgY > 0 && isValidCounterClockwise(chain)) {
            priority = Math.min(priority, shiftUp(orgX, orgY, zeroPos, zeroMirror,
                cost, limit, estMdLc, idxH, idxV, valH, valV, chain << 2 | CCW_VAL));
          }
          // DOWN
          if (orgY < ROW_SIZE - 1 && isValidClockwise(chain)) {
            priority = Math.min(priority, shiftDown(orgX, orgY, zeroPos, zeroMirror,
                cost, limit, estMdLc, idxH, idxV, valH, valV, chain << 2 | CW_VAL));
          }
        }
        break;
      case DOWN:
        // DOWN
        if (orgY < ROW_SIZE - 1) {
          priority = Math.min(priority, shiftDown(orgX, orgY, zeroPos, zeroMirror,
              cost, limit, estMdLc, idxH, idxV, valH, valV, RESET_VAL));
        }
        if (pass) {
          // LEFT
          if (orgX > 0 && isValidClockwise(chain)) {
            priority = Math.min(priority, shiftLeft(orgX, orgY, zeroPos, zeroMirror,
                cost, limit, estMdLc, idxH, idxV, valH, valV, chain << 2 | CW_VAL));
          }
          // RIGHT
          if (orgX < ROW_SIZE - 1 && isValidCounterClockwise(chain)) {
            priority = Math.min(priority, shiftRight(orgX, orgY, zeroPos, zeroMirror,
                cost, limit, estMdLc, idxH, idxV, valH, valV, chain << 2 | CCW_VAL));
          }
        }
        break;
      case LEFT:
        // LEFT
        if (orgX > 0) {
          priority = Math.min(priority, shiftLeft(orgX, orgY, zeroPos, zeroMirror,
              cost, limit, estMdLc, idxH, idxV, valH, valV, RESET_VAL));
        }
        if (pass) {
          // DOWN
          if (orgY < ROW_SIZE - 1 && isValidCounterClockwise(chain)) {
            priority = Math.min(priority, shiftDown(orgX, orgY, zeroPos, zeroMirror,
                cost, limit, estMdLc, idxH, idxV, valH, valV, chain << 2 | CCW_VAL));
          }
          // UP
          if (orgY > 0 && isValidClockwise(chain)) {
            priority = Math.min(priority, shiftUp(orgX, orgY, zeroPos, zeroMirror,
                cost, limit, estMdLc, idxH, idxV, valH, valV, chain << 2 | CW_VAL));
          }
        }
        break;
      case UP:
        // UP
        if (orgY > 0) {
          priority = Math.min(priority, shiftUp(orgX, orgY, zeroPos, zeroMirror,
              cost, limit, estMdLc, idxH, idxV, valH, valV, RESET_VAL));
        }
        if (pass) {
          // RIGHT
          if (orgX < ROW_SIZE - 1 && isValidClockwise(chain)) {
            priority = Math.min(priority, shiftRight(orgX, orgY, zeroPos, zeroMirror,
                cost, limit, estMdLc, idxH, idxV, valH, valV, chain << 2 | CW_VAL));
          }
          // LEFT
          if (orgX > 0 && isValidCounterClockwise(chain)) {
            priority = Math.min(priority, shiftLeft(orgX, orgY, zeroPos, zeroMirror,
                cost, limit, estMdLc, idxH, idxV, valH, valV, chain << 2 | CCW_VAL));
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
   * @param estMdLc the estimate of Manhattan distance with linear conflict of current state
   * @param idxH the walking distance horizontal index of current state
   * @param idxV the walking distance vertical index of current state
   * @param valH the walking distance horizontal value of current state
   * @param valV the walking distance vertical value of current state
   * @param chain the compress code of rotation sequence
   * @return integer value of best estimate from depth first search up to search limit
   */
  private int shiftRight(final int orgX, final int orgY, final int zeroPos, final int zeroMirror,
      final int cost, final int limit, final int estMdLc, final int idxH, final int idxV,
      final int valH, final int valV, final int chain) {
    if (terminated) {
      return END_OF_SEARCH;
    }

    int nextPos = zeroPos + 1;
    int shiftIdx = getWdPtnIdx(idxV, (tiles[nextPos] - 1) % ROW_SIZE, Arrow.FORWARD);
    int shiftVal = wdPattern[shiftIdx];
    int priority = valH + shiftVal;
    if (priority == 0) {
      solutionMove[cost] = MOVE_RT;
      return goalReached(cost);
    }

    if (priority < limit) {
      int nextMirror = zeroMirror + ROW_SIZE;
      byte value = tilesMirror[nextMirror];
      byte valuePos = (byte) (value - 1);
      int priority2 = estMdLc - 1;
      if (valuePos / ROW_SIZE > orgX) {
        priority2 = estMdLc + 1;
      }
      priority2 = updateLinearConflict(orgY, orgX, valuePos / ROW_SIZE, priority2, value,
          1, tilesMirror);
      if (priority2 < limit) {
        solutionMove[cost] = MOVE_RT;
        swap(zeroPos, nextPos, zeroMirror, nextMirror);
        priority2 = dfsNext(orgX + 1, orgY, nextPos, nextMirror, priority2, idxH, shiftIdx,
            valH, shiftVal, cost + 1, limit - 1, Math.max(priority, priority2), chain, MOVE_RT);
        swap(nextPos, zeroPos, nextMirror, zeroMirror);
      }
      return priority2;
    }
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
   * @param estMdLc the estimate of Manhattan distance with linear conflict of current state
   * @param idxH the walking distance horizontal index of current state
   * @param idxV the walking distance vertical index of current state
   * @param valH the walking distance horizontal value of current state
   * @param valV the walking distance vertical value of current state
   * @param chain the compress code of rotation sequence
   * @return integer value of best estimate from depth first search up to search limit
   */
  private int shiftDown(final int orgX, final int orgY, final int zeroPos, final int zeroMirror,
      final int cost, final int limit, final int estMdLc, final int idxH, final int idxV,
      final int valH, final int valV, final int chain) {
    if (terminated) {
      return END_OF_SEARCH;
    }

    int nextPos = zeroPos + ROW_SIZE;
    int shiftIdx = getWdPtnIdx(idxH, (tiles[nextPos] - 1) / ROW_SIZE, Arrow.FORWARD);
    int shiftVal = wdPattern[shiftIdx];
    int priority = valV + shiftVal;
    if (priority == 0) {
      solutionMove[cost] = MOVE_DN;
      return goalReached(cost);
    }
    if (priority < limit) {
      byte value = tiles[nextPos];
      byte valuePos = (byte) (value - 1);
      int priority2 = estMdLc - 1;
      if (valuePos / ROW_SIZE > orgY) {
        priority2 = estMdLc + 1;
      }
      priority2 = updateLinearConflict(orgX, orgY, valuePos / ROW_SIZE, priority2, value,
          1, tiles);

      if (priority2 < limit) {
        solutionMove[cost] = MOVE_DN;
        int nextMirror = zeroMirror + 1;
        swap(zeroPos, nextPos, zeroMirror, nextMirror);
        priority2 = dfsNext(orgX, orgY + 1, nextPos, nextMirror, priority2, shiftIdx, idxV,
            shiftVal, valV, cost + 1, limit - 1, Math.max(priority, priority2), chain, MOVE_DN);
        swap(nextPos, zeroPos, nextMirror, zeroMirror);
      }
      return priority2;
    }
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
   * @param estMdLc the estimate of Manhattan distance with linear conflict of current state
   * @param idxH the walking distance horizontal index of current state
   * @param idxV the walking distance vertical index of current state
   * @param valH the walking distance horizontal value of current state
   * @param valV the walking distance vertical value of current state
   * @param chain the compress code of rotation sequence
   * @return integer value of best estimate from depth first search up to search limit
   */
  private int shiftLeft(final int orgX, final int orgY, final int zeroPos, final int zeroMirror,
      final int cost, final int limit, final int estMdLc, final int idxH, final int idxV,
      final int valH, final int valV, final int chain) {
    if (terminated) {
      return END_OF_SEARCH;
    }

    int nextPos = zeroPos - 1;
    int shiftIdx = getWdPtnIdx(idxV, (tiles[nextPos] - 1) % ROW_SIZE, Arrow.BACKWARD);
    int shiftVal = wdPattern[shiftIdx];
    int priority = valH + shiftVal;
    if (priority < limit) {
      int nextMirror = zeroMirror - ROW_SIZE;
      byte value = tilesMirror[zeroMirror - ROW_SIZE];
      byte valuePos = (byte) (value - 1);
      int priority2 = estMdLc - 1;
      if (valuePos / ROW_SIZE < orgX) {
        priority2 = estMdLc + 1;
      }
      priority2 = updateLinearConflict(orgY, orgX, valuePos / ROW_SIZE, priority2, value,
          -1, tilesMirror);

      if (priority2 < limit) {
        solutionMove[cost] = MOVE_LT;
        swap(zeroPos, nextPos, zeroMirror, nextMirror);
        priority2 = dfsNext(orgX - 1, orgY, nextPos, nextMirror, priority2, idxH, shiftIdx,
            valH, shiftVal, cost + 1, limit - 1, Math.max(priority, priority2), chain, MOVE_LT);
        swap(nextPos, zeroPos, nextMirror, zeroMirror);
      }
      return priority2;
    }
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
   * @param estMdLc the estimate of Manhattan distance with linear conflict of current state
   * @param idxH the walking distance horizontal index of current state
   * @param idxV the walking distance vertical index of current state
   * @param valH the walking distance horizontal value of current state
   * @param valV the walking distance vertical value of current state
   * @param chain the compress code of rotation sequence
   * @return integer value of best estimate from depth first search up to search limit
   */
  private int shiftUp(final int orgX, final int orgY, final int zeroPos, final int zeroMirror,
      final int cost, final int limit, final int estMdLc, final int idxH, final int idxV,
      final int valH, final int valV, final int chain) {
    if (terminated) {
      return END_OF_SEARCH;
    }

    int nextPos = zeroPos - ROW_SIZE;
    int shiftIdx = getWdPtnIdx(idxH, (tiles[nextPos] - 1) / ROW_SIZE, Arrow.BACKWARD);
    int shiftVal = wdPattern[shiftIdx];
    int priority = valV + shiftVal;
    if (priority < limit) {
      byte value = tiles[nextPos];
      byte valuePos = (byte) (value - 1);
      int priority2 = estMdLc - 1;
      if (valuePos / ROW_SIZE < orgY) {
        priority2 = estMdLc + 1;
      }
      priority2 = updateLinearConflict(orgX, orgY, valuePos / ROW_SIZE, priority2, value,
          -1, tiles);
      if (priority2 < limit) {
        solutionMove[cost] = MOVE_UP;
        int nextMirror = zeroMirror - 1;
        swap(zeroPos, nextPos, zeroMirror, nextMirror);
        priority2 = dfsNext(orgX, orgY - 1, nextPos, nextMirror, priority2, shiftIdx, idxV,
            shiftVal, valV, cost + 1, limit - 1, Math.max(priority, priority2), chain, MOVE_UP);
        swap(nextPos, zeroPos, nextMirror, zeroMirror);
      }
      return priority2;
    }
    return priority;
  }
}
