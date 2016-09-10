package mwong.myprojects.fifteenpuzzle.solver;

/**
 * Heuristic options that can be used.
 *  <li>{@link #MD}</li>
 *  <li>{@link #MDLC}</li>
 *  <li>{@link #WD}</li>
 *  <li>{@link #PD555}</li>
 *  <li>{@link #PD663}</li>
 *  <li>{@link #PD78}</li>
 *  <li>{@link #PDCustom}</li>
 *
 * @author Meisze Wong
 *         www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public enum HeuristicOptions {
    /**
     *   Heuristic function using Manhattan Distance.
     */
    MD("Manhattan Distance"),

    /**
     *   Heuristic function using Manhattan Distance with Linear Conflict.
     */
    MDLC("Manhattan Distance with Linear Conflict"),

    /**
     *   Heuristic function using Walking Distance.
     */
    WD("Walking Distance"),

    /**
     *   Heuristic function using Walking Distance + Manhattan Distance with Linear Conflict.
     */
    WDMD("Walking Distance + Manhattan Distance with Linear Conflict"),

    /**
     *   Heuristic function using Additive Pattern Database 555.
     */
    PD555("Additive Pattern Database 555"),

    /**
     *   Heuristic function using Additive Pattern Database 663.
     */
    PD663("Additive Pattern Database 663"),

    /**
     *   Heuristic function using Additive Pattern Database 78.
     */
    PD78("Additive Pattern Database 78"),

    /**
     *   Heuristic function using User Defined Custom Pattern Database.
     */
    PDCustom("Additive Pattern - user defined custom pattern");

    // initialize heuristic options
    private String description;
    HeuristicOptions(String str) {
        description = str;
    }

    /**
     * Return heuristic function description.
     *
     * @return heuristic function description
     */
    public String getDescription() {
        return description;
    }
}
