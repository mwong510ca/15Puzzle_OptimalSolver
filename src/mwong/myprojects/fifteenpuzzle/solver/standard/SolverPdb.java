package mwong.myprojects.fifteenpuzzle.solver.standard;

import mwong.myprojects.fifteenpuzzle.solver.SolverProperties;
import mwong.myprojects.fifteenpuzzle.solver.components.Direction;
import mwong.myprojects.fifteenpuzzle.solver.components.PatternOptions;

import java.util.Arrays;

/**
 * SolverPdb extends SolverPdbEnh2 with enhancement 3 starting order detection.  This is
 * the completed standard version of 15 puzzle optimal solver using pattern database.
 *
 * <p>Dependencies : AbstractSolver.java, Board.java, Direction.java, HeuristicOptions.java,
 *                   PatternDatabase.java, PatternElement.java, PatternElementMode.java,
 *                   PatternOptions.java, PatternPreoperties.java
 *
 * @author   Meisze Wong
 *           www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public class SolverPdb extends SolverPdbEnh2 {
    /**
     *  Initializes SolverPdb object using default preset pattern.
     */
    public SolverPdb() {
        this(SolverProperties.getDefaultPattern());
    }

    /**
     *  Initializes SolverPdb object using given preset pattern.
     *
     *  @param presetPattern the given preset pattern type
     */
    public SolverPdb(PatternOptions presetPattern) {
        this(presetPattern, 0);
    }

    /**
     *  Initializes SolverPdb object with choice of given preset pattern.
     *
     *  @param presetPattern the given preset pattern type
     *  @param choice the number of preset pattern option
     */
    public SolverPdb(PatternOptions presetPattern, int choice) {
        super(presetPattern, choice);
    }

    /**
     *  Initializes SolverPdb object with user defined custom pattern.
     *
     *  @param customPattern byte array of user defined custom pattern
     *  @param elementGroups boolean array of groups reference to given pattern
     */
    public SolverPdb(byte[] customPattern, boolean[] elementGroups) {
        super(customPattern, elementGroups);
    }

    /**
     *  Initializes SolverPdb object with a given concrete class.
     *
     *  @param copySolver an instance of SolverPdb
     */
    public SolverPdb(SolverPdbBase copySolver) {
        super(copySolver);
    }

    // ----- Enhancement 3, implement starting order detection -----

    // recursive depth first search until it reach the goal state or timeout, the least
    // estimate and node counts will be use to determine the starting order of next search
    protected void dfsStartingOrder(int orgX, int orgY, int limit, int orgValReg,
            int orgValSym) {
        int zeroPos = orgY * rowSize + orgX;
        int zeroSym = symmetryPos[zeroPos];
        int[] orgCopy = new int[szPdKeys];
        System.arraycopy(pdKeys, 0, orgCopy, 0, szPdKeys);
        int[] estimate1stMove = new int[rowSize * 2];
        System.arraycopy(lastDepthSummary, 0, estimate1stMove, 0, rowSize * 2);
        if (flagMessage) {
            System.out.println(Arrays.toString(lastDepthSummary));
        }

        int estimate = limit;
        while (!terminated && estimate != endOfSearch) {
            int firstMoveIdx = -1;
            int nodeCount = Integer.MAX_VALUE;

            estimate = endOfSearch;
            for (int i = 0; i < 4; i++) {
                if (estimate1stMove[i] == endOfSearch) {
                    continue;
                } else if (lastDepthSummary[i] < estimate) {
                    estimate = estimate1stMove[i];
                    nodeCount = lastDepthSummary[i + 4];
                    firstMoveIdx = i;
                } else if (lastDepthSummary[i] == estimate && lastDepthSummary[i + 4] < nodeCount) {
                    nodeCount = lastDepthSummary[i + 4];
                    firstMoveIdx = i;
                }
            }

            if (!terminated && estimate < endOfSearch) {
                int startCounter = idaCount++;
                if (firstMoveIdx == Direction.RIGHT.getValue()) {
                    if (flagMessage) {
                        System.out.print("R ");
                    }
                    lastDepthSummary[firstMoveIdx] = shiftRight(orgX, orgY, zeroPos, zeroSym,
                            1, limit, orgValReg, orgValSym, orgCopy, reset);
                } else if (firstMoveIdx == Direction.DOWN.getValue()) {
                    if (flagMessage) {
                        System.out.print("D ");
                    }
                    lastDepthSummary[firstMoveIdx] = shiftDown(orgX, orgY, zeroPos, zeroSym,
                            1, limit, orgValReg, orgValSym, orgCopy, reset);
                } else if (firstMoveIdx == Direction.LEFT.getValue()) {
                    if (flagMessage) {
                        System.out.print("L ");
                    }
                    lastDepthSummary[firstMoveIdx] = shiftLeft(orgX, orgY, zeroPos, zeroSym,
                            1, limit, orgValReg, orgValSym, orgCopy, reset);
                } else if (firstMoveIdx == Direction.UP.getValue()) {
                    if (flagMessage) {
                        System.out.print("U ");
                    }
                    lastDepthSummary[firstMoveIdx] = shiftUp(orgX, orgY, zeroPos, zeroSym,
                            1, limit, orgValReg, orgValSym, orgCopy, reset);
                }
                lastDepthSummary[firstMoveIdx + rowSize] = idaCount - startCounter;
                estimate1stMove[firstMoveIdx] = endOfSearch;
            }
        }
        if (flagMessage) {
            System.out.println();
        }
    }
}
