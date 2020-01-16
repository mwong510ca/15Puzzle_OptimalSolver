package mwong.myprojects.fifteenpuzzle.puzzle;

/**
 * Heuristic options is enum type of heuristic function that can be used.
 *
 * @author <a href="http://www.linkedin.com/pub/macy-wong/46/550/37b/"
 *            target="_blank">Meisze Wong (linkedin)</a>
 * @see <a href="http://www.github.com/mwong510ca/15PuzzleOptimalSolver/"
 *         target="_blank">GitHub (full project)</a>
 */
public enum HeuristicOptions {
  /**
   *  Heuristic function using Manhattan Distance.
   */
  MD("Manhattan Distance"),

  /**
   *  Heuristic function using Manhattan Distance with Linear Conflict.
   */
  MDLC("Manhattan Distance with Linear Conflict"),

  /**
   *  Heuristic function using Walking Distance.
   */
  WD("Walking Distance"),

  /**
   *  Heuristic function using Walking Distance + Manhattan Distance with Linear Conflict.
   */
  WDMD("Walking Distance + Manhattan Distance with Linear Conflict"),

  /**
   *  Heuristic function using Additive Pattern Database 555.
   */
  PD555("Additive Pattern Database 555"),

  /**
   *  Heuristic function using Additive Pattern Database 663.
   */
  PD663("Additive Pattern Database 663"),

  /**
   *  Heuristic function using Additive Pattern Database 78.
   */
  PD78("Additive Pattern Database 78"),

  /**
   *  Heuristic function using User Defined Custom Pattern Database.
   */
  PDCustom("Additive Pattern Database - user defined custom pattern");

  /** The description of HeusristicOptions. */
  private String description;

  /**
   * Initialize heuristic options.
   *
   * @param str the string of heuristic description
   */
  HeuristicOptions(final String str) {
    description = str;
  }

  /**
   * Returns heuristic function description.
   *
   * @return heuristic function description
   */
  public String getDescription() {
    return description;
  }
}
