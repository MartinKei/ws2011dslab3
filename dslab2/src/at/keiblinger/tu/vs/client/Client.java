package at.keiblinger.tu.vs.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import at.keiblinger.tu.vs.rmi.LoginRMI;
import at.keiblinger.tu.vs.shared.InputHandler;

/**
 * @author Thomas Chrenko - 0728121
 *
 * Client administers the tasks, in some way(creating and closing) the connection to the scheduler
 * and the user input from console.
 * 
 */
public class Client implements Runnable {

	// tasks holds the tasks from the client, taskCounter is used for generating keys for the map,
	// tasksLock is for editing or adding tasks, executionLock is for editing executionWorkers
	private AtomicBoolean stop = new AtomicBoolean(false);
	
	public boolean getStop() {
		return stop.get();
	}
	
	public void setStop(boolean newValue) {
		stop.set(newValue);
	}
	
	private String managementComponent;
	private File taskDir;
	
	public Client(String managementComponent, File taskDir) {
		this.managementComponent = managementComponent;
		this.taskDir = taskDir;
	}
	
	public void run() {	
		int registryPort;
		String registryHost;
		
		// read registry properties and check if port is valid
		InputStream registryIn = ClassLoader.getSystemResourceAsStream("registry.properties");
		if(registryIn != null) {
			Properties registryProperties = new Properties();
			try {
				registryProperties.load(registryIn);
				registryPort = Integer.parseInt(registryProperties.getProperty("registry.port"));
				registryHost = registryProperties.getProperty("registry.host");
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
		
		if(registryHost == null || registryHost.equals("")) {
			System.err.println("No valid registry host!");
			return;
		}
		
		Registry registry;
		
		LoginRMI loginRemote;
		try {
			registry = LocateRegistry.getRegistry(registryHost, registryPort);
			loginRemote = (LoginRMI) registry.lookup(managementComponent);
		} catch (AccessException e1) {
			System.err.println("Error getting registry or login object!");
			return;
		} catch (RemoteException e1) {
			System.err.println("Error getting registry or login object!");
			return;
		} catch (NotBoundException e1) {
			System.err.println("Error getting registry or login object!");
			return;
		}
		
		// Create the connection to the scheduler, the ConsoleInputHandler and start the SchedulerWorker
		InputHandler ih = new ClientConsoleInputHandler(this, loginRemote, taskDir);
		
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		String userInput;
		
		// listens(as long nobody set stop) for user input from console and process it in the ConsoleInputHandler
		try {
			while (!getStop() && (userInput = stdIn.readLine()) != null) {
				ih.processInput(userInput);
			}
		} catch (IOException e) {
			System.out.println("An error occurred while reading from standard input!");
		}
		
		System.out.println("Bye");
	}
}
