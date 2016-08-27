/****************************************************************************
 *  @author   Meisze Wong
 *            www.linkedin.com/pub/macy-wong/46/550/37b/
 *
 *  Compilation :  javac ApplicationCompareHeuristic.java
 *  Execution:     java ApplicationCompareHeuristic
 *  Dependencies : Board.java, Direction.java, Stopwatch.java,
 *                 PDPresetPatterns.java, SolverInterface.java
 *                 SolverMD.java, SolverWD.java, SolverWDMD.java,
 *                 SolverPD.java, SolverPDWD.java, AdvancedAccumulator.java
 *
 *  ApplicationCompareHeuristic is a console application to take 16 number
 *  of 15 puzzle.  It will go through each heuristic function, display
 *  the process time and number of nodes generated during the search.
 *  Each search will timeout in 10 seconds, except pattern database 78.
 *
 *  sample output: output_CompareHeuristic.txt
 *
 ****************************************************************************/

package mwong.myprojects.fifteenpuzzle.console;

import mwong.myprojects.fifteenpuzzle.solver.Solver;
import mwong.myprojects.fifteenpuzzle.solver.advanced.SmartSolverMd;
import mwong.myprojects.fifteenpuzzle.solver.advanced.SmartSolverPdb;
import mwong.myprojects.fifteenpuzzle.solver.advanced.SmartSolverPdbWd;
import mwong.myprojects.fifteenpuzzle.solver.advanced.SmartSolverWd;
import mwong.myprojects.fifteenpuzzle.solver.advanced.SmartSolverWdMd;
import mwong.myprojects.fifteenpuzzle.solver.advanced.ai.ReferenceAccumulator;
import mwong.myprojects.fifteenpuzzle.solver.components.Board;
import mwong.myprojects.fifteenpuzzle.solver.components.PatternOptions;
import mwong.myprojects.fifteenpuzzle.solver.components.PuzzleDifficultyLevel;

import java.util.Scanner;

public class ApplicationCompareHeuristic extends AbstractApplication {
    private final ApplicationType applicationType;
    private final boolean tagLinearConflict;
    private final boolean tagAdvanced;

    private SmartSolverMd solverMd;
    private SmartSolverWd solverWd;
    private SmartSolverWdMd solverWdMd;
    private SmartSolverPdbWd solverPdbWd555;
    private SmartSolverPdbWd solverPdbWd663;
    private SmartSolverPdb solverPdb78;
    private final ReferenceAccumulator refAccumulator;

    public ApplicationCompareHeuristic() {
        super();
        applicationType = ApplicationType.CompareHeuristic;

        final boolean messageOff = !ApplicationProperties.isMessageOn();
        final boolean timeoutOff = !ApplicationProperties.isTimeoutOn();
        tagLinearConflict = ApplicationProperties.isTagLinearConflict();
        tagAdvanced = ApplicationProperties.isTagAdvanced();
        refAccumulator = new ReferenceAccumulator();

        solverMd = new SmartSolverMd(refAccumulator);
        solverMd.messageSwitch(messageOff);

        solverWd = new SmartSolverWd(refAccumulator);
        solverWd.messageSwitch(messageOff);

        solverWdMd = new SmartSolverWdMd(refAccumulator);
        solverWdMd.messageSwitch(messageOff);

        solverPdbWd555 = new SmartSolverPdbWd(PatternOptions.Pattern_555, refAccumulator);
        solverPdbWd555.messageSwitch(messageOff);

        solverPdbWd663 = new SmartSolverPdbWd(PatternOptions.Pattern_663, refAccumulator);
        solverPdbWd663.messageSwitch(messageOff);

        solverPdb78 = new SmartSolverPdb(PatternOptions.Pattern_78, refAccumulator);
        solverPdb78.timeoutSwitch(timeoutOff);
        solverPdb78.messageSwitch(messageOff);
    }

    //  It take a solver and a 15 puzzle board, display the the process time and number of
    //  nodes generated during the search, time out after 10 seconds.
    private void solvePuzzle(Solver solver, Board board, boolean estimateOnly) {
        printHeading(applicationType, solver);

        solver.advPrioritySwitch(!tagAdvanced);
        int heuristicStandard = solver.heuristicStandard(board);

        System.out.print("Standard\t" + heuristicStandard + "\t\t");
        if (estimateOnly) {
        	System.out.println("Skip searching - will not solved in 10s.");
        } else { 
        	solver.findOptimalPath(board);
            if (solver.isSearchTimeout()) {
                System.out.println("Timeout: " + solver.searchTime() + "s at depth "
                        + solver.searchTerminateAtDepth() + "\t" + solver.searchNodeCount());
            } else {
                System.out.printf("%-15s %-15s " + solver.searchNodeCount() + "\n",
                        solver.searchTime() + "s", solver.moves());
            }
        } 
        
        if (solver.advPrioritySwitch(tagAdvanced)) {
            int heuristicAdvanced = solver.heuristicAdvanced(board);
            if (heuristicStandard == heuristicAdvanced) {
                System.out.println("Advanced\t" + "Same value");
            } else {
            	System.out.print("Advanced\t" + heuristicAdvanced + "\t\t");
            	if (estimateOnly) {
                	System.out.println("Skip searching - will not solved in 10s.");
                } else { 
                	solver.findOptimalPath(board);
                    if (solver.isSearchTimeout()) {
                        System.out.println("Timeout: " + solver.searchTime() + "s at depth "
                                + solver.searchTerminateAtDepth() + "\t" + solver.searchNodeCount());
                    } else {
                        System.out.printf("%-15s %-15s " + solver.searchNodeCount() + "\n",
                                solver.searchTime() + "s", solver.moves());
                    }
                }
            }
        }
    }

    public void run() {
        scanner = new Scanner(System.in, "UTF-8");
        while (true) {
            System.out.println("Enter 'Q' - quit the program");
            System.out.println("      'E' - Easy | 'M' - Moderate | 'H' - Hard | 'R' - Random");
            System.out.println("      or 16 numbers from 0 to 15 for the puzzle");

            Board board = null;
            while (true) {
                if (scanner.hasNextInt()) {
                    break;
                }
                char value = scanner.next().charAt(0);
                if (value == 'Q' || value == 'q') {
                    System.out.println("Goodbye!\n");
                    System.exit(0);
                }
                if (value == 'E' || value == 'e') {
                    board = new Board(PuzzleDifficultyLevel.EASY);
                    break;
                }
                if (value == 'M' || value == 'm') {
                    board = new Board(PuzzleDifficultyLevel.MODERATE);
                    break;
                }
                if (value == 'H' || value == 'h') {
                    board = new Board(PuzzleDifficultyLevel.HARD);
                    break;
                }
                if (value == 'R' || value == 'r') {
                    board = new Board();
                    break;
                }
                System.out.println("Please enter 'Q', 'E', 'M', 'H', 'R' or 16 numbers (0 - 15):");
            }

            if (board == null) {
                board = puzzleIn();
            }

            System.out.print("\n" + board);
            if (board.isSolvable()) {
                System.out.print("\t\tEstimate\tTime\t\tMinimum Moves\tNodes generated");

                boolean estimateOnly = false;
                solvePuzzle(solverPdb78, board, estimateOnly);
                
                solvePuzzle(solverPdbWd663, board, estimateOnly);
                if (solverPdbWd663.isSearchTimeout()) {
                	estimateOnly = true;
                }
                
                solvePuzzle(solverPdbWd555, board, estimateOnly);
                if (!estimateOnly && solverPdbWd555.isSearchTimeout()) {
                	estimateOnly = true;
                }    
                
                solvePuzzle(solverWdMd, board, estimateOnly);
                if (!estimateOnly && solverWdMd.isSearchTimeout()) {
                	estimateOnly = true;
                }    
                
                solvePuzzle(solverWd, board, estimateOnly);
                if (!estimateOnly && solverWd.isSearchTimeout()) {
                	estimateOnly = true;
                }
                
                solverMd.linearConflictSwitch(tagLinearConflict);
                solvePuzzle(solverMd, board, estimateOnly);
                if (!estimateOnly && solverMd.isSearchTimeout()) {
                	estimateOnly = true;
                }
                   
                solverMd.linearConflictSwitch(!tagLinearConflict);
                solvePuzzle(solverMd, board, estimateOnly);
                
                // Notes: updateLastSearch is optional.
                refAccumulator.updateLastSearch(solverPdb78);
            } else {
                System.out.println("The board is unsolvable, try again!");
            }
            System.out.println();
        }
    }
}
