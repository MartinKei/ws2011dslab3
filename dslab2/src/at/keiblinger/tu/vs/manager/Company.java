package at.keiblinger.tu.vs.manager;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

import at.keiblinger.tu.vs.rmi.CompanyHandler;
import at.keiblinger.tu.vs.rmi.NotificationRMI;
import at.keiblinger.tu.vs.shared.FileTask;
import at.keiblinger.tu.vs.shared.TaskState;
import at.keiblinger.tu.vs.shared.TaskType;

/**
 * @author Thomas Chrenko - 0728121
 *
 * Company represents a engine on the scheduler
 *
 */
public class Company extends User {

	private static final long serialVersionUID = -6311625145637614397L;

	private int countLow;
	
	private int countMiddle;
	
	private int countHigh;
	
	private int credits;
	
	private Map<Integer, FileTask> tasks;
	
	public int getCountLow() {
		return countLow;
	}
	
	public void incrementCountLow() {
		countLow++;
	}

	public int getCountMiddle() {
		return countMiddle;
	}
	
	public void incrementCountMiddle() {
		countMiddle++;
	}

	public int getCountHigh() {
		return countHigh;
	}
	
	public void incrementCountHigh() {
		countHigh++;
	}
	
	public int getCredits() {
		return credits;
	}
	
	public int addCredits(int credits) {
		return this.credits += credits;
	}
	
	public int getTaskCount() {
		return countLow + countMiddle + countHigh;
	}
	
	public Company(Manager manager, String name, String password, int credits) {
		super(manager, name, password);
		this.credits = credits;
		this.countLow = 0;
		this.countMiddle = 0;
		this.countHigh = 0;
		this.tasks = new HashMap<Integer, FileTask>();
	}
	
	/**
	 * 
	 * Adds a task to the task Map.
	 * 
	 * @param name - The name of the task
	 * @param type - The type of the task
	 * @return The Id of the created task
	 */
	public int addTask(String name, TaskType type, byte[] file) throws RemoteException {
		if(getCredits() < manager.getPreparationCosts()) {
			throw new RemoteException("Not enough credits to prepare a task.");
		}
		
		int taskId = manager.getNewTaskId();
		
		FileTask task = new FileTask(taskId, name, file, type, TaskState.PREPARED);
		
		tasks.put(taskId, task);
		
		addCredits(-manager.getPreparationCosts());
		
		return taskId;
	}
	
	public String taskInfo(int taskId) throws RemoteException{
		if(taskId > manager.getHighestTaskId())
			throw new RemoteException("Error: Task " + taskId + " does not exist.");
		
		FileTask task = tasks.get(taskId);
		
		if(task == null)
			throw new RemoteException("Error: Task " + taskId + " does not belong to your company.");
		
		StringBuilder result = new StringBuilder("Task ");
		result.append(task.getId());
		result.append(" (");
		result.append(task.getFileName());
		result.append(")\n");
		
		result.append("Type: ");
		result.append(task.getTaskType().toString());
		result.append("\n");
		
		result.append("Assigned engine: ");
		if(task.getAssignedPort() == 0)
			result.append("none\n");
		else {
			result.append(task.getAssignedAddress().getHostAddress());
			result.append(":");
			result.append(task.getAssignedPort());
			result.append("\n");
		}
		
		result.append("Status: ");
		TaskState state = task.getTaskState();
		result.append(state.toString().toLowerCase());
		result.append("\n");
		
		result.append("Costs: ");
		if(state.equals(TaskState.FINISHED)) {
			result.append(task.getCosts());
		} else {
			result.append("unknown");
		}
		
		return result.toString();
	}
	
	public String executeTask(int taskId, String startScript, NotificationRMI callback) throws RemoteException {
		if(taskId > manager.getHighestTaskId())
			throw new RemoteException("Error: Task " + taskId + " does not exist.");
		
		FileTask task = tasks.get(taskId);
		
		if(task == null)
			throw new RemoteException("Error: Task " + taskId + " does not belong to your company.");
		
		if(task.getTaskState().equals(TaskState.EXECUTING))
			throw new RemoteException("Error: Task " + taskId + " has not been finished yet.");
		
		manager.getLock().lock();
		
		task.setStartScript(startScript);

		String retValue = manager.executeTask(this, task, callback);
		
		if(retValue.startsWith("Error"))
			throw new RemoteException(retValue);
		
		manager.getLock().unlock();
		
		return retValue;
	}
	
	public String getOutput(int taskId) throws RemoteException {
		if(taskId > manager.getHighestTaskId())
			throw new RemoteException("Error: Task " + taskId + " does not exist.");
		
		FileTask task = tasks.get(taskId);
		
		if(task == null)
			throw new RemoteException("Error: Task " + taskId + " does not belong to your company.");
		
		if(task.getTaskState().equals(TaskState.PREPARED))
			throw new RemoteException("Error: Task " + taskId + " has not been started yet.");
		
		if(task.getTaskState().equals(TaskState.EXECUTING))
			throw new RemoteException("Error: Task " + taskId + " has not been finished yet.");
		
		if(!task.isPaid()) {
			if(credits < task.getCosts()) {
				throw new RemoteException("Error: You do not have enough credits to pay this execution. (Costs: " + task.getCosts() + " credits) Buy new credits for retrieving the output.");
			} else {
				addCredits(-task.getCosts());
				task.setPaid(true);
			}
		}
		
		return task.getOutput();
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
			remote = new CompanyHandler(user);
			try {
				UnicastRemoteObject.exportObject(remote, 0);
			} catch (RemoteException e) {
				System.err.println(e.getMessage());
			}
		}
	}
}
