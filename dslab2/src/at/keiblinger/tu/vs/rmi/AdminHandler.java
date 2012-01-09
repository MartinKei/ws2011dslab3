package at.keiblinger.tu.vs.rmi;

import java.io.Serializable;
import java.rmi.RemoteException;

import at.keiblinger.tu.vs.manager.Admin;
import at.keiblinger.tu.vs.manager.User;
import at.keiblinger.tu.vs.shared.Message;
import at.keiblinger.tu.vs.shared.TaskType;

/**
 * @author Thomas Chrenko - 0728121
 *
 */
public class AdminHandler implements UserRMI, Serializable {

	private static final long serialVersionUID = 933453121831526865L;
	
	private Admin admin;
	
	public AdminHandler(User user) {
		this.admin = (Admin)user;
	}

	@Override
	public String logout() throws RemoteException {
		admin.getLock().lock();
		
		admin.setRemote(null);
		admin.setLoggedIn(false);
		
		admin.getLock().unlock();
		
		admin = null;
		
		return Message.LOGGED_OUT;
	}

	@Override
	public String exit() throws RemoteException {
		return logout();
	}

	@Override
	public String getPricingCurve() throws RemoteException {
		return admin.getPricingCurve();
	}

	@Override
	public String setPriceStep(Integer taskCount, Float percent) throws RemoteException {
		return admin.setPriceStep(taskCount, percent);
	}

	@Override
	public String mode() throws RemoteException {
		return "admin";
	}

	@Override
	public String credits() throws RemoteException {
		return notCompany();
	}

	@Override
	public String buy(int credits) throws RemoteException {
		return notCompany();
	}

	@Override
	public String prepare(String taskName, TaskType taskType, byte[] file) throws RemoteException {
		return notCompany();
	}

	@Override
	public String executeTask(int taskId, String startScript, NotificationRMI callback) throws RemoteException {
		return notCompany();
	}

	@Override
	public String info(int taskId) throws RemoteException {
		return notCompany();
	}

	@Override
	public String getOutput(int taskId) throws RemoteException {
		return notCompany();
	}
	
	private String notCompany() {
		return "Command not allowed. Your are not a company.";
	}
}
