/****************************************************************************
 *  @author   Meisze Wong
 *            www.linkedin.com/pub/macy-wong/46/550/37b/
 *
 *  Compilation :  javac ApplicationCustomPattern.java
 *  Execution :    java ApplicationCustomPattern
 *  Dependencies : Board.java, Stopwatch.java, PDElement.java,
 *                 PDPresetPatterns.java, SolverInterface.java
 *                 SolverPD.java, AdvancedAccumulator.java
 *
 *  ApplicationCustomPattern is a console application to allow user to enter
 *  custom defined pattern.  User may change the timeout limit from 3 to
 *  300 seconds.  It will display the process time, number of solution moves,
 *  and number of nodes generated.  The starts with Pattern Database 663 solver.
 *  The solver will timeout after timeout limit include pattern database 78.
 *
 *  sample output: output_CustomPattern.txt
 *
 ****************************************************************************/

package mwong.myprojects.fifteenpuzzle.console;

import mwong.myprojects.fifteenpuzzle.solver.Solver;
import mwong.myprojects.fifteenpuzzle.solver.SolverConstants;
import mwong.myprojects.fifteenpuzzle.solver.SolverProperties;
import mwong.myprojects.fifteenpuzzle.console.ApplicationType;
import mwong.myprojects.fifteenpuzzle.solver.HeuristicOptions;
import mwong.myprojects.fifteenpuzzle.solver.advanced.ai.ReferenceAccumulator;
import mwong.myprojects.fifteenpuzzle.solver.components.Board;
import mwong.myprojects.fifteenpuzzle.solver.components.PatternConstants;
import mwong.myprojects.fifteenpuzzle.solver.components.PatternElement;
import mwong.myprojects.fifteenpuzzle.solver.components.PatternOptions;
import mwong.myprojects.fifteenpuzzle.solver.advanced.SmartSolverPD;

import java.util.Scanner;

public class ApplicationCustomPattern extends AbstractApplication {
    private final int puzzleSize = SolverConstants.getPuzzleSize();
    private final ApplicationType applicationType = ApplicationType.CustomPattern;

    private final ReferenceAccumulator refAccumulator = new ReferenceAccumulator();
    private Solver solver = new SmartSolverPD(PatternOptions.Pattern_663, refAccumulator);
    private HeuristicOptions inUsePattern = solver.getHeuristicOptions();
    private int inUsePatternOption = 0;
    private final Scanner scanner  = new Scanner(System.in, "UTF-8");
    private int timeoutLimit = 10;
    private boolean flagAdvPriority = false;
    private boolean flagAdvancedUpdate = false;

    
    
    // display the more choice of each additive pattern and allow user to
    // enter a custom defined pattern from group size 2 to 7
    private void menuChangeSolver() {
        System.out.println("Choose your heuristic functions:");
        System.out.println("Enter '1' to 555 preset patterns");
        System.out.println("      '2' to 663 preset patterns");
        System.out.println("      '3' to 78 preset patterns");
        System.out.println("      '4' enter your custom pattern");
        System.out.println("      '0' no change");
        boolean pending = true;
        int choice;
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
                    System.out.println(PatternOptions.Pattern_555);
                    do {
                        System.out.println("Choose your pattern option, enter '0' for default");
                        System.out.println("Notes: If data file not exists, it takes about"
                                + " 15s to generate.");
                        choice = scanner.nextInt();
                    } while (!PatternOptions.Pattern_555.isValidPattern(choice));

                    if (inUsePattern != HeuristicOptions.PD555 || inUsePatternOption != choice) {
                        solver = new SmartSolverPD(PatternOptions.Pattern_555, choice, refAccumulator);
                        inUsePattern = HeuristicOptions.PD555;
                        inUsePatternOption = choice;
                    } else {
                        System.out.println("No Change, currently in use.");
                    }
                    pending = false;
                    break;
                case 2:
                    System.out.println(PatternOptions.Pattern_663);
                    do {
                        System.out.println("Choose your pattern option, enter '0' for default");
                        System.out.println("Notes: If data file not exists, it takes about"
                                + " 2 minutes to generate.");
                        choice = scanner.nextInt();
                    } while (!PatternOptions.Pattern_663.isValidPattern(choice));

                    if (inUsePattern != HeuristicOptions.PD663 || inUsePatternOption != choice) {
                        solver = new SmartSolverPD(PatternOptions.Pattern_663, choice, refAccumulator);
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
                        choice = scanner.nextInt();
                    } while (!PatternOptions.Pattern_78.isValidPattern(choice));

                    if (inUsePattern != HeuristicOptions.PD78 || inUsePatternOption != choice) {
                        solver = new SmartSolverPD(PatternOptions.Pattern_78, choice, refAccumulator);
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

                    boolean [] elementGroups = pattern2elementGroups(pattern, 7);
                    for (count = 0; count < puzzleSize; count++) {
                        System.out.print(pattern[count] + " ");
                        if (count % 4 == 3) {
                            System.out.println();
                        }
                    }
                    System.out.println();
                    
                    if (elementGroups != null) {
                        solver = null;
                        solver = new SmartSolverPD(pattern, elementGroups, refAccumulator);
                        inUsePattern = HeuristicOptions.PDCustom;
                        inUsePatternOption = -1;
                        pending = false;
                        break;
                    }
                    System.out.println("Your input pattern is invalid, please try again");
                    System.out.println("Enter '1 - 4' for 15puzzle patterns, '0' no change");
                    break;
                default:
                    System.out.println("Enter '1 - 4' for 15puzzle patterns, '0' no change");
            }
        } while (pending);

        solver.advPrioritySwitch(flagAdvPriority);
        solver.setTimeoutLimit(timeoutLimit);
        solver.printDescription();
        flagAdvancedUpdate = false;
        if (solver.getHeuristicOptions() == HeuristicOptions.PD78
                && timeoutLimit > refAccumulator.getCutoffLimit()) {
            flagAdvancedUpdate = true;
        }
        menuSub(true, true);
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
        System.out.println("      'T' - change timeout limit");
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
                if (solver.getHeuristicOptions() != HeuristicOptions.PD78) {
                    solver = null;
                }
                System.out.println("Goodbye!\n");
                System.exit(0);
            }

            if (value == 'H' || value == 'h') {
                flagAdvPriority = !flagAdvPriority;
                solver.advPrioritySwitch(flagAdvPriority);
                if (inUsePattern != HeuristicOptions.PD78) {
                    menuSub(false, true);
                } else {
                    menuSub(false, false);
                }
                break;
            }
            if (value == 'T' || value == 't') {
                int limit = 0;
                do {
                    System.out.println("Enter timeout limit in seconds, minimun 3 seconds"
                            + " and maximum 5 mins (300s):");
                    if (!scanner.hasNextInt()) {
                        scanner.next();
                    } else {
                        limit = scanner.nextInt();
                    }
                } while (limit < 3 || limit > 300);
                timeoutLimit = limit;
                solver.setTimeoutLimit(timeoutLimit);
                if (solver.getHeuristicOptions() == HeuristicOptions.PD78) {
                    if (timeoutLimit > refAccumulator.getCutoffLimit()) {
                        flagAdvancedUpdate = true;
                    } else {
                        flagAdvancedUpdate = false;
                    }
                }
                menuSub(true, false);
                break;
            }
            System.out.println("Please enter 'Q', 'C', 'H', 'T' or 16 numbers (0 - 15):");
        } while (true);
    }

    // display a list of options after user change the solver
    private void menuSub(boolean optionH, boolean optionT) {
        System.out.print("Enter ");
        if (optionH) {
            if (flagAdvPriority) {
                System.out.println("'H' - change initial heuristic estimate from Advanced "
                        + "to Original");
            } else {
                System.out.println("'H' - change initial heuristic estimate from Original "
                        + "to Advanced");
            }
            System.out.print("      ");
        }
        if (optionT) {
            System.out.println("'T' - change timeout limit");
            System.out.print("      ");
        }
        System.out.println("16 numbers from 0 to 15 for the puzzle");
        do {
            if (scanner.hasNextInt()) {
                break;
            }
            char value = scanner.next().charAt(0);
            if (optionH && (value == 'H' || value == 'h')) {
                flagAdvPriority = !flagAdvPriority;
                solver.advPrioritySwitch(flagAdvPriority);
                if (optionT) {
                    menuSub(false, true);
                } else {
                    optionH = false;
                    System.out.println("Enter 16 numbers from 0 to 15 for the puzzle");
                }
                break;
            } else if (optionT && (value == 'T' || value == 't')) {
                int limit = 0;
                do {
                    System.out.println("Enter timeout limit in seconds, minimun 3 seconds"
                            + " and maximum 5 mins (300s):");
                    if (!scanner.hasNextInt()) {
                        scanner.next();
                    } else {
                        limit = scanner.nextInt();
                    }
                } while (limit < 3 || limit > 300);
                timeoutLimit = limit;
                solver.setTimeoutLimit(timeoutLimit);
                if (solver.getHeuristicOptions() == HeuristicOptions.PD78) {
                    if (timeoutLimit > refAccumulator.getCutoffLimit()) {
                        flagAdvancedUpdate = true;
                    } else {
                        flagAdvancedUpdate = false;
                    }
                }
                if (optionH) {
                    menuSub(true, false);
                } else {
                    optionT = false;
                    System.out.println("Enter 16 numbers from 0 to 15 for the puzzle");
                }
                break;
            } else {
                System.out.print("Please enter ");
                if (optionH) {
                    System.out.print("'H', ");
                }
                if (optionT) {
                    System.out.print("'T', ");
                }
                System.out.println("or 16 numbers (0 - 15):");
            }
        } while (true);
    }

    /**
     *  a console application offer more preset patterns and allow user to enter
     *  custom defined pattern.  User may change the timeout limit from 5 to
     *  300 seconds.  It will display the process time, number of solution moves,
     *  and number of nodes generated, timeout after user defined limit.
     *
     *  @param args standard argument main function
     */
    public void run() {
        solver.advPrioritySwitch(flagAdvPriority);
        solver.setTimeoutLimit(timeoutLimit);
        solver.printDescription();

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

            System.out.println();
            Board initial = new Board(blocks);
            System.out.print(inUsePattern.getDescription());
            printHeading(applicationType, solver);
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

            // Notes: updateLastSearch is optional.
            if (flagAdvancedUpdate) {
                refAccumulator.updateLastSearch(solver);
            }

            menuMain();
        } while (true);
    }
}
