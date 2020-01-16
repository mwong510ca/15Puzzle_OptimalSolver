package mwong.myprojects.fifteenpuzzle.puzzle;

/**
 * PatternOptions the preset additive pattern database can be use.
 *
 * <p>Dependencies : HeuristicOptions.java
 *
 * @author <a href="http://www.linkedin.com/pub/macy-wong/46/550/37b/"
 *            target="_blank">Meisze Wong (linkedin)</a>
 * @see <a href="http://www.github.com/mwong510ca/15PuzzleOptimalSolver/"
 *         target="_blank">GitHub (full project)</a>
 */
public enum PatternOptions {
  /**
   * Static pattern 555.
   */
  Pattern_555("555",
      new byte[][] {{2, 2, 1, 1, 2, 3, 1, 1, 2, 3, 3, 1, 2, 3, 3, 0},
        {1, 1, 1, 1, 2, 2, 1, 3, 2, 2, 3, 3, 2, 3, 3, 0},
        {1, 1, 1, 1, 1, 2, 2, 2, 3, 2, 3, 2, 3, 3, 3, 0},
        {1, 1, 1, 1, 2, 2, 1, 3, 2, 3, 3, 3, 2, 2, 3, 0},
        {1, 2, 2, 2, 1, 1, 1, 2, 3, 3, 1, 2, 3, 3, 3, 0},
        {1, 2, 2, 2, 1, 1, 2, 2, 1, 3, 3, 3, 1, 3, 3, 0},
        {1, 1, 1, 2, 1, 1, 2, 2, 3, 3, 2, 2, 3, 3, 3, 0}},
      new boolean[] {false, false, false, false, false, true, false, false, false},
      HeuristicOptions.PD555),

  /**
   * Static pattern 663.
   */
  Pattern_663("663",
      new byte[][] {{1, 1, 1, 1, 1, 1, 2, 2, 3, 3, 3, 2, 3, 3, 3, 0},
        {1, 2, 2, 2, 1, 1, 3, 3, 1, 1, 3, 3, 1, 3, 3, 0},
        {3, 1, 1, 1, 3, 2, 2, 1, 3, 2, 1, 1, 3, 3, 3, 0},
        {1, 1, 1, 2, 1, 1, 1, 2, 3, 3, 3, 2, 3, 3, 3, 0},
        {1, 2, 2, 2, 1, 2, 2, 2, 1, 1, 3, 3, 1, 1, 3, 0},
        {1, 1, 1, 2, 3, 1, 1, 2, 3, 3, 1, 2, 3, 3, 3, 0},
        {1, 1, 2, 2, 3, 1, 2, 2, 3, 3, 2, 2, 3, 3, 3, 0}},
      new boolean[] {false, false, false, true, false, false, true, false, false},
      HeuristicOptions.PD663),

  /**
   * Static and disjoint pattern 78.
   */
  Pattern_78("78",
      new byte[][] {{1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 0},
        {1, 1, 1, 1, 2, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 0},
        {1, 1, 1, 1, 1, 2, 2, 2, 1, 2, 2, 2, 1, 2, 2, 0},
        {1, 1, 1, 1, 2, 2, 1, 1, 2, 2, 1, 1, 2, 2, 2, 0},
        {2, 1, 1, 1, 2, 1, 1, 1, 2, 2, 1, 1, 2, 2, 2, 0},
        {1, 1, 1, 1, 2, 1, 1, 1, 2, 2, 2, 1, 2, 2, 2, 0}},
      new boolean[] {false, false, false, false, false, false, false, true, true},
      HeuristicOptions.PD78),

  /**
   * User defined custom pattern.
   */
  Pattern_Custom("Custom", null, null, HeuristicOptions.PDCustom);

  /** The string of pattern option type. */
  private String type;
  /** The double byte array of preset patterns. */
  private byte[][] patterns;
  /** The boolean array of pattern group size in use. */
  private boolean[] elements;
  /** THe HeuristionOptions of the PatternOptions. */
  private HeuristicOptions heuristic;

  /**
   * Initializes a PatternOptions reference type.
   *
   * @param type the string of pattern name
   * @param patterns the double byte array of pattern
   * @param elements the boolean array of element group
   * @param heuristic the associated HeuristicOptions
   */
  PatternOptions(final String type, final byte[][] patterns, final boolean[] elements,
      final HeuristicOptions heuristic) {
    this.type = type;
    this.patterns = patterns;
    this.elements = elements;
    this.heuristic = heuristic;
  }

  /**
   * Print the string of the default pattern of the preset pattern.
   */
  public void printPresetChoices() {
    final int puzzleSize = PuzzleConstants.getSize();
    final int rowSize = PuzzleConstants.getRowSize();

    System.out.println("15 puzzle preset patterns :");
    PatternOptions[] allPatterns = values();
    for (int i = 0; i < allPatterns.length - 1; i++) {
      System.out.print(allPatterns[i].getType() + "\t\t");
    }
    System.out.println();
    for (int row = 0; row < puzzleSize; row += rowSize) {
      for (int i = 0; i < allPatterns.length; i++) {
        for (int col = row; col < row + rowSize; col++) {
          System.out.print(allPatterns[i].patterns[0][col] + " ");
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
  public byte[] getPattern(final int choice) {
    if (this.equals(Pattern_Custom)) {
      throw new UnsupportedOperationException("Custom pattern will not store a local copy.");
    }
    return patterns[choice];
  }

  /**
   * Returns HeuristicOptions.
   *
   * @return HeuristicOptions HeuristicOptions
   */
  public HeuristicOptions getHeuristic() {
    return heuristic;
  }

  /**
   * Returns the integer value of number of preset patterns.
   *
   * @return integer value of number of preset patterns
   */
  public int getPatternSize() {
    return patterns.length;
  }

  /**
   * Returns the boolean represent the given pattern order is valid.
   *
   * @param choice the integer of preset pattern order
   * @return boolean represent the given pattern order is valid
   */
  public boolean isValidPattern(final int choice) {
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
  public boolean[] getElements() {
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
