package mwong.myprojects.fifteenpuzzle.solver.ai;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class DummyObject extends UnicastRemoteObject implements Remote {
    private static final long serialVersionUID = 17195273121L;

    public DummyObject() throws RemoteException {
    }
}
