/**
 * 
 */
package at.keiblinger.tu.vs.manager;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import at.keiblinger.tu.vs.rmi.AdminHandler;

/**
 * @author Thomas Chrenko - 0728121
 *
 */
public class Admin extends User {
	
	private static final long serialVersionUID = 2211415522611309162L;

	public Admin(Manager manager, String name, String password) {
		super(manager, name, password);
	}
	
	public String getPricingCurve() throws RemoteException {
		return manager.getPricingCurve().getPricingCurve();
	}
	
	public String setPriceStep(Integer taskCount, Float percent) throws RemoteException {
		return manager.getPricingCurve().setPriceStep(taskCount, percent);
	}

	@Override
	public void setRemote(User user) {
		if(user == null) {
			try {
				UnicastRemoteObject.unexportObject(remote, true);
			} catch (NoSuchObjectException e) {}
			remote = null;
		}
		else {
			remote = new AdminHandler(user);
			try {
				UnicastRemoteObject.exportObject(remote, 0);
			} catch (RemoteException e) {
				System.err.println(e.getMessage());
			}
		}
	}
}
