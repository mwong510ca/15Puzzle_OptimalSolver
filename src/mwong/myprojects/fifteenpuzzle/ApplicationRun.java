package mwong.myprojects.fifteenpuzzle;

import mwong.myprojects.fifteenpuzzle.console.ApplicationCompareHeuristic;

class ApplicationRun {
    /**
     *  A console application to compare 5 types of 15 puzzle heuristic function.  The user
     *  can choose a random board or enter 16 numbers of 15 puzzle.  It will go through
     *  each heuristic function and display the results.
     *
     *  @param args standard argument main function
     */
    public static void main(String[] args) {
        ApplicationCompareHeuristic app = new ApplicationCompareHeuristic();
        app.run();
    }
}