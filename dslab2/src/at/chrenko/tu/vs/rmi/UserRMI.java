/**
 * 
 */
package at.chrenko.tu.vs.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

import at.chrenko.tu.vs.shared.TaskType;

/**
 * @author Thomas Chrenko - 0728121
 *
 */
public interface UserRMI extends Remote {
	String logout() throws RemoteException;
	
	String exit() throws RemoteException;
	
	String mode() throws RemoteException;
	
	String getPricingCurve() throws RemoteException;
	
	String setPriceStep(Integer taskCount, Float percent) throws RemoteException;
	
	String credits() throws RemoteException;
	
	String buy(int credits) throws RemoteException;
	
	String prepare(String taskName, TaskType taskType, byte[] file) throws RemoteException;
	
	String executeTask(int taskId, String startScript, NotificationRMI callback) throws RemoteException;
	
	String info(int taskId) throws RemoteException;
	
	String getOutput(int taskId) throws RemoteException;
}
