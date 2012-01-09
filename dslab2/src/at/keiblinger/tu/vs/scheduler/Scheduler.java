package at.keiblinger.tu.vs.scheduler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

import at.keiblinger.tu.vs.shared.InputHandler;

/**
 * @author Thomas Chrenko - 0728121
 *
 * Scheduler administers the timer for adjusting the engines,
 * starts threads for udp and tcp listening, the user input from console
 * and the engines.
 *
 */
public class Scheduler implements Runnable {

	private AtomicBoolean stop = new AtomicBoolean(false);
	private ConcurrentMap<InetSocketAddress, TaskEngine> taskEngines = new ConcurrentHashMap<InetSocketAddress, TaskEngine>();
	private int timeout;
	private int min;
	private int max;
	private int tcpPort;
	private int udpPort;
	private int checkPeriod;
	
	public TaskEngine getTaskEngine(InetSocketAddress key) {
		return taskEngines.get(key);
	}
	
	public Collection<TaskEngine> getTaskEngines() {
		return taskEngines.values();
	}
	
	public TaskEngine putTaskEngine(InetSocketAddress key, TaskEngine value) {
		return taskEngines.putIfAbsent(key, value);
	}
	
	public boolean getStop() {
		return stop.get();
	}
	
	public void setStop(boolean newValue) {
		stop.set(newValue);
	}
	
	public Scheduler(int tcpPort, int udpPort, int min, int max, int timeout, int checkPeriod) {
		this.tcpPort = tcpPort;
		this.udpPort = udpPort;
		this.min = min;
		this.max = max;
		this.timeout = timeout;
		this.checkPeriod = checkPeriod;
	}
	
	public void run() {
		
		TCPListener cl = new TCPListener(this, tcpPort);
		EngineListener el = new EngineListener(this, udpPort, timeout);
		Timer timer = new Timer(true);
		
		cl.start();
		el.start();
		timer.schedule(new AdjustEnginesTimer(this, min, max), 0, checkPeriod);

		InputHandler ih = new SchedulerConsoleInputHandler(this);
		
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		String consoleInput;
		
		// listens(as long nobody set stop) for user input from console and process it in the ConsoleInputHandler
		try {
			while (!getStop() && (consoleInput = stdIn.readLine()) != null) {
			    ih.processInput(consoleInput);
			}
		} catch (IOException e) {
			System.out.println("An error occurred while reading from standard input!");
		}
		
		System.out.println("waiting for timers to stop...");
		// if listening threads are running close their connections => terminate
		// and cancel the timer
		timer.cancel();
		
		for(TaskEngine engine : getTaskEngines()) {
			engine.getCheckIsAliveTimer().cancel();
		}
		
		if(cl.isAlive())
			cl.closeConnection();
		
		if(el.isAlive())
			el.closeConnection();
	}

}
