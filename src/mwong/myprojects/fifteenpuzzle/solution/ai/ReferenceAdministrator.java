package mwong.myprojects.fifteenpuzzle.solution.ai;

import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.util.Arrays;
import java.util.Scanner;

import mwong.myprojects.fifteenpuzzle.puzzle.Board;
import mwong.myprojects.fifteenpuzzle.server.ReferenceServerProperties;
import mwong.myprojects.fifteenpuzzle.solution.SolverPdb78;

/**
 * ReferenceAdministrator is the administrator tool to maintain the reference collection,
 * such as change cutoff setting, add or remove a board, clear the collection back to
 * default setting, etc.
 *
 * @author <a href="http://www.linkedin.com/pub/macy-wong/46/550/37b/"
 *            target="_blank">Meisze Wong (linkedin)</a>
 * @see <a href="http://www.github.com/mwong510ca/15PuzzleOptimalSolver/"
 *         target="_blank">GitHub (full project)</a>
 */
public final class ReferenceAdministrator {
  /** The Scanner instance. */
  private static Scanner scanner;

  /** Private constructor, no instance. */
  private ReferenceAdministrator() {
    // not called
  }

  /**
   * Display the menu choices of the administrator tool.
   *
   * @param recorder the instance of ReferenceRecorder
   * @param action the current action of add, add always, or remove
   */
  private static void menu(final ReferenceRecorder recorder, final Action action) {
    System.out.println("Enter 'Q' - Quit the program");
    System.out.println("      'V' - Veiw the current storage of archived boards.");
    System.out.println("      'P' - Print all archived boards");
    System.out.println("      'C' - Change cutoff time limit from "
        + recorder.getCutoffSetting() + "s to 1s to 10s:");
    System.out.println("      'U' - Update the current storage, review and add "
        + "all pending boards to storage.");
    System.out.println("      'F' - Refresh the data file.");
    System.out.println("      'S' - Reset to default setting.");
    System.out.println("            Warning: All accumulated boards will be removed.");
    if (action != Action.Remove) {
      System.out.println("      'R' - Remove a board if exists.");
    }
    if (action != Action.Add) {
      System.out.println("      'A' - Add a board if over "
          + recorder.getCutoffSetting() + "s to solve.");
    }
    if (action != Action.AddAlways) {
      System.out.println("      'B' - Add a board bypass time limit requirement.");
    }
    System.out.print("      16 numbers from 0 to 15 to ");
    if (action == Action.Remove) {
      System.out.println("remove a board if exists:\n");
    } else if (action == Action.Add) {
      System.out.println("add a board if over "
          + recorder.getCutoffSetting() + "s to solve:\n");
    } else if (action == Action.AddAlways) {
      System.out.println("add a board bypass time limit requirement:\n");
    }
  }

  /**
   *  A console application to manage the advanced accumulator storage.
   *  It allow to view the summary, print all board, change cutoff limit,
   *  update the data set, add or remove a board.
   *
   *  @param args standard argument main function
   *  @throws IOException any IOException
   */
  public static void main(final String[] args) throws IOException {
    if (ReferenceServerProperties.isPortInUse()) {
      System.out.println("Remote server is running, system exit.");
      System.exit(0);
    } else {
      int remotePort = ReferenceServerProperties.getRemotePort();
      LocateRegistry.createRegistry(remotePort);
      System.out.println("Port disabled for system update.\n");
    }

    ReferenceRecorder recorder = new ReferenceRecorder(true);
    int sizePuzzle = ReferenceConstants.getPuzzleSize();
    SolverPdb78 solver = recorder.getSolver78();

    recorder.printStatus();

    boolean bypass = true;
    Action action = Action.Add;

    scanner = new Scanner(System.in, "UTF-8");
    int count = -1;

    while (true) {
      menu(recorder, action);

      do {
        if (scanner.hasNextInt()) {
          break;
        }

        char value = scanner.next().toUpperCase().charAt(0);
        if (value == 'Q' || value == 'q') {
          System.out.println("Goodbye!\n");
          recorder.updateData();
          System.exit(0);
        }

        if (value == 'V' || value == 'v') {
          recorder.printStatus();
          continue;
        }
        if (value == 'P' || value == 'p') {
          recorder.printAllBoards();
          continue;
        }
        if (value == 'C' || value == 'c') {
          System.out.println("Enter new limit from 1 to 10 seconds:");
          do {
            if (scanner.hasNextInt()) {
              recorder.setCutoffArchive(scanner.nextInt());
              break;
            }
          } while (true);
        }
        if (value == 'U' || value == 'u') {
          recorder.updateData();
          System.out.println("Complteted.  Storage is up to date.");
        }
        if (value == 'F' || value == 'f') {
          recorder.refreshFile();
        }
        if (value == 'S' || value == 's') {
          recorder.reset();
        }
        if (value == 'R' || value == 'r') {
          action = Action.Remove;
          count = -1;
        }
        if (value == 'A' || value == 'a') {
          action = Action.Add;
          count = -1;
        }
        if (value == 'B' || value == 'b') {
          action = Action.AddAlways;
          count = -1;
        }
      } while (true);

      do {
        byte[] blocks = new byte[sizePuzzle];
        boolean[] used = new boolean[sizePuzzle];
        int idx = 0;
        while (idx < sizePuzzle) {
          if (!scanner.hasNextInt()) {
            scanner.next();
          } else {
            int value = scanner.nextInt();
            if (value < 0 || value >= sizePuzzle) {
              System.out.println("Invalid number " + value + ", try again.");
            } else if (used[value]) {
              System.out.println(value + " already entered, try again.");
            } else {
              blocks[idx++] = (byte) value;
              used[value] = true;
            }
          }
        }

        if (count == -1) {
          System.out.println();
          count++;
        }

        Board initial = new Board(blocks);

        if (initial.isSolvable()) {
          if (action == Action.Remove) {
            ++count;
            ReferenceBoard advBoard = new ReferenceBoard(initial);
            if (!recorder.getActiveMap().containsKey(advBoard)) {
              System.out.println(count + " : \t\tNo changed, not in system");
            } else {
              System.out.println(count + " : \t\tRemoved");
              recorder.removeBoard(initial);
            }
          } else {
            solver.findOptimalPath(initial);
            System.out.println(++count + " " + solver.searchTime() + "\t");
            if (action == Action.AddAlways) {
              recorder.addBoard(solver, bypass);
            }
            if (recorder.updateLastSearch(solver)) {
              solver.findOptimalPath(initial);
              System.out.println("\t\t" + solver.searchTime());
            } else {
              System.out.println("\t\t\t\tskip " + solver.heuristicBasis(initial)
                  + " " + solver.heuristicBoost(initial));
            }
          }
        } else {
          System.out.println(Arrays.toString(initial.getTiles()));
          System.out.println("unsolvable");
        }

        System.out.println("Please 16 numbers (0 - 15) to " + action
                        + ", or any characters return to main menu:");
      } while (scanner.hasNextInt());
      scanner.next();
    }
  }

  /** The enum type of action. */
  private enum Action {
    /** Add a board without checking the total time to solve. */
    AddAlways,
    /** Add a board if it takes more than preset limit to solve. */
    Add,
    /** Remove a board. */
    Remove;

    @Override
    public String toString() {
      if (this == Remove) {
        return "remove";
      } else {
        return "add";
      }
    }
  }
}
