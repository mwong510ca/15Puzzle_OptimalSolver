package mwong.myprojects.fifteenpuzzle.solver;

import mwong.myprojects.fifteenpuzzle.solver.components.PatternOptions;

/**
 * SolverProperties contains all default setting of the solver.
 *
 * <p>Dependencies : PatternOptions.java
 *
 * @author   Meisze Wong
 *           www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public class SolverProperties {
    private static final PatternOptions DEFAULT_PATTERN = PatternOptions.Pattern_663;
    private static final int DEFAULT_TIMEOUT_LIMIT = 10;

    /**
     * Returns the PatternOption of the default pattern.
     *
     * @return PatternOptions of the default pattern
     */
    public static final PatternOptions getDefaultPattern() {
        return DEFAULT_PATTERN;
    }

    /**
     * Returns the integer value of default timeout limit.
     *
     * @return integer value of default timeout limit
     */
    public static final int getDefaultTimeoutLimit() {
        return DEFAULT_TIMEOUT_LIMIT;
    }
}