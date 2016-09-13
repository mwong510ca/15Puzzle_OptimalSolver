package mwong.myprojects.fifteenpuzzle.console;

import mwong.myprojects.fifteenpuzzle.solver.HeuristicOptions;
import mwong.myprojects.fifteenpuzzle.solver.SmartSolver;
import mwong.myprojects.fifteenpuzzle.solver.SolverConstants;
import mwong.myprojects.fifteenpuzzle.solver.advanced.SmartSolverMd;
import mwong.myprojects.fifteenpuzzle.solver.advanced.SmartSolverPdb;
import mwong.myprojects.fifteenpuzzle.solver.advanced.SmartSolverPdbWd;
import mwong.myprojects.fifteenpuzzle.solver.advanced.SmartSolverWd;
import mwong.myprojects.fifteenpuzzle.solver.advanced.SmartSolverWdMd;
import mwong.myprojects.fifteenpuzzle.solver.components.Board;
import mwong.myprojects.fifteenpuzzle.solver.components.PatternOptions;
import mwong.myprojects.fifteenpuzzle.solver.components.PuzzleDifficultyLevel;

import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Scanner;

/**
 * SolverHeuristicStats is the console application extends AbstractApplication.  It takes
 * a number of trial T and run the solver T time with random board.  User may choose the type of
 * heuristic function, type of random board, and change the timeout limit from 1 to 60 seconds.
 * Only pattern database 7-8 has an option to turn timeout feature off. It will display the total
 * process time, number of timeout boards, and average time of boards with solution.
 *
 * <p>Dependencies : AbstractApplication.java, Board.java, HeuristicOptions.java,
 *                   PatternOptions.java, PuzzleDifficultyLevel.java, SmartSolver.java
 *                   SmartSolverMd.java, SmartSolverPdb.java, SmartSolverPdbWd.java,
 *                   SmartSolverWd.java, SmartSolverWdMd.java, SolverConstants.java
 *
 * @author Meisze Wong
 *         www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public class SolverHeuristicStats extends AbstractApplication {
    private SmartSolver solver;
    private HeuristicOptions inUseHeuristic;

    /**
     * Initial SolverHeuristicStats object.
     */
    public SolverHeuristicStats() {
        super();
        solver = new SmartSolverPdbWd(defaultPattern, refConnection);
        inUseHeuristic = solver.getHeuristicOptions();
        setSolverVersion();
    }

    private void setSolverVersion() {
        solver.setReferenceConnection(refConnection);
        printConnectionType();
    }

    // display the solver options and change it with the user's choice
    private void menuChangeSolver() {
        menuOption('s');
        boolean pending = true;
        while (pending) {
            if (!scanner.hasNextInt()) {
                scanner.next();
            }
            int option = scanner.nextInt();

            switch (option) {
                case 0:
                    pending = false;
                    break;
                case 1:
                    if (inUseHeuristic != HeuristicOptions.MD) {
                        solver = new SmartSolverMd(refConnection);
                    } else {
                        System.out.println("No Change, currently in use.");
                    }
                    pending = false;
                    break;
                case 2:
                    if (inUseHeuristic != HeuristicOptions.MDLC) {
                        solver = new SmartSolverMd(SolverConstants.isTagLinearConflict(),
                                refConnection);
                    } else {
                        System.out.println("No Change, currently in use.");
                    }
                    pending = false;
                    break;
                case 3:
                    if (inUseHeuristic != HeuristicOptions.WD) {
                        solver = new SmartSolverWd(refConnection);
                    } else {
                        System.out.println("No Change, currently in use.");
                    }
                    pending = false;
                    break;
                case 4:
                    if (inUseHeuristic != HeuristicOptions.WDMD) {
                        solver = new SmartSolverWdMd(refConnection);
                    } else {
                        System.out.println("No Change, currently in use.");
                    }
                    pending = false;
                    break;
                case 5:
                    if (inUseHeuristic != HeuristicOptions.PD555) {
                        solver = new SmartSolverPdbWd(PatternOptions.Pattern_555, refConnection);
                    } else {
                        System.out.println("No Change, currently in use.");
                    }
                    pending = false;
                    break;
                case 6:
                    if (inUseHeuristic != HeuristicOptions.PD663) {
                        solver = new SmartSolverPdbWd(PatternOptions.Pattern_663, refConnection);
                    } else {
                        System.out.println("No Change, currently in use.");
                    }
                    pending = false;
                    break;
                case 7:
                    if (inUseHeuristic != HeuristicOptions.PD78) {
                        solver = new SmartSolverPdb(PatternOptions.Pattern_78, refConnection);
                    } else {
                        System.out.println("No Change, currently in use.");
                    }
                    pending = false;
                    break;
                default:
                    System.out.println("Enter '1 - 7' for heuristic function, '0' no change");
            }
        }

        inUseHeuristic = solver.getHeuristicOptions();
        solver.messageSwitch(messageOff);
        solver.versionSwitch(flagAdvVersion);
        solver.setTimeoutLimit(timeoutLimit);
        solver.setReferenceConnection(refConnection);
        solver.printDescription();
        boolean optionT = false;
        if (solver.isFlagTimeout()) {
            menuOption('t');
            optionT = true;
        }
        boolean optionO = false;
        if (inUseHeuristic == HeuristicOptions.PD78) {
            menuOption(solver);
            optionO = true;
        }
        menuSub(true, optionT, optionO);
    }

    // display a list of main menu options
    private void menuMain() {
        menuOption('q');
        menuOption('c');
        menuOption(flagAdvVersion);
        boolean optionT = false;
        if (solver.isFlagTimeout()) {
            menuOption('t');
            optionT = true;
        }
        boolean optionO = false;
        if (solver.isFlagTimeout() && inUseHeuristic == HeuristicOptions.PD78) {
            menuOption(solver);
            optionO = true;
        }
        System.out.println("      a positive integer of number of trials");

        while (true) {
            if (scanner.hasNextInt()) {
                break;
            }
            char choice = scanner.next().charAt(0);
            if (choice == 'q') {
                System.out.println("Goodbye!\n");
                System.exit(0);
            }
            switch (choice) {
                case 'C': case 'c':
                    menuChangeSolver();
                    break;
                case 'V': case 'v':
                    flipVersion(solver);
                    menuSub(false, optionT, optionO);
                    break;
                case 'T': case 't':
                    if (optionT) {
                        changeTimeout(solver, 1, 60);
                    }
                    menuSub(true, false, optionO);
                    break;
                case 'O': case 'o':
                    if (optionO && inUseHeuristic == HeuristicOptions.PD78) {
                        solver.timeoutSwitch(!solver.isFlagTimeout());
                    }
                    if (solver.isFlagTimeout()) {
                        menuSub(true, true, false);
                    } else {
                        menuSub(true, false, false);
                    }
                    break;
                default:
                    if (inUseHeuristic == HeuristicOptions.PD78) {
                        System.out.println("Please enter 'Q', 'C', 'H', 'T', 'O'"
                                + " or positive integer:");
                    } else {
                        System.out.println("Please enter 'Q', 'C', 'H', 'T' or positive integer:");
                    }
            }
        }
    }

    // display a list of options after user change the solver
    private void menuSub(boolean optionV, boolean optionT, boolean optionO) {
        menuOption('q');
        if (optionV) {
            menuOption(flagAdvVersion);
        }
        if (optionT) {
            menuOption('t');
        }
        if (optionO) {
            menuOption(solver);
        }
        System.out.println("      a positive integer of number of trials");

        while (true) {
            if (scanner.hasNextInt()) {
                break;
            }

            char choice = scanner.next().charAt(0);
            if (choice == 'q') {
                System.out.println("Goodbye!\n");
                System.exit(0);
            }

            switch (choice) {
                case 'V': case 'v':
                    if (optionV) {
                        flipVersion(solver);
                    }
                    menuSub(false, optionT, optionO);
                    break;
                case 'T': case 't':
                    if (optionT) {
                        changeTimeout(solver, 1, 60);
                    }
                    menuSub(optionV, false, optionO);
                    break;
                case 'O': case 'o':
                    if (optionO && inUseHeuristic == HeuristicOptions.PD78) {
                        solver.timeoutSwitch(!solver.isFlagTimeout());
                        optionT = !optionT;
                    }
                    menuSub(optionV, optionT, false);
                    break;
                default:
                    System.out.print("Please enter 'Q', ");
                    if (optionV) {
                        System.out.print(" 'V', ");
                    }
                    if (optionT) {
                        System.out.print(" 'T', ");
                    }
                    if (optionO) {
                        System.out.print(" 'O', ");
                    }
                    System.out.println("or a positive integer:");
            }
        }
    }

    /**
     * Start the application.
     */
    public void run() {
        scanner = new Scanner(System.in, "UTF-8");
        solver.messageSwitch(messageOff);
        solver.versionSwitch(flagAdvVersion);
        solver.setTimeoutLimit(timeoutLimit);
        solver.printDescription();
        menuMain();

        while (true) {
            int lineCount = 20;
            int divisor = 1;
            HashSet<Integer> remainders = new HashSet<Integer>();
            PuzzleDifficultyLevel boardLevel = PuzzleDifficultyLevel.RANDOM;

            int trails = 0;
            while (trails <= 0) {
                if (!scanner.hasNextInt()) {
                    scanner.next();
                } else {
                    trails = scanner.nextInt();
                }
            }

            if (inUseHeuristic == HeuristicOptions.MD
                    || inUseHeuristic == HeuristicOptions.MDLC
                    || inUseHeuristic == HeuristicOptions.WD
                    || inUseHeuristic == HeuristicOptions.WDMD) {
                System.out.println("Choose type of board: 'R'andom, 'E'asy, 'M'oderate");
            } else if (inUseHeuristic == HeuristicOptions.PD555
                    || inUseHeuristic == HeuristicOptions.PD663) {
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
                boardLevel = PuzzleDifficultyLevel.EASY;
                System.out.print(trails + " trials of easy random board with ");
                if (inUseHeuristic == HeuristicOptions.MD
                        || inUseHeuristic == HeuristicOptions.MDLC
                        || inUseHeuristic == HeuristicOptions.WD) {
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
                boardLevel = PuzzleDifficultyLevel.MODERATE;
                System.out.print(trails + " trials of moderate random board with ");
            } else if (choice == 'H' || choice == 'h') {
                if (inUseHeuristic == HeuristicOptions.MD
                        || inUseHeuristic == HeuristicOptions.MDLC
                        || inUseHeuristic == HeuristicOptions.WD
                        || inUseHeuristic == HeuristicOptions.WDMD) {
                    System.out.print(trails + " trials of any random board with ");
                } else if (timeoutLimit > 10
                        && (inUseHeuristic == HeuristicOptions.PD555
                        || inUseHeuristic == HeuristicOptions.PD663)) {
                    System.out.print(trails + " trials of any random board with ");
                } else {
                    boardLevel = PuzzleDifficultyLevel.HARD;
                    System.out.print(trails + " trials of hard random board with ");
                    if (inUseHeuristic == HeuristicOptions.PD78) {
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

            if (flagAdvVersion) {
                if (!testConnection()) {
                    setSolverVersion();
                }
            }

            System.out.print("\n" + solver.getHeuristicOptions().getDescription());
            if (flagAdvVersion) {
                System.out.print(" (Advanced version) ");
            } else {
                System.out.print(" (Standard version) ");
            }
            if (solver.isFlagTimeout()) {
                System.out.println("will timeout at " + solver.getSearchTimeoutLimit() + "s:");
            } else {
                System.out.println("will run until solution found:");
            }

            double totalSearchTime = 0.0;
            int timeoutCounter = 0;
            for (int i = 1; i <= trails; i++) {
                Board board = new Board(boardLevel);
                if (i % lineCount == 1) {
                    System.out.printf("\n%1.2fs : ", totalSearchTime);
                }

                try {
					solver.findOptimalPath(board);
				} catch (RemoteException ex) {
					System.out.println("Counnection lost: " + ex);
	                loadReferenceConnection();
	                setSolverVersion();
	                i--;
	                continue;
				}
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

            try {
                refConnection.updatePending(solver);
            } catch (RemoteException ex) {
                System.out.println("Counnection lost: " + ex);
                loadReferenceConnection();
                setSolverVersion();
            }
            menuMain();
        }
    }
}
