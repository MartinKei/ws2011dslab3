package at.chrenko.tu.vs.rmi;

import java.rmi.RemoteException;

public class NotificationHandler implements NotificationRMI {

	@Override
	public void notify(String message) throws RemoteException {
		System.out.println(message);
	}

}
