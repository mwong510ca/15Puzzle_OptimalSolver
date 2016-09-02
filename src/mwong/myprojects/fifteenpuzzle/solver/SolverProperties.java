package mwong.myprojects.fifteenpuzzle.solver;

import mwong.myprojects.fifteenpuzzle.PropertiesCache;
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
    private static PatternOptions defaultPattern;
    private static int defaultTimeoutLimit;

    static {
        defaultPattern = PatternOptions.Pattern_663;
        defaultTimeoutLimit = 10;

        if (PropertiesCache.getInstance().containsKey("solverPatternIndex")) {
            try {
                int index = Integer.parseInt(PropertiesCache.getInstance().getProperty(
                        "solverPatternIndex"));
                if (index >= 0 || index <= 2) {
                    defaultPattern = PatternOptions.values()[index];
                } else {
                    System.err.println("Invalid pattern index setting " + index
                            + ", allow 0 (5-5-5), 1 (6-6-3) or 2 (7-8) only."
                            + " Restore to system default 1, pattern 6-6-3.");
                }
            } catch (NumberFormatException ex) {
                System.err.println("Configuration pattern index is not an iteger,"
                        + " restore to system default 1, pattern 6-6-3.");
            }
        }

        if (PropertiesCache.getInstance().containsKey("solverTimeoutLimit")) {
            try {
                int limit = Integer.parseInt(PropertiesCache.getInstance().getProperty(
                        "solverTimeoutLimit"));
                if (limit > 0 && limit <= 300) {
                    defaultTimeoutLimit = limit;
                } else {
                    System.err.println("Invalid timeout limit setting " + limit
                            + ", allow minimum 1 second to maximum 5 minutes (300) only."
                            + " Restore to system default 10 seconds.");
                }
            } catch (NumberFormatException ex) {
                System.err.println("Configuration timeout limt is not an iteger,"
                        + " restore to system default 10 seconds.");
            }
        }
    }

    /**
     * Returns the PatternOption of the default pattern.
     *
     * @return PatternOptions of the default pattern
     */
    public static final PatternOptions getPattern() {
        return defaultPattern;
    }

    /**
     * Returns the integer value of default timeout limit.
     *
     * @return integer value of default timeout limit
     */
    public static final int getTimeoutLimit() {
        return defaultTimeoutLimit;
    }
}