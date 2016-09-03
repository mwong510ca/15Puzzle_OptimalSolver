package mwong.myprojects.fifteenpuzzle.solver.ai;

import mwong.myprojects.fifteenpuzzle.solver.advanced.SmartSolverPdb;
import mwong.myprojects.fifteenpuzzle.solver.components.Board;

import java.util.Arrays;
import java.util.Scanner;

//TODO
/**
 * @author   Meisze Wong
 *           www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public class ReferenceAdministrator {
    private enum Action {
        // Add a board without checking the total time to solve
        AddAlways,
        // Add a board if it takes more than preset limit to solve
        Add,
        // Remove a board
        Remove;

        @Override
        public String toString() {
            if (this == Remove) {
                return "remove";
            } else {
                return "add";
            }
        }
    }

    private static Scanner scanner;

    /**
     *  A console application to manage the advanced accumulator storage.
     *  It allow to view the summary, print all board, change cutoff limit,
     *  update the data set, add or remove a board.
     *
     *  @param args standard argument main function
     */
    public static void main(String[] args) {
        ReferenceAccumulator advAccumulator = new ReferenceAccumulator();
        advAccumulator.printStatus();
        int sizePuzzle = ReferenceConstants.getPuzzleSize();
        Action action = Action.Add;
        boolean bypass = true;

        SmartSolverPdb solver = advAccumulator.createSolver();
        if (solver == null) {
            return;
        }

        scanner = new Scanner(System.in, "UTF-8");
        int count = -1;

        while (true) {
            do {
                System.out.println("Enter 'Q' - Quit the program");
                System.out.println("      'V' - Veiw the current storage of archived boards.");
                System.out.println("      'P' - Print all archived boards");
                System.out.println("      'C' - Change cutoff time limit from "
                        + advAccumulator.getCutoffSetting() + "s to 1s to 10s:");
                System.out.println("      'U' - Update the current storage, review and add "
                        + "all pending boards to storage.");
                System.out.println("      'F' - Refresh the data file.");
                System.out.println("      'S' - Reset to default setting.");
                System.out.println("            Warning: All accumulated boards will be removed.");
                if (action != Action.Remove) {
                    System.out.println("      'R' - Remove a board if exists.");
                }
                if (action != Action.Add) {
                    System.out.println("      'A' - Add a board if over "
                            + advAccumulator.getCutoffSetting() + "s to solve.");
                }
                if (action != Action.AddAlways) {
                    System.out.println("      'B' - Add a board bypass time limit requirement.");
                }
                System.out.print("      16 numbers from 0 to 15 to ");
                if (action == Action.Remove) {
                    System.out.println("remove a board if exists:\n");
                } else if (action == Action.Add) {
                    System.out.println("add a board if over "
                            + advAccumulator.getCutoffSetting() + "s to solve:\n");
                } else if (action == Action.AddAlways) {
                    System.out.println("add a board bypass time limit requirement:\n");
                }

                if (scanner.hasNextInt()) {
                    break;
                }

                char value = scanner.next().charAt(0);

                if (value == 'Q' || value == 'q') {
                    System.out.println("Goodbye!\n");
                    advAccumulator.updateData(solver);
                    System.exit(0);
                }

                if (value == 'V' || value == 'v') {
                    advAccumulator.printStatus();
                    continue;
                }
                if (value == 'P' || value == 'p') {
                    advAccumulator.printAllBoards();
                    continue;
                }
                if (value == 'C' || value == 'c') {
                    System.out.println("Enter new limit from 1 to 10 seconds:");
                    do {
                        if (scanner.hasNextInt()) {
                            advAccumulator.setCutoffArchive(scanner.nextInt());
                            break;
                        }
                    } while (true);
                }
                if (value == 'U' || value == 'u') {
                    advAccumulator.updateData(solver);
                    System.out.println("Complteted.  Storage is up to date.");
                }
                if (value == 'F' || value == 'f') {
                    advAccumulator.refreshFile();
                }
                if (value == 'S' || value == 's') {
                    advAccumulator.reset();
                }
                if (value == 'R' || value == 'r') {
                    action = Action.Remove;
                    count = -1;
                }
                if (value == 'A' || value == 'a') {
                    action = Action.Add;
                    count = -1;
                }
                if (value == 'B' || value == 'b') {
                    action = Action.AddAlways;
                    count = -1;
                }
            } while (true);

            do {
                byte[] blocks = new byte[sizePuzzle];
                boolean[] used = new boolean[sizePuzzle];
                int idx = 0;
                while (idx < sizePuzzle) {
                    if (!scanner.hasNextInt()) {
                        scanner.next();
                    } else {
                        int value = scanner.nextInt();
                        if (value < 0 || value >= sizePuzzle) {
                            System.out.println("Invalid number " + value + ", try again.");
                        } else if (used[value]) {
                            System.out.println(value + " already entered, try again.");
                        } else {
                            blocks[idx++] = (byte) value;
                            used[value] = true;
                        }
                    }
                }

                if (count == -1) {
                    System.out.println();
                    count++;
                }

                Board initial = new Board(blocks);

                if (initial.isSolvable()) {
                    if (action == Action.Remove) {
                        ++count;
                        ReferenceBoard advBoard
                                = new ReferenceBoard(initial);
                        if (!advAccumulator.getActiveMap().containsKey(advBoard)) {
                            System.out.println(count + " : \t\tNo changed, not in system");
                        } else {
                            System.out.println(count + " : \t\tRemoved");
                            advAccumulator.removeBoard(initial);
                        }
                    } else {
                        solver.findOptimalPath(initial);
                        System.out.print(++count + " " + solver.searchTime());
                        if (action == Action.AddAlways) {
                            advAccumulator.addBoard(solver, bypass);
                        }
                        if (advAccumulator.updateLastSearch(solver)) {
                            solver.findOptimalPath(initial);
                            System.out.println("\t\t" + solver.searchTime());
                        } else {
                            System.out.println("\t\t\t\tskip");
                        }
                    }
                } else {
                    System.out.println(Arrays.toString(initial.getTiles()));
                    System.out.println("unsolvable");
                }

                System.out.println("Please 16 numbers (0 - 15) to " + action
                        + ", or any characters return to main menu:");
            } while (scanner.hasNextInt());
        }
    }
}
