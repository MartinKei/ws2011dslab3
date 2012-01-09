package at.chrenko.tu.vs.scheduler;

import java.net.InetAddress;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import at.chrenko.tu.vs.shared.EngineState;

/**
 * @author Thomas Chrenko - 0728121
 *
 * TaskEngine respresents an engine on the scheduler
 *
 */
public class TaskEngine {
	
	private InetAddress address;
	
	private int tcpPort;
	
	private int udpPort;
	
	private EngineState state;
	
	private int minConsumption;
	
	private int maxConsumption;
	
	private AtomicInteger load;
	
	private Lock lock;

	private Timer checkIsAliveTimer;
	
	private AtomicBoolean isAlive;
	
	public boolean setIsAlive(boolean isAlive) {
		return this.isAlive.getAndSet(isAlive);
	}
	
	public InetAddress getAddress() {
		return address;
	}

	public int getTcpPort() {
		return tcpPort;
	}

	public int getUdpPort() {
		return udpPort;
	}

	public synchronized EngineState getState() {
		return state;
	}

	public int getMinConsumption() {
		return minConsumption;
	}

	public int getMaxConsumption() {
		return maxConsumption;
	}

	public int getLoad() {
		return load.get();
	}
	
	public Timer getCheckIsAliveTimer() {
		return checkIsAliveTimer;
	}

	public synchronized void setState(EngineState state) {
		this.state = state;
	}

	public void setLoad(int newValue) {
		load.set(newValue);
	}
	
	public Lock getLock() {
		return lock;
	}
	
	public TaskEngine(InetAddress address, int tcpPort, int udpPort, int minConsumption, int maxConsumption) {
		this.address = address;
		this.tcpPort = tcpPort;
		this.udpPort = udpPort;
		this.minConsumption = minConsumption;
		this.maxConsumption = maxConsumption;
		this.state = EngineState.AVAILABLE;
		this.load =  new AtomicInteger(0);
		this.checkIsAliveTimer = new Timer(true);
		this.isAlive = new AtomicBoolean(false);
		this.lock = new ReentrantLock();
	}
}
