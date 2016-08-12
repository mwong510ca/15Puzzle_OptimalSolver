/****************************************************************************
 *  @author   Meisze Wong
 *            www.linkedin.com/pub/macy-wong/46/550/37b/
 *
 *  Compilation :  javac ApplicationTrailRun.java
 *  Execution:     java ApplicationTrailRun
 *  Dependencies : Board.java, Stopwatch.java, PDPresetPatterns.java,
 *                 SolverAbstract.java, SolverMD.java, SolverWD.java, SolverWDMD,
 *                 SolverPD.java, SolverPDWD.java, AdvancedAccumulator.java
 *
 *  ApplicationTrailRun is a console application take a number of trial T
 *  and run the solver T time with random board.  User may choose the type
 *  of heuristic function, type of random board, and change the timeout limit
 *  from 1 to 60 seconds.  It will display the total process time, number of
 *  timeout boards, and average time of boards with solution.
 *
 *  sample output: output_TrailRun.txt
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
import mwong.myprojects.fifteenpuzzle.solver.components.PDPresetPatterns;

import java.util.HashSet;
import java.util.Scanner;

public class ApplicationSolverStats {
    private static final boolean messageOff = SolverInterface.SWITCH_OFF;
    private static final ApplicationType applicationType = ApplicationType.Stats;
    private static AdvancedAccumulator advAccumulator = new AdvancedAccumulator();

    private static Scanner scanner;
    private static SolverInterface solver = new SolverPDWD(PDPresetPatterns.Pattern_663);
    private static HeuristicType inUseHeuristic = HeuristicType.PD663;
    private static int timeoutLimit = SolverInterface.DEFAULT_TIMEOUT_LIMIT;
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
                        solver = new SolverMD(SolverMD.TAG_LINEAR_CONFLICT);
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

        solver.messageSwitch(messageOff);
        solver.advPrioritySwitch(flagAdvPriority);
        solver.setAdvancedAccumulator(advAccumulator);
        solver.setTimeoutLimit(timeoutLimit);
        solver.printDescription();
        flagAdvancedUpdate = false;
        if (inUseHeuristic == HeuristicType.PD78) {
            solver.timeoutSwitch(SolverInterface.SWITCH_OFF);
            flagAdvancedUpdate = true;
        }
        menuSub(true, true);
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
        if (inUseHeuristic != HeuristicType.PD78) {
            System.out.println("      'T' - change timeout limit");
        }
        System.out.println("      a positive integer of number of trials");

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
                menuSub(false, true);
                break;
            }
            if (inUseHeuristic != HeuristicType.PD78 && (value == 'T' || value == 't')) {
                int limit = 0;
                do {
                    System.out.println("Enter timeout limit in seconds, maximum limit 60s:");
                    if (!scanner.hasNextInt()) {
                        scanner.next();
                    } else {
                        limit = scanner.nextInt();
                    }
                } while (limit <= 0 || limit > 60);
                timeoutLimit = limit;
                solver.setTimeoutLimit(timeoutLimit);
                menuSub(true, false);
                break;
            }
            if (inUseHeuristic != HeuristicType.PD78) {
                System.out.println("Please enter 'Q', 'C', 'H', 'T' or a positive integer:");
            } else {
                System.out.println("Please enter 'Q', 'C', 'H' or a positive integer:");
            }
        } while (true);
    }

    // display a list of options after user change the solver
    private static void menuSub(boolean optionH, boolean optionT) {
        if (inUseHeuristic == HeuristicType.PD78) {
            optionT = false;
        }
        System.out.print("Enter ");
        if (optionH) {
            if (flagAdvPriority) {
                System.out.println("      'H' - change initial heuristic estimate from Advanced "
                        + "to Original");
            } else {
                System.out.println("      'H' - change initial heuristic estimate from Original "
                        + "to Advanced");
            }
            System.out.print("      ");
        }
        if (optionT) {
            System.out.println("'T' - change timeout limit");
            System.out.print("      ");
        }
        System.out.println("a positive integer of number of trials");
        do {
            if (scanner.hasNextInt()) {
                break;
            }
            char value = scanner.next().charAt(0);
            if (optionH && (value == 'H' || value == 'h')) {
                flagAdvPriority = !flagAdvPriority;
                if (optionT) {
                    menuSub(false, true);
                } else {
                    optionH = false;
                    System.out.println("Enter a positive integer of number of trials");
                }
                break;
            } else if (optionT && (value == 'T' || value == 't')) {
                int limit = 0;
                do {
                    System.out.println("Enter timeout limit in seconds, maximum limit 60s:");
                    if (!scanner.hasNextInt()) {
                        scanner.next();
                    } else {
                        limit = scanner.nextInt();
                    }
                } while (limit <= 0 || limit > 60);
                timeoutLimit = limit;
                solver.setTimeoutLimit(timeoutLimit);
                if (optionH) {
                    menuSub(true, false);
                } else {
                    optionT = false;
                    System.out.println("Enter a positive integer of number of trials");
                }
                break;
            } else {
                System.out.print("Please enter ");
                if (optionH) {
                    System.out.print("'H', ");
                }
                if (optionT) {
                    System.out.print("'T' ");
                }
                System.out.println("a positive integer:");
            }
        } while (true);
    }

    /**
     *  A console application take a number of trial T and run the solver T time
     *  with random board.  User may choose the type of heuristic function,
     *  type of random board, and change the timeout limit from 1 to 60 seconds.
     *  It will display the total process time, number of timeout boards, and
     *  average time of boards with solution.
     *
     *  @param args standard argument main function
     */
    public static void main(String[] args) {
        scanner = new Scanner(System.in, "UTF-8");
        solver.messageSwitch(messageOff);
        solver.advPrioritySwitch(flagAdvPriority);
        solver.setAdvancedAccumulator(advAccumulator);
        solver.setTimeoutLimit(timeoutLimit);
        solver.printDescription();
        menuMain();

        do {
            int lineCount = 20;
            int divisor = 1;
            HashSet<Integer> remainders = new HashSet<Integer>();
            Board.Level boardLevel = Board.Level.Random;

            int trails = 0;
            do {
                if (!scanner.hasNextInt()) {
                    scanner.next();
                } else {
                    trails = scanner.nextInt();
                }
            } while (trails <= 0);

            if (inUseHeuristic == HeuristicType.MD
                    || inUseHeuristic == HeuristicType.MDLC
                    || inUseHeuristic == HeuristicType.WD
                    || inUseHeuristic == HeuristicType.WDMD) {
                System.out.println("Choose type of board: 'R'andom, 'E'asy, 'M'oderate");
            } else if (inUseHeuristic == HeuristicType.PD555
                    || inUseHeuristic == HeuristicType.PD663) {
                if (timeoutLimit <= 10) {
                    System.out.println("Choose type of board: 'R'andom, 'E'asy, 'M'oderate, "
                            + "'H'ard");
                } else {
                    System.out.println("Choose type of board: 'R'andom, 'E'asy, 'M'oderate");
                }
                lineCount = 100;
                divisor = 10;
                remainders.add(3);
                remainders.add(6);
                remainders.add(8);
            } else {
                System.out.println("Choose type of board: 'R'andom, 'E'asy, 'M'oderate, 'H'ard");
                lineCount = 1000;
                divisor = 100;
                remainders.add(26);
                remainders.add(51);
                remainders.add(76);
            }
            System.out.println("       Invalid entry will use random board");

            char choice = scanner.next().charAt(0);
            System.out.println();
            if (choice == 'E' || choice == 'e') {
                boardLevel = Board.Level.Easy;
                System.out.print(trails + " trials of easy random board with ");
                if (inUseHeuristic == HeuristicType.MD
                        || inUseHeuristic == HeuristicType.MDLC
                        || inUseHeuristic == HeuristicType.WD) {
                	lineCount = 50000;
                    divisor = 5000;
                    remainders = new HashSet<Integer>();
                    remainders.add(1500);
                    remainders.add(2500);
                    remainders.add(3500);
                } else {
                	lineCount = 300000;
                    divisor = 10000;
                    remainders = new HashSet<Integer>();
                    remainders.add(2500);
                    remainders.add(5000);
                    remainders.add(7500);
                }
            } else if (choice == 'M' || choice == 'm') {
                boardLevel = Board.Level.Moderate;
                System.out.print(trails + " trials of moderate random board with ");
            } else if (choice == 'H' || choice == 'h') {
                if (inUseHeuristic == HeuristicType.MD
                        || inUseHeuristic == HeuristicType.MDLC
                        || inUseHeuristic == HeuristicType.WD
                        || inUseHeuristic == HeuristicType.WDMD) {
                    System.out.print(trails + " trials of any random board with ");
                } else if (timeoutLimit > 10
                        && (inUseHeuristic == HeuristicType.PD555
                        || inUseHeuristic == HeuristicType.PD663)) {
                    System.out.print(trails + " trials of any random board with ");
                } else {
                    boardLevel = Board.Level.Hard;
                    System.out.print(trails + " trials of hard random board with ");
                    if (inUseHeuristic == HeuristicType.PD78) {
                        lineCount = 50;
                        divisor = 5;
                        remainders = new HashSet<Integer>();
                        remainders.add(2);
                        remainders.add(3);
                        remainders.add(4);
                    } else {
                        lineCount = 20;
                        divisor = 1;
                    }
                }
            } else {
                System.out.print(trails + " trials of any random board with ");
            }

            solver.printHeading(applicationType);
            double totalSearchTime = 0.0;
            int timeoutCounter = 0;
            for (int i = 1; i <= trails; i++) {
                Board board = new Board(boardLevel);
                if (i % lineCount == 1) {
                    //System.out.print("\n" + totalSearchTime + "s :");
                    System.out.printf("\n%1.2fs : ", totalSearchTime);
                }

                solver.findOptimalPath(board);
                totalSearchTime += solver.searchTime();
                if (solver.isSearchTimeout()) {
                    timeoutCounter++;
                }

                if (divisor == 1) {
                    System.out.print(" " + i);
                } else {
                    if (i % divisor == 1) {
                        System.out.print(" " + i + " ");
                    } else if (remainders.contains(i % divisor)) {
                        System.out.print(".");
                    }
                }
            }

            System.out.printf("\n" + trails + " trails completed - %1.2fs \n", totalSearchTime);
            if (timeoutCounter > 0) {
                System.out.println(timeoutCounter + " out of " + trails
                        + " boards are timeout after " + timeoutLimit + "s,");
                if (timeoutCounter < trails) {
                    double runtime = totalSearchTime - timeoutCounter * timeoutLimit;
                    System.out.printf("Average time per completed board: %1.6fs \n", 
                            runtime / trails);
                }
            } else {
                System.out.printf("Average time per board:  %1.6fs \n", totalSearchTime / trails);
            }
            System.out.println();

            if (flagAdvancedUpdate) {
                advAccumulator.updatePending(solver);
            }

            menuMain();
        } while (true);
    }
}
