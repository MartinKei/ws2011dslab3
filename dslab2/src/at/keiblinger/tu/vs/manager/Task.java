package at.keiblinger.tu.vs.manager;

import java.net.InetAddress;

import at.keiblinger.tu.vs.shared.TaskState;
import at.keiblinger.tu.vs.shared.TaskType;

/**
 * @author Thomas Chrenko - 0728121
 *
 * Task represents a task from the client
 *
 */
public class Task {

	private int id;
	
	private String name;
	
	private TaskType type;
	
	private TaskState state;
	
	private int assignedPort;
	
	private InetAddress assignedAddress;
	
	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public TaskType getType() {
		return type;
	}

	public TaskState getState() {
		return state;
	}

	public int getAssignedPort() {
		return assignedPort;
	}

	public InetAddress getAssignedAddress() {
		return assignedAddress;
	}

	public void setState(TaskState state) {
		this.state = state;
	}

	public void setAssignedPort(int assignedPort) {
		this.assignedPort = assignedPort;
	}

	public void setAssignedAddress(InetAddress assignedAddress) {
		this.assignedAddress = assignedAddress;
	}

	public Task(int id, String name, TaskType type, TaskState state) {
		this.id = id;
		this.name = name;
		this.type = type;
		this.state = state;
		this.assignedPort = 0;
		this.assignedAddress = null;
	}
}
