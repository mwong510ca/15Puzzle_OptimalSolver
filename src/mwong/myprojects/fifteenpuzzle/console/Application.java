package mwong.myprojects.fifteenpuzzle.console;

import mwong.myprojects.fifteenpuzzle.solver.components.Board;
import mwong.myprojects.fifteenpuzzle.solver.Solver;

public interface Application {

	void printHeading(ApplicationType type, Solver solver);

	void solutionDetail(Board board, Solver solver);

	void solutionList(Solver solver);

	void solutionSummary(Solver solver);

}
