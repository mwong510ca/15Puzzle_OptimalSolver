package mwong.myprojects.fifteenpuzzle.solver.standard;

import mwong.myprojects.fifteenpuzzle.solver.SolverProperties;
import mwong.myprojects.fifteenpuzzle.solver.components.PatternOptions;

/**
 * SolverPdbEnh2 extends SolverPdbEnh1 with enhancement 2 circular reduction.
 *
 * <p>Dependencies : PatternOptions.java, SolverPdbEnh1.java, SolverProperties.java
 *
 * @author   Meisze Wong
 *           www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public class SolverPdbEnh2 extends SolverPdbEnh1 {
    /**
     *  Initializes SolverPdbEnh2 object using default preset pattern.
     */
    public SolverPdbEnh2() {
        this(SolverProperties.getDefaultPattern());
    }

    /**
     *  Initializes SolverPdbEnh2 object using given preset pattern.
     *
     *  @param presetPattern the given preset pattern type
     */
    public SolverPdbEnh2(PatternOptions presetPattern) {
        this(presetPattern, 0);
    }

    /**
     *  Initializes SolverPdbEnh2 object with choice of given preset pattern.
     *
     *  @param presetPattern the given preset pattern type
     *  @param choice the number of preset pattern option
     */
    public SolverPdbEnh2(PatternOptions presetPattern, int choice) {
        super(presetPattern, choice);
    }

    /**
     *  Initializes SolverPdbEnh2 object with user defined custom pattern.
     *
     *  @param customPattern byte array of user defined custom pattern
     *  @param elementGroups boolean array of groups reference to given pattern
     */
    public SolverPdbEnh2(byte[] customPattern, boolean[] elementGroups) {
        super(customPattern, elementGroups);
    }

    /**
     *  Initializes SolverPdbEnh2 object with a given concrete class.
     *
     *  @param copySolver an instance of SolverPdbEnh2
     */
    public SolverPdbEnh2(SolverPdbBase copySolver) {
        super(copySolver);
    }

    // ----- Enhancement 2, enable circular reduction -----

    // restore isValidClockwise function, maximum allow 5 continues clockwise turn.
    @Override
    protected boolean isValidClockwise(int swirlKey) {
        return (swirlKey & 0x07FF) != 0x0155;
    }

    // restore isValidCounterClockwise function, maximum allow 4 continues counterclockwise turn.
    @Override
    protected boolean isValidCounterClockwise(int swirlKey) {
        return (swirlKey & 0x00FF) != 0x00AA;
    }
}
