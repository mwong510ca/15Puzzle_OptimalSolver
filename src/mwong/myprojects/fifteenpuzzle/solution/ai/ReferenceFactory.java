package mwong.myprojects.fifteenpuzzle.solution.ai;

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import mwong.myprojects.fifteenpuzzle.PropertiesCache;
import mwong.myprojects.fifteenpuzzle.solution.ai.Reference.ConnectionType;

/**
 * ReferenceFactory provides the choice of network connection of the client applications.
 * It returns an instance of the reference collection. getReferenceLocal returns the local
 * instance and getReferenceServer returns the network instance.
 *
 * <p>Dependencies : PropertiesCache.java, Reference.java
 *
 * @author <a href="http://www.linkedin.com/pub/macy-wong/46/550/37b/"
 *            target="_blank">Meisze Wong (linkedin)</a>
 * @see <a href="http://www.github.com/mwong510ca/15PuzzleOptimalSolver/"
 *         target="_blank">GitHub (full project)</a>
 */
public class ReferenceFactory {
  /** The instance of ReferenceRemote object. */
  private ReferenceRemote refObj;

  /**
   * A local Reference connection will be returned.
   *
   * @return ReferenceRemote ReferenceRemote
   * @throws RemoteException RemoteException
   * @throws IOException IOException
   */
  public ReferenceRemote getReferenceLocal()
      throws RemoteException, IOException {
    refObj = new ReferenceAdapter(ConnectionType.STANDALONE);
    return refObj;
  }

  /**
   * A network Reference connection will be returned.
   *
   * @return ReferenceRemote ReferenceRemote
   * @throws RemoteException RemoteException
   * @throws IOException IOException
   */
  public ReferenceRemote getReferenceServer() throws RemoteException, IOException {
    String rmiServiceName
        = PropertiesCache.getInstance().getRemoteProperty(
            ReferenceConstants.getRemoteServiceFieldName());
    String rmiHost
        = PropertiesCache.getInstance().getRemoteProperty(
            ReferenceConstants.getRemoteHostFieldName());
    int rmiPort
        = Integer.parseInt(PropertiesCache.getInstance().getRemoteProperty(
            ReferenceConstants.getRemotePortFieldName()));
    String lookupString = "rmi://" + rmiHost + ":" + rmiPort + "/" + rmiServiceName;

    try {
      refObj = (ReferenceRemote) Naming.lookup(lookupString);
      return refObj;
    } catch (NotBoundException nbex) {
      throw new RemoteException(nbex.toString() + " " + lookupString);
    }
  }
}
