package at.keiblinger.tu.vs.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author Thomas Chrenko - 0728121
 *
 */
public interface LoginRMI extends Remote {

	UserRMI login(String name, String password) throws RemoteException;
}
