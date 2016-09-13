package mwong.myprojects.fifteenpuzzle.solver.advanced;

import mwong.myprojects.fifteenpuzzle.solver.SmartSolverExtra;
import mwong.myprojects.fifteenpuzzle.solver.SolverProperties;
import mwong.myprojects.fifteenpuzzle.solver.ai.ReferenceRemote;
import mwong.myprojects.fifteenpuzzle.solver.components.Board;
import mwong.myprojects.fifteenpuzzle.solver.components.Direction;
import mwong.myprojects.fifteenpuzzle.solver.components.PatternOptions;

import java.io.Serializable;
import java.rmi.RemoteException;

/**
 * SmartSolverPdb extends SmartSolverPdbBase use preset partial solution from the reference
 * collection to boost the search time.  This is the completed advanced version of 15 puzzle
 * optimal solver using pattern database.
 *
 * <p>Dependencies : AdvancedRecord.java, Board.java, Direction.java, PatternOptions.java,
 *                   ReferenceRemote.java, SmartSolverExtra.java, SmartSolverPdbBase.java,
 *                   SolverProperties.java,
 *
 * @author Meisze Wong
 *         www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public class SmartSolverPdb extends SmartSolverPdbBase implements Serializable {
    private static final long serialVersionUID = 17195273121L;

    /**
     * Initializes SmartSolverPdb object using default preset pattern.
     *
     * @param refAccumulator the given ReferenceAccumulator object
     */
    public SmartSolverPdb(ReferenceRemote refAccumulator) {
        this(SolverProperties.getPattern(), refAccumulator);
    }

    /**
     * Initializes SmartSolverPdb object using given preset pattern.
     *
     * @param presetPattern the given preset pattern type
     * @param refConnection the given ReferenceRemote connection object
     */
    public SmartSolverPdb(PatternOptions presetPattern, ReferenceRemote refConnection) {
        this(presetPattern, 0, refConnection);
    }

    /**
     * Initializes SmartSolverPdb object with choice of given preset pattern.  If refAccumlator
     * is null or empty, it will act as standard version.
     *
     * @param presetPattern the given preset pattern type
     * @param choice the number of preset pattern option
     * @param refConnection the given ReferenceRemote connection object
     */
    public SmartSolverPdb(PatternOptions presetPattern, int choice,
            ReferenceRemote refConnection) {
        super(presetPattern, choice);
        try {
            if (refConnection == null || refConnection.getActiveMap() == null) {
                System.out.println("Referece board collection unavailable."
                        + " Resume to the 15 puzzle solver standard version.");
                extra = null;
                this.refConnection = null;
            } else {
                activeSmartSolver = true;
                extra = new SmartSolverExtra();
                this.refConnection = refConnection;
            }
        } catch (RemoteException ex) {
            System.out.println("Attention: Server connection failed."
                    + " Advanced estimate will use standard estimate.");
        }
    }

    /**
     * Initializes SmartSolverPdb object with user defined custom pattern.  If refAccumlator
     * is null or empty, it will act as standard version.
     *
     * @param customPattern byte array of user defined custom pattern
     * @param elementGroups boolean array of groups reference to given pattern
     * @param refConnection the given ReferenceRemote connection object
     */
    public SmartSolverPdb(byte[] customPattern, boolean[] elementGroups,
            ReferenceRemote refConnection) {
        super(customPattern, elementGroups);

        try {
            if (refConnection == null || refConnection.getActiveMap() == null) {
                System.out.println("Referece board collection unavailable."
                        + " Resume to the 15 puzzle solver standard version.");
                extra = null;
                this.refConnection = null;
            } else {
                activeSmartSolver = true;
                extra = new SmartSolverExtra();
                this.refConnection = refConnection;
            }
        } catch (RemoteException ex) {
            System.err.println("Attention: Server connection failed."
                    + " Advanced estimate will use standard estimate.");
        }
    }

    /**
     * Returns the boolean value of the given board is a reference board with partial solution.
     *
     * @param board the given Board object
     * @return boolean value of the given board is a reference board with partial solution.
     */
    public boolean hasPartialSolution(Board board) throws RemoteException {
        return extra.hasPartialSolution(board, refConnection.getActiveMap());
    }

    // solve the puzzle using interactive deepening A* algorithm
    protected void idaStar(int limit) throws RemoteException {
        if (inUsePattern == PatternOptions.Pattern_78) {
            lastSearchBoard = new Board(tiles);
        }
        addedReference = false;

        if (solutionMove[1] != null) {
            advancedSearch(limit);
            return;
        }
        super.idaStar(limit);
    }

    // skip the first 8 moves from stored record then solve the remaining puzzle
    // using depth first search with exact number of steps of optimal solution
    private void advancedSearch(int limit) throws RemoteException {
        Direction[] dupSolution = new Direction[limit + 1];
        Board board = prepareAdvancedSearch(limit, dupSolution);
        heuristic(board, tagStandard, tagSearch);
        setLastDepthSummary(dupSolution[numPartialMoves]);

        idaCount = numPartialMoves;
        if (flagMessage) {
            System.out.print("ida limit " + limit);
        }
        dfsStartingOrder(zeroX, zeroY, limit - numPartialMoves + 1, pdValReg, pdValSym);
        searchNodeCount = idaCount;
        afterAdvancedSearch(limit, dupSolution);
    }
}
