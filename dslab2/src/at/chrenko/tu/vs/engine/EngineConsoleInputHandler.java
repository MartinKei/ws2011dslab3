package at.chrenko.tu.vs.engine;

import at.chrenko.tu.vs.shared.Command;
import at.chrenko.tu.vs.shared.InputHandler;
import at.chrenko.tu.vs.shared.Message;

/**
 * @author Thomas Chrenko - 0728121
 *
 * ConsoleInputHandler processes the user input from console
 *
 */
public class EngineConsoleInputHandler implements InputHandler {

	private TaskEngine taskEngine;
	
	public EngineConsoleInputHandler(TaskEngine taskEngine) {
		this.taskEngine = taskEngine;
	}
	
	@Override
	public void processInput(String input) {
		if(input.startsWith(Command.LOAD)) {
			System.out.println(String.format(Message.CURRENT_LOAD, taskEngine.getLoad()));
		} 
		else if(input.startsWith(Command.EXIT)) {
			taskEngine.setStop(true);
		}
		else {
			System.out.println(Message.UNKNOWN_COMMAND);
		}
	}
}
