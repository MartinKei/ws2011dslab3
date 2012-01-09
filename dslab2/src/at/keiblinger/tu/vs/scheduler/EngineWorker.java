package at.keiblinger.tu.vs.scheduler;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.Scanner;

import at.keiblinger.tu.vs.shared.Command;
import at.keiblinger.tu.vs.shared.InputHandler;
import at.keiblinger.tu.vs.shared.Message;

/**
 * @author Thomas Chrenko - 0728121
 *
 * EngineWorker processes the alive datagrams from the engines.
 * If an alive datagram is received from an engine not in the scheduler
 * engines map => add it. If it's always in the map set isAlive of the engine to true.
 *
 */
public class EngineWorker implements Runnable, InputHandler {

	private Scheduler scheduler;
	private DatagramPacket packet;
	private int timeout;
	
	public EngineWorker(Scheduler scheduler, DatagramPacket packet, int timeout) {
		this.scheduler = scheduler;
		this.packet = packet;
		this.timeout = timeout;
	}
	
	@Override
	public void processInput(String input) {
		if(input.startsWith(Command.ALIVE)) {
			handleAlive(input);
		}
		else {
			System.out.println(Message.UNKNOWN_COMMAND);
		}
	}

	@Override
	public void run() {
		processInput(new String(packet.getData()));
	}
	
	private void handleAlive(String input) {
		input = input.replaceFirst(Command.ALIVE, "");
		
		Scanner scanner = new Scanner(input);
		
		if(!scanner.hasNextInt()) {
			System.out.println(Message.UNKNOWN_COMMAND);
			return;
		}
		
		int port = scanner.nextInt();
		
		if(!scanner.hasNextInt()) {
			System.out.println(Message.UNKNOWN_COMMAND);
			return;
		}
		
		int minConsumption = scanner.nextInt();
		
		if(!scanner.hasNextInt()) {
			System.out.println(Message.UNKNOWN_COMMAND);
			return;
		}
		
		int maxConsumption = scanner.nextInt();
		
		InetSocketAddress socketAddress = new InetSocketAddress(packet.getAddress(), port);
		
		TaskEngine taskEngine = new TaskEngine(packet.getAddress(), port, port, minConsumption, maxConsumption);
		
		if((taskEngine = scheduler.putTaskEngine(socketAddress, taskEngine)) == null) {
			taskEngine = scheduler.getTaskEngine(socketAddress);
			taskEngine.getCheckIsAliveTimer().schedule(new CheckIsAliveTimer(taskEngine), 0, timeout);
		} else {
			taskEngine.setIsAlive(true);
		}
	}
}
