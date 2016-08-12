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

package mwong.myprojects.fifteenpuzzle.application;

import mwong.myprojects.fifteenpuzzle.solver.SolverInterface;
import mwong.myprojects.fifteenpuzzle.solver.SolverInterface.ApplicationType;
import mwong.myprojects.fifteenpuzzle.solver.SolverInterface.HeuristicType;
import mwong.myprojects.fifteenpuzzle.solver.SolverMD;
import mwong.myprojects.fifteenpuzzle.solver.SolverPD;
import mwong.myprojects.fifteenpuzzle.solver.SolverPDWD;
import mwong.myprojects.fifteenpuzzle.solver.SolverWD;
import mwong.myprojects.fifteenpuzzle.solver.SolverWDMD;
import mwong.myprojects.fifteenpuzzle.solver.components.AdvancedAccumulator;
import mwong.myprojects.fifteenpuzzle.solver.components.Board;
import mwong.myprojects.fifteenpuzzle.solver.components.Direction;
import mwong.myprojects.fifteenpuzzle.solver.components.PDPresetPatterns;

import java.util.Scanner;

public class ApplicationSolver {
    private static final int puzzleSize = Board.getSize();
    private static final int timeoutLimit = SolverInterface.DEFAULT_TIMEOUT_LIMIT;
    private static final ApplicationType applicationType = ApplicationType.Solver;
    private static AdvancedAccumulator advAccumulator = new AdvancedAccumulator();

    private static Scanner scanner;
    private static SolverInterface solver = new SolverPDWD(PDPresetPatterns.Pattern_663);
    private static HeuristicType inUseHeuristic = HeuristicType.PD663;
    private static boolean flagAdvPriority = SolverInterface.SWITCH_OFF;
    private static boolean flagAdvancedUpdate = false;

    // display the solver options and change it with the user's choice
    private static void menuChangeSolver() {
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
                    if (inUseHeuristic != HeuristicType.MD) {
                        solver = new SolverMD();
                        inUseHeuristic = HeuristicType.MD;
                    } else {
                        System.out.println("No Change, currently in use.");
                    }
                    pending = false;
                    break;
                case 2:
                    if (inUseHeuristic != HeuristicType.MDLC) {
                        solver = new SolverMD(SolverInterface.SWITCH_ON);
                        inUseHeuristic = HeuristicType.MDLC;
                    } else {
                        System.out.println("No Change, currently in use.");
                    }
                    pending = false;
                    break;
                case 3:
                    if (inUseHeuristic != HeuristicType.WD) {
                        solver = new SolverWD();
                        inUseHeuristic = HeuristicType.WD;
                    } else {
                        System.out.println("No Change, currently in use.");
                    }
                    pending = false;
                    break;
                case 4:
                    if (inUseHeuristic != HeuristicType.WDMD) {
                        solver = new SolverWDMD();
                        inUseHeuristic = HeuristicType.WDMD;
                    } else {
                        System.out.println("No Change, currently in use.");
                    }
                    pending = false;
                    break;
                case 5:
                    if (inUseHeuristic != HeuristicType.PD555) {
                        solver = new SolverPDWD(PDPresetPatterns.Pattern_555);
                        inUseHeuristic = HeuristicType.PD555;
                    } else {
                        System.out.println("No Change, currently in use.");
                    }
                    pending = false;
                    break;
                case 6:
                    if (inUseHeuristic != HeuristicType.PD663) {
                        solver = new SolverPDWD(PDPresetPatterns.Pattern_663);
                        inUseHeuristic = HeuristicType.PD663;
                    } else {
                        System.out.println("No Change, currently in use.");
                    }
                    pending = false;
                    break;
                case 7:
                    if (inUseHeuristic != HeuristicType.PD78) {
                        solver = new SolverPD(PDPresetPatterns.Pattern_78);
                        inUseHeuristic = HeuristicType.PD78;
                    } else {
                        System.out.println("No Change, currently in use.");
                    }
                    pending = false;
                    break;
                default:
                    System.out.println("Enter '1 - 5' for heuristic function, '0' no change");
            }
        } while (pending);

        solver.advPrioritySwitch(flagAdvPriority);
        solver.printDescription();
        solver.setAdvancedAccumulator(advAccumulator);
        flagAdvancedUpdate = false;
        if (inUseHeuristic == HeuristicType.PD78) {
            solver.timeoutSwitch(SolverInterface.SWITCH_OFF);
            flagAdvancedUpdate = true;
        }
        menuSub();
    }

    // display a list of options
    private static void menuMain() {
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
    private static void menuSub() {
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
    private static void menuSubSolution(Board initial, int steps, Direction [] solutionMoves) {
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
                solutionList(steps, solutionMoves);
                menuMain();
                break;
            }
            if (value == 'D' || value == 'd') {
                solutionDetail(initial, steps, solutionMoves);
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

    /**
     * Print the minimum number of moves to the goal state.
     */
    private static void solutionSummary(Board initial, int steps, Direction [] solutionMoves) {
        System.out.println("Minimum number of moves = " + steps + "\n");
    }

    /**
     * Print the list of direction of moves to the goal state.
     */
    private static void solutionList(int steps, Direction [] solutionMove) {
        for (int i = 1; i <= steps; i++) {
            System.out.print(i + " : " + solutionMove[i] + " ");
            if (i % 10 == 0 && steps > i) {
                System.out.println();
            }
        }
        System.out.println("\n");
    }

    /**
     * Print all boards of moves to the goal state.
     */
    private static void solutionDetail(Board board, int steps, Direction [] solutionMoves) {
        int count = 0;
        Direction dir = Direction.NONE;
        do {
            System.out.print("Step : " + (count));
            if (count > 0) {
                System.out.print("\t" + dir);
            }
            System.out.println();
            System.out.println(board);
            count++;
            if (count <= steps) {
                dir = solutionMoves[count];
                board = board.shift(dir);
            }
        } while (count <= steps);
    }

    /**
     *  A console application to solve the 15 puzzle with optimal solution.  The user
     *  to choose 7 type of heuristic function, and display the solution when search
     *  completed.  Each search will timeout in 10 seconds.
     *
     *  @param args standard argument main function
     */
    public static void main(String[] args) {
        scanner = new Scanner(System.in, "UTF-8");
        solver.printDescription();
        solver.setAdvancedAccumulator(advAccumulator);
        solver.messageSwitch(SolverInterface.SWITCH_ON);
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
            solver.printHeading(applicationType);
            System.out.println(initial);
            if (!initial.isSolvable()) {
                System.out.println("The board is unsolvable, try again!\n");
                menuMain();
                continue;
            }

            int steps = 0;
            Direction [] solutionMoves = null;
            solver.findOptimalPath(initial);
            if (solver.isSearchTimeout()) {
                System.out.println("Search terminated after " + timeoutLimit + "s.\n");
                menuMain();
            } else {
                steps = solver.moves();
                solutionMoves = solver.solution();
                solutionSummary(initial, steps, solutionMoves);

                // Notes: updateLastSearch is optional.
                if (flagAdvancedUpdate) {
                    advAccumulator.updateLastSearch(solver);
                }

                if (solver.moves() > 0) {
                    menuSubSolution(initial, steps, solutionMoves);
                } else {
                    menuMain();
                }
            }
        } while (true);
    }
}
