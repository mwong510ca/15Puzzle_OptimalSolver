package mwong.myprojects.fifteenpuzzle.console;

import java.util.Scanner;

import mwong.myprojects.fifteenpuzzle.solver.components.Board;
import mwong.myprojects.fifteenpuzzle.solver.components.Direction;
import mwong.myprojects.fifteenpuzzle.solver.Solver;

class AbstractApplication implements Application {
	protected Scanner scanner;
	protected final int puzzleSize;
	
	AbstractApplication() {
		puzzleSize = ApplicationProperties.getPuzzleSize();
	}
	
    /**
     *  Print solver heading of given ApplicationType.
     */	
	@Override
	public void printHeading(ApplicationType type, Solver solver) {
        if (type != ApplicationType.CustomPattern) {
            if (type != ApplicationType.Stats) {
                System.out.println();
            }
            System.out.print(solver.getHeuristicType().getDescription());
        }

        if (solver.isFlagTimeout() || type == ApplicationType.CustomPattern) {
            System.out.println(" will timeout at " + solver.getSearchTimeoutLimit() + "s:");
        } else {
            System.out.println(" will run until solution found:");
        }
    }
    

    /**
     * Print the minimum number of moves to the goal state.
     */
	@Override
	public void solutionSummary(Solver solver) {
    	if (solver.isSearchTimeout()) {
            System.out.println("Search terminated after "
            		+ solver.searchTime() + "s.\n");
    	} else {
    		System.out.println("Minimum number of moves = "
    				+ solver.moves() + "\n");
    	}
    }

    /**
     * Print the list of direction of moves to the goal state.
     */
	@Override
	public void solutionList(Solver solver) {
    	int steps = solver.moves();
        for (int i = 1; i <= steps; i++) {
            System.out.print(i + " : " + solver.solution()[i] + " ");
            if (i % 10 == 0 && steps > i) {
                System.out.println();
            }
        }
        System.out.println("\n");
    }

    /**
     * Print all boards of moves to the goal state.
     */
	@Override
	public void solutionDetail(Board board, Solver solver) {
        int count = 0;
        int steps = solver.moves();
        Direction dir = Direction.NONE;
        do {
            System.out.print("Step : " + (count));
            if (count > 0) {
                System.out.print("\t" + dir);
            }
            System.out.println();
            System.out.println(board);
            count++;
            if (count <= steps) {
                dir = solver.solution()[count];
                board = board.shift(dir);
            }
        } while (count <= steps);
    }
	
	Board puzzleIn() {
		byte[] blocks = new byte[puzzleSize];
        boolean [] used = new boolean[puzzleSize];
        int count = 0;

        while (count < puzzleSize) {
            if (!scanner.hasNextInt()) {
                scanner.next();
            } else {
                int value = scanner.nextInt();
                if (value < 0 || value >= puzzleSize) {
                    System.out.println("Invalid number " + value + ", try again.");
                } else if (used[value]) {
                    System.out.println(value + " already entered, try again.");
                } else {
                    blocks[count++] = (byte) value;
                    used[value] = true;
                }
            }
        }
        return new Board(blocks);
	}
}
