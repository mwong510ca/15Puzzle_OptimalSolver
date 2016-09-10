package mwong.myprojects.fifteenpuzzle.solver.ai;

import mwong.myprojects.fifteenpuzzle.PropertiesCache;

/**
 * ReferenceProperties contains the default values of the ReferenceAccumulator.
 *
 * <p>Dependencies : PropertiesCache.java
 *
 * @author Meisze Wong
 *         www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public class ReferenceProperties {
    //ReferenceAccumulator
    private static final int DEFAULT_CUTOFF_LIMIT = 8;
    // selected reference boards for default setting, total 40 after generation.
    private static final byte[][][] DEFAULT_BOARDS = {
            {{ 0, 15,  8,  3, 12, 11,  7,  4, 14, 10,  6,  5,  9, 13,  2,  1}, {0,  70}},
            {{ 6,  5,  9, 13,  2,  1, 10, 14,  3,  7,  0, 15,  4,  8, 12, 11}, {10, 72}},
            {{ 0, 12,  8,  4, 15, 11,  7,  3, 14, 10,  6,  2, 13,  9,  5,  1}, {0,  72}},
            {{ 6,  5, 14, 13,  2,  1, 10,  9,  8,  7,  0, 15,  4,  3, 12, 11}, {10, 70}},
            {{ 0,  5,  9, 13,  2,  1, 10, 14,  3,  7, 11, 15,  4,  8, 12,  6}, {0,  72}},
            {{ 0, 12,  7,  4, 15, 11,  8,  3, 10, 14,  6,  2, 13,  9,  5,  1}, {0,  70}},
            {{ 0, 15,  8,  7, 12, 11,  4,  3, 14, 13,  6,  5, 10,  9,  2,  1}, {0,  72}},
            {{ 1,  5,  9, 13,  2,  6, 10, 14,  3,  7, 11, 15,  4,  8, 12,  0}, {15, 72}},
            {{ 0, 15,  8,  4, 12, 11,  7,  5, 14, 10,  6,  3, 13,  2,  9,  1}, {0,  70}},
            {{ 1, 10, 14, 13,  7,  6,  5,  9,  8,  2, 11, 15,  4,  3, 12,  0}, {15, 72}},
            {{ 0, 12,  8,  7, 15, 11,  4,  3, 14, 13,  6,  2, 10,  9,  5,  1}, {0,  72}},
            {{ 6,  5, 14, 13,  2,  1, 10,  9,  8,  7, 11, 12,  4,  3, 15,  0}, {15, 70}},
            {{ 0,  5,  9, 13,  2,  6, 10, 14,  3,  7,  1, 15,  4,  8, 12, 11}, {0,  72}},
            {{ 6,  5,  9, 13,  2,  1, 10, 14,  3,  7, 11, 12,  4,  8, 15,  0}, {15, 70}},

            {{ 0, 15,  8, 13, 12, 11,  9, 10, 14,  3,  6,  2,  4,  7,  5,  1}, { 0, 78}},
            {{11, 15,  9, 13, 12,  0, 10, 14,  3,  7,  6,  2,  4,  8,  5,  1}, { 5, 78}},
            {{ 0, 12,  5, 13, 15,  6, 10,  9,  2,  7, 11, 14,  4,  3,  8,  1}, { 0, 78}},
            {{ 0, 12,  8, 13, 15, 11,  7,  9, 14, 10,  6,  2,  4,  3,  5,  1}, { 0, 78}},
            {{ 0, 14, 15, 13,  8, 11, 10,  5, 12,  7,  6,  9,  4,  2,  3,  1}, { 0, 78}},
            {{ 0, 15,  9, 13, 11, 12, 10, 14,  3,  7,  6,  2,  4,  8,  5,  1}, { 0, 80}},
            {{ 0, 12,  9, 13, 15, 11, 10, 14,  8,  3,  6,  2,  4,  7,  5,  1}, { 0, 80}},
            {{ 0, 12,  9, 13, 15, 11, 10, 14,  7,  8,  6,  2,  4,  3,  5,  1}, { 0, 80}},
            {{ 0, 12,  9, 13, 15,  8, 10, 14,  11, 7,  6,  2,  4,  3,  5,  1}, { 0, 80}},
            {{ 0, 12,  9, 13, 15, 11, 10, 14,  3,  7,  5,  6,  4,  8,  2,  1}, { 0, 80}},
            {{ 0, 12,  9, 13, 15, 11, 10, 14,  7,  8,  5,  6,  4,  3,  2,  1}, { 0, 80}},
            {{ 0, 12,  9, 13, 15, 11, 10, 14,  3,  7,  6,  2,  4,  8,  5,  1}, { 0, 80}},
            {{ 0, 12,  9, 13, 15, 11, 14, 10,  3,  8,  6,  2,  4,  7,  5,  1}, { 0, 80}},
            {{ 0, 12, 10, 13, 15, 11,  9, 14,  7,  3,  6,  2,  4,  8,  5,  1}, { 0, 80}},
            {{ 0, 12, 14, 13, 15, 11,  9, 10,  8,  3,  6,  2,  4,  7,  5,  1}, { 0, 80}},
            {{ 0, 12, 10, 13, 15, 11, 14,  9,  7,  8,  6,  2,  4,  3,  5,  1}, { 0, 80}}
    };
    //ReferenceAccumulator
    private static int cutoffBuffer;

    static {
        cutoffBuffer = 5;
        if (PropertiesCache.getInstance().containsKey("referenceCutoffBuffer")) {
            try {
                int buffer = Integer.parseInt(PropertiesCache.getInstance().getProperty(
                        "referenceCutoffBuffer"));
                if (buffer >= -5 && buffer <= 15) {
                    cutoffBuffer = buffer;
                } else {
                    System.err.println("Invalid reference cutoff buffer setting " + buffer
                            + ", allow minimum -5 (105%) to maximum 15 (85%) only."
                            + " Restore to system default 5 (95%).");
                }
            } catch (NumberFormatException ex) {
                System.err.println("Configuration reference cutoff limit is not an iteger,"
                        + " restore to system default 5 (95%).");
            }
        }
    }

    /**
     * Returns the integer value of the default cutoff limit (10 seconds).
     *
     * @return integer value of the default cutoff limit
     */
    public static final int getDefaultCutoffLimit() {
        return DEFAULT_CUTOFF_LIMIT;
    }

    /**
     * Returns the double value of the default cutoff buffer (95%).
     *
     * @return double value of the default cutoff buffer
     */
    public static final int getCutoffBuffer() {
        return cutoffBuffer;
    }

    /**
     * Returns the arrays of selected default reference boards.
     *
     * @return arrays of selected default reference boards
     */
    public static final byte[][][] getDefaultBoards() {
        return DEFAULT_BOARDS;
    }
}