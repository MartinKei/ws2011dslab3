package at.chrenko.tu.vs.shared;

import java.io.Serializable;
import java.net.InetAddress;

/**
 * @author Thomas Chrenko - 0728121
 *
 * FileTask contains all the information needed for executing a task.
 *
 */
public class FileTask implements Serializable {

	private static final long serialVersionUID = -8082760177598401056L;
	
	private int id;

	private String fileName;
	
	private byte[] file;
	
	private String startScript;
	
	private TaskType taskType;
	
	private TaskState taskState;
	
	private int assignedPort;
	
	private InetAddress assignedAddress;
	
	private int costs;
	
	private StringBuffer output;
	
	private boolean paid;
	
	public int getId() {
		return id;
	}

	public String getFileName() {
		return fileName;
	}

	public byte[] getFile() {
		return file;
	}
	
	public String getStartScript() {
		return startScript;
	}
	
	public TaskType getTaskType() {
		return taskType;
	}
	
	public TaskState getTaskState() {
		return taskState;
	}
	
	public int getAssignedPort() {
		return assignedPort;
	}

	public InetAddress getAssignedAddress() {
		return assignedAddress;
	}
	
	public int getCosts() {
		return costs;
	}
	
	public String getOutput() {
		return output.toString().replaceFirst("\n", "");
	}
	
	public boolean isPaid() {
		return paid;
	}
	
	public void setStartScript(String startScript) {
		this.startScript = startScript;
	}
	
	public void setTaskState(TaskState taskState) {
		this.taskState = taskState;
	}
	
	public void setAssignedPort(int assignedPort) {
		this.assignedPort = assignedPort;
	}

	public void setAssignedAddress(InetAddress assignedAddress) {
		this.assignedAddress = assignedAddress;
	}
	
	public void setCosts(int costs) {
		this.costs = costs;
	}
	
	public void appendOutput(String output) {
		this.output.append("\n");
		this.output.append(output);
	}

	public void setPaid(boolean paid) {
		this.paid = paid;
	}
	
	public FileTask(int id, String filename, byte[] file, TaskType taskType, TaskState taskState) {
		this.id = id;
		this.fileName = filename;
		this.file = file;
		this.taskType= taskType;
		this.taskState = taskState;
		
		this.assignedPort = 0;
		this.assignedAddress = null;
		this.costs = 0;
		this.output = new StringBuffer();
		this.paid = false;
	}
}
