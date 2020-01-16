package mwong.myprojects.fifteenpuzzle.execution;

import java.io.IOException;
import java.net.BindException;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.ExportException;

import mwong.myprojects.fifteenpuzzle.server.ReferenceRemoteImpl;
import mwong.myprojects.fifteenpuzzle.server.ReferenceServerProperties;
import mwong.myprojects.fifteenpuzzle.solution.ai.ReferenceRemote;

/**
 * ReferenceRemoteServer starts the remote connection server.
 *
 * <p>Dependencies : ReferenceRemoteImpl.java, ReferenceServerProperties.java
 *                   ReferenceRemote.java
 *
 * @author <a href="http://www.linkedin.com/pub/macy-wong/46/550/37b/"
 *            target="_blank">Meisze Wong (linkedin)</a>
 * @see ReferenceServerProperties
 * @see <a href="http://www.github.com/mwong510ca/15PuzzleOptimalSolver/"
 *         target="_blank">GitHub (full project)</a>
 */
public final class ReferenceRemoteServer {
  /** private constructor, no instance. */
  private ReferenceRemoteServer() {
    // Not called
  }

  /**
   * Create a reference server service using the specific port number and
   * the location from ReferenceServerProperties class.
   *
   * @param args standard argument main function
   */
  public static void main(final String[] args) {
    String remoteHost = ReferenceServerProperties.getRemoteHost();
    int remotePort = ReferenceServerProperties.getRemotePort();
    String remoteServiceName = ReferenceServerProperties.getRemoteServiceName();
    String lookupString = "rmi://" + remoteHost + ":" + remotePort + "/" + remoteServiceName;

    boolean success = false;
    try {
      ReferenceRemote refServer = new ReferenceRemoteImpl();
      LocateRegistry.createRegistry(remotePort);
      Naming.rebind(lookupString, refServer);
      System.out.println("Reference server is running");
      success = true;
    } catch (BindException ex) {
      System.out.println("Port " + remotePort + " is not available.");
      System.out.println("System maintainence for reference collection. Try again later.");
    } catch (ExportException ex) {
      System.out.println(ex);
    } catch (IOException ex) {
      System.out.println("Check connection or use different port.");
    } finally {
      if (!success) {
        System.exit(0);
      }
    }
  }
}
