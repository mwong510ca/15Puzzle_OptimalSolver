package mwong.myprojects.fifteenpuzzle.server;

import mwong.myprojects.fifteenpuzzle.solver.ai.ReferenceRemote;

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.ExportException;

/**
 * DBRemoteServer creates a data server connection for the Data using RMI.
 */

public class ReferenceRemoteServer {
    /**
     * Create a reference server service using the specific port number and
     * the location of the data file.
     */
    public static void main(String[] args) throws RemoteException, IOException {    	
        String remoteHost = ReferenceServerProperties.getRemoteHost();
        int remotePort = ReferenceServerProperties.getRemotePort();
        String remoteServiceName = ReferenceServerProperties.getRemoteServiceName();
        String lookupString = "rmi://" + remoteHost + ":" + remotePort + "/" + remoteServiceName;

        try {
        	ReferenceRemote refServer = new ReferenceRemoteImpl();
        	LocateRegistry.createRegistry(remotePort);
        	Naming.rebind(lookupString, refServer);
        	System.out.println("Reference server is running");
        } catch (ExportException ex) {
        	System.out.println("Port " + remotePort + " is not available.");        	
        	System.out.println("System maintainence for reference collection.  Try again later.");        	
        } catch (IOException ex) {
        	System.out.println("Check connection or use different port.");        	
        }
    }
}
