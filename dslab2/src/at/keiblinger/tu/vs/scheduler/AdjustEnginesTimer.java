package at.keiblinger.tu.vs.scheduler;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.TimerTask;

import at.keiblinger.tu.vs.shared.Command;
import at.keiblinger.tu.vs.shared.EngineState;

/**
 * @author Thomas Chrenko - 0728121
 *
 * AdjustEnginesTimer is a timertask for administer the engines(suspend, activate,...)
 *
 */
public class AdjustEnginesTimer extends TimerTask {

	private Scheduler scheduler;
	private int min;
	private int max;
	
	public AdjustEnginesTimer(Scheduler scheduler, int min, int max) {
		this.scheduler = scheduler;
		this.min = min;
		this.max = max;
	}
	
	@Override
	public void run() {
		adjustEngine();
		return;
	}

	/**
	 * Suspends or activates engines regarding engine information.
	 */
	private void adjustEngine() {
		ArrayList<TaskEngine> availableEngines = new ArrayList<TaskEngine>();
		ArrayList<TaskEngine> suspendedEngines = new ArrayList<TaskEngine>();
		
		// get all available and suspended engines, offline engines are not of interest
		for(TaskEngine engine : scheduler.getTaskEngines()) {
			switch(engine.getState()) {
				case AVAILABLE:
					availableEngines.add(engine);
					break;
				case SUSPENDED:
					suspendedEngines.add(engine);
				break;
				default:
					break;
			}
		}
		
		//if there are less available engines than max and
		//we have suspended engines it's possible that we have to activate some of them
		if(availableEngines.size() < max && suspendedEngines.size() > 0)
		{
			ArrayList<TaskEngine> toActivate;
			//check how many engines should be activated to achieve the min goal
			int activate = min - availableEngines.size();
			
			if(activate > 0) {
				// if we have to activate some => get the engines with the lowest consumption
				toActivate = getBestActivateEngines(suspendedEngines, activate);
				if(toActivate != null)
					sendActivate(toActivate);
				return;
			}
			
			boolean loadSmaller66 = false;
			// check if there is an active engine with load < 66
			for(TaskEngine engine : availableEngines)
				if(engine.getLoad() < 66) {
					loadSmaller66 = true;
					break;
				}
			// if there is no engine with load < 66 get one for activation
			if(!loadSmaller66) {
				toActivate = getBestActivateEngines(suspendedEngines, 1);
				if(toActivate != null)
					sendActivate(toActivate);
				return;
			}
		}
		//reach this code only if there are no engines to activate => check suspend
		
		// if there are more available engines than min we have to check is we should suspend some
		if(availableEngines.size() > min) {
			ArrayList<TaskEngine> toSuspend = new ArrayList<TaskEngine>();
			
			// get engines with load 0
			for(TaskEngine engine : availableEngines)
				if(engine.getLoad() == 0)
					toSuspend.add(engine);
			
			//if there are 2 or more engines with load 0 suspend the one with most load
			if(toSuspend.size() >= 2) {
				sendSuspend(getBestSuspendEngine(toSuspend));
				return;
			}
		}
	}
	
	/**
	 * Gets the best engines to activate regarding their energy signature
	 * 
	 * @param availableEngines - List of available Engines for activation
	 * @param activate - Amount of engines to activate
	 * @return List of the best engines to activate
	 */
	private ArrayList<TaskEngine> getBestActivateEngines(ArrayList<TaskEngine> availableEngines, int activate) {
		ArrayList<TaskEngine> retValue = new ArrayList<TaskEngine>();
		
		for(int i=0;i<activate;i++) {
			TaskEngine toAdd = getBestActivateEngine(availableEngines);
			
			if(toAdd != null) {
				retValue.add(toAdd);
				availableEngines.remove(toAdd);
			}
			else
				break;
		}
		
		if(retValue.size() == 0)
			return null;
		
		return retValue;
	}
	
	/**
	 * Gets the best engine to activate regarding their energy consumption
	 * 
	 * @param availableEngines - List of available engines for activation
	 * @return The best engine to activate
	 */
	private TaskEngine getBestActivateEngine(ArrayList<TaskEngine> availableEngines) {
		TaskEngine retValue = null;
		
		for(TaskEngine engine : availableEngines) {
			if(retValue == null) {
				retValue = engine;
			}
			else if(engine.getMaxConsumption() < retValue.getMaxConsumption()) {
				retValue = engine;
			}
			else if(engine.getMaxConsumption() == retValue.getMaxConsumption()) {
				if(engine.getMinConsumption() < retValue.getMinConsumption())
					retValue = engine;
			}
		}
		
		return retValue;
	}
	
	/**
	 * Gets the best engine to suspend regarding their energy consumption
	 * 
	 * @param toSuspend - List of available engines for suspending
	 * @return The best engine to suspend
	 */
	private TaskEngine getBestSuspendEngine(ArrayList<TaskEngine> toSuspend) {
		TaskEngine retValue = null;
		
		for(TaskEngine engine : toSuspend) {
			if(retValue == null) {
				retValue = engine;
			}
			else if(engine.getMaxConsumption() > retValue.getMaxConsumption()) {
				retValue = engine;
			}
			else if(engine.getMaxConsumption() == retValue.getMaxConsumption()) {
				if(engine.getMinConsumption() > retValue.getMinConsumption())
					retValue = engine;
			}
		}
		
		return retValue;
	}
	
	/**
	 * Sends the activate udp datagram to the engines
	 * 
	 * @param toActivate - The engines which get the activate datagram
	 */
	private void sendActivate(ArrayList<TaskEngine> toActivate) {
		for(TaskEngine engine : toActivate) {
			try {
				DatagramSocket socket = new DatagramSocket();
				
				byte[] buf = new byte[256];
				
				buf = Command.ACTIVATE.getBytes();
				
				DatagramPacket packet = new DatagramPacket(buf, buf.length, engine.getAddress(), engine.getUdpPort());
				
				socket.send(packet);
			}
			catch (SocketException e) {}
			catch (IOException e) {}
			
			engine.getLock().lock();
			
			engine.setState(EngineState.OFFLINE);
			
			engine.getLock().unlock();
		}
	}
	
	/**
	 * Sends the suspend udp datagram to the engine
	 * 
	 * @param toSuspend - The engine which get the suspend datagram
	 */
	private void sendSuspend(TaskEngine toSuspend) {
		try {
			DatagramSocket socket = new DatagramSocket();
			
			byte[] buf = new byte[256];
			
			buf = Command.SUSPEND.getBytes();
			
			DatagramPacket packet = new DatagramPacket(buf, buf.length, toSuspend.getAddress(), toSuspend.getUdpPort());
			
			socket.send(packet);
		}
		catch (SocketException e) {}
		catch (IOException e) {}
		
		toSuspend.getLock().lock();
		
		toSuspend.setState(EngineState.SUSPENDED);
		
		toSuspend.getLock().unlock();
	}
}
