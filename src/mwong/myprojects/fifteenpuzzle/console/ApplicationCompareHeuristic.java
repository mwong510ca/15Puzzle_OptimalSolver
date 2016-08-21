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

import mwong.myprojects.fifteenpuzzle.solver.components.Board;
import mwong.myprojects.fifteenpuzzle.solver.components.PatternOptions;
import mwong.myprojects.fifteenpuzzle.solver.components.PuzzleDifficultyLevel;
import mwong.myprojects.fifteenpuzzle.solver.Solver;
import mwong.myprojects.fifteenpuzzle.solver.advanced.SmartSolverMD;
import mwong.myprojects.fifteenpuzzle.solver.advanced.SmartSolverPD;
import mwong.myprojects.fifteenpuzzle.solver.advanced.SmartSolverPDWD;
import mwong.myprojects.fifteenpuzzle.solver.advanced.SmartSolverWD;
import mwong.myprojects.fifteenpuzzle.solver.advanced.SmartSolverWDMD;
import mwong.myprojects.fifteenpuzzle.solver.advanced.ai.ReferenceAccumulator;

import mwong.myprojects.fifteenpuzzle.solver.standard.SolverMD;
import mwong.myprojects.fifteenpuzzle.solver.standard.SolverPD;
import mwong.myprojects.fifteenpuzzle.solver.standard.SolverPDWD;
import mwong.myprojects.fifteenpuzzle.solver.standard.SolverWD;
import mwong.myprojects.fifteenpuzzle.solver.standard.SolverWDMD;


import java.util.Scanner;

public class ApplicationCompareHeuristic extends AbstractApplication {
    private final ApplicationType applicationType;
    private final boolean tagLinearConflict;
    private final boolean tagAdvanced;
//    private SolverMD solverMd;
//    private SolverWD solverWd;
//    private SolverWDMD solverWdMd;
//    private SolverPDWD solverPdWd555;
//    private SolverPDWD solverPdWd663;
//    private SolverPD solverPd78;
    
    private SmartSolverMD solverMd;
    private SmartSolverWD solverWd;
    private SmartSolverWDMD solverWdMd;
    private SmartSolverPDWD solverPdWd555;
    private SmartSolverPDWD solverPdWd663;
    private SmartSolverPD solverPd78;
    private final ReferenceAccumulator refAccumulator;

    public ApplicationCompareHeuristic() {
    	super();
        applicationType = ApplicationType.CompareHeuristic;
        
        final boolean messageOff = !ApplicationProperties.isMessageOn();
        final boolean timeoutOff = !ApplicationProperties.isTimeoutOn();
        tagLinearConflict = ApplicationProperties.isTagLinearConflict();
        tagAdvanced = ApplicationProperties.isTagAdvanced();
        refAccumulator = new ReferenceAccumulator();
//        refAccumulator = null;
        
//        solverMd = new SolverMD();
        solverMd = new SmartSolverMD(refAccumulator);
        solverMd.messageSwitch(messageOff);

//        solverWd = new SolverWD();
        solverWd = new SmartSolverWD(refAccumulator);
        solverWd.messageSwitch(messageOff);

//        solverWdMd = new SolverWDMD();
        solverWdMd = new SmartSolverWDMD(refAccumulator);
        solverWdMd.messageSwitch(messageOff);

//        solverPdWd555 = new SolverPDWD(PatternOptions.Pattern_555);
        solverPdWd555 = new SmartSolverPDWD(PatternOptions.Pattern_555, refAccumulator);
        solverPdWd555.messageSwitch(messageOff);

//        solverPdWd663 = new SolverPDWD(PatternOptions.Pattern_663);
        solverPdWd663 = new SmartSolverPDWD(PatternOptions.Pattern_663, refAccumulator);
        solverPdWd663.messageSwitch(messageOff);

//        solverPd78 = new SolverPD(PatternOptions.Pattern_78);
        solverPd78 = new SmartSolverPD(PatternOptions.Pattern_78, refAccumulator);
        solverPd78.timeoutSwitch(timeoutOff);
        solverPd78.messageSwitch(messageOff);
    }

    //  It take a solver and a 15 puzzle board, display the the process time and number of
    //  nodes generated during the search, time out after 10 seconds.
    private void solvePuzzle(Solver solver, Board board) {
        printHeading(applicationType, solver);
        
        solver.advPrioritySwitch(!tagAdvanced);
        int heuristicStandard = solver.heuristicStandard(board);
        System.out.print("Standard\t" + heuristicStandard + "\t\t");
        solver.findOptimalPath(board);
        if (solver.isSearchTimeout()) {
            System.out.println("Timeout: " + solver.searchTime() + "s at depth "
                    + solver.searchTerminateAtDepth() + "\t" + solver.searchNodeCount());
        } else {
            System.out.printf("%-15s %-15s " + solver.searchNodeCount() + "\n",
            		solver.searchTime() + "s", solver.moves());
        }
        
        if (solver.advPrioritySwitch(tagAdvanced)) {
        	int heuristicAdvanced = solver.heuristicAdvanced(board);
            if (heuristicStandard == heuristicAdvanced) {
                System.out.println("Advanced\t" + "Same value");
            } else {
            	System.out.print("Advanced\t" + heuristicAdvanced + "\t\t");
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

    public void run() {
        scanner = new Scanner(System.in, "UTF-8");
        do {
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
                
                solvePuzzle(solverPd78, board);
                /*
                solvePuzzle(solverPdWd663, board);
                solvePuzzle(solverPdWd555, board);
                solvePuzzle(solverWdMd, board);
                solvePuzzle(solverWd, board);
                
                solverMd.linearConflictSwitch(tagLinearConflict);
                solvePuzzle(solverMd, board);
                solverMd.linearConflictSwitch(!tagLinearConflict);
                solvePuzzle(solverMd, board);
				*/
                // Notes: updateLastSearch is optional.
                //refAccumulator.updateLastSearch(solverPd78);
            } else {
                System.out.println("The board is unsolvable, try again!");
            }
            System.out.println();
        } while (true);
    }
}
