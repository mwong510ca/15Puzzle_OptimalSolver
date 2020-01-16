package mwong.myprojects.fifteenpuzzle.solution;

import java.util.Scanner;

/**
 * An data type Stopwatch with start, stop and reset features.
 *
 * @author <a href="http://www.linkedin.com/pub/macy-wong/46/550/37b/"
 *            target="_blank">Meisze Wong (linkedin)</a>
 * @see <a href="http://www.github.com/mwong510ca/15PuzzleOptimalSolver/"
 *         target="_blank">GitHub (full project)</a>
 */
public class Stopwatch {
  /** Second to milisecond conversion. */
  private static final double SEC_TO_MS = 1000.0;
  /** The Scanner object. */
  private static Scanner scanner;
  /** The long value of start time. */
  private long startTime;
  /** The double value of duration of stop time. */
  private double stopTime;
  /** The boolean value represent the Sotpwatch in active. */
  private boolean active;

  /**
   * Initializes a new stop watch.
   */
  public Stopwatch() {
    reset();
    start();
  }

  /**
   * Reset the stop watch to 0.0s.
   */
  public void reset() {
    startTime = System.currentTimeMillis();
    stopTime = 0.0;
    active = false;
  }

  /**
   * Start the stop watch without reset.
   */
  public void start() {
    if (!active) {
      startTime = (long) (System.currentTimeMillis() - stopTime);
      active = true;
    }
  }

  /**
   * Stop the stop watch without reset.
   */
  public void stop() {
    if (active) {
      stopTime = System.currentTimeMillis() - startTime;
      active = false;
    }
  }

  /**
   * Returns the boolean of active status of stopwatch.
   *
   * @return boolean of active status of stopwatch
   */
  public boolean isActive() {
    return active;
  }

  /**
   * Returns the current time of stop watch.
   *
   * @return current time of stop watch in second
   */
  public double currentTime() {
    if (active) {
      long now = System.currentTimeMillis();
      return (now - startTime) / SEC_TO_MS;
    }
    return stopTime / SEC_TO_MS;
  }

  /**
   * Returns the boolean represent the active state of stop watch.
   *
   * @return boolean represent the active state of stop watch
   */
  protected boolean status() {
    return active;
  }

  /**
   * Returns the double value of duration of stop time.
   *
   * @return double value of duration of stop time
   */
  private double getStopTime() {
    return stopTime;
  }

  /**
   * Unit test.
   *
   * @param str Standard argument main function
   */
  public static void main(final String[] str) {
    Stopwatch stopwatch = new Stopwatch();
    scanner = new Scanner(System.in, "UTF-8");
    do {
      System.out.println("Enter s-start, t-stop, c-current time or r-reset: ");
      char value = scanner.next().charAt(0);
      if (value == 's') {
        stopwatch.start();
        System.out.println(stopwatch.startTime + " "
            + stopwatch.getStopTime() + " " + stopwatch.currentTime());
      } else if (value == 't') {
        stopwatch.stop();
        System.out.println(stopwatch.startTime + " "
            + stopwatch.getStopTime() + " " + stopwatch.currentTime());
      } else if (value == 'c') {
        System.out.println(stopwatch.startTime + " "
            + stopwatch.getStopTime() + " " + stopwatch.currentTime());
      } else if (value == 'r') {
        stopwatch.reset();
        System.out.println(stopwatch.startTime + " "
            + stopwatch.getStopTime() + " " + stopwatch.currentTime());
      }
    } while (true);
  }
}
