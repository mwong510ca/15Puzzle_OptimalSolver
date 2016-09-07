package mwong.myprojects.fifteenpuzzle.console;

import mwong.myprojects.fifteenpuzzle.solver.HeuristicOptions;
import mwong.myprojects.fifteenpuzzle.solver.SmartSolver;
import mwong.myprojects.fifteenpuzzle.solver.advanced.SmartSolverPdb;
import mwong.myprojects.fifteenpuzzle.solver.components.Board;
import mwong.myprojects.fifteenpuzzle.solver.components.PatternConstants;
import mwong.myprojects.fifteenpuzzle.solver.components.PatternOptions;

/**
 * SolverPdbCustomPattern is the console application extends AbstractApplication allow user to
 * enter custom defined pattern or choice of preset patterns.  User may change the timeout limit
 * from 3 to 300 seconds. It will display the process time, number of solution moves, and
 * number of nodes generated.  The solver will timeout after timeout limit include
 * pattern database 78.
 *
 * <p>Dependencies : AbstractApplication.java, Board.java, PatternOptions.java,
 *                   HeuristicOptions.java, Solver.java, SmartSolverMd.java,
 *                   SmartSolverPdb.java, SmartSolverPdbWd.java, SmartSolverWd.java,
 *                   SmartSolverWdMd.java
 *
 * @author   Meisze Wong
 *           www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public class SolverPdbCustomPattern extends AbstractApplication {
    private SmartSolver solver;
    private HeuristicOptions inUsePattern;
    private int inUsePatternOption;

    /**
     * Initial SolverPdbCustomPattern object.
     */
    public SolverPdbCustomPattern() {
        super();
        solver = new SmartSolverPdb(defaultPattern, refAccumulator);
        inUsePattern = solver.getHeuristicOptions();
        inUsePatternOption = 0;
    }

    // display the more choice of each additive pattern and allow user to
    // enter a custom defined pattern from group size 2 to 7
    private Board menuChangeSolver() {
        System.out.println("Choose your heuristic functions:");
        System.out.println("Enter '1' to 555 preset patterns");
        System.out.println("      '2' to 663 preset patterns");
        System.out.println("      '3' to 78 preset patterns");
        System.out.println("      '4' enter your custom pattern");
        System.out.println("      '0' no change");
        boolean pending = true;
        int option = 0;
        while (pending) {
            if (!scanner.hasNextInt()) {
                scanner.next();
            }
            int choice = -1;
            option = scanner.nextInt();

            switch (option) {
                case 0:
                    pending = false;
                    break;
                case 1:
                    System.out.println(PatternOptions.Pattern_555);
                    while (!PatternOptions.Pattern_555.isValidPattern(choice)) {
                        System.out.println("Choose your pattern option, enter '0' for default");
                        System.out.println("Notes: If data file not exists, it takes about"
                                + " 15s to generate.");
                        while (!scanner.hasNextInt()) {
                            scanner.next();
                        }
                        choice = scanner.nextInt();
                    }

                    if (inUsePattern != HeuristicOptions.PD555 || inUsePatternOption != choice) {
                        solver = new SmartSolverPdb(PatternOptions.Pattern_555, choice,
                                refAccumulator);
                        inUsePattern = HeuristicOptions.PD555;
                        inUsePatternOption = choice;
                    } else {
                        System.out.println("No Change, currently in use.");
                    }
                    pending = false;
                    break;
                case 2:
                    System.out.println(PatternOptions.Pattern_663);
                    while (!PatternOptions.Pattern_663.isValidPattern(choice)) {
                        System.out.println("Choose your pattern option, enter '0' for default");
                        System.out.println("Notes: If data file not exists, it takes about"
                                + " 2 minutes to generate.");
                        while (!scanner.hasNextInt()) {
                            scanner.next();
                        }
                        choice = scanner.nextInt();
                    }

                    if (inUsePattern != HeuristicOptions.PD663 || inUsePatternOption != choice) {
                        solver = new SmartSolverPdb(PatternOptions.Pattern_663, choice,
                                refAccumulator);
                        inUsePattern = HeuristicOptions.PD663;
                        inUsePatternOption = choice;
                    } else {
                        System.out.println("No Change, currently in use.");
                    }
                    pending = false;
                    break;
                case 3:
                    System.out.println(PatternOptions.Pattern_78);
                    do {
                        System.out.println("Choose your pattern option, enter '0' for default");
                        System.out.println("Notes: If data file not exists, it takes about"
                                + " 2.5-3 hours to generate.");
                        System.out.println("       Also equire minimum 2gig memory -Xms2g "
                                + "to run.");
                        System.out.println("If you are not sure, please choose default '0'.");
                        while (!scanner.hasNextInt()) {
                            scanner.next();
                        }
                        choice = scanner.nextInt();
                    } while (!PatternOptions.Pattern_78.isValidPattern(choice));

                    if (inUsePattern != HeuristicOptions.PD78 || inUsePatternOption != choice) {
                        solver = new SmartSolverPdb(PatternOptions.Pattern_78, choice,
                                refAccumulator);
                        inUsePattern = HeuristicOptions.PD78;
                        inUsePatternOption = choice;
                    } else {
                        System.out.println("No Change, currently in use.");
                    }
                    pending = false;
                    break;
                case 4:
                    byte [] pattern = new byte[16];
                    System.out.println("Enter 15 numbers (1, 2, 3 ... represent the group"
                            + " number) for your pattern, ");
                    System.out.println("min group size is 2 and max group 7, position 15 "
                            + " must be the last group, example:");
                    System.out.println("        1 1 1 1");
                    System.out.println("        2 2 2 2");
                    System.out.println("        3 3 3 3");
                    System.out.println("        4 4 4 0");
                    System.out.println("Notes (estimated generation time): size 5 or less"
                            + " - < 5s, size 6 - 1 min, size 7 - 10 mins");

                    int count = 0;
                    while (count < puzzleSize - 1) {
                        if (!scanner.hasNextInt()) {
                            scanner.next();
                        } else {
                            int value = scanner.nextInt();
                            if (value > 0 && value <= 7) {
                                pattern[count++] = (byte) value;
                            }
                        }
                    }
                    while (!scanner.hasNextInt()) {
                        scanner.next();
                    }
                    int zero = scanner.nextInt();
                    if (zero != 0) {
                        System.out.println("Your input pattern is invalid, please try again");
                        System.out.println("Enter '1 - 3' for default patterns,"
                                + "'4' for custome pattern' or '0' no change");
                        break;
                    }

                    for (count = 0; count < puzzleSize; count++) {
                        System.out.print(pattern[count] + " ");
                        if (count % 4 == 3) {
                            System.out.println();
                        }
                    }
                    System.out.println();
                    try {
                        boolean [] elementGroups = pattern2elementGroups(pattern, 7);

                        if (elementGroups != null) {
                            solver = null;
                            solver = new SmartSolverPdb(pattern, elementGroups, refAccumulator);
                            inUsePattern = HeuristicOptions.PDCustom;
                            inUsePatternOption = -1;
                            pending = false;
                            break;
                        }
                    } catch (ArrayIndexOutOfBoundsException | UnsupportedOperationException ex) {
                        System.err.println(ex);
                        System.out.println("Your input pattern is invalid, please try again");
                        System.out.println("Enter '1 - 3' for default patterns,"
                                + "'4' for custome pattern' or '0' no change");
                        break;
                    }
                    System.out.println("Your input pattern is invalid, please try again");
                    System.out.println("Enter '1 - 3' for default patterns,"
                            + "'4' for custome pattern' or '0' no change");
                    break;
                default:
                    System.out.println("Enter '1 - 3' for default patterns,"
                            + "'4' for custome pattern' or '0' no change");
            }
        }

        if (option > 0 && option < 5) {
            solver.versionSwitch(flagAdvVersion);
            solver.setTimeoutLimit(timeoutLimit);
            solver.printDescription();
        }
        return menuSub(true, true);
    }

    // verify the user defined pattern and convert it to element groups
    private boolean [] pattern2elementGroups(byte [] pattern, int maxSize) {
        if (pattern.length != 16) {
            return null;
        }
        if (pattern[15] != 0) {
            return null;
        }
        if (pattern[14] < 2 || pattern[14] > maxSize) {
            return null;
        }

        int numPatterns = pattern[14];
        byte [] patternGroups = new byte[numPatterns];
        for (int i = 0; i < puzzleSize - 1; i++) {
            if (pattern[i] < 1 || pattern[i] > numPatterns) {
                return null;
            }
            int group = pattern[i] - 1;
            patternGroups[group]++;
        }

        boolean [] elementGroups = new boolean [PatternConstants.getMaxGroupSize() + 1];
        for (byte group : patternGroups) {
            elementGroups[group] = true;
        }
        return elementGroups;
    }

    // display a list of options in main menu
    private Board menuMain() {
        printOption('q');
        printOption('c');
        printOption(flagAdvVersion);
        printOption('t');
        printOption('b');

        while (true) {
            if (scanner.hasNextInt()) {
                return keyInBoard();
            }
            char choice = scanner.next().charAt(0);
            if (choice == 'q') {
                System.out.println("Goodbye!\n");
                System.exit(0);
            }
            Board board = action(choice, true, true);
            if (board != null) {
                return board;
            }
            System.out.println("Please enter 'Q', 'C', 'V', 'T', 'E', 'M', 'H', 'R',"
                    + " or 16 numbers (0 - 15):");
        }
    }

    private Board action(char choice, boolean optionV, boolean optionT) {
        switch (choice) {
            case 'C': case 'c':
                return menuChangeSolver();
            case 'V': case 'v':
                if (optionV) {
                    flipVersion(solver);
                    return menuSub(false, optionT);
                }
                return null;
            case 'T': case 't':
                if (optionT) {
                    changeTimeout(solver, 3, 300);
                    return menuSub(optionV, false);
                }
                return null;
            case 'E':  case 'e': case 'M': case 'm': case 'H': case 'h':case 'R': case 'r':
                return createBoard(choice);
            default: return null;
        }
    }

    // display a list of options after user change the solver
    private Board menuSub(boolean optionV, boolean optionT) {
        printOption('q');
        if (optionV) {
            printOption(flagAdvVersion);
        }
        if (optionT) {
            printOption('t');
        }
        printOption('b');

        while (true) {
            if (scanner.hasNextInt()) {
                return keyInBoard();
            }
            char choice = scanner.next().charAt(0);
            if (choice == 'q') {
                System.out.println("Goodbye!\n");
                System.exit(0);
            }
            Board board = action(choice, optionV, optionT);
            if (board != null) {
                return board;
            }
            System.out.print("Please enter 'Q', ");
            if (optionV) {
                System.out.print("'H', ");
            }
            if (optionT) {
                System.out.print("'T', ");
            }
            System.out.println("'E', 'M', 'H, 'R', or 16 numbers (0 - 15):");
        }
    }

    /**
     * Start the application.
     */
    public void run() {
        System.out.println("Allow user to try on more preset patterns"
                + " or enter a user defined custom pattern.\n");

        solver.versionSwitch(flagAdvVersion);
        solver.setTimeoutLimit(timeoutLimit);
        solver.printDescription();

        Board initial = menuMain();

        while (true) {
            System.out.print(solver.getHeuristicOptions().getDescription());
            if (flagAdvVersion) {
                System.out.print(" (Advanced version) ");
            } else {
                System.out.print(" (Standard version) ");
            }
            System.out.println("will timeout at " + solver.getSearchTimeoutLimit() + "s:");
            System.out.println(initial);

            if (initial.isSolvable()) {
                solver.findOptimalPath(initial);
                if (solver.isSearchTimeout()) {
                    System.out.println("Search terminated after " + timeoutLimit + "s.");
                } else {
                    System.out.println("Minimum number of moves = " + solver.moves());
                }
            } else {
                System.out.println("The board is unsolvable, try again!\n");
            }
            System.out.println();
            if (refAccumulator.validateSolver(solver)) {
                refAccumulator.updateLastSearch(solver);
            }
            initial = menuMain();
        }
    }
}
