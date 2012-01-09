package at.keiblinger.tu.vs.manager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import at.keiblinger.tu.vs.rmi.LoginHandler;
import at.keiblinger.tu.vs.rmi.NotificationRMI;
import at.keiblinger.tu.vs.shared.FileTask;
import at.keiblinger.tu.vs.shared.InputHandler;

/**
 * @author Thomas Chrenko - 0728121
 *
 */
public class Manager implements Runnable {

	private PricingCurve pricingCurve = new PricingCurve();
	private AtomicBoolean stop = new AtomicBoolean(false);
	private Map<String, User> users;
	private AtomicInteger taskCounter = new AtomicInteger(0);
	private int preparationCosts;
	private String bindingName;
	private String schedulerHost;
	private int schedulerTCPPort;
	private File taskDir;
	private Socket schedulerSocket;
	private Lock lock;
	private SchedulerWorker schedulerWorker;
	private Lock executionLock;
	private Set<ExecutionWorker> executionWorkers;
	private AtomicBoolean acceptRequest = new AtomicBoolean(true);
	
	public PricingCurve getPricingCurve() {
		return pricingCurve;
	}
	
	public boolean getStop() {
		return stop.get();
	}
	
	public void setStop(boolean newValue) {
		stop.set(newValue);
	}
	
	public Collection<User> getUsers() {
		return users.values();
	}
	
	public boolean usersContains(String userName) {
		return users.containsKey(userName);
	}
	
	public User getUser(String userName) {
		return users.get(userName);
	}
	
	public int getNewTaskId() {
		return taskCounter.incrementAndGet();
	}
	
	public int getHighestTaskId() {
		return taskCounter.get();
	}
	
	public int getPreparationCosts() {
		return preparationCosts;
	}
	
	public Lock getLock() {
		return lock;
	}
	
	public Lock getExecutionLock() {
		return executionLock;
	}
	
	public boolean addExecutionWorker(ExecutionWorker worker) {
		return executionWorkers.add(worker);
	}
	
	public boolean removeExecutionWorker(ExecutionWorker worker) {
		return executionWorkers.remove(worker);
	}
	
	public String executeTask(Company company, FileTask task, NotificationRMI callback) {
		if(!acceptRequest.get())
			return "Manager is shutting down!";
			
		if(schedulerWorker == null) {
			try {
				if(schedulerSocket == null)
					schedulerSocket = new Socket(schedulerHost, schedulerTCPPort);
				
				schedulerWorker = new SchedulerWorker(schedulerSocket.getInputStream(), schedulerSocket.getOutputStream(), task, callback, company);
			} catch (UnknownHostException e) {
				return "Unknown host!";
			} catch (IOException e) {
				return "Error connecting to scheduler!";
			}
        	schedulerWorker.start();
		}
		else
		{
			schedulerWorker.setTask(task);
			schedulerWorker.setCallback(callback);
			schedulerWorker.setCompany(company);
		}
		
		schedulerWorker.requestEngine(task);
		
		while(true)
			try {
				synchronized(schedulerWorker) {
					schedulerWorker.wait();
				}
				return schedulerWorker.getMessage();
			} catch (InterruptedException e) {}
	}
	
	public Manager(String bindingName, String schedulerHost, int schedulerTCPPort, int preparationCosts, File taskDir) {
		this.bindingName = bindingName;
		this.schedulerHost = schedulerHost;
		this.schedulerTCPPort = schedulerTCPPort;
		this.preparationCosts = preparationCosts;
		this.taskDir = taskDir;
		
		this.lock = new ReentrantLock();
		this.schedulerSocket = null;
		this.schedulerWorker = null;
		this.executionWorkers = new HashSet<ExecutionWorker>();
		this.executionLock = new ReentrantLock();
	}
	
	@Override
	public void run() {
		Registry registry;
		int registryPort;
		
		// read registry properties and check if port is valid
		InputStream registryIn = ClassLoader.getSystemResourceAsStream("registry.properties");
		if(registryIn != null) {
			Properties registryProperties = new Properties();
			try {
				registryProperties.load(registryIn);
				registryPort = Integer.parseInt(registryProperties.getProperty("registry.port"));
			} catch (NumberFormatException e) {
				System.err.println(registryProperties.getProperty("registry.port") + " is no valid port!");
				return;
			} catch (IOException e) {
				System.err.println("Could not read the registry properties!");
				return;
			} finally {
				try {
					registryIn.close();
				} catch (IOException ignored) {}
			}
		} else {
			System.err.println("Registry properties file not found!");
			return;
		}
		
		if(registryPort < 0 || registryPort > 65535) {
			System.err.println(registryPort + " is no valid port!");
			return;
		}
		
		//read user properties
		InputStream userIn = ClassLoader.getSystemResourceAsStream("user.properties");
		if(userIn != null) {
			Properties userProperties = new Properties();
			
			try {
				userProperties.load(userIn);
				
				Set<String> properties = userProperties.stringPropertyNames();
				
				users = new HashMap<String, User>();
				
				for(String property : properties) {
					if(!property.endsWith(".admin") && !property.endsWith(".credits")) {
						String password = userProperties.getProperty(property);
						
						String value = (String) userProperties.get(property + ".admin");
						
						if(value == null) {
							System.err.println("Malformed user properties!");
							return;
						}
						
						boolean admin = Boolean.parseBoolean(value);
						
						if(admin) {
							Admin user = new Admin(this, property, password);
							users.put(property, user);
						} else {
							value = (String) userProperties.get(property + ".credits");
							
							if(value == null) {
								System.err.println("Malformed user properties!");
								return;
							}
							
							int credits = Integer.parseInt(value);
							
							Company company = new Company(this, property, password, credits);
							
							users.put(property, company);
						}
					}
				}
				users = Collections.unmodifiableMap(users);
			} catch (NumberFormatException e) {
				System.err.println("Malformed user properties (credits)!");
				return;
			} catch (IOException e) {
				System.err.println("Could not read the user properties!");
				return;
			} finally {
				try {
					userIn.close();
				} catch (IOException ignored) {}
			}
		} else {
			System.err.println("User properties file not found!");
			return;
		}
		
		//create registry
		try {
			registry = LocateRegistry.createRegistry(registryPort);
		} catch (RemoteException e) {
			System.err.println("Could not create registry!");
			return;
		}
		
		//create login remote object, bind it and export it
		LoginHandler lh = new LoginHandler(this);
		
		try {
			registry.bind(bindingName, lh);
			UnicastRemoteObject.exportObject(lh, 0);
		} catch (AccessException e1) {
			System.err.println("Binding error!");
			return;
		} catch (RemoteException e1) {
			System.err.println("Binding or Export error!");
			return;
		} catch (AlreadyBoundException e1) {
			System.err.println("Binding error!");
			return;
		}
		
		//input handler for user console input
		InputHandler ih = new ManagerConsoleInputHandler(this);
		
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
		
		acceptRequest.set(false);
		
		try {
			UnicastRemoteObject.unexportObject(lh, true);
		} catch (NoSuchObjectException e) {}
		
		System.out.println("stopping all executions...");
		
		// stop all executions, wait for thread to terminate and then close the connection to the scheduler
		executionLock.lock();
		for(ExecutionWorker worker : executionWorkers) {
			worker.sendAbort();
		}
		executionLock.unlock();
		
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {}
		
		for(User user : users.values()) {
			if(user.remote != null) {
				
				user.setLoggedIn(false);
				try {
					UnicastRemoteObject.unexportObject(user.remote, true);
				} catch (NoSuchObjectException e) {}
			}
		}
		
		if(schedulerSocket != null)
			try {
				schedulerSocket.close();
			} catch (IOException e) {}
		
		if(schedulerWorker != null) {
			while(schedulerWorker.isAlive())
				try {
					schedulerWorker.join();
				} catch (InterruptedException e) {}
		}
	}

}
