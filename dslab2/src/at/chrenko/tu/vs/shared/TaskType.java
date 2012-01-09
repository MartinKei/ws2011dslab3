package at.chrenko.tu.vs.shared;

/**
 * @author Thomas Chrenko - 0728121
 *
 * TaskType represents the possible types of a task and the load a task causes.
 *
 */
public enum TaskType {
	LOW(33),
	MIDDLE(66),
	HIGH(100);
	
	private final int load;
	
	private TaskType(int load) {
		this.load = load;
	}
	
	public int getLoad() {
		return load;
	}
}
