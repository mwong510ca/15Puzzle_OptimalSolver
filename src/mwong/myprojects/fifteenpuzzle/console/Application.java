package mwong.myprojects.fifteenpuzzle.console;

import mwong.myprojects.fifteenpuzzle.solver.Solver;
import mwong.myprojects.fifteenpuzzle.solver.components.Board;

public interface Application {

	void printHeading(ApplicationType type, Solver solver);

	void solutionDetail(Board board, Solver solver);

	void solutionList(Solver solver);

	void solutionSummary(Solver solver);

}
