package mwong.myprojects.fifteenpuzzle.console;

import mwong.myprojects.fifteenpuzzle.solver.Solver;
import mwong.myprojects.fifteenpuzzle.solver.advanced.SmartSolverPdb;
import mwong.myprojects.fifteenpuzzle.solver.advanced.SmartSolverPdbBase;
import mwong.myprojects.fifteenpuzzle.solver.components.Board;
import mwong.myprojects.fifteenpuzzle.solver.components.PatternOptions;
import mwong.myprojects.fifteenpuzzle.solver.standard.SolverPdbBase;
import mwong.myprojects.fifteenpuzzle.solver.standard.SolverPdbEnh1;
import mwong.myprojects.fifteenpuzzle.solver.standard.SolverPdbEnh2;

/**
 * CompareEnhancement is the console application extends AbstractApplication. It takes a
 * 16 numbers or choice of random board.  It use default pattern database 7-8 heuristic function,
 * display the process time and number of nodes generated during the search by adding the
 * enhancement one at a time.  If standard estimate and advanced estimate are the same, it will not
 * display the runtime for advanced version.
 *
 * <p>Dependencies : AbstractApplication.java, Board.java, PatternOptions.java, Solver.java,
 *                   SmartSolverPdb.java, SmartSolverPdbBase.java, SolverPdbBase.java,
 *                   SolverPdbEnh1.java, SolverPdbEnh2.java
 *
 * @author   Meisze Wong
 *           www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public class CompareEnhancement extends AbstractApplication {
    private SolverPdbBase solverNoEnh;
    private SolverPdbEnh1 solverEnh1;
    private SolverPdbEnh2 solverEnh2;
    private SmartSolverPdbBase solverAdvEst;
    private SmartSolverPdb solverAdvanced;

    /**
     * Initial CompareEnhancement object.
     */
    public CompareEnhancement() {
        super();

        solverAdvanced = new SmartSolverPdb(PatternOptions.Pattern_78, refAccumulator);
        solverAdvanced.messageSwitch(messageOff);
        solverAdvanced.timeoutSwitch(timeoutOff);

        solverAdvEst = new SmartSolverPdbBase(solverAdvanced, refAccumulator);
        solverAdvEst.messageSwitch(messageOff);
        solverAdvEst.timeoutSwitch(timeoutOff);
        solverAdvEst.versionSwitch(tagAdvanced);

        solverEnh2 = new SolverPdbEnh2(solverAdvanced);
        solverEnh2.messageSwitch(messageOff);
        solverEnh2.timeoutSwitch(timeoutOff);

        solverEnh1 = new SolverPdbEnh1(solverAdvanced);
        solverEnh1.messageSwitch(messageOff);
        solverEnh1.timeoutSwitch(timeoutOff);

        solverNoEnh = new SolverPdbBase(solverAdvanced);
        solverNoEnh.messageSwitch(messageOff);
        solverNoEnh.timeoutSwitch(timeoutOff);
    }

    //  It take a solver and a 15 puzzle board, display the the process time and number of
    //  nodes generated during the search.
    private void solvePuzzle(Solver solver, Board board) {
        solver.findOptimalPath(board);
        System.out.printf("%-15s %-20s\n", solver.searchTime() + "s", solver.searchNodeCount());
    }

    /**
     * Start the application.
     */
    public void run() {
        System.out.println("Compare 15 puzzle solver enhancement using "
                + solverAdvanced.getHeuristicOptions().getDescription() + "\n");
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
                int heuristicStandard = solverAdvanced.heuristicStandard(board);
                int heuristicAdvanced = solverAdvanced.heuristicAdvanced(board);
                System.out.print("Standard estimate : " + heuristicStandard + "\t\t");
                System.out.println("    Advanced estimate : " + heuristicAdvanced);
                System.out.println("\t\t\t\t    Time\t    Nodes");

                System.out.printf("%-36s", "1. No enhancement : ");
                solvePuzzle(solverNoEnh, board);
                System.out.printf("%-36s", "2. Add symmetry reduction : ");
                solvePuzzle(solverEnh1, board);
                System.out.printf("%-36s", "3. Add circular reduction : ");
                solvePuzzle(solverEnh2, board);
                System.out.printf("%-36s", "4. Add starting order detection : ");
                solverAdvanced.versionSwitch(tagStandard);
                solvePuzzle(solverAdvanced, board);
                if (solverAdvanced.isAddedReference()) {
                    heuristicAdvanced = solverAdvanced.heuristicAdvanced(board);
                }
                if (heuristicAdvanced > heuristicStandard) {
                    System.out.printf("%-36s", "5. Advanced estimate : ");
                    solvePuzzle(solverAdvEst, board);
                    if (solverAdvanced.hasPartialSolution(board)) {
                        System.out.printf("%-36s", "6. Use preset partial solution :");
                        solverAdvanced.versionSwitch(tagAdvanced);
                        solvePuzzle(solverAdvanced, board);
                        if (solverAdvEst.isAddedReference()) {
                            refAccumulator.updateLastSearch(solverAdvEst);
                        }
                    } else {
                        System.out.println("6. Skip - No preset partial solution.");
                        if (solverAdvanced.isAddedReference()) {
                            refAccumulator.updateLastSearch(solverAdvEst);
                        }
                    }
                } else {
                    System.out.println("5 & 6. Skip - Both estimate are the same.");
                    if (solverAdvanced.isAddedReference()) {
                        refAccumulator.updateLastSearch(solverAdvEst);
                    }
                }
            } else {
                System.out.println("The board is unsolvable, try again!");
            }
            System.out.println("\t\t\tActual number of solution move : "
                    + solverAdvanced.moves() + "\n");
        }
    }
}
