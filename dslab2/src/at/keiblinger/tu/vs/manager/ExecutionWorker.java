package at.keiblinger.tu.vs.manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.Locale;
import java.util.Scanner;

import at.keiblinger.tu.vs.rmi.NotificationRMI;
import at.keiblinger.tu.vs.shared.Command;
import at.keiblinger.tu.vs.shared.FileTask;
import at.keiblinger.tu.vs.shared.InputHandler;
import at.keiblinger.tu.vs.shared.TaskState;

/**
 * @author Thomas Chrenko - 0728121
 *
 * ExecutionWorker administers the connection to an engine,
 * containing requests, object serialization and printing input
 *
 */
public class ExecutionWorker extends Thread implements InputHandler {

	private FileTask task;
	private NotificationRMI callback;
	private Company company;
	private String message;
	
	private boolean startedExecution = false;
	
	private Socket socket;
	private BufferedReader socketIn;
	private ObjectOutputStream socketOutStream;
	private PrintWriter socketOutWriter;
	
	public String getMessage() {
		return message;
	}
	
	public ExecutionWorker(FileTask task, NotificationRMI callback, Company company) {
		this.task = task;
		this.callback = callback;
		this.company = company;
	}
	
	@Override
	public void run() {
		
		handleExecute();
	}
	
	/**
	 * Creates the FileTask which gets send to the engine, creates a connection to the engine,
	 * sends a request and then waits for the answer
	 * 
	 */
	private void handleExecute() {
		try {
			socket = new Socket(task.getAssignedAddress(), task.getAssignedPort());
			
			socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			socketOutWriter = new PrintWriter(socket.getOutputStream(), true);
			socketOutWriter.println(Command.EXECUTETASK + " " + task.getTaskType());
			
			String inputLine;
			
			inputLine = socketIn.readLine();
			
			if(inputLine != null)
				processInput(inputLine);
			else {
				message = "Error: Engine is not ready!";
				notifySchedulerWorker();
				return;
			}
		} catch (IOException e) {
			message = "Error: " + e.getMessage();
			notifySchedulerWorker();
			return;
		} finally {
			closeConnection();
		}
	}
	
	@Override
	public void processInput(String input) {
		if(input.startsWith(Command.FINISH)) {
			handleFinish(input);
		}
		else if(input.startsWith(Command.TOOBUSY)) {
			handleTooBusy(input);
		}
		else if(input.startsWith(Command.READY)) {
			handleTransfer();
		}
		else {
			//first process output received from the engine => set state to executing and print started...
			if(!startedExecution) {
				task.setTaskState(TaskState.EXECUTING);
				
				company.manager.getExecutionLock().lock();
				company.manager.addExecutionWorker(this);
				company.manager.getExecutionLock().unlock();
				
				message = "Execution for task " + task.getId() + " started.";
				notifySchedulerWorker();
				startedExecution = true;
			}
			task.appendOutput(input);
		}
	}
	
	/**
	 * If engine is ready for the FileTask serialize it, send it and wait for input
	 */
	private void handleTransfer() {
		try {
			socketOutStream = new ObjectOutputStream(socket.getOutputStream());
			socketOutStream.writeObject(task);
			socketOutStream.flush();
			
			String inputLine;
			
			while ((inputLine = socketIn.readLine()) != null) {   
				processInput(inputLine);
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
			return;
		}
	}
	
	/**
	 * Handles finish commands
	 * 
	 * If input is a valid finish command it outputs a finish message with
	 * return value and duration to the console
	 * 
	 * @param input
	 */
	private void handleFinish(String input) {
		input = input.replaceFirst(Command.FINISH + " ", "");
		
		Scanner scanner = new Scanner(input);
		scanner.useLocale(Locale.ENGLISH);
		
		if(!scanner.hasNextInt()) {
			System.out.println("Wrong !finish command.");
			closeConnection();
			return;
		}
		
		int exitValue = scanner.nextInt();
		
		if(!scanner.hasNextDouble()) {
			System.out.println("Wrong !finish command.");
			closeConnection();
			return;
		}
		
		double duration = scanner.nextDouble();
		
		if(scanner.hasNext()) {
			System.out.println("Wrong !finish command.");
			closeConnection();
			return;
		}
		
		company.getLock().lock();
		
		task.setTaskState(TaskState.FINISHED);
		
		int minutes = (int)duration / 60;
		
		if((duration - minutes) > 0)
			minutes++;
		
		task.setCosts(company.manager.getPricingCurve().getCosts(company.getTaskCount(), minutes));
		
		if(task.getCosts() <= company.getCredits()) {
			company.addCredits(-task.getCosts());
			task.setPaid(true);
		}
		
		switch(task.getTaskType()) {
			case LOW:
				company.incrementCountLow();
				break;
			case MIDDLE:
				company.incrementCountMiddle();
				break;
			case HIGH:
				company.incrementCountHigh();
				break;
			default:
				break;
		}
		
		task.appendOutput("Finished Task " + task.getId() + "!");
		task.appendOutput("Process finished with exit value: " + exitValue + ". Duration: " + duration + " seconds");
		
		company.getLock().unlock();
		
		try {
			callback.notify("Execution of task " + task.getId() + " finished.");
		} catch (RemoteException e) {
			System.err.println("Error notifying the client!");
		}
		
		company.manager.getExecutionLock().lock();
		company.manager.removeExecutionWorker(this);
		company.manager.getExecutionLock().unlock();
	}
	
	/**
	 * Handles too busy command
	 * 
	 * Informs the user that the engine can't handle the request because of the heavy load
	 * and sets the task back to prepared
	 * 
	 * @param input
	 */
	private void handleTooBusy(String input) {
		input = input.replaceFirst(Command.TOOBUSY + " ", "");
		
		task.setAssignedAddress(null);
		task.setAssignedPort(0);
		message = "Error: " + input + "\nSet state of Task back to PREPARED!";
		notifySchedulerWorker();
	}
	
	/**
	 * Send abort command to engine to stop execution of task.
	 */
	public void sendAbort() {
		socketOutWriter.println(Command.ABORT);
	}
	
	/**
	 * Closes all open streams/socket
	 */
	private void closeConnection() {
		try {
			if(socketIn != null)
				socketIn.close();
			
			if(socketOutWriter != null)
				socketOutWriter.close();
			
			if(socketOutStream != null)
				socketOutStream.close();
			
			if(socket != null)
				socket.close();
		} catch (IOException e) {}
	}
	
	private void notifySchedulerWorker() {
		synchronized(this){
			notify();
		}
	}
}
