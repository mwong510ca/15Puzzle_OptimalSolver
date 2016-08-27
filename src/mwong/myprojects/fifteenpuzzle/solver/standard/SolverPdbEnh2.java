package mwong.myprojects.fifteenpuzzle.solver.standard;

/**
 * SolverPdbEnh2 extends SolverPdbEnh1 with enhancement 2 circular reduction.
 *
 * <p>Dependencies : SolverPdbEnh1.java
 *
 * @author   Meisze Wong
 *           www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public class SolverPdbEnh2 extends SolverPdbEnh1 {
    /**
     * Default constructor.
     */
    SolverPdbEnh2() {
        super();
    }

    /**
     * Initializes SolverPdbEnh2 object with a given standard version SolverPdb instance,
     * the concrete class of SolverPdbEnh2.
     *
     *  @param copySolver an instance of SolverPdb
     */
    public SolverPdbEnh2(SolverPdb copySolver) {
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
