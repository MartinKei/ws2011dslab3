package at.chrenko.tu.vs.manager;

import at.chrenko.tu.vs.shared.Command;
import at.chrenko.tu.vs.shared.InputHandler;
import at.chrenko.tu.vs.shared.Message;

/**
 * @author Thomas Chrenko - 0728121
 *
 * ConsoleInputHandler processes the user input from console
 *
 */
public class ManagerConsoleInputHandler implements InputHandler {

	private Manager manager;
	
	public ManagerConsoleInputHandler(Manager manager) {
		this.manager = manager;
	}
	
	@Override
	public void processInput(String input) {
		if(input.startsWith(Command.USERS)) {
			handleUsers(input);
		} 
		else if(input.startsWith(Command.EXIT)) {
			handleExit(input);
		} 
		else {
			System.out.println(Message.UNKNOWN_COMMAND);
		}
	}
	
	/**
	 * Handles users command
	 * 
	 * If input is a valid companies command it prints out information about the companies
	 * 
	 * @param input
	 */
	private void handleUsers(String input) {
		if(!input.equals(Command.USERS)) {
			System.out.println("!users command does not take arguments.");
			return;
		}
			
		int i = 1;
		for(User user : manager.getUsers()) {
			user.getLock().lock();
			
			StringBuilder output = new StringBuilder();
			
			output.append(i);
			output.append(". ");
			output.append(user.getName());
			
			if(user.isLoggedIn())
				output.append(" (online)");
			else
				output.append(" (offline)");
			
			if(user instanceof Company) {
				output.append(": LOW ");
				output.append(((Company)user).getCountLow());
				output.append(", MIDDLE ");
				output.append(((Company)user).getCountMiddle());
				output.append(", HIGH ");
				output.append(((Company)user).getCountHigh());
			}
			user.getLock().unlock();
			
			System.out.println(output.toString());
			
			i++;
		}
	}
	
	/**
	 * Handles exit command
	 * 
	 * If input is a valid exit command it indirectly stops the while loop from the manager
	 * 
	 * @param input
	 */
	private void handleExit(String input) {
		if(!input.equals(Command.EXIT)) {
			System.out.println(Message.EXIT_COMMAND_ERROR);
			return;
		}
		
		manager.setStop(true);
	}
}
