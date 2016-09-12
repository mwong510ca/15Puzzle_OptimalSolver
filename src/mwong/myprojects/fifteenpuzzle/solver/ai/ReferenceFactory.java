package mwong.myprojects.fifteenpuzzle.solver.ai;

import mwong.myprojects.fifteenpuzzle.server.ReferenceServerProperties;

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

/**
 * ReferenceFactory provides the choice of network connection of the client applications.
 * It returns an instance of the reference collection.  getReferenceLocal returns the local
 * instance and getReferenceServer returns the network instance.
 *
 * @author Meisze Wong
 *         www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public class ReferenceFactory {
    private ReferenceRemote refObj;

    /**
     * A local Reference connection will be returned.
     */
    public ReferenceRemote getReferenceLocal()
            throws RemoteException, IOException {
        refObj = new ReferenceAdapter();
        return refObj;
    }

    /**
     * A network Reference connection will be returned.
     */
    public ReferenceRemote getReferenceServer() throws RemoteException, IOException {
        String rmiServiceName = ReferenceServerProperties.getRemoteServiceName();
        String rmiHost = ReferenceServerProperties.getRemoteHost();
        int rmiPort = ReferenceServerProperties.getRemotePort();
        String lookupString = "rmi://" + rmiHost + ":" + rmiPort + "/" + rmiServiceName;

        try {
            refObj = (ReferenceRemote) Naming.lookup(lookupString);
            return refObj;
        } catch (NotBoundException nbex) {
            throw new RemoteException(nbex.toString());
        }
    }
}
