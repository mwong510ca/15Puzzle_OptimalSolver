package mwong.myprojects.fifteenpuzzle.console;

import mwong.myprojects.fifteenpuzzle.solver.Solver;
import mwong.myprojects.fifteenpuzzle.solver.advanced.ai.ReferenceAccumulator;
import mwong.myprojects.fifteenpuzzle.solver.components.Board;
import mwong.myprojects.fifteenpuzzle.solver.components.PatternOptions;
import mwong.myprojects.fifteenpuzzle.solver.components.PuzzleDifficultyLevel;
import mwong.myprojects.fifteenpuzzle.solver.advanced.SmartSolverPdb;
import mwong.myprojects.fifteenpuzzle.solver.advanced.SmartSolverPdbBase;
import mwong.myprojects.fifteenpuzzle.solver.standard.SolverPdb;
import mwong.myprojects.fifteenpuzzle.solver.standard.SolverPdbEnh2;
import mwong.myprojects.fifteenpuzzle.solver.standard.SolverPdbEnh1;
import mwong.myprojects.fifteenpuzzle.solver.standard.SolverPdbBase;

import java.util.Scanner;

public class ApplicationCompareEnhancement extends AbstractApplication {
    private final ApplicationType applicationType;
    private final boolean tagAdvanced;
    private SolverPdbBase solverNoEnh;
    private SolverPdbEnh1 solverEnh1;
    private SolverPdbEnh2 solverEnh2;
    private SolverPdb solverStandard;
    private SmartSolverPdbBase solverAdvEst;
    private SmartSolverPdb solverAdvanced;
    private final ReferenceAccumulator refAccumulator;

    public ApplicationCompareEnhancement() {
        super();
        applicationType = ApplicationType.CompareEnhancement;
        final boolean messageOff = !ApplicationProperties.isMessageOn();
        final boolean timeoutOff = !ApplicationProperties.isTimeoutOn();
        tagAdvanced = ApplicationProperties.isTagAdvanced();
        refAccumulator = new ReferenceAccumulator();

        solverAdvanced = new SmartSolverPdb(PatternOptions.Pattern_78, refAccumulator);
        solverAdvanced.messageSwitch(messageOff);
        solverAdvanced.timeoutSwitch(timeoutOff);
        solverAdvanced.advPrioritySwitch(tagAdvanced);

        solverAdvEst = new SmartSolverPdbBase(solverAdvanced, refAccumulator);
        solverAdvEst.messageSwitch(messageOff);
        solverAdvEst.timeoutSwitch(timeoutOff);
        solverAdvEst.advPrioritySwitch(tagAdvanced);

        solverStandard = new SolverPdb(solverAdvanced);
        solverStandard.messageSwitch(messageOff);
        solverStandard.timeoutSwitch(timeoutOff);

        solverEnh2 = new SolverPdbEnh2(solverAdvanced);
        solverEnh2.messageSwitch(messageOff);
        solverEnh2.timeoutSwitch(timeoutOff);

        solverEnh1 = new SolverPdbEnh1(solverAdvanced);
        solverEnh1.messageSwitch(messageOff);
        solverEnh1.timeoutSwitch(timeoutOff);

        solverNoEnh = new SolverPdbBase(solverAdvanced);
        solverNoEnh.messageSwitch(messageOff);
        solverNoEnh.timeoutSwitch(timeoutOff);
    }

    @Override
    public void printHeading(ApplicationType type, Solver solver) {
        System.out.println("Compare 15 puzzle solver enhancement using "
                + solver.getHeuristicOptions().getDescription() + "\n");
    }

    //  It take a solver and a 15 puzzle board, display the the process time and number of
    //  nodes generated during the search, time out after 10 seconds.
    private void solvePuzzle(Solver solver, Board board) {
        solver.findOptimalPath(board);
        System.out.printf("%-15s %-20s\n", solver.searchTime() + "s", solver.searchNodeCount());
    }

    public void run() {
        printHeading(applicationType, solverAdvanced);
        
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
                int heuristicStandard = solverAdvanced.heuristicStandard(board);
                int heuristicAdvanced = solverAdvanced.heuristicAdvanced(board);
                System.out.print("Standard estimate : " + heuristicStandard + "\t\t");
                System.out.println("Advanced estimate : " + heuristicAdvanced);
                System.out.println("\t\t\t\tTime\t\tNodes");
                
                System.out.printf("%-32s", "No enhancement : ");
                solvePuzzle(solverNoEnh, board);
                System.out.printf("%-32s", "Add symmetry reduction : ");
                solvePuzzle(solverEnh1, board);
                System.out.printf("%-32s", "Add circular reduction : ");
                solvePuzzle(solverEnh2, board);
                System.out.printf("%-32s", "Add starting order detection : ");
                solvePuzzle(solverStandard, board);
                if (heuristicAdvanced > heuristicStandard) {
                    System.out.printf("%-32s", "Advanced version : ");
                    solvePuzzle(solverAdvEst, board);
                    if (solverAdvanced.hasPartialSolution(board)) {
                        System.out.printf("%-32s", "Use preset partial solution :");
                        solvePuzzle(solverAdvanced, board);
                    } else {
                        System.out.printf("%-32s", "No preset partial solution.");
                    }
                }
            } else {
                System.out.println("The board is unsolvable, try again!");
            }
            System.out.println("\t\t\tActual number of solution move : " + solverStandard.moves() + "\n");
            
            
        } while (true);
    }
}
