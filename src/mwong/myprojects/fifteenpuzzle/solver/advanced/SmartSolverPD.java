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

import mwong.myprojects.fifteenpuzzle.solver.HeuristicOptions;
import mwong.myprojects.fifteenpuzzle.solver.SolverProperties;
import mwong.myprojects.fifteenpuzzle.solver.advanced.ai.ReferenceAccumulator;
import mwong.myprojects.fifteenpuzzle.solver.components.Board;
import mwong.myprojects.fifteenpuzzle.solver.components.Direction;
import mwong.myprojects.fifteenpuzzle.solver.components.PatternOptions;
import mwong.myprojects.fifteenpuzzle.solver.standard.SolverPD;
import mwong.myprojects.fifteenpuzzle.utilities.Stopwatch;

import java.util.Arrays;

public class SmartSolverPD extends SolverPD {
    private final byte numPartialMoves;
    private final byte refCutoff;
    private final ReferenceAccumulator refAccumulator;
    private final SmartSolverExtra extra;
    private Board lastSearchBoard;

    /**
     *  Initializes SolverPD object using default preset pattern.
     */
    public SmartSolverPD(ReferenceAccumulator refAccumulator) {
        this(SolverProperties.getDefaultPattern(), refAccumulator);
    }

     /**
     *  Initializes SolverPD object using given preset pattern.
     *
     *  @param presetPattern the given preset pattern type
     */
    public SmartSolverPD(PatternOptions presetPattern, ReferenceAccumulator refAccumulator) {
        this(presetPattern, 0, refAccumulator);
    }

    /**
     *  Initializes SolverPD object with choice of given preset pattern.
     *
     *  @param presetPattern the given preset pattern type
     *  @param choice the number of preset pattern option
     */
    public SmartSolverPD(PatternOptions presetPattern, int choice, ReferenceAccumulator refAccumulator) {
        super(presetPattern, choice);
        if (refAccumulator == null || refAccumulator.getActiveMap() == null) {
            System.out.println("Referece board collection unavailable."
                    + " Resume to the 15 puzzle solver standard version.");
            extra = null;
            this.refAccumulator = null;
            refCutoff = 0;
            numPartialMoves = 0;
        } else {
            activeSmartSolver = true;
            extra = new SmartSolverExtra();
            this.refAccumulator = refAccumulator;
            refCutoff = SmartSolverProperties.getReferenceCutoff();
            numPartialMoves = SmartSolverConstants.getNumPartialMoves();
        }
    }

    /**
     *  Initializes SolverPD object with user defined custom pattern.
     *
     *  @param customPattern byte array of user defined custom pattern
     *  @param elementGroups boolean array of groups reference to given pattern
     */
    public SmartSolverPD(byte[] customPattern, boolean[] elementGroups,
    		ReferenceAccumulator refAccumulator) {
    	super(customPattern, elementGroups);
        if (refAccumulator == null || refAccumulator.getActiveMap() == null) {
            System.out.println("Referece board collection unavailable."
                    + " Resume to the 15 puzzle solver standard version.");
            extra = null;
            this.refAccumulator = null;
            refCutoff = 0;
            numPartialMoves = 0;
        } else {
            activeSmartSolver = true;
            extra = new SmartSolverExtra();
            this.refAccumulator = refAccumulator;
            refCutoff = SmartSolverProperties.getReferenceCutoff();
            numPartialMoves = SmartSolverConstants.getNumPartialMoves();
        }
    }

    /**
     *  Print solver description with in use pattern.
     */
    @Override
    public void printDescription() {
    	extra.printDescription(flagAdvancedPriority, inUseHeuristic);
    	printInUsePattern();
    }

    /**
     *  Find the optimal path to goal state if the given board is solvable.
     *  Overload findOptimalPath with given heuristic value (for AdvancedAccumulator)
     *
     *  @param board the initial puzzle Board object to solve
     *  @param estimate the given initial limit to solve the puzzle
     */
    public void findOptimalPath(Board board, byte estimate) {
        if (board.isSolvable()) {
            clearHistory();
            stopwatch = new Stopwatch();
            priority1stMove = new int[rowSize * 2];
            System.arraycopy(board.getValidMoves(), 0, priority1stMove, rowSize, rowSize);
            // initializes the board by calling heuristic function using original priority
            // then solve the puzzle with given estimate instead
            heuristic(board, tagStandard, tagSearch);
            idaStar(estimate);
            stopwatch = null;
        }
    }

    @Override
    public byte heuristic(Board board) {
    	return heuristic(board, flagAdvancedPriority, tagSearch);
    }
    
    @Override 
    public byte heuristicStandard(Board board) {
    	if (board == null) {
            throw new IllegalArgumentException("Board is null");
        }
        if (!board.isSolvable()) {
            return -1;
        }
        return heuristic(board, tagStandard, tagReview);    	
    }
    
    @Override 
    public byte heuristicAdvanced(Board board) {
    	if (!activeSmartSolver) {
            throw new UnsupportedOperationException("Advanced version currently inactive."
                    + " Reference collection unavailable. Check the system.");
    	}
    	if (board == null) {
            throw new IllegalArgumentException("Board is null");
        }
        if (!board.isSolvable()) {
            return -1;
        }
        return heuristic(board, tagAdvanced, tagReview);    	
    }
    
    // calculate the heuristic value of the given board and save the properties
    private byte heuristic(Board board, boolean isAdvanced, boolean isSearch) {
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

        priorityAdvanced = extra.advancedEstimate(board, priorityAdvanced, refCutoff, refAccumulator.getActiveMap());

        if ((priorityAdvanced - priorityGoal) % 2 == 1) {
            priorityAdvanced++;
        }
        return priorityAdvanced;
    }

    protected // solve the puzzle using interactive deepening A* algorithm
    void idaStar(int limit) {
        if (inUsePattern == PatternOptions.Pattern_78) {
            lastSearchBoard = new Board(tiles);
        }

        if (solutionMove[1] != null) {
        	advancedSearch(limit);
            return;
        }
        
        int countDir = 0;
        for (int i = 0; i < rowSize; i++) {
            if (priority1stMove[i + rowSize] > 0) {
                countDir++;
            }
        }

        // quick scan for advanced priority, determine the start order for optimization
        if (flagAdvancedPriority && countDir > 1) {
            int initLimit = priorityGoal;
            while (initLimit < limit) {
                idaCount = 0;
                dfs1stPrio(zeroX, zeroY, 0, initLimit, pdValReg, pdValSym);
                initLimit += 2;

                boolean overload = false;
                for (int i = rowSize; i < rowSize * 2; i++) {
                    if (priority1stMove[i] > 10000) {
                        overload = true;
                        break;
                    }
                }
                if (overload) {
                    break;
                }
            }
        }

        // start searching for solution
        while (limit <= maxMoves) {
        	idaCount = 0;
            if (flagMessage) {
                System.out.print("ida limit " + limit);
            }
            dfs1stPrio(zeroX, zeroY, 0, limit, pdValReg, pdValSym);
            searchDepth = limit;
            searchNodeCount += idaCount;

            if (timeout) {
                if (flagMessage) {
                    System.out.printf("\tNodes : %-15s timeout\n", Integer.toString(idaCount));
                }
                return;
            } else {
                if (flagMessage) {
                    System.out.printf("\tNodes : %-15s  " + stopwatch.currentTime() + "s\n",
                            Integer.toString(idaCount));
                }
            	if (solved) {
            		
            		// if currently using pattern database 7-8 and it takes long than cutoff limit
                    // to solve, add the board and solutions to reference boards collection.
                    if (activeSmartSolver && inUseHeuristic == HeuristicOptions.PD78
                            && stopwatch.currentTime() >  refAccumulator.getCutoffLimit()) {
                        // backup original solutions
                        final Stopwatch backupTime = stopwatch;
                        final byte backupSteps = steps;
                        final int backupIdaCount = searchNodeCount;
                        final Direction[] backupSolution = solutionMove.clone();

                        searchTime = stopwatch.currentTime();
                        stopwatch = new Stopwatch();
                        // only update cached advanced priority if using original priority search
                        // and the initial board has added to the reference boards
                        if (refAccumulator.addBoard(this) && !flagAdvancedPriority) {
                            priorityAdvanced = backupSteps;
                        }

                        // restore original solutions
                        stopwatch = backupTime;
                        steps = backupSteps;
                        searchNodeCount = backupIdaCount;
                        solutionMove = backupSolution;
                    }
                    return;
            	}
            }
            limit += 2;
        }
    }

    // skip the first 8 moves from stored record then solve the remaining puzzle
    // using depth first search with exact number of steps of optimal solution
    private void advancedSearch(int limit) {
    	System.out.println("\t\t\t in advanced search");
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
                priority1stMove[i + 4] = 0;
            } else {
                priority1stMove[i + 4] = 1;
            }
        }

        idaCount = numPartialMoves;
        if (flagMessage) {
            System.out.print("ida limit " + limit);
        }
        dfs1stPrio(zeroX, zeroY, 0, limit - numPartialMoves + 1, pdValReg, pdValSym);
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
    
    /**
     * Returns the board object of last search.
     *
     * @return board object of last search
     */
    public final Board lastSearchBoard() {
        if (inUsePattern != PatternOptions.Pattern_78) {
            throw new UnsupportedOperationException();
        }
        return lastSearchBoard;
    }
}
