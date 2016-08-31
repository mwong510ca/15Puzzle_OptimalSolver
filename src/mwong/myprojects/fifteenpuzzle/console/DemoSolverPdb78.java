package mwong.myprojects.fifteenpuzzle.console;

import mwong.myprojects.fifteenpuzzle.solver.advanced.SmartSolverExtra;
import mwong.myprojects.fifteenpuzzle.solver.advanced.SmartSolverPdb;
import mwong.myprojects.fifteenpuzzle.solver.components.Board;
import mwong.myprojects.fifteenpuzzle.solver.components.PatternOptions;

/**
 * DemoSolverPdb78 is the console application extends AbstractApplication.  It use default
 * pattern database 7-8 heuristic function with monitor the changes on reference collection.
 * It takes a 16 numbers or choice of random board. It solved with standard version and
 * advanced version, while the estimate are different.  If it takes more than 8s and added to
 * reference collection, it will display the new count of reference collection.  It will
 * search again to showed the difference after the puzzle added to the reference collection.
 * If the board is the reference board, standard version will timeout at 8s.
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
        solverPdb78.setTimeoutLimit(refAccumulator.getCutoffSetting());
        solverPdb78.timeoutSwitch(timeoutOff);
        extra = new SmartSolverExtra();
    }

    //  It take a solver and a 15 puzzle board, display the the process time and number of
    //  nodes generated during the search, time out after 10 seconds.
    private void solvePuzzle(Board board) {
        if (extra.advancedContains(board, false, refAccumulator) == null) {
            System.out.println("\t\tThis is NOT a reference board.\n");
        } else {
            System.out.println("\t\tExists in stored reference collection.\n");
            solverPdb78.timeoutSwitch(timeoutOn);
        }

        solverPdb78.versionSwitch(!tagAdvanced);
        int heuristicStandard = solverPdb78.heuristicStandard(board);

        System.out.println("Standard Estimate\t" + heuristicStandard);
        solverPdb78.findOptimalPath(board);
        if (solverPdb78.isSearchTimeout()) {
            System.out.println("\t\tTimeout: " + solverPdb78.searchTime() + "s at depth "
                    + solverPdb78.searchTerminateAtDepth() + "\t" + solverPdb78.searchNodeCount());
        } else {
            System.out.printf("\t\tTotal : %-15s  Time : "
                    + solverPdb78.searchTime() + "s\n\n", solverPdb78.searchNodeCount());
        }
        if (solverPdb78.isAddedReference()) {
            System.out.println("System detect the dvanced estimate is the same, added to"
                    + " reference collection after the search.");
            System.out.println(refAccumulator.getActiveMap().size()
                    + " reference board in system.\n");
            refAccumulator.updateLastSearch(solverPdb78);
        }

        solverPdb78.timeoutSwitch(timeoutOff);
        solverPdb78.versionSwitch(tagAdvanced);
        int heuristicAdvanced = solverPdb78.heuristicAdvanced(board);
        if (heuristicStandard == heuristicAdvanced) {
            System.out.println("Advanced Estimate\t" + "Same value");
        } else {
            System.out.println("Advanced Estimate \t" + heuristicAdvanced);
            solverPdb78.findOptimalPath(board);
            System.out.printf("\t\tTotal : %-15s  Time : "
                    + solverPdb78.searchTime() + "s\n", solverPdb78.searchNodeCount());
            if (solverPdb78.isAddedReference()) {
                System.out.println("\nIt added to reference collection after the search.");
                System.out.println(refAccumulator.getActiveMap().size()
                        + " reference board in system.\n");
                heuristicAdvanced = solverPdb78.heuristicAdvanced(board);
                System.out.println("Estimate change to\t" + heuristicAdvanced
                        + "\t\t(Search again)");
                solverPdb78.findOptimalPath(board);
                System.out.printf("\t\tTotal : %-15s  Time : "
                        + solverPdb78.searchTime() + "s\n", solverPdb78.searchNodeCount());
                refAccumulator.updateLastSearch(solverPdb78);
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
                solvePuzzle(board);
            } else {
                System.out.println("The board is unsolvable, try again!");
            }
            System.out.println();
        } while (true);
    }
}
