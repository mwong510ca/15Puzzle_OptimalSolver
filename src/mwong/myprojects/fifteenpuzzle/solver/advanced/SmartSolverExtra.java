package mwong.myprojects.fifteenpuzzle.solver.advanced;

import mwong.myprojects.fifteenpuzzle.solver.HeuristicOptions;
import mwong.myprojects.fifteenpuzzle.solver.SolverConstants;
import mwong.myprojects.fifteenpuzzle.solver.advanced.ai.ReferenceAccumulator;
import mwong.myprojects.fifteenpuzzle.solver.advanced.ai.ReferenceBoard;
import mwong.myprojects.fifteenpuzzle.solver.advanced.ai.ReferenceMoves;
import mwong.myprojects.fifteenpuzzle.solver.components.Board;
import mwong.myprojects.fifteenpuzzle.solver.components.Direction;
import mwong.myprojects.fifteenpuzzle.solver.components.PuzzleProperties;
import mwong.myprojects.fifteenpuzzle.solver.standard.SolverMd;
import mwong.myprojects.fifteenpuzzle.utilities.Stopwatch;

import java.util.Map;
import java.util.Map.Entry;

/**
 * SmartSolverExtra has the add on functions for advanced version.  It return the
 * reference moves and solutions if the puzzle has been stored as a reference board.
 * It use Manhattan distance to calculate the advanced estimate from the collection
 * of reference boards.
 *
 * <p>Dependencies : Board.java, Direction.java, HeuristicOptions.java, PuzzleProperties.java,
 *                   ReferenceAccumulator.java ReferenceBoard.java, ReferenceMoves.java,
 *                   SolverConstants.java, SolverMD.java, Stopwatch.java
 *
 * @author   Meisze Wong
 *           www.linkedin.com/pub/macy-wong/46/550/37b/
 */
class SmartSolverExtra extends SolverMd {
    /**
     * Print solver description.
     */
    public void printDescription(boolean inUseAdvancedPriority, HeuristicOptions inUseHeuristic) {
        System.out.println("15 puzzle solver using " + inUseHeuristic.getDescription());
        if (inUseAdvancedPriority) {
            System.out.println("Advanced version - initial estimate use the goal state and "
                    + "stored reference boards.");
        } else {
            System.out.println("Standard version - initial estimate use the goal state only.");
        }
    }

    // quick check if the given board is one of the reference board.  If so,
    // use the reference estimate.  If search in progress, also update partial
    // solutions if exists.
    final AdvancedRecord advancedContains(Board board, boolean inSearch,
            ReferenceAccumulator refAccumulator) {
        Map<ReferenceBoard, ReferenceMoves> refMap = refAccumulator.getActiveMap();
        if (refMap == null || refMap.size() == 0) {
            return null;
        }

        byte lookupKey = SolverConstants.getReferenceLookup(board.getZero1d());
        int group = SolverConstants.getReferenceGroup(board.getZero1d());

        ReferenceBoard checkBoard = new ReferenceBoard(board);
        ReferenceBoard checkBoardSym = null;

        if (group == 0 || group == 2) {
            checkBoardSym = new ReferenceBoard(new Board(board.getTilesSym()));
        }

        if (refMap.containsKey(checkBoard)) {
            boolean symmetry = SolverConstants.isSymmetry();
            int numPartialMoves = SolverConstants.getNumPartialMoves();

            ReferenceMoves advMoves = refMap.get(checkBoard);
            final byte steps = advMoves.getEstimate(lookupKey);

            if (inSearch && advMoves.hasInitialMoves(lookupKey)) {
                Direction[] solutionMove = new Direction[steps + 1];
                solutionMove[0] = Direction.NONE;
                if (group == 3) {
                    System.arraycopy(advMoves.getInitialMoves(lookupKey, symmetry), 0,
                            solutionMove, 1, numPartialMoves);
                    assert checkVaildMoves(board, solutionMove, numPartialMoves) :
                        "Incorrect initial moves (group 3 symmetry)";
                } else {
                    System.arraycopy(advMoves.getInitialMoves(lookupKey, !symmetry), 0,
                            solutionMove, 1, numPartialMoves);
                    assert checkVaildMoves(board, solutionMove, numPartialMoves) :
                        "Incorrect initial moves";
                }
                return new AdvancedRecord(steps, solutionMove);
            }
            return new AdvancedRecord(steps);
        } else if (refMap.containsKey(checkBoardSym)) {
            boolean symmetry = SolverConstants.isSymmetry();
            int numPartialMoves = SolverConstants.getNumPartialMoves();

            ReferenceMoves advMoves = refMap.get(checkBoardSym);
            if (lookupKey == 1) {
                lookupKey = 3;
            } else if (lookupKey == 3) {
                lookupKey = 1;
            }
            final byte steps = advMoves.getEstimate(lookupKey);

            if (inSearch && advMoves.hasInitialMoves(lookupKey)) {
                Direction[] solutionMove = new Direction[steps + 1];
                solutionMove[0] = Direction.NONE;
                System.arraycopy(advMoves.getInitialMoves(lookupKey, symmetry), 0,
                        solutionMove, 1, numPartialMoves);
                assert checkVaildMoves(board, solutionMove, numPartialMoves) :
                    "Incorrect initial moves (group 0 or 2 symmetry)";
                return new AdvancedRecord(steps, solutionMove);
            }
            return new AdvancedRecord(steps);
        }
        return null;
    }

    // assertion tool : check all partial solution are the valid of the given board
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

    // calculate the advanced estimate from the stored boards, use Manhattan distance only
    byte advancedEstimate(Board board, byte estimate, int refCutoff,
            Map<ReferenceBoard, ReferenceMoves> advMap) {
        final int rowSize = SolverConstants.getRowSize();

        for (Entry<ReferenceBoard, ReferenceMoves> entry
                : advMap.entrySet()) {
            byte[] transTiles = entry.getKey().transformer(board.getTiles());
            byte[] transTilesSym = PuzzleProperties.tiles2sym(transTiles);

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
            stopwatch = new Stopwatch();
            if (advancedDistance(temp, transPriority,
                    entry.getValue().getEstimate() - estimate)) {
                estimate = (byte) (entry.getValue().getEstimate() - steps);
            }
        }
        return estimate;
    }

    // returns the boolean value represent the given board is solve in the given range.
    private boolean advancedDistance(Board board, int lowerLimit, int upperLimit) {
        clearHistory();
        heuristic(board);
        setLastDepthSummary(board);
        int initLimit = lowerLimit;
        while (lowerLimit <= upperLimit) {
            dfsStartingOrder(zeroX, zeroY, lowerLimit, initLimit);
            if (solved) {
                return true;
            }
            lowerLimit += 2;
        }
        return false;
    }
}
