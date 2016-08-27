/****************************************************************************
 *  @author   Meisze Wong
 *            www.linkedin.com/pub/macy-wong/46/550/37b/
 *
 *  Compilation  : javac SolverPD.java
 *  Dependencies : Board.java, Direction.java, Stopwatch.java,
 *                 PDElement.java, PDCombo.java, PDPresetPatterns.java,
 *                 SolverAbstract, AdvancedAccumulator.java
 *                 AdvancedBoard.java, AdvancedMoves.java
 *
 *  SolverPD implements SolverInterface.  It take a Board object and solve the
 *  puzzle with IDA* using additive pattern database.
 *
 ****************************************************************************/

package mwong.myprojects.fifteenpuzzle.solver.advanced;

import mwong.myprojects.fifteenpuzzle.solver.SolverProperties;
import mwong.myprojects.fifteenpuzzle.solver.advanced.ai.ReferenceAccumulator;
import mwong.myprojects.fifteenpuzzle.solver.components.Board;
import mwong.myprojects.fifteenpuzzle.solver.components.Direction;
import mwong.myprojects.fifteenpuzzle.solver.components.PatternOptions;
import mwong.myprojects.fifteenpuzzle.solver.standard.SolverPdbBase;

import java.util.Arrays;

/**
 * SmartSolverPD extends SolverPD.  The advanced version extend the standard solver
 * using the reference boards collection to boost the initial estimate.
 *
 * <p>Dependencies : AdvancedRecord.java, Board.java, Direction.java, HeuristicOptions.java,
 *                   PatternOptions.java, ReferenceAccumulator.java, SmartSolverConstants.java,
 *                   SmartSolverExtra.java, SolverPD.java, SolverProperties.java, Stopwatch.java
 *
 * @author   Meisze Wong
 *           www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public class SmartSolverPdb extends SmartSolverPdbBase {
    /**
     * Initializes SolverPD object using default preset pattern.
     *
     * @param refAccumulator the given ReferenceAccumulator object
     */
    public SmartSolverPdb(ReferenceAccumulator refAccumulator) {
        this(SolverProperties.getDefaultPattern(), refAccumulator);
    }

    /**
     * Initializes SolverPD object using given preset pattern.
     *
     * @param presetPattern the given preset pattern type
     * @param refAccumulator the given ReferenceAccumulator object
     */
    public SmartSolverPdb(PatternOptions presetPattern, ReferenceAccumulator refAccumulator) {
        this(presetPattern, 0, refAccumulator);
    }

    /**
     * Initializes SolverPD object with choice of given preset pattern.  If refAccumlator is null
     * or empty, it will act as standard version.
     *
     * @param presetPattern the given preset pattern type
     * @param choice the number of preset pattern option
     * @param refAccumulator the given ReferenceAccumulator object
     */
    public SmartSolverPdb(PatternOptions presetPattern, int choice,
            ReferenceAccumulator refAccumulator) {
        super(presetPattern, choice, refAccumulator);
    }

    /**
     * Initializes SolverPD object with user defined custom pattern.  If refAccumlator is null
     * or empty, it will act as standard version.
     *
     * @param customPattern byte array of user defined custom pattern
     * @param elementGroups boolean array of groups reference to given pattern
     * @param refAccumulator the given ReferenceAccumulator object
     */
    public SmartSolverPdb(byte[] customPattern, boolean[] elementGroups,
            ReferenceAccumulator refAccumulator) {
        super(customPattern, elementGroups, refAccumulator);
    }

    /**
     *  Initializes SolverPdb object with a given concrete class.
     *
     *  @param copySolver an instance of SolverPdb
     */
    public SmartSolverPdb(SolverPdbBase copySolver, ReferenceAccumulator refAccumulator) {
        super(copySolver, refAccumulator);
    }

    // overload method to calculate the heuristic value of the given board and conditions
    @Override
    protected byte heuristic(Board board, boolean isAdvanced, boolean isSearch) {
        if (!board.isSolvable()) {
            return -1;
        }

        if (!board.equals(lastBoard) || isSearch) {
            priorityAdvanced = -1;
            lastBoard = board;
            tiles = board.getTiles();
            zeroX = board.getZeroX();
            zeroY = board.getZeroY();
            byte[] tilesSym = board.getTilesSym();

            // additive pattern database components
            pdKeys = convert2pd(tiles, tilesSym, szGroup);
            pdValReg = 0;
            pdValSym = 0;
            for (int i = szGroup; i < szGroup * 2; i++) {
                pdValReg += pdKeys[i];
                pdValSym += pdKeys[i +  offsetPdSym];
            }

            priorityGoal = (byte) Math.max(pdValReg, pdValSym);
        }

        if (!isAdvanced) {
            return priorityGoal;
        }

        AdvancedRecord record = extra.advancedContains(board, isSearch, refAccumulator);
        if (record != null) {
            priorityAdvanced = record.getEstimate();
            if (record.hasPartialMoves()) {
                solutionMove = record.getPartialMoves();
            }
        }
        if (priorityAdvanced != -1) {
            return priorityAdvanced;
        }

        priorityAdvanced = priorityGoal;
        if (priorityAdvanced < refCutoff) {
            return priorityAdvanced;
        }
        //System.out.println("send to advanced estimate");
        priorityAdvanced = extra.advancedEstimate(board, priorityAdvanced, refCutoff,
                refAccumulator.getActiveMap());

        if ((priorityAdvanced - priorityGoal) % 2 == 1) {
            priorityAdvanced++;
        }
        return priorityAdvanced;
    }

    public boolean hasPartialSolution(Board board) {
        AdvancedRecord record = extra.advancedContains(board, tagSearch, refAccumulator);
        if (record != null && record.hasPartialMoves()) {
            return true;
        }
        return false;
    }

    // solve the puzzle using interactive deepening A* algorithm
    protected void idaStar(int limit) {
        if (inUsePattern == PatternOptions.Pattern_78) {
            lastSearchBoard = new Board(tiles);
        }

        if (solutionMove[1] != null) {
            advancedSearch(limit);
            return;
        }
        super.idaStar(limit);
    }

    // skip the first 8 moves from stored record then solve the remaining puzzle
    // using depth first search with exact number of steps of optimal solution
    private void advancedSearch(int limit) {
        Direction[] dupSolution = new Direction[limit + 1];
        System.arraycopy(solutionMove, 1, dupSolution, 1, numPartialMoves);

        Board board = new Board(tiles);
        for (int i = 1; i < numPartialMoves; i++) {
            board = board.shift(dupSolution[i]);
            if (board == null) {
                System.out.println(i + "board is null");
                System.out.println(Arrays.toString(solutionMove));
                System.out.println(new Board(tiles));
            }
        }
        heuristic(board, tagStandard, tagSearch);

        int firstDirValue = dupSolution[numPartialMoves].getValue();
        for (int i = 0; i < 4; i++) {
            if (i != firstDirValue) {
                lastDepthSummary[i] = endOfSearch;
                lastDepthSummary[i + 4] = 0;
            } else {
                lastDepthSummary[i + 4] = 1;
            }
        }

        idaCount = numPartialMoves;
        if (flagMessage) {
            System.out.print("ida limit " + limit);
        }
        dfsStartingOrder(zeroX, zeroY, limit - numPartialMoves + 1, pdValReg, pdValSym);
        if (solved) {
            System.arraycopy(solutionMove, 2, dupSolution, numPartialMoves + 1,
                    limit - numPartialMoves);
            solutionMove = dupSolution;
        }
        steps = (byte) limit;
        searchDepth = limit;
        searchNodeCount += idaCount;

        if (flagMessage) {
            if (timeout) {
                System.out.printf("\tNodes : %-15s timeout\n", Integer.toString(idaCount));
            } else {
                System.out.printf("\tNodes : %-15s  " + stopwatch.currentTime() + "s\n",
                        Integer.toString(idaCount));
            }
        }
    }
}
