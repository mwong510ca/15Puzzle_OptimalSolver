package mwong.myprojects.fifteenpuzzle.solver.components;

/**
 * PatternOptions the preset additive pattern database can be use
 * <li>{@link #Pattern_555}</li>
 * <li>{@link #Pattern_663}</li>
 * <li>{@link #Pattern_78}</li>
 * <li>{@link #Pattern_Custom}</li>
 *
 * <p>Dependencies : PatternConstants.java
 *
 * @author Meisze Wong
 *         www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public enum PatternOptions {
    /**
     * Static pattern 555.
     */
    Pattern_555("555",
            new byte [][] {{ 2, 2, 1, 1, 2, 3, 1, 1, 2, 3, 3, 1, 2, 3, 3, 0 },
                { 1, 1, 1, 1, 2, 2, 1, 3, 2, 2, 3, 3, 2, 3, 3, 0 },
                { 1, 1, 1, 1, 1, 2, 2, 2, 3, 2, 3, 2, 3, 3, 3, 0 },
                { 1, 1, 1, 1, 2, 2, 1, 3, 2, 3, 3, 3, 2, 2, 3, 0 },
                { 1, 2, 2, 2, 1, 1, 1, 2, 3, 3, 1, 2, 3, 3, 3, 0 },
                { 1, 2, 2, 2, 1, 1, 2, 2, 1, 3, 3, 3, 1, 3, 3, 0 },
                { 1, 1, 1, 2, 1, 1, 2, 2, 3, 3, 2, 2, 3, 3, 3, 0 }},
            new boolean [] {false, false, false, false, false, true, false, false, false}),

    /**
     * Static pattern 663.
     */
    Pattern_663("663",
            new byte [][] {{ 1, 1, 1, 1, 1, 1, 2, 2, 3, 3, 3, 2, 3, 3, 3, 0 },
                { 1, 2, 2, 2, 1, 1, 3, 3, 1, 1, 3, 3, 1, 3, 3, 0 },
                { 3, 1, 1, 1, 3, 2, 2, 1, 3, 2, 1, 1, 3, 3, 3, 0 },
                { 1, 1, 1, 2, 1, 1, 1, 2, 3, 3, 3, 2, 3, 3, 3, 0 },
                { 1, 2, 2, 2, 1, 2, 2, 2, 1, 1, 3, 3, 1, 1, 3, 0 },
                { 1, 1, 1, 2, 3, 1, 1, 2, 3, 3, 1, 2, 3, 3, 3, 0 },
                { 1, 1, 2, 2, 3, 1, 2, 2, 3, 3, 2, 2, 3, 3, 3, 0 }},
            new boolean [] {false, false, false, true, false, false, true, false, false}),

    /**
     * Static and disjoint pattern 78.
     */
    Pattern_78("78",
            new byte [][] {{ 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 0 },
                { 1, 1, 1, 1, 2, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 0 },
                { 1, 1, 1, 1, 1, 2, 2, 2, 1, 2, 2, 2, 1, 2, 2, 0 },
                { 1, 1, 1, 1, 2, 2, 1, 1, 2, 2, 1, 1, 2, 2, 2, 0 },
                { 2, 1, 1, 1, 2, 1, 1, 1, 2, 2, 1, 1, 2, 2, 2, 0 },
                { 1, 1, 1, 1, 2, 1, 1, 1, 2, 2, 2, 1, 2, 2, 2, 0 }},
            new boolean [] {false, false, false, false, false, false, false, true, true}),
    /**
     * User defined custom pattern.
     */
    Pattern_Custom("Custom", null, null);

    private String type;
    private byte[][] patterns;
    private boolean[] elements;

    /**
     * Initializes a PatternOptions reference type.
     */
    PatternOptions(String type, byte [][] patterns, boolean [] elements) {
        this.type = type;
        this.patterns = patterns;
        this.elements = elements;
    }

    /**
     * Print the string of the default pattern of the preset pattern.
     */
    public void printDefaults() {
        final  int puzzleSize = PuzzleConstants.getSize();
        final int rowSize = PuzzleConstants.getRowSize();

        System.out.println("15 puzzle preset patterns :");
        PatternOptions[] patterns = values();
        for (int i = 0; i < patterns.length - 1; i++) {
            System.out.print(patterns[i].getType() + "\t\t");
        }
        System.out.println();
        for (int row = 0; row < puzzleSize; row += rowSize) {
            for (int i = 0; i < patterns.length; i++) {
                for (int col = row; col < row + rowSize; col++) {
                    System.out.print(patterns[i].patterns[0][col] + " ");
                }
                System.out.print("\t");
            }
            System.out.println();
        }
    }

    /**
     * Returns the string of the preset pattern type.
     *
     * @return string of the preset pattern type
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the byte array of pattern of given pattern order.
     *
     * @param choice the integer of preset pattern order
     * @return the byte array of pattern
     */
    public final byte[] getPattern(int choice) {
        if (this.equals(Pattern_Custom)) {
            throw new UnsupportedOperationException("Custom pattern will not store a local copy.");
        }
        return patterns[choice];
    }


    /**
     * Returns the boolean represent the given pattern order is valid.
     *
     * @param choice the integer of preset pattern order
     * @return boolean represent the given pattern order is valid
     */
    public boolean isValidPattern(int choice) {
        if (this.equals(Pattern_Custom)) {
            throw new UnsupportedOperationException("Custom pattern in use.");
        }
        if (choice < 0 || choice >= patterns.length) {
            return false;
        }
        return true;
    }

    /**
     * Returns the boolean array of element groups of the preset pattern.
     *
     * @return boolean array of element groups of the preset pattern
     */
    public final boolean[] getElements() {
        if (this.equals(Pattern_Custom)) {
            throw new UnsupportedOperationException("Custom pattern in use.");
        }
        return elements;
    }

    /**
     * Returns the string of the preset pattern with all options.
     *
     * @return string of the preset pattern with all options
     */
    @Override
    public String toString() {
        final int puzzleSize = PuzzleConstants.getSize();
        final int rowSize = PuzzleConstants.getRowSize();

        String str = "15 puzzle preset patterns " + type + " :\n";
        str += "default\t\t";
        for (int i = 1; i < patterns.length; i++) {
            str += "option " + i + "\t";
        }
        str += "\n";
        for (int row = 0; row < puzzleSize; row += rowSize) {
            for (int i = 0; i < patterns.length; i++) {
                for (int col = row; col < row + rowSize; col++) {
                    str += patterns[i][col] + " ";
                }
                str += "\t";
            }
            str += "\n";
        }
        return str;
    }
}
