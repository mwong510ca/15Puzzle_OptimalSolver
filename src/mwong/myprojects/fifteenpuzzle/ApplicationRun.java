package mwong.myprojects.fifteenpuzzle;

import mwong.myprojects.fifteenpuzzle.console.AbstractApplication;
import mwong.myprojects.fifteenpuzzle.console.CompareEnhancement;
import mwong.myprojects.fifteenpuzzle.console.CompareHeuristic;
import mwong.myprojects.fifteenpuzzle.console.DemoSolverPdb78;
import mwong.myprojects.fifteenpuzzle.console.SolverHeuristic;
import mwong.myprojects.fifteenpuzzle.console.SolverHeuristicStats;
import mwong.myprojects.fifteenpuzzle.console.SolverPdbCustomPattern;

/**
 * ApplicationRun is the main console application of the 15 puzzle optimal solver.
 *
 * @author Meisze Wong
 *         www.linkedin.com/pub/macy-wong/46/550/37b/
 */
class ApplicationRun {
    /**
     * The main method takes the choice of console application of 15 puzzle solver.
     * Option 1: Demo version of pattern database 7-8 with monitor reference collection.
     * Option 2: Compare each enhancement added on pattern database 7-8 solver.
     * Option 3: Compare all 7 heuristic functions.
     * Option 4: User choice of heuristic functions, with option to display the solution.
     * Option 5: User choice of preset pattern database or user defined custom pattern.
     * Option 6: Run a number trails, display the average solved time and number of puzzles
     *           has been timeout.
     * Default : use option 3, Compare all 7 heuristic functions.
     * @param args standard argument main function
     */
    public static void main(String[] args) {
        AbstractApplication app = null;
        int choice = 3;
        if (args.length > 0 && args[0].matches("\\d+")) {
            choice = Integer.parseInt(args[0]);
        }

        switch (choice) {
            case 1 : app = new DemoSolverPdb78();
                break;
            case 2 : app = new CompareEnhancement();
                break;
            case 3 : app = new CompareHeuristic();
                break;
            case 4 : app = new SolverPdbCustomPattern();
                break;
            case 5 : app = new SolverHeuristic();
                break;
            case 6 : app = new SolverHeuristicStats();
                break;
            default :app = new CompareHeuristic();
        }
        app.run();
    }
}