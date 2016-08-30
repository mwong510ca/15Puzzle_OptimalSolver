package mwong.myprojects.fifteenpuzzle.solver.standard;

/**
 * SolverPdbEnh1 extends SolverPdbBase with enhancement 1 symmetry reduction.
 *
 * <p>Dependencies : SolverPdbBase.java
 *
 * @author   Meisze Wong
 *           www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public class SolverPdbEnh1 extends SolverPdbBase {
    /**
     * Default constructor.
     */
    SolverPdbEnh1() {}

    /**
     * Initializes SolverPdbEnh1 object with a given standard version SolverPdb instance,
     * the concrete class of SolverPdbEnh1.
     *
     *  @param copySolver an instance of SolverPdb
     */
    public SolverPdbEnh1(SolverPdb copySolver) {
        super(copySolver);
    }

    // ----- Enhancement 1, enable symmetry reduction -----

    // recursive depth first search until it reach the goal state or timeout, use
    // hard coded order Right -> Down -> Left -> Up with symmetry reduction.
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
