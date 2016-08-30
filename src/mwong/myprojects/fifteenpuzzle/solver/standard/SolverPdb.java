package mwong.myprojects.fifteenpuzzle.solver.standard;

import mwong.myprojects.fifteenpuzzle.solver.HeuristicOptions;
import mwong.myprojects.fifteenpuzzle.solver.SolverProperties;
import mwong.myprojects.fifteenpuzzle.solver.components.Direction;
import mwong.myprojects.fifteenpuzzle.solver.components.PatternOptions;

/**
 * SolverPdb extends SolverPdbEnh2 with enhancement 3 starting order detection.  This is
 * the completed standard version of 15 puzzle optimal solver using pattern database.
 *
 * <p>Dependencies : Direction.java, HeuristicOptions.java, PatternOptions.java,
 *                   SolverPdbEnh2.java, SolverProperties.java
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
        loadPDComponents(presetPattern, choice);
        loadPDElements(presetPattern.getElements());
        inUsePattern = presetPattern;
        inUsePtnArray = presetPattern.getPattern(choice);
        if (presetPattern == PatternOptions.Pattern_555) {
            inUseHeuristic = HeuristicOptions.PD555;
        } else if (presetPattern == PatternOptions.Pattern_663) {
            inUseHeuristic = HeuristicOptions.PD663;
        } else if (presetPattern == PatternOptions.Pattern_78) {
            inUseHeuristic = HeuristicOptions.PD78;
        } else {
            System.err.println("SolverPD init error");
        }
    }

    /**
     *  Initializes SolverPdb object with user defined custom pattern.
     *
     *  @param customPattern byte array of user defined custom pattern
     *  @param elementGroups boolean array of groups reference to given pattern
     */
    public SolverPdb(byte[] customPattern, boolean[] elementGroups) {
        customPDComponents(customPattern);
        loadPDElements(elementGroups);
        inUsePattern = PatternOptions.Pattern_Custom;
        inUsePtnArray = customPattern;
        inUseHeuristic = HeuristicOptions.PDCustom;
    }

    /**
     *  Initializes SolverPdb object with a given concrete class.
     *
     *  @param copySolver an instance of SolverPdb
     */
    protected SolverPdb(SolverPdb copySolver) {
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
        int[] estimate1stMove = new int[4 * 2];
        System.arraycopy(lastDepthSummary, 0, estimate1stMove, 0, 4 * 2);

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
                    lastDepthSummary[firstMoveIdx] = shiftRight(orgX, orgY, zeroPos, zeroSym,
                            1, limit, orgValReg, orgValSym, orgCopy, reset);
                } else if (firstMoveIdx == Direction.DOWN.getValue()) {
                    lastDepthSummary[firstMoveIdx] = shiftDown(orgX, orgY, zeroPos, zeroSym,
                            1, limit, orgValReg, orgValSym, orgCopy, reset);
                } else if (firstMoveIdx == Direction.LEFT.getValue()) {
                    lastDepthSummary[firstMoveIdx] = shiftLeft(orgX, orgY, zeroPos, zeroSym,
                            1, limit, orgValReg, orgValSym, orgCopy, reset);
                } else if (firstMoveIdx == Direction.UP.getValue()) {
                    lastDepthSummary[firstMoveIdx] = shiftUp(orgX, orgY, zeroPos, zeroSym,
                            1, limit, orgValReg, orgValSym, orgCopy, reset);
                }
                lastDepthSummary[firstMoveIdx + rowSize] = idaCount - startCounter;
                estimate1stMove[firstMoveIdx] = endOfSearch;
            }
        }
    }
}
