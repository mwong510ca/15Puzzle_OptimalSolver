package mwong.myprojects.fifteenpuzzle.server;

import mwong.myprojects.fifteenpuzzle.solver.ai.ReferenceAdapter;

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;

/**
 * DBRemoteServer creates a data server connection for the Data using RMI.
 */

public class ReferenceRemoteServer {
    public static Remote remote = null;

    /**
     * Create a reference server service using the specific port number and
     * the location of the data file.
     */
    public static void main(String[] args) {
        String remoteServiceName = ReferenceServerProperties.getRemoteServiceName();
        String remoteHost = ReferenceServerProperties.getRemoteHost();
        int remotePort = ReferenceServerProperties.getRemotePort();
        String lookupString = "rmi://" + remoteHost + ":" + remotePort + "/" + remoteServiceName;

        try {
            final ReferenceAdapter refServer = new ReferenceAdapter();
            remote = LocateRegistry.createRegistry(remotePort);
            Naming.rebind(lookupString, refServer);
        } catch (IOException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }
    }
}
