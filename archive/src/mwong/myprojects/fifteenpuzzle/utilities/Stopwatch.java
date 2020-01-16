package mwong.myprojects.fifteenpuzzle.utilities;

import java.util.Scanner;

/**
 * An immutable data type Stopwatch with start, stop and reset features
 *
 * @author Meisze Wong
 *         www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public class Stopwatch {
    private long startTime;
    private double stopTime;
    private boolean active;
    private static Scanner scanner;

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
     * Start the stop watch.
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
            return (now - startTime) / 1000.0;
        }
        return stopTime / 1000.0;
    }

    /**
     * Returns the boolean represent the active state of stop watch.
     *
     * @return boolean represent the active state of stop watch
     */
    protected boolean status() {
        return active;
    }

    private long getStartTime() {
        return startTime;
    }

    private double getStopTime() {
        return stopTime;
    }

    /**
     * Unit test.
     *
     * @param str Standard argument main function
     */
    public static void main(String[] str) {
        Stopwatch stopwatch = new Stopwatch();
        scanner = new Scanner(System.in, "UTF-8");
        do {
            System.out.println("Enter s-start, t-stop, c-current time or r-reset: ");
            char value = scanner.next().charAt(0);
            if (value == 's') {
                stopwatch.start();
                System.out.println(stopwatch.getStartTime() + " "
                        + stopwatch.getStopTime() + " " + stopwatch.currentTime());
            } else if (value == 't') {
                stopwatch.stop();
                System.out.println(stopwatch.getStartTime() + " "
                        + stopwatch.getStopTime() + " " + stopwatch.currentTime());
            } else if (value == 'c') {
                System.out.println(stopwatch.getStartTime() + " "
                        + stopwatch.getStopTime() + " " + stopwatch.currentTime());
            } else if (value == 'r') {
                stopwatch.reset();
                System.out.println(stopwatch.getStartTime() + " "
                        + stopwatch.getStopTime() + " " + stopwatch.currentTime());
            }
        } while (true);
    }
}
