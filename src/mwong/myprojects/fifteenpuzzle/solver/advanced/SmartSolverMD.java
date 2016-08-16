/****************************************************************************
 *  @author   Meisze Wong
 *            www.linkedin.com/pub/macy-wong/46/550/37b/
 *
 *  Compilation  : javac SolverMD.java
 *  Dependencies : Board.java, Direction.java, Stopwatch.java,
 *                 SolverAbstract.java, AdvancedAccumulator.java
 *                 ReferenceBoard.java, ReferenceMoves.java
 *
 *  SolverMD implements SolverInterface.  It take a Board object and solve
 *  the puzzle with IDA* using manhattan distance with linear conflict option.
 *
 ****************************************************************************/

package mwong.myprojects.fifteenpuzzle.solver.advanced;

import mwong.myprojects.fifteenpuzzle.solver.advanced.ai.ReferenceBoard;
import mwong.myprojects.fifteenpuzzle.solver.advanced.ai.ReferenceMoves;
import mwong.myprojects.fifteenpuzzle.solver.advanced.ai.ReferenceAccumulator;
import mwong.myprojects.fifteenpuzzle.solver.components.Board;
import mwong.myprojects.fifteenpuzzle.solver.components.Direction;
import mwong.myprojects.fifteenpuzzle.solver.standard.SolverMD;
import mwong.myprojects.utilities.Stopwatch;

import java.util.Map;
import java.util.Map.Entry;

public class SmartSolverMD extends SolverMD {
	private final byte numPartialMoves;
	private final byte refCutoff;    
	private final ReferenceAccumulator refAccumulator;
    private SmartSolverExtra extra;
    
    /**
     * Initializes Solver object.
     */
    public SmartSolverMD() {
        this(!TAG_LINEAR_CONFLICT);
    }

    /**
     * Initializes Solver object.
     *
     * @param lcFlag boolean flag for message feature
     */
    public SmartSolverMD(boolean lcFlag) {
        this(lcFlag, new ReferenceAccumulator());
    }

    /**
     * Initializes Solver object.
     *
     * @param lcFlag boolean flag for message feature
     */
    public SmartSolverMD(ReferenceAccumulator refAccumulator) {
    	this(!TAG_LINEAR_CONFLICT, refAccumulator);
    }

    /**
     * Initializes Solver object.
     *
     * @param lcFlag boolean flag for message feature
     */
    public SmartSolverMD(boolean lcFlag, ReferenceAccumulator refAccumulator) {
        super(lcFlag);
        extra = new SmartSolverExtra();
        this.refAccumulator = refAccumulator;
        refCutoff = SmartSolverProperties.getReferenceCutoff();
        numPartialMoves = SmartSolverProperties.getNumPartialMoves();
        //TODO is refAccumulator unavailable
    }
    
    /**
     *  Print solver description.
     */
    @Override
    public void printDescription() {
    	extra.printDescription(flagAdvancedPriority, inUseHeuristic);
    }

    // calculate the heuristic value of the given board and save the properties
    protected byte heuristic(Board board, boolean isAdvanced, boolean isSearch) {
        if (!board.isSolvable()) {
            return -1;
        }

        priorityAdvanced = -1;
        if (!board.equals(lastBoard)) {
            lastBoard = board;
            zeroX = board.getZeroX();
            zeroY = board.getZeroY();
            tiles = board.getTiles();
            tilesSym = board.getTilesSym();
            priority1stMove = new int [rowSize * 2];
            System.arraycopy(board.getValidMoves(), 0, priority1stMove, rowSize, rowSize);

            priorityGoal = 0;
            int base = 0;

            for (int row = 0; row < rowSize; row++) {
                final int baseRange = base + rowSize;
                for (int col = 0; col < rowSize; col++) {
                    int value = tiles[base + col];
                    if (value > 0) {
                        priorityGoal += Math.abs((value - 1) % rowSize - col);
                        priorityGoal += Math.abs((((value - 1)
                                - (value - 1) % rowSize) / rowSize) - row);

                        // linear conflict horizontal
                        if (flagLinearConflict) {
                            if (value > base && value <= baseRange) {
                                for (int col2 = col + 1; col2 < rowSize; col2++) {
                                    int value2 = tiles[base + col2];
                                    if ((value2 > base) && (value2 < value)) {
                                        priorityGoal += 2;
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    // linear conflict vertical
                    if (flagLinearConflict && tilesSym[base + col] > 0) {
                        value = tilesSym[base + col];
                        if (value > base && value <= baseRange) {
                            for (int col2 = col + 1; col2 < rowSize; col2++) {
                                int value2 = tilesSym[base + col2];
                                if ((value2 > base) && (value2 < value)) {
                                    priorityGoal += 2;
                                    break;
                                }
                            }
                        }
                    }
                }
                base += rowSize;
            }
        } else if (isSearch) {
            zeroX = board.getZeroX();
            zeroY = board.getZeroY();
            tiles = board.getTiles();
            tilesSym = board.getTilesSym();
            priority1stMove = new int [rowSize * 2];
            System.arraycopy(board.getValidMoves(), 0, priority1stMove, rowSize, rowSize);
        }
        
        if (!isAdvanced) {
            return priorityGoal;
        }

        SmartRecord record = extra.advancedContains(board, isSearch, refAccumulator);
        if (record != null) {
        	priorityAdvanced = record.getMoves();
        	if (record.hasInitialMoves()) {
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


    // solve the puzzle using interactive deepening A* algorithm
    @Override
	protected void idaStar(int limit) {
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
                dfs1stPrio(zeroX, zeroY, 0, initLimit, priorityGoal);
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

        while (limit <= maxMoves) {
            idaCount = 0;
            if (flagMessage) {
                System.out.print("ida limit " + limit);
            }
            dfs1stPrio(zeroX, zeroY, 0, limit, priorityGoal);
            searchDepth = limit;
            searchNodeCount += idaCount;
            
            if (timeout) {
                if (flagMessage) {
                	System.out.println("\tNodes : " + num2string(idaCount) + "timeout");
                }
                return;
            } else {
            	if (flagMessage) {
            		System.out.println("\tNodes : " + num2string(idaCount) + stopwatch.currentTime() + "s");
            	}
            	if (solved) {
                    return;
                }
            }
            limit += 2;
        }
    }

    // overload idaStar to solve the puzzle with the given max limit for advancedEstimate
    void idaStar(int limit, int maxLimit) {
        int initLimit = limit;
        while (limit <= maxLimit) {
            super.dfs1stPrio(zeroX, zeroY, 0, limit, initLimit);
            if (solved) {
                return;
            }
            limit += 2;
        }
    }

    // skip the first 8 moves from stored record then solve the remaining puzzle
    // using depth first search with exact number of steps of optimal solution
    private void advancedSearch(int limit) {
        Direction[] dupSolution = new Direction[limit + 1];
        System.arraycopy(solutionMove, 1, dupSolution, 1, numPartialMoves);

        Board board = new Board(tiles);
        for (int i = 1; i < numPartialMoves; i++) {
            board = board.shift(dupSolution[i]);
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
        dfs1stPrio(zeroX, zeroY, 0, limit - numPartialMoves + 1, priorityGoal);
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
            	System.out.println("\tNodes : " + num2string(idaCount) + "timeout");
            } else {
            	System.out.println("\tNodes : " + num2string(idaCount) + stopwatch.currentTime() + "s");
            }
        }
    }
}
