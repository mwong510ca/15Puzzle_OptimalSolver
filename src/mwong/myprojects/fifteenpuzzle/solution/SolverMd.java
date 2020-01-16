package mwong.myprojects.fifteenpuzzle.solution;

import mwong.myprojects.fifteenpuzzle.puzzle.Board;
import mwong.myprojects.fifteenpuzzle.puzzle.HeuristicOptions;

/**
 * SolverMd extends SolverSetting, is the 15 puzzle optimal solver using Manhattan distance
 * with linear conflict option. It takes a Board object of the puzzle and solve it with IDA*.
 *
 * <p>Dependencies : Board.java, SolverBuilder.java, SolverSetting.java, HeuristicOptions.java
 *
 * @author <a href="http://www.linkedin.com/pub/macy-wong/46/550/37b/"
 *            target="_blank">Meisze Wong (linkedin)</a>
 * @see <a href="http://www.github.com/mwong510ca/15PuzzleOptimalSolver/"
 *         target="_blank">GitHub (full project)</a>
 */
final class SolverMd extends SolverSetup {
  /** The boolean flag represents the add on linear conflict feature. */
  private final boolean flagLinearConflict;

  /**
   * Initializes SolverMd object.
   */
  SolverMd() {
    this(HeuristicOptions.MD);
  }

  /**
   * Initializes SolverMd object, with Linear Conflict option.
   *
   * @param option  HeuristicOptions for Linear Conflict option
   */
  SolverMd(final HeuristicOptions option) {
    super(option);
    if (option == HeuristicOptions.MD) {
      flagLinearConflict = false;
    } else if (option == HeuristicOptions.MDLC) {
      flagLinearConflict = true;
    } else {
      throw new UnsupportedOperationException();
    }
  }

  @Override
  void setPriorityBasis(final Board board) throws IllegalArgumentException {
    if (!board.equals(lastBoard)) {
      throw new IllegalArgumentException("must initialize before call setPriorityBasis");
    }

    priorityBasis = mdEstimate(flagLinearConflict);
  }

  @Override
  void dfsStartingOrder(final int limit) {
    dfsStartingOrder(limit, priorityBasis);
  }

  /**
   * Solve the puzzle with depth first search up to the given limit and given estimate
   * instead of original heuristic.
   * Notes: special function use by SupplementaryEstimator.
   *
   * @param limit the upper limit to solve the puzzle
   * @param orgPrio the estimate of minimum move
   */
  void dfsStartingOrder(final int limit, final int orgPrio) {
    dfsStartingOrder(zeroX, zeroY, limit, orgPrio);
  }

  /**
   * Recursive depth first search until it reach the goal state, reach the limit or timeout,
   * the least estimate and node counts will be use to determine the starting order of next search.
   *
   * @param orgX the x-coordinate of zero space
   * @param orgY the y-coordinate of zero space
   * @param limit the maximum search limit
   * @param orgPrio the initial priority
   */
  private void dfsStartingOrder(final int orgX, final int orgY, final int limit,
      final int orgPrio) {
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
                1, limit, orgPrio, RESET_VAL);
            break;
          case DOWN:
            lastDepthSummary[firstMoveIdx] = shiftDown(orgX, orgY, zeroPos, zeroMirror,
                1, limit, orgPrio, RESET_VAL);
            break;
          case LEFT:
            lastDepthSummary[firstMoveIdx] = shiftLeft(orgX, orgY, zeroPos, zeroMirror,
                1, limit, orgPrio, RESET_VAL);
            break;
          case UP:
            lastDepthSummary[firstMoveIdx] = shiftUp(orgX, orgY, zeroPos, zeroMirror,
                1, limit, orgPrio, RESET_VAL);
            break;
          default:
            assert false : "Error: starting order switch statement";
        }

        if (terminated) {
          return;
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
   * @param cost the number of move of current state
   * @param limit the remaining search limit allowance
   * @param estimate the best estimate of current state
   * @param chain the compress code of rotation sequence
   * @param currMove the current Board.Move direction
   * @return integer value of best estimate from depth first search up to search limit
   */
  private int dfsNext(final int orgX, final int orgY, final int zeroPos, final int zeroMirror,
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

    switch (currMove) {
      case RIGHT:
        // RIGHT
        if (orgX < ROW_SIZE - 1) {
          priority = Math.min(priority, shiftRight(orgX, orgY, zeroPos, zeroMirror,
              cost, limit, estimate, RESET_VAL));
        }
        if (pass) {
          // UP
          if (orgY > 0 && isValidCounterClockwise(chain)) {
            priority = Math.min(priority, shiftUp(orgX, orgY, zeroPos, zeroMirror,
                cost, limit, estimate, chain << 2 | CCW_VAL));
          }
          // DOWN
          if (orgY < ROW_SIZE - 1 && isValidClockwise(chain)) {
            priority = Math.min(priority, shiftDown(orgX, orgY, zeroPos, zeroMirror,
                cost, limit, estimate, chain << 2 | CW_VAL));
          }
        }
        break;
      case DOWN:
        // DOWN
        if (orgY < ROW_SIZE - 1) {
          priority = Math.min(priority, shiftDown(orgX, orgY, zeroPos, zeroMirror,
              cost, limit, estimate, RESET_VAL));
        }
        if (pass) {
          // LEFT
          if (orgX > 0 && isValidClockwise(chain)) {
            priority = Math.min(priority, shiftLeft(orgX, orgY, zeroPos, zeroMirror,
                cost, limit, estimate, chain << 2 | CW_VAL));
          }
          // RIGHT
          if (orgX < ROW_SIZE - 1 && isValidCounterClockwise(chain)) {
            priority = Math.min(priority, shiftRight(orgX, orgY, zeroPos, zeroMirror,
                cost, limit, estimate, chain << 2 | CCW_VAL));
          }
        }
        break;
      case LEFT:
        // LEFT
        if (orgX > 0) {
          priority = Math.min(priority, shiftLeft(orgX, orgY, zeroPos, zeroMirror,
              cost, limit, estimate, RESET_VAL));
        }
        if (pass) {
          // DOWN
          if (orgY < ROW_SIZE - 1 && isValidCounterClockwise(chain)) {
            priority = Math.min(priority, shiftDown(orgX, orgY, zeroPos, zeroMirror,
                cost, limit, estimate, chain << 2 | CCW_VAL));
          }
          // UP
          if (orgY > 0 && isValidClockwise(chain)) {
            priority = Math.min(priority, shiftUp(orgX, orgY, zeroPos, zeroMirror,
                cost, limit, estimate, chain << 2 | CW_VAL));
          }
        }
        break;
      case UP:
        // UP
        if (orgY > 0) {
          priority = Math.min(priority, shiftUp(orgX, orgY, zeroPos, zeroMirror,
              cost, limit, estimate, RESET_VAL));
        }
        if (pass) {
          // RIGHT
          if (orgX < ROW_SIZE - 1 && isValidClockwise(chain)) {
            priority = Math.min(priority, shiftRight(orgX, orgY, zeroPos, zeroMirror,
                cost, limit, estimate, chain << 2 | CW_VAL));
          }
          // LEFT
          if (orgX > 0 && isValidCounterClockwise(chain)) {
            priority = Math.min(priority, shiftLeft(orgX, orgY, zeroPos, zeroMirror,
                cost, limit, estimate, chain << 2 | CCW_VAL));
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
   * @param estimate the best estimate of current state
   * @param chain the compress code of rotation sequence
   * @return integer value of best estimate from depth first search up to search limit
   */
  private int shiftRight(final int orgX, final int orgY, final int zeroPos, final int zeroMirror,
      final int cost, final int limit, final int estimate, final int chain) {
    if (terminated) {
      return END_OF_SEARCH;
    }

    int nextMirror = zeroMirror + ROW_SIZE;
    byte value = tilesMirror[nextMirror];
    byte valuePos = (byte) (value - 1);
    int priority = estimate - 1;
    if (valuePos / ROW_SIZE > orgX) {
      priority = estimate + 1;
    }
    if (flagLinearConflict) {
      priority = updateLinearConflict(orgY, orgX, valuePos / ROW_SIZE, priority, value,
          1, tilesMirror);
    }

    if (priority < limit) {
      solutionMove[cost] = MOVE_RT;
      if (priority == 0) {
        return goalReached(cost);
      }
      int nextPos = zeroPos + 1;
      swap(zeroPos, nextPos, zeroMirror, nextMirror);
      priority = dfsNext(orgX + 1, orgY, nextPos, nextMirror,
          cost + 1, limit - 1, priority, chain, MOVE_RT);
      swap(nextPos, zeroPos, nextMirror, zeroMirror);
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
   * @param estimate the best estimate of current state
   * @param chain the compress code of rotation sequence
   * @return integer value of best estimate from depth first search up to search limit
   */
  private int shiftDown(final int orgX, final int orgY, final int zeroPos, final int zeroMirror,
      final int cost, final int limit, final  int estimate, final int chain) {
    if (terminated) {
      return END_OF_SEARCH;
    }

    int nextPos = zeroPos + ROW_SIZE;
    byte value = tiles[nextPos];
    byte valuePos = (byte) (value - 1);
    int priority = estimate - 1;
    if (valuePos / ROW_SIZE > orgY) {
      priority = estimate + 1;
    }
    if (flagLinearConflict) {
      priority = updateLinearConflict(orgX, orgY, valuePos / ROW_SIZE, priority, value,
                1, tiles);
    }

    if (priority < limit) {
      solutionMove[cost] = MOVE_DN;
      if (priority == 0) {
        return goalReached(cost);
      }
      int nextMirror = zeroMirror + 1;
      swap(zeroPos, nextPos, zeroMirror, nextMirror);
      priority = dfsNext(orgX, orgY + 1, nextPos, nextMirror,
          cost + 1, limit - 1, priority, chain, MOVE_DN);
      swap(nextPos, zeroPos, nextMirror, zeroMirror);
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
   * @param estimate the best estimate of current state
   * @param chain the compress code of rotation sequence
   * @return integer value of best estimate from depth first search up to search limit
   */
  private int shiftLeft(final int orgX, final int orgY, final int zeroPos, final int zeroMirror,
      final int cost, final int limit, final int estimate, final int chain) {
    if (terminated) {
      return END_OF_SEARCH;
    }

    int nextMirror = zeroMirror - ROW_SIZE;
    byte value = tilesMirror[nextMirror];
    byte valuePos = (byte) (value - 1);
    int priority = estimate - 1;
    if (valuePos / ROW_SIZE < orgX) {
      priority = estimate + 1;
    }
    if (flagLinearConflict) {
      priority = updateLinearConflict(orgY, orgX, valuePos / ROW_SIZE, priority, value,
          -1, tilesMirror);
    }

    if (priority < limit) {
      solutionMove[cost] = MOVE_LT;
      int nextPos = zeroPos - 1;
      swap(zeroPos, nextPos, zeroMirror, nextMirror);
      priority = dfsNext(orgX - 1, orgY, nextPos, nextMirror,
          cost + 1, limit - 1, priority, chain, MOVE_LT);
      swap(nextPos, zeroPos, nextMirror, zeroMirror);
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
   * @param estimate the best estimate of current state
   * @param chain the compress code of rotation sequence
   * @return integer value of best estimate from depth first search up to search limit
   */
  private int shiftUp(final int orgX, final int orgY, final int zeroPos, final int zeroMirror,
      final int cost, final int limit, final int estimate, final int chain) {
    if (terminated) {
      return END_OF_SEARCH;
    }

    int nextPos = zeroPos - ROW_SIZE;
    byte value = tiles[nextPos];
    byte valuePos = (byte) (value - 1);
    int priority = estimate - 1;
    if (valuePos / ROW_SIZE < orgY) {
      priority = estimate + 1;
    }
    if (flagLinearConflict) {
      priority = updateLinearConflict(orgX, orgY, valuePos / ROW_SIZE, priority, value,
          -1, tiles);
    }

    if (priority < limit) {
      solutionMove[cost] = MOVE_UP;
      int nextMirror = zeroMirror - 1;
      swap(zeroPos, nextPos, zeroMirror, nextMirror);
      priority = dfsNext(orgX, orgY - 1, nextPos, nextMirror,
          cost + 1, limit - 1, priority, chain, MOVE_UP);
      swap(nextPos, zeroPos, nextMirror, zeroMirror);
    }
    return priority;
  }
}
