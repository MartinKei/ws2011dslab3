package at.keiblinger.tu.vs.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface NotificationRMI extends Remote {

	public void notify(String message) throws RemoteException;
}
