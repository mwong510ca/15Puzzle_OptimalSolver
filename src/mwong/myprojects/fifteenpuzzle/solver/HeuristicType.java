package mwong.myprojects.fifteenpuzzle.solver;

/**
 *  Directions that can be used.
 *  <li>{@link #MD}</li>
 *  <li>{@link #MDLC}</li>
 *  <li>{@link #WD}</li>
 *  <li>{@link #PD555}</li>
 *  <li>{@link #PD663}</li>
 *  <li>{@link #PD78}</li>
 *  <li>{@link #PDCustom}</li>
 */
public enum HeuristicType {
    /**
     *   Heuristic Type Manhattan Distance.
     */
    MD("Manhattan Distance"),
    /**
     *   Heuristic Type Manhattan Distance with Linear Conflict.
     */
    MDLC("Manhattan Distance with Linear Conflict"),
    /**
     *   Heuristic Type Walking Distance.
     */
    WD("Walking Distance"),
    /**
     *   Heuristic Type Walking Distance + Manhattan Distance with Linear Conflict.
     */
    WDMD("Walking Distance + Manhattan Distance with Linear Conflict"),
    /**
     *   Heuristic Type Additive Pattern Database 555.
     */
    PD555("Additive Pattern Database 555"),
    /**
     *   Heuristic Type Additive Pattern Database 663.
     */
    PD663("Additive Pattern Database 663"),
    /**
     *   Additive Pattern Database 78.
     */
    PD78("Additive Pattern Database 78"),
    /**
     *   Heuristic Type User Defined Custom Pattern Database.
     */
    PDCustom("Additive Pattern - user defined custom pattern");

    // initialize solver option
    private String description;
    HeuristicType(String str) {
        description = str;
    }

    /**
     *  Return heuristic function description.
     *
     *  @return heuristic function description
     */
    public String getDescription() {
        return description;
    }
}
