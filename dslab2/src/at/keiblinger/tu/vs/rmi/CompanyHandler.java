package at.keiblinger.tu.vs.rmi;

import java.io.Serializable;
import java.rmi.RemoteException;

import at.keiblinger.tu.vs.manager.Company;
import at.keiblinger.tu.vs.manager.User;
import at.keiblinger.tu.vs.shared.Message;
import at.keiblinger.tu.vs.shared.TaskType;

/**
 * @author Thomas Chrenko - 0728121
 *
 */
public class CompanyHandler implements UserRMI, Serializable {

	private static final long serialVersionUID = -7038633446772543742L;

	private Company company;
	
	public CompanyHandler(User user) {
		this.company = (Company)user;
	}

	@Override
	public String logout() throws RemoteException {
		company.getLock().lock();
		
		company.setLoggedIn(false);
		company.setRemote(null);
		
		company.getLock().unlock();
		
		return Message.LOGGED_OUT;
	}

	@Override
	public String exit() throws RemoteException {
		return logout();
	}

	@Override
	public String mode() throws RemoteException {
		return "company";
	}

	@Override
	public String getPricingCurve() throws RemoteException {
		return notAdmin();
	}

	@Override
	public String setPriceStep(Integer taskCount, Float percent) throws RemoteException {
		return notAdmin();
	}
	
	@Override
	public String credits() throws RemoteException {
		company.getLock().lock();
		
		String credit = "You have " + company.getCredits() + " credits left.";
		
		company.getLock().unlock();
		
		return credit;
	}

	@Override
	public String buy(int credits) throws RemoteException {
		if(credits < 0)
			throw new RemoteException("Error: Invalid amount of credits.");
		
		company.getLock().lock();
		
		int credit = company.addCredits(credits);
		
		company.getLock().lock();
		
		return "Successfully bought credits. You have " + credit + " credits left.";
	}

	@Override
	public String prepare(String taskName, TaskType taskType, byte[] file) throws RemoteException {
		company.getLock().lock();
		
		int taskId;
		try {
			taskId = company.addTask(taskName, taskType, file);
		} catch (RemoteException e) {
			throw e;
		} finally {
			company.getLock().unlock();
		}
		
		return "Task with id " + taskId + " prepared.";
	}

	@Override
	public String executeTask(int taskId, String startScript, NotificationRMI callback) throws RemoteException {
		company.getLock().lock();
		
		String retValue = null;
		try {
			retValue = company.executeTask(taskId, startScript, callback);
		} catch (RemoteException e) {
			throw e;
		} finally {
			company.getLock().unlock();
		}
		
		return retValue;
	}

	@Override
	public String info(int taskId) throws RemoteException {
		company.getLock().lock();
		
		String retValue = null;
		try {
			retValue = company.taskInfo(taskId);
		} catch (RemoteException e) {
			throw e;
		} finally {
			company.getLock().unlock();
		}
		
		return retValue;
	}

	@Override
	public String getOutput(int taskId) throws RemoteException {
		company.getLock().lock();
		
		String retValue = null;
		try {
			retValue = company.getOutput(taskId);
		} catch (RemoteException e) {
			throw e;
		} finally {
			company.getLock().unlock();
		}
		
		return retValue;
	}
	
	private String notAdmin() {
		return "Command not allowed. Your are not a admin.";
	}
}
