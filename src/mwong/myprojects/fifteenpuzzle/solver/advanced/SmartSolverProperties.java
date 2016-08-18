package mwong.myprojects.fifteenpuzzle.solver.advanced;

/**
 * SmartSolverProperties contains all default setting of the smart solver.
 *
 * @author   Meisze Wong
 *           www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public class SmartSolverProperties {
    private static final byte REFERENCE_CUTOFF = 30;

    /**
     * Returns the byte value of the cutoff estimate for advanced search.
     *
     * @return byte value of the cutoff estimate for advanced search
     */
    static final byte getReferenceCutoff() {
        return REFERENCE_CUTOFF;
    }
}