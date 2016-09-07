package mwong.myprojects.fifteenpuzzle.solver.ai;

import mwong.myprojects.fifteenpuzzle.server.ReferenceServerProperties;

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class ReferenceFactory {
    private ReferenceRemote refObj;

    public ReferenceRemote getDBLocal()
            throws RemoteException, IOException {
        refObj = new ReferenceAdapter();
        return refObj;
    }

    public ReferenceRemote getDBServer()
            throws RemoteException, IOException {
    	String rmiHost = ReferenceServerProperties.getDefaultHost();
    	int rmiPort = ReferenceServerProperties.getDefaultPort();
    	String rmiServiceName = ReferenceServerProperties.getDBServiceName();
    	String lookupString = "rmi://" + rmiHost + ":" + rmiPort + "/" + rmiServiceName; 
    	
        try {
            refObj = (ReferenceRemote) Naming.lookup(lookupString);
            return refObj;
        } catch (NotBoundException nbex) {
            throw new RemoteException(nbex.toString());
        }
    }
}
