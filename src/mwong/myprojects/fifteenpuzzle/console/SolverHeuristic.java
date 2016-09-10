package mwong.myprojects.fifteenpuzzle.console;

import mwong.myprojects.fifteenpuzzle.solver.HeuristicOptions;
import mwong.myprojects.fifteenpuzzle.solver.SmartSolver;
import mwong.myprojects.fifteenpuzzle.solver.advanced.SmartSolverMd;
import mwong.myprojects.fifteenpuzzle.solver.advanced.SmartSolverPdb;
import mwong.myprojects.fifteenpuzzle.solver.advanced.SmartSolverPdbWd;
import mwong.myprojects.fifteenpuzzle.solver.advanced.SmartSolverWd;
import mwong.myprojects.fifteenpuzzle.solver.advanced.SmartSolverWdMd;
import mwong.myprojects.fifteenpuzzle.solver.components.Board;
import mwong.myprojects.fifteenpuzzle.solver.components.PatternOptions;

import java.rmi.RemoteException;

/**
 * SolverHeuristic is the console application extends AbstractApplication.  User can select the
 * choice of heuristic function and version.  It takes a 16 numbers or choice of random board.
 * It display the process time and number of nodes generated at each depth.  It will timeout
 * after timeout setting (in resources/config.properties or default is 10 seconds), except pattern
 * database 78.
 *
 * <p>Dependencies : AbstractApplication.java, Board.java, PatternOptions.java,
 *                   HeuristicOptions.java, SmartSolver.java, SmartSolverMd.java,
 *                   SmartSolverPdb.java, SmartSolverPdbWd.java, SmartSolverWd.java,
 *                   SmartSolverWdMd.java
 *
 * @author Meisze Wong
 *         www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public class SolverHeuristic extends AbstractApplication {
    private SmartSolver solver;
    private HeuristicOptions inUseHeuristic;

    /**
     * Initial SolverHeuristic object.
     */
    public SolverHeuristic() {
        super();
        solver = new SmartSolverPdbWd(defaultPattern, refAccumulator);
        inUseHeuristic = solver.getHeuristicOptions();
        solver.versionSwitch(flagAdvVersion);
    }

    // display the solver options and change it with the user's choice
    private Board menuChangeSolver() {
        menuOption('s');
        boolean pending = true;
        while (pending) {
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
                        solver = new SmartSolverMd(refAccumulator);
                    } else {
                        System.out.println("No Change, currently in use.");
                    }
                    pending = false;
                    break;
                case 2:
                    if (inUseHeuristic != HeuristicOptions.MDLC) {
                        solver = new SmartSolverMd(tagLinearConflict, refAccumulator);
                    } else {
                        System.out.println("No Change, currently in use.");
                    }
                    pending = false;
                    break;
                case 3:
                    if (inUseHeuristic != HeuristicOptions.WD) {
                        solver = new SmartSolverWd(refAccumulator);
                    } else {
                        System.out.println("No Change, currently in use.");
                    }
                    pending = false;
                    break;
                case 4:
                    if (inUseHeuristic != HeuristicOptions.WDMD) {
                        solver = new SmartSolverWdMd(refAccumulator);
                    } else {
                        System.out.println("No Change, currently in use.");
                    }
                    pending = false;
                    break;
                case 5:
                    if (inUseHeuristic != HeuristicOptions.PD555) {
                        solver = new SmartSolverPdbWd(PatternOptions.Pattern_555, refAccumulator);
                    } else {
                        System.out.println("No Change, currently in use.");
                    }
                    pending = false;
                    break;
                case 6:
                    if (inUseHeuristic != HeuristicOptions.PD663) {
                        solver = new SmartSolverPdbWd(PatternOptions.Pattern_663, refAccumulator);
                    } else {
                        System.out.println("No Change, currently in use.");
                    }
                    pending = false;
                    break;
                case 7:
                    if (inUseHeuristic != HeuristicOptions.PD78) {
                        solver = new SmartSolverPdb(PatternOptions.Pattern_78, refAccumulator);
                    } else {
                        System.out.println("No Change, currently in use.");
                    }
                    pending = false;
                    break;
                default:
                    System.out.println("Enter '1 - 5' for heuristic function, '0' no change");
            }
        }

        inUseHeuristic = solver.getHeuristicOptions();
        solver.versionSwitch(flagAdvVersion);
        solver.printDescription();
        if (inUseHeuristic == HeuristicOptions.PD78) {
            solver.timeoutSwitch(timeoutOff);
        }
        return menuSub();
    }

    // display a list of main menu options
    private Board menuMain() {
        menuOption('q');
        menuOption('c');
        menuOption(flagAdvVersion);
        menuOption('b');

        while (true) {
            if (scanner.hasNextInt()) {
                return keyInBoard();
            }

            char choice = scanner.next().charAt(0);
            if (choice == 'q') {
                System.out.println("Goodbye!\n");
                System.exit(0);
            }

            Board board = action(choice, null);
            if (board != null) {
                return board;
            }
            System.out.println("Please enter 'Q', 'C', 'V', 'E', 'M', 'H', 'R'"
                    + " or 16 numbers (0 - 15):");
        }
    }

    // display a menu to create a board object
    private Board menuCreateBoard() {
        menuOption('q');
        menuOption('b');

        while (true) {
            if (scanner.hasNextInt()) {
                return keyInBoard();
            }

            char choice = scanner.next().charAt(0);
            Board board = createBoard(choice);
            if (board != null) {
                return board;
            }
            System.out.println("Please enter 'Q', 'E', 'M', 'H', 'R' or 16 numbers (0 - 15):");
        }
    }

    // display a list of options after user changed the solver
    private Board menuSub() {
        menuOption('q');
        menuOption(flagAdvVersion);
        menuOption('b');

        while (true) {
            if (scanner.hasNextInt()) {
                return keyInBoard();
            }

            char choice = scanner.next().charAt(0);
            if (choice == 'q') {
                System.out.println("Goodbye!\n");
                System.exit(0);
            }

            Board board = action(choice, null);
            if (board != null) {
                return board;
            }
            System.out.println("Please enter 'Q', 'V', 'E', 'M', 'H', 'R' or 16 numbers (0 - 15):");
        }
    }

    // display a list of options after the puzzle has solved
    private Board menuSubSolution(Board initial) {
        menuOption('q');
        menuOption('m');
        menuOption('c');
        menuOption(flagAdvVersion);
        menuOption('b');

        while (true) {
            if (scanner.hasNextInt()) {
                return keyInBoard();
            }

            char choice = scanner.next().charAt(0);
            if (choice == 'q') {
                System.out.println("Goodbye!\n");
                System.exit(0);
            }

            Board board = action(choice, initial);
            if (board != null) {
                return board;
            }
            System.out.println("Please enter 'L', 'D', 'Q', 'C', 'V', 'E', 'M', 'H', 'R'"
                    + " or 16 numbers"
                    + " (0 - 15):");
        }
    }

    // create a board object after the user pick an option from the menu
    private Board action(char choice, Board initial) {
        switch (choice) {
            case 'L': case 'l':
                solutionList(solver);
                return menuMain();
            case 'D': case'd':
                solutionDetail(initial, solver);
                return menuMain();
            case 'C': case 'c':
                return menuChangeSolver();
            case 'V': case 'v':
                flipVersion(solver);
                return menuCreateBoard();
            case 'E':  case 'e': case 'M': case 'm': case 'H': case 'h':case 'R': case 'r':
                return createBoard(choice);
            default: return null;
        }
    }

    /**
     * Start the application.
     */
    public void run() {
        solver.printDescription();
        solver.messageSwitch(messageOn);
        solver.versionSwitch(flagAdvVersion);

        Board initial = menuMain();
        while (true) {
            System.out.print(solver.getHeuristicOptions().getDescription());
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

            System.out.println(initial);
            if (!initial.isSolvable()) {
                System.out.println("The board is unsolvable, try again!\n");
                initial = menuMain();
                continue;
            }

            solver.findOptimalPath(initial);
            if (solver.isSearchTimeout()) {
                System.out.println("Search terminated after " + timeoutLimit + "s.\n");
                initial = menuMain();
            } else {
                solutionSummary(solver);
                // Notes: updateLastSearch is optional.
                try {
                    if (solver.getClass().getSimpleName().equals("SmartSolverPdb")
                            && solver.getHeuristicOptions() == HeuristicOptions.PD78
                            && ((SmartSolverPdb) solver).isAddedReference()) {
                        refAccumulator.updateLastSearch(solver);
                    }
                } catch (RemoteException ex) {
                    // TODO Auto-generated catch block
                    ex.printStackTrace();
                }

                if (solver.moves() > 0) {
                    initial = menuSubSolution(initial);
                } else {
                    // search has timeout after 10 seconds.
                    initial = menuMain();
                }
            }
        }
    }
}
