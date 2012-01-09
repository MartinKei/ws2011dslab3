package at.keiblinger.tu.vs.scheduler;

import java.util.Collection;

import at.keiblinger.tu.vs.shared.Command;
import at.keiblinger.tu.vs.shared.InputHandler;
import at.keiblinger.tu.vs.shared.Message;

/**
 * @author Thomas Chrenko - 0728121
 *
 * ConsoleInputHandler processes the user input from console
 *
 */
public class SchedulerConsoleInputHandler implements InputHandler {

	private Scheduler scheduler;
	
	public SchedulerConsoleInputHandler(Scheduler scheduler) {
		this.scheduler = scheduler;
	}
	
	@Override
	public void processInput(String input) {
		if(input.startsWith(Command.ENGINES)) {
			handleEngines(input);
		}
		else if(input.startsWith(Command.EXIT)) {
			handleExit(input);
		} 
		else {
			System.out.println(Message.UNKNOWN_COMMAND);
		}
	}
	
	/**
	 * Handle engines command
	 * 
	 * If input is a valid engines command it prints out information about the engines
	 * 
	 * @param input
	 */
	private void handleEngines(String input) {
		if(!input.equals(Command.ENGINES)) {
			System.out.println("!engines command does not take arguments.");
			return;
		}
		
		Collection<TaskEngine> engines = scheduler.getTaskEngines();
		
		if(engines.size() == 0) {
			System.out.println("No engines.");
			return;
		}
		
		int i = 1;
		
		for(TaskEngine taskEngine : scheduler.getTaskEngines()) {
			StringBuilder output = new StringBuilder();
			
			output.append(i);
			output.append(". IP:");
			output.append(taskEngine.getAddress().getHostAddress());
			output.append(", TCP:");
			output.append(taskEngine.getTcpPort());
			output.append(", UDP:");
			output.append(taskEngine.getUdpPort());
			output.append(", ");
			
			switch(taskEngine.getState()) {
				case OFFLINE:
					output.append("offline");
					break;
				case SUSPENDED:
					output.append("suspended");
					break;
					
				case AVAILABLE:
					output.append("online");
					break;
				default:
					break;
			}
			
			output.append(", Energy Signature: min ");
			output.append(taskEngine.getMinConsumption());
			output.append("W, max ");
			output.append(taskEngine.getMaxConsumption());
			output.append("W, Load: ");
			output.append(taskEngine.getLoad());
			output.append("%");
			
			System.out.println(output.toString());
			
			i++;
		}
	}

	/**
	 * Handles exit command
	 * 
	 * If input is a valid exit command it indirectly stops 
	 * the TCPWorkers associated with the companies
	 * 
	 * @param input
	 */
	private void handleExit(String input) {
		if(!input.equals(Command.EXIT)) {
			System.out.println(Message.EXIT_COMMAND_ERROR);
			return;
		}
		
		scheduler.setStop(true);
	}
}
