package at.chrenko.tu.vs.scheduler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.Callable;

import at.chrenko.tu.vs.shared.Command;
import at.chrenko.tu.vs.shared.InputHandler;

/**
 * @author Thomas Chrenko - 0728121
 *
 * RequestLoadCallable sends and load request to an engine and waits for it's response.
 *
 */
public class RequestLoadCallable implements Callable<Boolean>, InputHandler {

	private Scheduler scheduler;
	private InetAddress address;
	private int port;
	private Socket socket;
	private boolean retValue = false;
	
	public RequestLoadCallable(Scheduler scheduler, InetAddress address, int port) {
		this.scheduler = scheduler;
		this.address = address;
		this.port = port;
	}
	
	@Override
	public Boolean call() throws Exception {
		socket = new Socket(address, port);
		
		BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
		
		String input;
		
		String output = Command.LOAD;
		
		out.println(output);
		
		input = in.readLine();
		
		if(input != null) {
			processInput(input);
		}
			
		return retValue;
	}

	@Override
	public void processInput(String input) {
		if(input.startsWith(Command.LOAD)) {
			input = input.replaceFirst(Command.LOAD, "");
			
			Scanner scanner = new Scanner(input);
			
			if(!scanner.hasNextInt())
				return;
			
			int load = scanner.nextInt();
			
			InetSocketAddress key = new InetSocketAddress(address, port);
			
			TaskEngine engine = scheduler.getTaskEngine(key);
			
			engine.setLoad(load);
			
			retValue = true;
		}
		
		return;
	}

}
