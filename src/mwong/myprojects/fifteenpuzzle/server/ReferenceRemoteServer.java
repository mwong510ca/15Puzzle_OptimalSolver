package mwong.myprojects.fifteenpuzzle.server;

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import mwong.myprojects.fifteenpuzzle.solver.ai.ReferenceAdapter;
import mwong.myprojects.fifteenpuzzle.solver.ai.ReferenceRemote;

/**
 * DBRemoteServer creates a data server connection for the Data using RMI.
 */

public class ReferenceRemoteServer {
	public static Remote remote = null;
	 
    /**
     * Create a data server service using the specific port number and 
     * the location of the data file.
     *
     * @param port An integer value of the port number.
     * @param dbPath A string value of the location of the data file.
     * @throws RemoteException Indicates remote exception when creating
     * the server connection.
     * @throws IOException Indicates an error when trying to create the
     * object for RMI service.
     */
    public static void main(String[] args) {
    	String rmiHost = ReferenceServerProperties.getDefaultHost();
    	int rmiPort = ReferenceServerProperties.getDefaultPort();
    	String rmiServiceName = ReferenceServerProperties.getDBServiceName();
    	String lookupString = "rmi://" + rmiHost + ":" + rmiPort + "/" + rmiServiceName; 
    	
		try {
			final ReferenceAdapter refServer = new ReferenceAdapter();
			//注册通讯端口
			remote = LocateRegistry.createRegistry(1099);
			//注册通讯路径
			Naming.rebind(lookupString, refServer);    // 客户端和服务端均在本地
//			System.out.println("server is running");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
