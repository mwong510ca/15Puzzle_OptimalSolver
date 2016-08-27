package mwong.myprojects.fifteenpuzzle.solver.standard;

import mwong.myprojects.fifteenpuzzle.solver.SolverProperties;
import mwong.myprojects.fifteenpuzzle.solver.components.PatternOptions;

/**
 * SolverPdbEnh1 extends SolverPdbBase with enhancement 1 symmetry reduction.
 *
 * <p>Dependencies : PatternOptions.java, SolverPdbBase.java, SolverProperties.java
 *
 * @author   Meisze Wong
 *           www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public class SolverPdbEnh1 extends SolverPdbBase {
    /**
     *  Initializes SolverPdbEnh1 object using default preset pattern.
     */
    public SolverPdbEnh1() {
        this(SolverProperties.getDefaultPattern());
    }

    /**
     *  Initializes SolverPdbEnh1 object using given preset pattern.
     *
     *  @param presetPattern the given preset pattern type
     */
    public SolverPdbEnh1(PatternOptions presetPattern) {
        this(presetPattern, 0);
    }

    /**
     *  Initializes SolverPdbEnh1 object with choice of given preset pattern.
     *
     *  @param presetPattern the given preset pattern type
     *  @param choice the number of preset pattern option
     */
    public SolverPdbEnh1(PatternOptions presetPattern, int choice) {
        super(presetPattern, choice);
    }

    /**
     *  Initializes SolverPdbEnh1 object with user defined custom pattern.
     *
     *  @param customPattern byte array of user defined custom pattern
     *  @param elementGroups boolean array of groups reference to given pattern
     */
    public SolverPdbEnh1(byte[] customPattern, boolean[] elementGroups) {
        super(customPattern, elementGroups);
    }

    /**
     *  Initializes SolverPdbEnh1 object with a given concrete class.
     *
     *  @param copySolver an instance of SolverPdbEnh1
     */
    public SolverPdbEnh1(SolverPdbBase copySolver) {
        super(copySolver);
    }

    // ----- Enhancement 1, enable symmetry reduction -----

    // recursive depth first search until it reach the goal state or timeout, the least
    // estimate and node counts will be use to determine the starting order of next search
    @Override
    protected void dfsStartingOrder(int orgX, int orgY, int limit, int orgValReg,
            int orgValSym) {
        int zeroPos = orgY * rowSize + orgX;
        int zeroSym = symmetryPos[zeroPos];
        int[] orgCopy = new int[szPdKeys];
        System.arraycopy(pdKeys, 0, orgCopy, 0, szPdKeys);
        if (orgX < rowSize - 1 && lastDepthSummary[4] > 0) {
            shiftRight(orgX, orgY, zeroPos, zeroSym,
                1, limit, orgValReg, orgValSym, orgCopy, reset);
        }
        if (orgY < rowSize - 1 && lastDepthSummary[5] > 0) {
            shiftDown(orgX, orgY, zeroPos, zeroSym,
                1, limit, orgValReg, orgValSym, orgCopy, reset);
        }
        if (orgX > 0 && lastDepthSummary[6] > 0) {
            shiftLeft(orgX, orgY, zeroPos, zeroSym,
                1, limit, orgValReg, orgValSym, orgCopy, reset);
        }
        if (orgY > 0 && lastDepthSummary[7] > 0) {
            shiftUp(orgX, orgY, zeroPos, zeroSym,
                1, limit, orgValReg, orgValSym, orgCopy, reset);
        }
    }

    // enable symmetry reduction, restore isIdenticalSymmetry function.
    @Override
    protected boolean isIdenticalSymmetry(int zeroPos, int zeroSym) {
        if (zeroPos == zeroSym) {
            for (int i = 0; i < szGroup; i++) {
                if (pdKeys[i] != pdKeys[i + offsetPdSym]) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
