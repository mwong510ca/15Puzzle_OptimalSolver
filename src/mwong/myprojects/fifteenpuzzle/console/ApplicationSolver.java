/****************************************************************************
 *  @author   Meisze Wong
 *            www.linkedin.com/pub/macy-wong/46/550/37b/
 *
 *  Compilation :  javac ApplicationSolver.java
 *  Execution:     java ApplicationSolver
 *  Dependencies : Board.java, Direction.java, Stopwatch.java,
 *                 PDPresetPatterns.java, SolverInterface.java
 *                 SolverMD.java, SolverWD.java, SolverWDMD,
 *                 SolverPD.java, SolverPDWD.java
 *                 AdvancedAccumulator.java
 *
 *  ApplicationSolver is a console application to solve the 15 puzzle
 *  with optimal solution.  It allowed the user to choose 7 type of heuristic
 *  function, and display the solution when search completed.  Each search will
 *  timeout in 10 seconds except Pattern Database 78.  Pattern Database guarantee
 *  to find the optimal solution, a very rare case will takes about 1 minute at most.
 *
 *  sample output: output_15PuzzleSolver.txt
 *
 ****************************************************************************/

package mwong.myprojects.fifteenpuzzle.console;

import mwong.myprojects.fifteenpuzzle.console.ApplicationType;
import mwong.myprojects.fifteenpuzzle.solver.HeuristicOptions;
import mwong.myprojects.fifteenpuzzle.solver.Solver;
import mwong.myprojects.fifteenpuzzle.solver.SolverConstants;
import mwong.myprojects.fifteenpuzzle.solver.SolverProperties;
import mwong.myprojects.fifteenpuzzle.solver.advanced.SmartSolverMD;
import mwong.myprojects.fifteenpuzzle.solver.advanced.SmartSolverPD;
import mwong.myprojects.fifteenpuzzle.solver.advanced.SmartSolverPDWD;
import mwong.myprojects.fifteenpuzzle.solver.advanced.SmartSolverWD;
import mwong.myprojects.fifteenpuzzle.solver.advanced.SmartSolverWDMD;
import mwong.myprojects.fifteenpuzzle.solver.advanced.ai.ReferenceAccumulator;
import mwong.myprojects.fifteenpuzzle.solver.components.Board;
import mwong.myprojects.fifteenpuzzle.solver.components.PatternOptions;

import java.util.Scanner;

public class ApplicationSolver extends AbstractApplication {
	private Solver solver;
	private final Scanner scanner;
	private final ReferenceAccumulator refAccumulator;
	private final ApplicationType applicationType;
	private final boolean onSwitch;
	private final boolean offSwitch;
	private final int puzzleSize;
	
	private int timeoutLimit;
	private HeuristicOptions inUseHeuristic;
	private boolean flagAdvPriority;
	private boolean flagAdvancedUpdate;
	
	
	public ApplicationSolver() {
		scanner = new Scanner(System.in, "UTF-8");
		refAccumulator = new ReferenceAccumulator(); 
		solver = new SmartSolverPDWD(PatternOptions.Pattern_663, refAccumulator);
		inUseHeuristic = solver.getHeuristicOptions();
		applicationType = ApplicationType.Solver;
		onSwitch = SolverConstants.isOnSwitch();
		offSwitch = !onSwitch;
		puzzleSize = SolverConstants.getPuzzleSize();
		timeoutLimit = SolverProperties.getDefaultTimeoutLimit();
		flagAdvPriority = !SolverConstants.isTagAdvanced();
		solver.advPrioritySwitch(flagAdvPriority);
		flagAdvancedUpdate = false;
	}
	/*
    private static final int puzzleSize = Board.getSize();
    private static final int timeoutLimit = 10;

    private static Scanner scanner;
    private static SolverInterface solver = new SolverPDWD(PDPresetPatterns.Pattern_663);
    private static HeuristicType inUseHeuristic = solver.getHeuristicType();
    private static boolean flagAdvPriority = SWITCH_OFF;
    private static boolean flagAdvancedUpdate = false;
	*/
    // display the solver options and change it with the user's choice
    private void menuChangeSolver() {
        System.out.println("Choose your heuristic functions:");
        System.out.println("Enter '1' for Manhattan Distance");
        System.out.println("      '2' for Manhattan Distance with Linear Conflict");
        System.out.println("      '3' for Walking Distance");
        System.out.println("      '4' for Walking Distance + Manhattan Distance with "
                + "Linear Conflict");
        System.out.println("      '5' to 555 pattern + Walking Distance");
        System.out.println("      '6' to 663 pattern + Walking Distance");
        System.out.println("      '7' to 78 pattern (never timeout until solution found)");
        System.out.println("      '0' no change");
        boolean pending = true;
        
        do {
            if (!scanner.hasNextInt()) {
                scanner.next();
                continue;
            }
            int option = scanner.nextInt();
            switch (option) {
                case 0:
                    pending = false;
                    break;
                case 1:
                    if (inUseHeuristic != HeuristicOptions.MD) {
                        solver = new SmartSolverMD(refAccumulator);
                    } else {
                        System.out.println("No Change, currently in use.");
                    }
                    pending = false;
                    break;
                case 2:
                    if (inUseHeuristic != HeuristicOptions.MDLC) {
                        solver = new SmartSolverMD(ApplicationProperties.isTagLinearConflict(), refAccumulator);
                    } else {
                        System.out.println("No Change, currently in use.");
                    }
                    pending = false;
                    break;
                case 3:
                    if (inUseHeuristic != HeuristicOptions.WD) {
                        solver = new SmartSolverWD(refAccumulator);
                    } else {
                        System.out.println("No Change, currently in use.");
                    }
                    pending = false;
                    break;
                case 4:
                    if (inUseHeuristic != HeuristicOptions.WDMD) {
                        solver = new SmartSolverWDMD(refAccumulator);
                    } else {
                        System.out.println("No Change, currently in use.");
                    }
                    pending = false;
                    break;
                case 5:
                    if (inUseHeuristic != HeuristicOptions.PD555) {
                        solver = new SmartSolverPDWD(PatternOptions.Pattern_555, refAccumulator);
                    } else {
                        System.out.println("No Change, currently in use.");
                    }
                    pending = false;
                    break;
                case 6:
                    if (inUseHeuristic != HeuristicOptions.PD663) {
                        solver = new SmartSolverPDWD(PatternOptions.Pattern_663, refAccumulator);
                    } else {
                        System.out.println("No Change, currently in use.");
                    }
                    pending = false;
                    break;
                case 7:
                    if (inUseHeuristic != HeuristicOptions.PD78) {
                        solver = new SmartSolverPD(PatternOptions.Pattern_78, refAccumulator);
                     } else {
                        System.out.println("No Change, currently in use.");
                    }
                    pending = false;
                    break;
                default:
                    System.out.println("Enter '1 - 5' for heuristic function, '0' no change");
            }
        } while (pending);

        inUseHeuristic = solver.getHeuristicOptions();
        solver.advPrioritySwitch(flagAdvPriority);
        solver.printDescription();
        flagAdvancedUpdate = false;
        if (inUseHeuristic == HeuristicOptions.PD78) {
            solver.timeoutSwitch(offSwitch);
            flagAdvancedUpdate = true;
        }
        menuSub();
    }

    // display a list of options
    private void menuMain() {
        System.out.println("Enter 'Q' - quit the program");
        System.out.println("      'C' - change your choice of heuristic function");
        if (flagAdvPriority) {
            System.out.println("      'H' - change initial heuristic estimate from Advanced "
                    + "to Original");
        } else {
            System.out.println("      'H' - change initial heuristic estimate from Original "
                    + "to Advanced");
        }
        System.out.println("      16 numbers from 0 to 15 for the puzzle");
        do {
            if (scanner.hasNextInt()) {
                break;
            }
            char value = scanner.next().charAt(0);
            if (value == 'C' || value == 'c') {
                menuChangeSolver();
                break;
            }
            if (value == 'Q' || value == 'q') {
                System.out.println("Goodbye!\n");
                System.exit(0);
            }
            if (value == 'H' || value == 'h') {
                flagAdvPriority = !flagAdvPriority;
                solver.advPrioritySwitch(flagAdvPriority);
                System.out.println("Enter 16 numbers from 0 to 15 for the puzzle");
                break;
            }
            System.out.println("Please enter 'Q', 'C', 'H' or 16 numbers (0 - 15):");
        } while (true);
    }

    // display a list of options after user change the solver
    private void menuSub() {
        if (flagAdvPriority) {
            System.out.println("Enter 'H' - change initial heuristic estimate from Advanced "
                    + "to Original");
        } else {
            System.out.println("Enter 'H' - change initial heuristic estimate from Original "
                    + "to Advanced");
        }
        System.out.println("      16 numbers from 0 to 15 for the puzzle");
        do {
            if (scanner.hasNextInt()) {
                break;
            }
            char value = scanner.next().charAt(0);
            if (value == 'H' || value == 'h') {
                flagAdvPriority = !flagAdvPriority;
                solver.advPrioritySwitch(flagAdvPriority);
                System.out.println("Enter 16 numbers from 0 to 15 for the puzzle");
                break;
            }
            System.out.println("Please enter 'H' or 16 numbers (0 - 15):");
        } while (true);
    }

    // display a list of options after the puzzle has solved
    private void menuSubSolution(Board initial) {
        System.out.println("Enter 'M' - print a list of moves");
        System.out.println("      'D' - display the board of each moves");
        System.out.println("      'Q' - for quit the program");
        System.out.println("      'C' - for change your choice of heuristic function");
        if (flagAdvPriority) {
            System.out.println("      'H' - change initial heuristic estimate from Advanced "
                    + "to Original");
        } else {
            System.out.println("      'H' - change initial heuristic estimate from Original "
                    + "to Advanced");
        }

        System.out.println("      16 numbers from 0 to 15 for the puzzle");

        do {
            if (scanner.hasNextInt()) {
                break;
            }
            char value = scanner.next().charAt(0);
            if (value == 'M' || value == 'm') {
                solutionList(solver);
                menuMain();
                break;
            }
            if (value == 'D' || value == 'd') {
                solutionDetail(initial, solver);
                menuMain();
                break;
            }
            if (value == 'C' || value == 'c') {
                menuChangeSolver();
                break;
            }
            if (value == 'Q' || value == 'q') {
                System.out.println("Goodbye!\n");
                System.exit(0);
            }
            if (value == 'H' || value == 'h') {
                flagAdvPriority = !flagAdvPriority;
                solver.advPrioritySwitch(flagAdvPriority);
                System.out.println("Enter 16 numbers from 0 to 15 for the puzzle");
                break;
            }
            System.out.println("Please enter 'M', 'D', 'Q', 'C', 'H' or 16 numbers"
                    + " (0 - 15):");
        } while (true);
    }
    
    public void run() {
        solver.printDescription();
        solver.messageSwitch(onSwitch);
        solver.advPrioritySwitch(flagAdvPriority);

        menuMain();

        do {
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

            Board initial = new Board(blocks);
            printHeading(applicationType, solver);
            System.out.println(initial);
            if (!initial.isSolvable()) {
                System.out.println("The board is unsolvable, try again!\n");
                menuMain();
                continue;
            }

            solver.findOptimalPath(initial);
            if (solver.isSearchTimeout()) {
                System.out.println("Search terminated after " + timeoutLimit + "s.\n");
                menuMain();
            } else {
                solutionSummary(solver);

                // Notes: updateLastSearch is optional.
                if (flagAdvancedUpdate) {
                    refAccumulator.updateLastSearch(solver);
                }

                if (solver.moves() > 0) {
                    menuSubSolution(initial);
                } else {
                    menuMain();
                }
            }
        } while (true);
    }
}
