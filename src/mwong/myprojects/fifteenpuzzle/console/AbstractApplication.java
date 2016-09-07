package mwong.myprojects.fifteenpuzzle.console;

import mwong.myprojects.fifteenpuzzle.PropertiesCache;
import mwong.myprojects.fifteenpuzzle.solver.SmartSolver;
import mwong.myprojects.fifteenpuzzle.solver.Solver;
import mwong.myprojects.fifteenpuzzle.solver.SolverProperties;
import mwong.myprojects.fifteenpuzzle.solver.ai.ReferenceAccumulator;
import mwong.myprojects.fifteenpuzzle.solver.components.Board;
import mwong.myprojects.fifteenpuzzle.solver.components.Direction;
import mwong.myprojects.fifteenpuzzle.solver.components.PatternOptions;
import mwong.myprojects.fifteenpuzzle.solver.components.PuzzleDifficultyLevel;

import java.util.Scanner;

/**
 * AbstractApplication is the abstract class of console application that
 * has the following variables and methods.
 *
 * <p>Dependencies : Board.java, Direction.java, Solver.java, Stopwatch.java
 *
 * @author   Meisze Wong
 *           www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public abstract class AbstractApplication {
    private static int displayDelay;
    protected final int puzzleSize;
    protected final boolean tagLinearConflict;
    protected final boolean messageOn;
    protected final boolean messageOff;
    protected final boolean timeoutOn;
    protected final boolean timeoutOff;
    protected final boolean tagAdvanced;
    protected final boolean tagStandard;
    protected final PatternOptions defaultPattern;

    protected Scanner scanner;
    protected ReferenceAccumulator refAccumulator;
    protected int timeoutLimit;
    protected boolean flagAdvVersion;

    static {
        displayDelay = 1000;
        if (PropertiesCache.getInstance().containsKey("solutionDisplayRate")) {
            try {
                int delay = Integer.parseInt(PropertiesCache.getInstance().getProperty(
                        "solutionDisplayRate"));
                if (delay >= 100 || delay <= 5000) {
                    displayDelay = delay;
                } else {
                    System.err.println("Invalid solution display rate setting " + delay
                            + ", allow minimum 0.1 second (100) to maximum 5 seconds (5000) only."
                            + " Restore to system default 1 second.");
                }
            } catch (NumberFormatException ex) {
                System.err.println("Configuration solution display rate is not an iteger,"
                        + " restore to system default 1 second.");
            }
        }
    }

    protected AbstractApplication() {
        puzzleSize = ApplicationConstants.getPuzzleSize();
        tagLinearConflict = ApplicationConstants.isTagLinearConflict();
        messageOn = ApplicationConstants.isMessageOn();
        messageOff = !messageOn;
        timeoutOn = ApplicationConstants.isTimeoutOn();
        timeoutOff = !timeoutOn;
        tagAdvanced = ApplicationConstants.isTagAdvanced();
        tagStandard = !tagAdvanced;
        defaultPattern = SolverProperties.getPattern();

        scanner = new Scanner(System.in, "UTF-8");
        refAccumulator = new ReferenceAccumulator();
        timeoutLimit = SolverProperties.getTimeoutLimit();
        flagAdvVersion = tagStandard;
    }

    public abstract void run();

    /**
     * Print the minimum number of moves to the goal state.
     */
    void solutionSummary(Solver solver) {
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
    void solutionList(Solver solver) {
        int steps = solver.moves();
        for (int i = 1; i <= steps; i++) {
            System.out.printf("%2s : %-5s ", Integer.toString(i), solver.solution()[i]);
            if (i % 10 == 0 && steps > i) {
                System.out.println();
            }
        }
        System.out.println("\n");
    }

    /**
     * Print all boards of moves to the goal state.
     */
    void solutionDetail(Board board, Solver solver) {
        int count = 0;
        int steps = solver.moves();
        System.out.print("Step : " + (count));
        System.out.println();
        System.out.println(board);

        Direction dir = Direction.NONE;
        dir = solver.solution()[++count];
        board = board.shift(dir);

        try {
            Thread.sleep(displayDelay);
            while (count <= steps) {
                System.out.print("Step : " + (count));
                System.out.print("\t" + dir);
                System.out.println();
                System.out.println(board);
                if (++count <= steps) {
                    dir = solver.solution()[count];
                    board = board.shift(dir);
                    Thread.sleep(displayDelay);
                }
            }
        } catch (InterruptedException ex) {
            while (count <= steps) {
                System.out.print("Step : " + (count));
                System.out.print("\t" + dir);
                System.out.println();
                System.out.println(board);
                if (++count <= steps) {
                    dir = solver.solution()[count];
                    board = board.shift(dir);
                }
            }
        }
    }

    void printOption(char option) {
        switch (option) {
            case 's':
                System.out.println("Choose your heuristic functions:");
                System.out.println("Enter '1' for Manhattan Distance");
                System.out.println("      '2' for Manhattan Distance with Linear Conflict");
                System.out.println("      '3' for Walking Distance");
                System.out.println("      '4' for Walking Distance + Manhattan Distance with "
                        + "Linear Conflict");
                System.out.println("      '5' to 555 pattern + Walking Distance");
                System.out.println("      '6' to 663 pattern + Walking Distance");
                System.out.println("      '7' to 78 pattern (never timeout until solution found)");
                System.out.println("      '0' no change");
                return;
            case 'q':
                System.out.println("Enter 'Q' - quit the program");
                return;
            case 'm':
                System.out.println("      'L' - print a direction list of moves");
                System.out.println("      'D' - display each board of moves at "
                        + (displayDelay / 1000.0) + " board per second rate");
                return;
            case 'c':
                System.out.println("      'C' - for change your choice of heuristic function");
                return;
            case 'b':
                System.out.println("      'E' - Easy | 'M' - Moderate | 'H' - Hard | 'R' - Random");
                System.out.println("      16 numbers from 0 to 15 for the puzzle");
                return;
            case 't':
                System.out.println("      'T' - change timeout limit");
                return;
            default: return;
        }
    }

    void printOption(boolean flagAdvVersion) {
        if (flagAdvVersion) {
            System.out.println("      'V' - change initial heuristic estimate from Advanced "
                    + "to Standard version");
        } else {
            System.out.println("      'V' - change initial heuristic estimate from Standard "
                    + "to Advanced version");
        }
    }

    void printOption(Solver solver) {
        if (solver.isFlagTimeout()) {
            System.out.println("      'O' - turn timeout feature off");
        } else {
            System.out.println("      'O' - turn timeout feature on");
        }
    }

    Board keyInBoard() {
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

    Board createBoard(char choice) {
        switch (choice) {
            case 'E': case 'e':
                return (new Board(PuzzleDifficultyLevel.EASY));
            case 'M': case 'm':
                return (new Board(PuzzleDifficultyLevel.MODERATE));
            case 'H': case 'h':
                return (new Board(PuzzleDifficultyLevel.HARD));
            case 'R': case 'r':
                return (new Board());
            default: return null;
        }
    }

    void flipVersion(SmartSolver solver) {
        flagAdvVersion = !flagAdvVersion;
        solver.versionSwitch(flagAdvVersion);
        if (flagAdvVersion) {
            System.out.println("Changed from Standard version to Advanced version.");
        } else {
            System.out.println("Changed from Advanced version to Standard version.");
        }
    }

    void changeTimeout(Solver solver, int min, int max) {
        int limit = 0;
        while (limit < min || limit > max) {
            System.out.println("Enter timeout limit in seconds, minimun " + min + " seconds"
                    + " and maximum " + max + " seconds:");
            if (!scanner.hasNextInt()) {
                scanner.next();
            } else {
                limit = scanner.nextInt();
            }
        }
        timeoutLimit = limit;
        solver.setTimeoutLimit(timeoutLimit);
    }
}
