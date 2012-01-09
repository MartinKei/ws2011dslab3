package at.chrenko.tu.vs.scheduler;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import at.chrenko.tu.vs.shared.Command;
import at.chrenko.tu.vs.shared.EngineState;
import at.chrenko.tu.vs.shared.InputHandler;
import at.chrenko.tu.vs.shared.Message;
import at.chrenko.tu.vs.shared.TaskType;

/**
 * @author Thomas Chrenko - 0728121
 *
 * TCPWorker administers connections to the clients and
 * processes input from them
 *
 */
public class TCPWorker implements Runnable, InputHandler  {

	private Scheduler scheduler;
	private Socket socket;
	private BufferedReader in;
	private PrintWriter out;
	
	/**
	 * Force client logout and
	 * close the socket => terminate
	 */
	public void closeConnection() {
		try {
			if(socket != null)
				socket.close();
		} catch (IOException e) {}
	}
	
	public TCPWorker(Scheduler scheduler, Socket socket) {
		this.scheduler = scheduler;
		this.socket = socket;
	}
	
	@Override
	public void processInput(String input) {
		if(input.startsWith(Command.REQUESTENGINE)) {
			out.println(handleRequestEngine(input));
		}
		else {
			System.out.println(Message.UNKNOWN_COMMAND);
		}
	}
	
	@Override
	public void run() {
		try {
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);
		} catch (IOException e) {
			System.out.println("Error opening input stream!");
			return;
		}
		
		String input;
		
		try {
			while ((input = in.readLine()) != null) {   
			    processInput(input);
			}
		} catch (IOException e) {}
	}

	/**
	 * Handles requestengine command
	 * 
	 * @param input
	 * @return If input is a vlid requestengine command and the client is logged in
	 * 		   it request the load from all engines and returns the engines address and
	 * 		   and an error message otherwise
	 */
	private String handleRequestEngine(String input) {
		input = input.replaceFirst(Command.REQUESTENGINE, "");
		
		StringBuilder response = new StringBuilder(Command.REQUESTENGINE);
		response.append(" ");
		
		Scanner scanner = new Scanner(input);
		
		if(!scanner.hasNext()) {
			response.append(Message.REQUESTENGINE_SCHEDULER_COMMAND_ERROR);
			return response.toString();
		}
		
		TaskType taskType;
		
		try {
			taskType = TaskType.valueOf(TaskType.class, scanner.next());
		} catch (Exception e) {
			response.append(Message.REQUESTENGINE_SCHEDULER_COMMAND_ERROR);
			return response.toString();
		}
		
		if(!scanner.hasNextInt()) {
			response.append(Message.REQUESTENGINE_SCHEDULER_COMMAND_ERROR);
			return response.toString();
		}
		
		int taskID = scanner.nextInt();
		
		if(scanner.hasNext()) {
			response.append(Message.REQUESTENGINE_SCHEDULER_COMMAND_ERROR);
			return response.toString();
		}
		
		// request the load from all available engines with RequestLoadCallables
		ExecutorService threadPool = Executors.newCachedThreadPool();
		
		List<RequestLoadCallable> callables = new ArrayList<RequestLoadCallable>();
		
		for(TaskEngine engine : scheduler.getTaskEngines()) {
			if(engine.getState().equals(EngineState.AVAILABLE))
				callables.add(new RequestLoadCallable(scheduler, engine.getAddress(), engine.getTcpPort()));
		}
		
		//invoke all callables and give them 5 seconds to finish
		try {
			threadPool.invokeAll(callables, 5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			response.append(Message.REQUESTENGINE_SCHEDULER_COMMAND_ERROR);
			return response.toString();
		}
		
		TaskEngine bestEngine = getBestEngine(taskType);
		
		if(bestEngine == null) {
			response.append("Error: No engine available for execution. Please try again later.");
			return response.toString();
		}
		
		//update load
		bestEngine.setLoad(bestEngine.getLoad() + taskType.getLoad());
		
		response.append(bestEngine.getAddress().getHostAddress());
		response.append(" ");
		response.append(bestEngine.getTcpPort());
		response.append(" ");
		response.append(taskID);
		
		return response.toString();
	}

	/**
	 * Gets the best engine to handle the task
	 * 
	 * @param taskType - The type of the task to handle
	 * @return The best engine to handle the task
	 */
	private TaskEngine getBestEngine(TaskType taskType) {
		TaskEngine retValue = null;
		
		for(TaskEngine engine : scheduler.getTaskEngines())
			if(engine.getState().equals(EngineState.AVAILABLE))
				if((100 - engine.getLoad()) >= taskType.getLoad() && (retValue == null || engine.getMaxConsumption()-engine.getMinConsumption() < retValue.getMaxConsumption()-retValue.getMinConsumption()))
					retValue = engine;
		
		return retValue;
	}
}
