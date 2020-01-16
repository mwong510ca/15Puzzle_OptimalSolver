package mwong.myprojects.fifteenpuzzle.server;

import java.net.Socket;
import java.net.SocketException;

/**
 * ReferenceServerProperties contains the service name, host and port information
 * for the remote server.
 *
 * @author <a href="http://www.linkedin.com/pub/macy-wong/46/550/37b/"
 *            target="_blank">Meisze Wong (linkedin)</a>
 * @see <a href="http://www.github.com/mwong510ca/15PuzzleOptimalSolver/"
 *         target="_blank">GitHub (full project)</a>
 */
public final class ReferenceServerProperties {
  /** The string of remote host name. */
  private static final String REMOTE_HOST = "localhost";
  /** The string of remote service name. */
  private static final String REMOTE_SERVICE_NAME = "ReferenceService";
  /** The integer of remote port number to be use. */
  private static final int REMOTE_PORT = 1099;

  /** private constructor, no instance. */
  private ReferenceServerProperties() {
    // hide constructor
  }

  /**
   * Retrieve the service name of the server.
   *
   * @return A string value of database service name.
   */
  public static String getRemoteServiceName() {
    return REMOTE_SERVICE_NAME;
  }

  /**
   * Retrieve the default host name.
   *
   * @return A string value of the default host name.
   */
  public static String getRemoteHost() {
    return REMOTE_HOST;
  }

  /**
   * Retrieve the default port number.
   *
   * @return An integer value of the default port number.
   */
  public static int getRemotePort() {
    return REMOTE_PORT;
  }

  /**
   * Returns the boolean value if port in use.
   *
   * @return boolean value if port in use.
   */
  public static boolean isPortInUse() {
    // Assume port is available.
    boolean result = false;
    try {
      (new Socket(REMOTE_HOST, REMOTE_PORT)).close();
      // Successful connection means the port is taken.
      result = true;
    } catch (SocketException e) {
      // Could not connect.
    } catch (Exception ex) {
      System.err.println(ex);
    }
    return result;
  }
}
