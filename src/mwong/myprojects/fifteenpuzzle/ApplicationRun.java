package mwong.myprojects.fifteenpuzzle;

import mwong.myprojects.fifteenpuzzle.console.AbstractApplication;
import mwong.myprojects.fifteenpuzzle.console.ApplicationCompareHeuristic;
import mwong.myprojects.fifteenpuzzle.console.ApplicationCustomPattern;
import mwong.myprojects.fifteenpuzzle.console.ApplicationDemo;
import mwong.myprojects.fifteenpuzzle.console.ApplicationSolver;
import mwong.myprojects.fifteenpuzzle.console.ApplicationSolverStats;

class ApplicationRun {
    /**
     *  A console application to compare 5 types of 15 puzzle heuristic function.  The user
     *  can choose a random board or enter 16 numbers of 15 puzzle.  It will go through
     *  each heuristic function and display the results.
     *
     *  @param args standard argument main function
     */
    public static void main(String[] args) {
    	AbstractApplication app = null;
    	if (args.length == 0) {
    		app = new ApplicationDemo();            
    	} else {
        	switch (args[0].charAt(0)) {
        	case '1' : app = new ApplicationSolver(); break;
        	case '2' : app = new ApplicationCompareHeuristic(); break;
        	case '3' : app = new ApplicationCustomPattern(); break;
        	case '4' : app = new ApplicationSolverStats(); break;    		
        	}
    	}
        //ApplicationSolver app = new ApplicationSolver();
        app.run();
    }
}