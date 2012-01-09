package at.keiblinger.tu.vs.manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

import at.keiblinger.tu.vs.rmi.NotificationRMI;
import at.keiblinger.tu.vs.shared.Command;
import at.keiblinger.tu.vs.shared.FileTask;
import at.keiblinger.tu.vs.shared.InputHandler;
import at.keiblinger.tu.vs.shared.Message;

/**
 * @author Thomas Chrenko - 0728121
 *
 * SchedulerWorker processes the input from the scheduler
 * 
 */
public class SchedulerWorker extends Thread implements InputHandler {

	private BufferedReader in;
	private PrintWriter out;
	private FileTask task;
	private String message;
	private NotificationRMI callback;
	private Company company;
	
	public void setTask(FileTask task) {
		this.task = task;
	}
	
	public void setCallback(NotificationRMI callback) {
		this.callback = callback;
	}
	
	public void setCompany(Company company) {
		this.company = company;
	}
	
	public String getMessage() {
		return message;
	}
	
	public SchedulerWorker(InputStream in, OutputStream out, FileTask task, NotificationRMI callback, Company company) {
		this.in = new BufferedReader(new InputStreamReader(in));
		this.out = new PrintWriter(out, true);
		this.task = task;
		this.callback = callback;
		this.company = company;
	}
	
	@Override
	public void run() {
		
		String inputLine;
		
		try {
			while ((inputLine = in.readLine()) != null) {   
				processInput(inputLine);
			}
		} catch (IOException e) {}
	}
	
	@Override
	public void processInput(String input) {
		if(input.startsWith(Command.REQUESTENGINE)) {
			handleRequestEngine(input);
		}
		else {
			System.out.println(Message.UNKNOWN_RESPONSE);
		}
	}
	
	/**
	 * Sends request command
	 * 
	 * If input is a valid request command it sends an engine request to the scheduler
	 * 
	 * @param input
	 */
	public void requestEngine(FileTask task) {
		StringBuilder request = new StringBuilder(Command.REQUESTENGINE);
		request.append(" ");
		request.append(task.getTaskType());
		request.append(" ");
		request.append(task.getId());
		
		out.println(request.toString());
	}
	
	/**
	 * Handles request command
	 * 
	 * @param input
	 * @return The assigned engine if input is a positive request answer and the error messgage otherwise
	 */
	private void handleRequestEngine(String input) {
		input = input.replaceFirst(Command.REQUESTENGINE + " ", "");
		
		Scanner scanner = new Scanner(input);
		
		if(!scanner.hasNext()) {
			message = input;
			notifyManager();
			return;
		}
		
		String address = scanner.next();
		
		if(!scanner.hasNextInt()){
			message = input;
			notifyManager();
			return;
		}
		
		int port = scanner.nextInt();
		
		if(!scanner.hasNextInt()){
			message = input;
			notifyManager();
			return;
		}
		
		int taskID = scanner.nextInt();
		
		if(scanner.hasNext()){
			message = input;
			notifyManager();
			return;
		}
		
		try {
			task.setAssignedAddress(InetAddress.getByName(address));
		} catch (UnknownHostException e) {
			message = String.format("Error: Can not assign %s:%d to task %d.", address, port, taskID);
			notifyManager();
			return;
		}
		
		task.setAssignedPort(port);
		
		ExecutionWorker exWorker = new ExecutionWorker(task, callback, company);
		exWorker.start();
		while(true)
			try {
				synchronized (exWorker) {
					exWorker.wait();
				}
				
				message = exWorker.getMessage();
				notifyManager();
				return;
			} catch (InterruptedException e) {}
	}
	
	private void notifyManager() {
		synchronized (this) {
			notify();
		}
	}
}
