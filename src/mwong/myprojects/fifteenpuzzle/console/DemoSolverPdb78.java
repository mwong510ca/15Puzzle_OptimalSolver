package mwong.myprojects.fifteenpuzzle.console;

import mwong.myprojects.fifteenpuzzle.solver.advanced.SmartSolverExtra;
import mwong.myprojects.fifteenpuzzle.solver.advanced.SmartSolverPdb;
import mwong.myprojects.fifteenpuzzle.solver.components.Board;
import mwong.myprojects.fifteenpuzzle.solver.components.PatternOptions;

/**
 * DemoSolverPdb78 is the console application extends AbstractApplication.  It use default
 * pattern database 7-8 heuristic function with monitor the changes on reference collection.
 * It takes a 16 numbers or choice of random board. It solved with standard version and
 * advanced version, while the estimate are different.  If it takes more than 10s and added to
 * reference collection, it will display the new count of reference collection.  It will
 * search again to showed the difference after the puzzle added to the reference collection.
 *
 * <p>Dependencies : AdvancedRecord.java, Board.java, PatternOptions.java, SmartSolverExtra.java,
 *                   SmartSolverPdbBase.java
 *
 * @author   Meisze Wong
 *           www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public class DemoSolverPdb78 extends AbstractApplication {
    private SmartSolverPdb solverPdb78;
    private SmartSolverExtra extra;

    /**
     * Initial DemoSolverPdb78 object.
     */
    public DemoSolverPdb78() {
        super();
        solverPdb78 = new SmartSolverPdb(PatternOptions.Pattern_78, refAccumulator);
        solverPdb78.timeoutSwitch(timeoutOff);
        extra = new SmartSolverExtra();
    }

    //  It take a solver and a 15 puzzle board, display the the process time and number of
    //  nodes generated during the search, time out after 10 seconds.
    private void solvePuzzle(SmartSolverPdb solver, Board board) {
        if (extra.advancedContains(board, false, refAccumulator) == null) {
            System.out.println("This is not a reference board.\n");
        } else {
            System.out.println("This board stored as reference board.\n");
        }

        solver.versionSwitch(!tagAdvanced);
        int heuristicStandard = solver.heuristicStandard(board);

        System.out.println("Standard Estimate\t" + heuristicStandard);
        solver.findOptimalPath(board);
        System.out.printf("\t\tTotal : %-15s  Time : "
                + solver.searchTime() + "s\n\n", solver.searchNodeCount());
        if (solver.isAddedReference()) {
            System.out.println("It added to reference collection after the search.");
            System.out.println(refAccumulator.getActiveMap().size()
                    + " reference board in system.\n");
        }

        solver.versionSwitch(tagAdvanced);
        int heuristicAdvanced = solver.heuristicAdvanced(board);
        if (heuristicStandard == heuristicAdvanced) {
            System.out.println("Advanced Estimate\t" + "Same value");
        } else {
            System.out.println("Advanced Estimate \t" + heuristicAdvanced);
            solver.findOptimalPath(board);
            System.out.printf("\t\tTotal : %-15s  Time : "
                    + solver.searchTime() + "s\n", solver.searchNodeCount());
            if (solver.isAddedReference()) {
                System.out.println("\nIt added to reference collection after the search.");
                System.out.println(refAccumulator.getActiveMap().size()
                        + " reference board in system.\n");
                System.out.println("Advanced\t" + heuristicAdvanced + "\t\tSearch again.");
                solver.findOptimalPath(board);
                System.out.printf("\t\tTotal : %-15s  Time : "
                        + solver.searchTime() + "s\n", solver.searchNodeCount());
            }
        }
    }

    /**
     * Start the application.
     */
    public void run() {
        System.out.println(refAccumulator.getActiveMap().size() + " reference board in system.");
        do {
            printOption('q');
            printOption('b');

            Board board = null;
            while (true) {
                if (scanner.hasNextInt()) {
                    board = keyInBoard();
                    break;
                }
                char choice = scanner.next().charAt(0);
                if (choice == 'q') {
                    System.out.println("Goodbye!\n");
                    System.exit(0);
                }
                board = createBoard(choice);
                if (board != null) {
                    break;
                }
                System.out.println("Please enter 'Q', 'E', 'M', 'H', 'R' or 16 numbers (0 - 15):");
            }

            System.out.print("\n" + board);
            if (board.isSolvable()) {
                solvePuzzle(solverPdb78, board);
                // Notes: updateLastSearch is optional.
                refAccumulator.updateLastSearch(solverPdb78);
            } else {
                System.out.println("The board is unsolvable, try again!");
            }
            System.out.println();
        } while (true);
    }
}
