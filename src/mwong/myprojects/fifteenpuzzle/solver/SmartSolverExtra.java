package mwong.myprojects.fifteenpuzzle.solver;

import mwong.myprojects.fifteenpuzzle.solver.ai.ReferenceBoard;
import mwong.myprojects.fifteenpuzzle.solver.ai.ReferenceMoves;
import mwong.myprojects.fifteenpuzzle.solver.components.Board;
import mwong.myprojects.fifteenpuzzle.solver.components.Direction;
import mwong.myprojects.fifteenpuzzle.solver.components.PuzzleConstants;
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
 * <p>Dependencies : Board.java, Direction.java, PuzzleConstants.java, ReferenceBoard.java,
 *                   ReferenceMoves.java, SolverMD.java, Stopwatch.java
 *
 * @author Meisze Wong
 *         www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public class SmartSolverExtra extends SolverMd {
    private boolean symmetry;
    private int numPartialMoves;

    public SmartSolverExtra() {
        symmetry = SolverConstants.isSymmetry();
        numPartialMoves = SolverConstants.getNumPartialMoves();
    }

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

    /**
     * Returns the AdvancedRecord object if the given board is one of the reference board.
     * It carries the reference estimate and partial solutions for searching if exists.
     * Otherwise return null.
     *
     * @param board the given board object
     * @param inSearch the boolean value represent the usage for search or review.
     * @param refMap the given reference collection in HashMap.
     * @return AdvancedRecord object if the given board is one of the reference board.
     */
    public final AdvancedRecord advancedContains(Board board, boolean inSearch,
            Map<ReferenceBoard, ReferenceMoves> refMap) {
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

    /**
     * Returns the boolean value if the given board is one of the reference board with
     * partial solutions stored.
     *
     * @param board the given board object
     * @param refMap the given reference collection in HashMap.
     * @return boolean value if the given board is a reference board with partial solutions
     *         stored in reference collection.
     */
    public final boolean hasPartialSolution(Board board, Map<ReferenceBoard,
            ReferenceMoves> refMap) {
        if (refMap == null || refMap.size() == 0) {
            return false;
        }

        byte lookupKey = SolverConstants.getReferenceLookup(board.getZero1d());
        int group = SolverConstants.getReferenceGroup(board.getZero1d());

        ReferenceBoard checkBoard = new ReferenceBoard(board);
        ReferenceBoard checkBoardSym = null;

        if (group == 0 || group == 2) {
            checkBoardSym = new ReferenceBoard(new Board(board.getTilesSym()));
        }

        if (refMap.containsKey(checkBoard)) {
            ReferenceMoves advMoves = refMap.get(checkBoard);
            if (advMoves.hasInitialMoves(lookupKey)) {
                return true;
            }
            return false;
        } else if (refMap.containsKey(checkBoardSym)) {
            ReferenceMoves advMoves = refMap.get(checkBoardSym);
            if (lookupKey == 1) {
                lookupKey = 3;
            } else if (lookupKey == 3) {
                lookupKey = 1;
            }
            if (advMoves.hasInitialMoves(lookupKey)) {
                return true;
            }
            return false;
        }
        return false;
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

    /**
     * Returns the best estimate of the given reference collection.
     *
     * @param board the given board object
     * @param estimate the current estimate of the given puzzle.
     * @param refCutoff the given cutoff range from goal state or to reference board.
     * @param refMap the given reference collection in HashMap.
     * @return AdvancedRecord object if the given board is one of the reference board.
     */
    byte advancedEstimate(Board board, byte estimate, int refCutoff,
            Map<ReferenceBoard, ReferenceMoves> refMap) {
        final int rowSize = SolverConstants.getRowSize();

        for (Entry<ReferenceBoard, ReferenceMoves> entry
                : refMap.entrySet()) {
            byte[] transTiles = entry.getKey().transformer(board.getTiles());
            byte[] transTilesSym = PuzzleConstants.tiles2sym(transTiles);

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
