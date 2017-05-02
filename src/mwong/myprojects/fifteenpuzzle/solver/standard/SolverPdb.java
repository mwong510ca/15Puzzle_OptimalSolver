package mwong.myprojects.fifteenpuzzle.solver.standard;

import mwong.myprojects.fifteenpuzzle.solver.HeuristicOptions;
import mwong.myprojects.fifteenpuzzle.solver.SolverProperties;
import mwong.myprojects.fifteenpuzzle.solver.components.ApplicationMode;
import mwong.myprojects.fifteenpuzzle.solver.components.Direction;
import mwong.myprojects.fifteenpuzzle.solver.components.PatternOptions;

/**
 * SolverPdb extends SolverPdbEnh2 with enhancement 3 starting order detection.  This is
 * the completed standard version of 15 puzzle optimal solver using pattern database.
 *
 * <p>Dependencies : Direction.java, HeuristicOptions.java, PatternOptions.java,
 *                   SolverPdbEnh2.java, SolverProperties.java
 *
 * @author Meisze Wong
 *         www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public class SolverPdb extends SolverPdbEnh2 {
    /**
     * Initializes SolverPdb object using default preset pattern.
     */
    public SolverPdb() {
        this(SolverProperties.getPattern());
    }

    /**
     * Initializes SolverPdb object using given preset pattern.
     *
     * @param presetPattern the given preset pattern type
     */
    public SolverPdb(PatternOptions presetPattern) {
        this(presetPattern, 0);
    }

    /**
     * Initializes SolverPdb object using given preset pattern.
     *
     * @param presetPattern the given preset pattern type
     * @param appMode the given applicationMode for GUI or CONSOLE
     */
    protected SolverPdb(PatternOptions presetPattern, ApplicationMode appMode) {
        this(presetPattern, 0, appMode);
    }

    /**
     * Initializes SolverPdb object with choice of given preset pattern.
     *
     * @param presetPattern the given preset pattern type
     * @param choice the number of preset pattern option
     */
    public SolverPdb(PatternOptions presetPattern, int choice) {
        this(presetPattern, choice, ApplicationMode.CONSOLE);
    }

    private SolverPdb(PatternOptions presetPattern, int choice, ApplicationMode appMode) {
        super(appMode);
        loadPdbComponents(presetPattern, choice, appMode);
        loadPdbElements(presetPattern.getElements(), appMode);
        inUsePattern = presetPattern;
        inUsePtnArray = presetPattern.getPattern(choice);

        switch (presetPattern) {
            case Pattern_555:
                inUseHeuristic = HeuristicOptions.PD555;
                break;
            case Pattern_663:
                inUseHeuristic = HeuristicOptions.PD663;
                break;
            case Pattern_78:
                inUseHeuristic = HeuristicOptions.PD78;
                break;
            default:
                System.err.println("SolverPdb invalid presetPattern " + presetPattern);
        }
    }

    /**
     * Initializes SolverPdb object with user defined custom pattern.
     *
     * @param customPattern byte array of user defined custom pattern
     * @param elementGroups boolean array of groups reference to given pattern
     */
    public SolverPdb(byte[] customPattern, boolean[] elementGroups) {
        customPdbComponents(customPattern);
        loadPdbElements(elementGroups);
        inUsePattern = PatternOptions.Pattern_Custom;
        inUsePtnArray = customPattern;
        inUseHeuristic = HeuristicOptions.PDCustom;
    }

    /**
     * Initializes SolverPdb object with a given concrete class.
     *
     * @param copySolver an instance of SolverPdb
     */
    protected SolverPdb(SolverPdb copySolver) {
        super(copySolver);
    }

    // ----- Enhancement 3, implement starting order detection -----

    // recursive depth first search until it reach the goal state or timeout, the least
    // estimate and node counts will be use to determine the starting order of next search
    protected void dfsStartingOrder(int orgX, int orgY, int limit, int orgValReg,
            int orgValSym) {
        searchDepth = limit;
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

                switch (Direction.values()[firstMoveIdx]) {
                    case RIGHT:
                        lastDepthSummary[firstMoveIdx] = shiftRight(orgX, orgY, zeroPos, zeroSym,
                                1, limit, orgValReg, orgValSym, orgCopy, resetKey);
                        break;
                    case DOWN:
                        lastDepthSummary[firstMoveIdx] = shiftDown(orgX, orgY, zeroPos, zeroSym,
                                1, limit, orgValReg, orgValSym, orgCopy, resetKey);
                        break;
                    case LEFT:
                        lastDepthSummary[firstMoveIdx] = shiftLeft(orgX, orgY, zeroPos, zeroSym,
                                1, limit, orgValReg, orgValSym, orgCopy, resetKey);
                        break;
                    case UP:
                        lastDepthSummary[firstMoveIdx] = shiftUp(orgX, orgY, zeroPos, zeroSym,
                                1, limit, orgValReg, orgValSym, orgCopy, resetKey);
                        break;
                    default:
                        assert false : "Error: starting order switch statement";
                }

                lastDepthSummary[firstMoveIdx + rowSize] = idaCount - startCounter;
                estimate1stMove[firstMoveIdx] = endOfSearch;
            }
        }
    }
}
