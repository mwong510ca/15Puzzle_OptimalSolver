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

package mwong.myprojects.fifteenpuzzle.application;

import mwong.myprojects.fifteenpuzzle.solver.SolverInterface;
import mwong.myprojects.fifteenpuzzle.solver.SolverInterface.ApplicationType;
import mwong.myprojects.fifteenpuzzle.solver.SolverMD;
import mwong.myprojects.fifteenpuzzle.solver.SolverPD;
import mwong.myprojects.fifteenpuzzle.solver.SolverPDWD;
import mwong.myprojects.fifteenpuzzle.solver.SolverWD;
import mwong.myprojects.fifteenpuzzle.solver.SolverWDMD;
import mwong.myprojects.fifteenpuzzle.solver.components.AdvancedAccumulator;
import mwong.myprojects.fifteenpuzzle.solver.components.Board;
import mwong.myprojects.fifteenpuzzle.solver.components.PDPresetPatterns;

import java.util.Scanner;

public class ApplicationCompareHeuristic {
    private static final int puzzleSize = Board.getSize();
    private static final boolean tagAdvanced = SolverInterface.SWITCH_ON;
    private static final ApplicationType applicationType = ApplicationType.CompareHeuristic;
    private static Scanner scanner;

    //  It take a solver and a 15 puzzle board, display the the process time and number of
    //  nodes generated during the search, time out after 10 seconds.
    private static void solvePuzzle(SolverInterface solver, Board board) {
        solver.printHeading(applicationType);
        solver.advPrioritySwitch(!tagAdvanced);

        solver.findOptimalPath(board);
        System.out.print("Original\t" + solver.heuristicOrg(board) + "\t\t");
        if (solver.isSearchTimeout()) {
            System.out.println("Timeout: " + solver.searchTime() + "s at depth "
                    + solver.searchTerminateAtDepth() + "\t" + solver.searchNodeCount());
        } else {
            System.out.println(solver.searchTime() + "\t\t" + solver.moves() + "\t\t"
                    + solver.searchNodeCount());
        }

        solver.advPrioritySwitch(tagAdvanced);
        if (solver.heuristicOrg(board) == solver.heuristicAdv(board)) {
            System.out.println("Advanced\t" + "Same value");
        } else {
            solver.findOptimalPath(board);
            System.out.print("Advanced\t" + solver.heuristicAdv(board) + "\t\t");
            if (solver.isSearchTimeout()) {
                System.out.println("Timeout: " + solver.searchTime() + "s at depth "
                        + solver.searchTerminateAtDepth() + "\t" + solver.searchNodeCount());
            } else {
                System.out.println(solver.searchTime() + "\t\t" + solver.moves() + "\t\t"
                        + solver.searchNodeCount());
            }
        }
    }

    /**
     *  A console application to compare 5 types of 15 puzzle heuristic function.  The user
     *  can choose a random board or enter 16 numbers of 15 puzzle.  It will go through
     *  each heuristic function and display the results.
     *
     *  @param args standard argument main function
     */
    public static void main(String[] args) {
        AdvancedAccumulator advAccumulator = new AdvancedAccumulator();

        scanner = new Scanner(System.in, "UTF-8");

        SolverInterface solverMD = new SolverMD();
        solverMD.messageSwitch(SolverInterface.SWITCH_OFF);
        solverMD.setAdvancedAccumulator(advAccumulator);

        SolverInterface solverWD = new SolverWD();
        solverWD.messageSwitch(SolverInterface.SWITCH_OFF);
        solverWD.setAdvancedAccumulator(advAccumulator);

        SolverInterface solverWDMD = new SolverWDMD();
        solverWDMD.messageSwitch(SolverInterface.SWITCH_OFF);
        solverWDMD.setAdvancedAccumulator(advAccumulator);

        SolverInterface solverPD555 = new SolverPDWD(PDPresetPatterns.Pattern_555);
        solverPD555.messageSwitch(SolverInterface.SWITCH_OFF);
        solverPD555.setAdvancedAccumulator(advAccumulator);

        SolverInterface solverPD663 = new SolverPDWD(PDPresetPatterns.Pattern_663);
        solverPD663.messageSwitch(SolverInterface.SWITCH_OFF);
        solverPD663.setAdvancedAccumulator(advAccumulator);

        SolverInterface solverPD78 = new SolverPD(PDPresetPatterns.Pattern_78);
        solverPD78.timeoutSwitch(SolverInterface.SWITCH_OFF);
        solverPD78.messageSwitch(SolverInterface.SWITCH_OFF);
        solverPD78.setAdvancedAccumulator(advAccumulator);

        do {
            System.out.println("Enter 'Q' - quit the program");
            System.out.println("      'E' - generate a easy board");
            System.out.println("      'M' - generate a moderate board");
            System.out.println("      'H' - generate a hard board");
            System.out.println("      'R' - generate a random board");
            System.out.println("      16 numbers from 0 to 15 for the puzzle");

            Board initial = null;
            do {
                if (scanner.hasNextInt()) {
                    break;
                }
                char value = scanner.next().charAt(0);
                if (value == 'Q' || value == 'q') {
                    System.out.println("Goodbye!\n");
                    System.exit(0);
                }
                if (value == 'E' || value == 'e') {
                    initial = new Board(Board.Level.Easy);
                    break;
                }
                if (value == 'M' || value == 'm') {
                    initial = new Board(Board.Level.Moderate);
                    break;
                }
                if (value == 'H' || value == 'h') {
                    initial = new Board(Board.Level.Hard);
                    break;
                }
                if (value == 'R' || value == 'r') {
                    initial = new Board();
                    break;
                }
                System.out.println("Please enter 'Q', 'E', 'M', 'H', 'R' or 16 numbers (0 - 15):");
            } while (true);

            if (initial == null) {
                byte[] blocks = new byte[puzzleSize];
                boolean [] used = new boolean[puzzleSize];
                int count = 0;

                while (count < puzzleSize) {
                    if (!scanner.hasNextInt()) {
                        scanner.next();
                    } else {
                        int value = scanner.nextInt();
                        if (value < 0 || value >= puzzleSize) {
                            System.out.println("Invalid number " + value + ", try again.");
                        } else if (used[value]) {
                            System.out.println(value + " already entered, try again.");
                        } else {
                            blocks[count++] = (byte) value;
                            used[value] = true;
                        }
                    }
                }
                initial = new Board(blocks);
            }

            System.out.print("\n" + initial);

            if (initial.isSolvable()) {
                System.out.print("\t\tEstimate\tTime\t\tMinimum Moves\tNodes generated");
                solvePuzzle(solverPD78, initial);
                solvePuzzle(solverPD663, initial);
                solvePuzzle(solverPD555, initial);
                solvePuzzle(solverWDMD, initial);
                solvePuzzle(solverWD, initial);
                ((SolverMD) solverMD).linearConflictSwitch(SolverMD.TAG_LINEAR_CONFLICT);
                solvePuzzle(solverMD, initial);
                ((SolverMD) solverMD).linearConflictSwitch(!SolverMD.TAG_LINEAR_CONFLICT);
                solvePuzzle(solverMD, initial);

                // Notes: updateLastSearch is optional.
                advAccumulator.updateLastSearch(solverPD78);
            } else {
                System.out.println("The board is unsolvable, try again!");
            }
            System.out.println();
        } while (true);
    }
}
