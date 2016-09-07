package mwong.myprojects.fifteenpuzzle.console;

import mwong.myprojects.fifteenpuzzle.solver.SmartSolver;
import mwong.myprojects.fifteenpuzzle.solver.advanced.SmartSolverMd;
import mwong.myprojects.fifteenpuzzle.solver.advanced.SmartSolverPdb;
import mwong.myprojects.fifteenpuzzle.solver.advanced.SmartSolverPdbWd;
import mwong.myprojects.fifteenpuzzle.solver.advanced.SmartSolverWd;
import mwong.myprojects.fifteenpuzzle.solver.advanced.SmartSolverWdMd;
import mwong.myprojects.fifteenpuzzle.solver.components.Board;
import mwong.myprojects.fifteenpuzzle.solver.components.PatternOptions;

/**
 * CompareHeuristic is the console application extends AbstractApplication. It takes a
 * 16 numbers or choice of random board.  It will go through each heuristic function from
 * fastest to slowest.  It display the process time and number of nodes generated during
 * the search.  If it timeout after 10 seconds, except pattern database 78.  The remaining
 * heuristic function will display the estimate only.
 *
 * <p>Dependencies : AbstractApplication.java, Board.java, PatternOptions.java, Solver.java
 *                   SmartSolverMd.java, SmartSolverPdb.java, SmartSolverPdbWd.java,
 *                   SmartSolverWd.java, SmartSolverWdMd.java
 *
 * @author   Meisze Wong
 *           www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public class CompareHeuristic extends AbstractApplication {
    private SmartSolverMd solverMd;

    private SmartSolverWd solverWd;
    private SmartSolverWdMd solverWdMd;
    private SmartSolverPdbWd solverPdbWd555;
    private SmartSolverPdbWd solverPdbWd663;
    private SmartSolverPdb solverPdb78;
    private boolean stdSearch;
    private boolean advSearch;

    /**
     * Initial CompareHeuristic object.
     */
    public CompareHeuristic() {
        super();
        solverMd = new SmartSolverMd(refAccumulator);
        solverMd.messageSwitch(messageOff);

        solverWd = new SmartSolverWd(refAccumulator);
        solverWd.messageSwitch(messageOff);

        solverWdMd = new SmartSolverWdMd(refAccumulator);
        solverWdMd.messageSwitch(messageOff);

        solverPdbWd555 = new SmartSolverPdbWd(PatternOptions.Pattern_555, refAccumulator);
        solverPdbWd555.messageSwitch(messageOff);

        solverPdbWd663 = new SmartSolverPdbWd(PatternOptions.Pattern_663, refAccumulator);
        solverPdbWd663.messageSwitch(messageOff);

        solverPdb78 = new SmartSolverPdb(PatternOptions.Pattern_78, refAccumulator);
        solverPdb78.timeoutSwitch(timeoutOff);
        solverPdb78.messageSwitch(messageOff);
    }

    //  It take a solver and a 15 puzzle board, display the the process time and number of
    //  nodes generated during the search, time out after 10 seconds.
    private void solvePuzzle(SmartSolver solver, Board board) {
        System.out.print(solver.getHeuristicOptions().getDescription());
        if (solver.isFlagTimeout()) {
            System.out.println(" will timeout at " + solver.getSearchTimeoutLimit() + "s:");
        } else {
            System.out.println(" will run until solution found:");
        }

        solver.versionSwitch(tagStandard);
        int heuristicStandard = solver.heuristicStandard(board);

        System.out.print("Standard\t" + heuristicStandard + "\t\t");
        if (stdSearch) {
            solver.findOptimalPath(board);
            if (solver.isSearchTimeout()) {
                System.out.println("Timeout: " + solver.searchTime() + "s at depth "
                        + solver.searchTerminateAtDepth() + "\t" + solver.searchNodeCount());
                stdSearch = false;
            } else {
                System.out.printf("%-15s %-15s " + solver.searchNodeCount() + "\n",
                        solver.searchTime() + "s", solver.moves());
            }
        } else {
            System.out.println("Skip searching - will not solved in 10s.");
        }

        if (solver.versionSwitch(tagAdvanced)) {
            int heuristicAdvanced = solver.heuristicAdvanced(board);
            if (heuristicStandard == heuristicAdvanced) {
                System.out.println("Advanced\t" + "Same value");
                if (!stdSearch) {
                    advSearch = false;
                }
            } else {
                System.out.print("Advanced\t" + heuristicAdvanced + "\t\t");
                if (advSearch) {
                    solver.findOptimalPath(board);
                    if (solver.isSearchTimeout()) {
                        System.out.println("Timeout: " + solver.searchTime() + "s at depth "
                                + solver.searchTerminateAtDepth() + "\t"
                                + solver.searchNodeCount());
                        advSearch = false;
                    } else {
                        System.out.printf("%-15s %-15s " + solver.searchNodeCount() + "\n",
                                solver.searchTime() + "s", solver.moves());
                    }
                } else {
                    System.out.println("Skip searching - will not solved in 10s.");
                }
            }
        }
    }

    /**
     * Start the application.
     */
    public void run() {
        System.out.println("Compare 7 heuristic functions with standard and advanced version.\n");

        while (true) {
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
                System.out.println("\t\tEstimate\tTime\t\tMinimum Moves\tNodes generated");

                stdSearch = true;
                advSearch = true;
                solvePuzzle(solverPdb78, board);
                solvePuzzle(solverPdbWd663, board);
                solvePuzzle(solverPdbWd555, board);
                solvePuzzle(solverWdMd, board);
                solvePuzzle(solverWd, board);
                solverMd.linearConflictSwitch(tagLinearConflict);
                solvePuzzle(solverMd, board);
                solverMd.linearConflictSwitch(!tagLinearConflict);
                solvePuzzle(solverMd, board);

                // Notes: updateLastSearch is optional.
 //               refAccumulator.updateLastSearch(solverPdb78);
            } else {
                System.out.println("The board is unsolvable, try again!");
            }
            System.out.println();
        }
    }
}
