package at.chrenko.tu.vs.engine;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import at.chrenko.tu.vs.shared.InputHandler;

/**
 * @author Thomas Chrenko - 0728121
 *
 * TaskEngine administers the timer for sending alive packets,
 * starts threads for udp and tcp listening, the user input from console
 * and the load of the engine
 * 
 */
public class TaskEngine implements Runnable {

	// sendIsAlive controls if alive packets are sent
	private AtomicBoolean stop = new AtomicBoolean(false);
	private String schedulerHost;
	private int minConsumption;
	private int maxConsumption;
	private int schedulerUDPPort;
	private int port;
	private AtomicBoolean sendIsAlive = new AtomicBoolean(false);
	private AtomicInteger load = new AtomicInteger(0);
	private File taskDir;
	
	private int alivePeriod;
	
	public boolean getStop() {
		return stop.get();
	}

	public boolean sendIsAlive() {
		return sendIsAlive.get();
	}
	
	public void setSendIsAlive(boolean sendIsAlive) {
		this.sendIsAlive.getAndSet(sendIsAlive);
	}
	
	public int getLoad() {
		return load.get();
	}
	
	/**
	 * Checks if the engine can handle the additional load
	 * 
	 * @param addend - The additional load
	 * @return The new load if the engine can handle the additional load and -1 otherwise
	 */
	public synchronized int getAndAddLoad(int addend) {
		int currentLoad = getLoad();
		
		currentLoad += addend;
		
		if(currentLoad > 100)
			return -1;
		
		return load.getAndAdd(addend);
	}
	
	public void setStop(boolean newValue) {
		stop.set(newValue);
	}
	
	public TaskEngine(int port, int alivePeriod, int schedulerUDPPort, int minConsumption,
						int maxConsumption, String schedulerHost, File taskDir) {
		this.port = port;
		this.alivePeriod = alivePeriod;
		this.schedulerUDPPort = schedulerUDPPort;
		this.minConsumption = minConsumption;
		this.maxConsumption = maxConsumption;
		this.schedulerHost = schedulerHost;
		this.taskDir = taskDir;
	}

	public void run() {
		setSendIsAlive(true);
		
		TCPListener tcpl = new TCPListener(this, port, taskDir);
		UDPListener udpl = new UDPListener(this, port);
		Thread tUdpl = new Thread(udpl);
		Timer timer = new Timer(true);
		
		tcpl.start();
		tUdpl.start();
		timer.schedule(new SendIsAliveTimer(this, schedulerHost, minConsumption, maxConsumption, schedulerUDPPort, port), 0, alivePeriod);
		
		InputHandler ih = new EngineConsoleInputHandler(this);
		
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		String consoleInput;
		
		// listens(as long nobody set stop) for user input from console and process it in the ConsoleInputHandler
		try {
			while (!getStop() && tcpl.isAlive() && tUdpl.isAlive() && (consoleInput = stdIn.readLine()) != null) {
			    ih.processInput(consoleInput);
			}
		} catch (IOException e) {
			System.out.println("An error occurred while reading from standard input!");
		}
		
		System.out.println("waiting for executions to finish...");
		// if listening threads are running close their connections => terminate
		// and cancel the timer
		timer.cancel();
		
		if(tcpl.isAlive())
			tcpl.closeConnection();
		
		if(tUdpl.isAlive())
			udpl.closeConnection();
	}

}
