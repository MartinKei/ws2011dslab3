package at.chrenko.tu.vs.scheduler;

import java.util.TimerTask;

import at.chrenko.tu.vs.shared.EngineState;

/**
 * @author Thomas Chrenko - 0728121
 *
 * CheckIsAliveTimer is a timertask for checking the availibility of an engine
 * 
 */
public class CheckIsAliveTimer extends TimerTask {

	private TaskEngine taskEngine;
	
	public CheckIsAliveTimer(TaskEngine taskEngine) {
		this.taskEngine = taskEngine;
	}
	
	@Override
	public void run() {
		checkIsAlive();
		return;
	}
	
	/**
	 * If engine is not suspended it checks if an alive packet arrived
	 * (alive packets set isAlive of the engine to true) since the last check.
	 * If not, set engine to offline, otherwise set the engine to available if it's offline.
	 */
	private  void checkIsAlive() {
		taskEngine.getLock().lock();
		
		if(taskEngine.getState().equals(EngineState.SUSPENDED)) {
			taskEngine.getLock().unlock();
			return;
		}
		
		if(!taskEngine.setIsAlive(false))
			taskEngine.setState(EngineState.OFFLINE);
		else
			if(taskEngine.getState().equals(EngineState.OFFLINE))
				taskEngine.setState(EngineState.AVAILABLE);
		
		taskEngine.getLock().unlock();
	}
}
