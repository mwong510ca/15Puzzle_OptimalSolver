/****************************************************************************
 *  @author   Meisze Wong
 *            www.linkedin.com/pub/macy-wong/46/550/37b/
 *
 *  Compilation  : javac SolverAbstract.java
 *  Dependencies : Board.java, Direction.java, Stopwatch.java,
 *                 SolverInterface.java, AdvancedAccumulator.java,
 *                 ReferenceBoard.java, ReferenceMoves.java
 *
 *  SolverAbstract class implements SolverInterface of 15 puzzle that has the
 *  following variables and methods.
 *
 ****************************************************************************/

package mwong.myprojects.fifteenpuzzle.solver.advanced;

import mwong.myprojects.fifteenpuzzle.solver.advanced.ai.ReferenceBoard;
import mwong.myprojects.fifteenpuzzle.solver.advanced.ai.ReferenceMoves;
import mwong.myprojects.fifteenpuzzle.solver.components.Board;
import mwong.myprojects.fifteenpuzzle.solver.components.Direction;
import mwong.myprojects.fifteenpuzzle.solver.standard.SolverMD;
import mwong.myprojects.fifteenpuzzle.solver.HeuristicType;
import mwong.myprojects.fifteenpuzzle.solver.SolverProperties;
import mwong.myprojects.fifteenpuzzle.solver.advanced.ai.ReferenceAccumulator;

import java.util.Map;
import java.util.Map.Entry;

class SmartSolverExtra {
	private final int puzzleSize = SolverProperties.getPuzzleSize();
	private final int rowSize = SolverProperties.getRowSize();
	private final byte[] symmetryPos = SolverProperties.getSymmetryPos();
	private final byte[] symmetryVal = SolverProperties.getSymmetryVal();
	
	/**
     *  Print solver description.
     */
    public void printDescription(boolean flagAdvancedPriority, HeuristicType inUseHeuristic) {
        System.out.println("15 puzzle solver using " + inUseHeuristic.getDescription());
        if (flagAdvancedPriority) {
            System.out.println("Advance option - initial estimate use the goal state and "
                    + "archived boards.");
        } else {
            System.out.println("Original option - initial estimate use the goal state only.");
        }
    }


    // quick check if the given board is one of the reference board.  If so,
    // use the reference estimate.  If search in progress, also update partial
    // solutions if exists.
    final SmartRecord advancedContains(Board board, boolean inSearch, ReferenceAccumulator refAccumulator) {
    	Map<ReferenceBoard, ReferenceMoves> refMap = refAccumulator.getActiveMap();
    	if (refMap == null || refMap.size() == 0) {
    		return null;
    	}

        byte lookupKey = SmartSolverProperties.getReferenceLookup(board.getZero1d());
        int group = SmartSolverProperties.getReferenceGroup(board.getZero1d());

        ReferenceBoard checkBoard = new ReferenceBoard(board);
        ReferenceBoard checkBoardSym = null;
        if (group == 0 || group == 2) {
            checkBoardSym = new ReferenceBoard(new Board(board.getTilesSym()));
        }

        if (refMap.containsKey(checkBoard)) {
        	boolean symmetry = SmartSolverProperties.isSymmetry();
        	int numPartialMoves = SmartSolverProperties.getNumPartialMoves();
        	
        	ReferenceMoves advMoves = refMap.get(checkBoard);
            final byte steps = advMoves.getEstimate(lookupKey);
            
            if (inSearch && advMoves.hasInitialMoves(lookupKey)) {
            	Direction[] solutionMove = new Direction[steps + 1];
                if (group == 3) {
                    System.arraycopy(advMoves.getInitialMoves(lookupKey, symmetry), 0,
                            solutionMove, 1, numPartialMoves);
                    assert checkVaildMoves(board, solutionMove, numPartialMoves) : "Incorrect initial moves (group 3 symmetry)";
                } else {
                    System.arraycopy(advMoves.getInitialMoves(lookupKey, !symmetry), 0,
                            solutionMove, 1, numPartialMoves);
                    assert checkVaildMoves(board, solutionMove, numPartialMoves) : "Incorrect initial moves";
                }
                return new SmartRecord(steps, solutionMove);
            }
            return new SmartRecord(steps);
        } else if (refMap.containsKey(checkBoardSym)) {
        	boolean symmetry = SmartSolverProperties.isSymmetry();
        	int numPartialMoves = SmartSolverProperties.getNumPartialMoves();
        	
        	ReferenceMoves advMoves = refMap.get(checkBoardSym);
            if (lookupKey == 1) {
                lookupKey = 3;
            } else if (lookupKey == 3) {
                lookupKey = 1;
            }
            final byte steps = advMoves.getEstimate(lookupKey);

            if (inSearch && advMoves.hasInitialMoves(lookupKey)) {
            	Direction[] solutionMove = new Direction[steps + 1];
                System.arraycopy(advMoves.getInitialMoves(lookupKey, symmetry), 0,
                        solutionMove, 1, numPartialMoves);
                assert checkVaildMoves(board, solutionMove, numPartialMoves) : "Incorrect initial moves (group 0 or 2 symmetry)";
                return new SmartRecord(steps, solutionMove);
            }
            return new SmartRecord(steps);
        }
        return null;
    }

    // check all partial solution are the valid of the given baord
    private boolean checkVaildMoves(Board initial, Direction[] partialMoves, int numPartialMoves) {
        if (initial == null) {
            throw new IllegalArgumentException("Board is null");
        }
        Board board = new Board(initial.getTiles());
        for (int i = 1; i <= numPartialMoves; i++) {
            board = board.shift(partialMoves[i]);
            if (board == null) {
                return false;
            }
        }
        return true;
    }
    
    // calculate the advanced estimate from the stored boards, use manhattan distance only
    byte advancedEstimate(Board board, byte estimate, int refCutoff, Map<ReferenceBoard, ReferenceMoves> advMap) {
        SolverMD solverMD = new SolverMD(SolverProperties.isTagSearch());
        solverMD.messageSwitch(!SolverProperties.isOnSwitch());
        solverMD.timeoutSwitch(!SolverProperties.isOnSwitch());
               
        for (Entry<ReferenceBoard, ReferenceMoves> entry
                : advMap.entrySet()) {
            byte[] transTiles = entry.getKey().transformer(board.getTiles());
            byte[] transTilesSym = tiles2sym(transTiles);

            int transPriority = 0;
            int transPrioritySym = 0;
            int value;
            int base = 0;
            for (int row = 0; row < rowSize; row++) {
                for (int col = 0; col < rowSize; col++) {
                    value = transTiles[base + col];
                    if (value > 0) {
                        transPriority += Math.abs((value - 1) % rowSize - col);
                        transPriority += Math.abs((((value - 1)
                                - (value - 1) % rowSize) / rowSize) - row);
                    }

                    value = transTilesSym[base + col];
                    if (value > 0) {
                        transPrioritySym += Math.abs((value - 1) % rowSize - col);
                        transPrioritySym += Math.abs((((value - 1)
                                - (value - 1) % rowSize) / rowSize) - row);
                    }
                }
                base += rowSize;
            }

            transPriority = Math.max(transPriority, transPrioritySym);
            if (transPriority > refCutoff) {
                continue;
            }
            if (entry.getValue().getEstimate() - transPriority <= estimate) {
                continue;
            }

            Board temp = new Board(transTiles);
            solverMD.findOptimalPath(temp);
            estimate = (byte) (entry.getValue().getEstimate() - solverMD.moves());
        }
        return estimate;
    }
    
    // convert the given tiles to symmetry tiles
    private final byte[] tiles2sym(byte[] original) {
        byte[] tiles2sym = new byte[puzzleSize];
        for (int i = 0; i < puzzleSize; i++) {
            tiles2sym[symmetryPos[i]] = symmetryVal[original[i]];
        }
        return tiles2sym;
    }

}
