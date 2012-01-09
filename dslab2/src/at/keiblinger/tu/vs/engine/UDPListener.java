package at.keiblinger.tu.vs.engine;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import at.keiblinger.tu.vs.shared.Command;
import at.keiblinger.tu.vs.shared.InputHandler;
import at.keiblinger.tu.vs.shared.Message;

/**
 * @author Thomas Chrenko - 0728121
 *
 * UDPListener listens for incoming upd datagrams and
 * set the sendIsAlive of the engine regarding to the
 * datagram content(suspend or activate).
 *
 */
public class UDPListener implements Runnable, InputHandler {
	
	private TaskEngine taskEngine;
	private DatagramSocket socket;
	private int port;
	private boolean listening = true;
	
	public void closeConnection() {
		listening = false;
		socket.close();	
	}
	
	public UDPListener(TaskEngine taskEngine, int port) {
		this.taskEngine = taskEngine;
		this.port = port;
	}
	
	@Override
	public void processInput(String input) {
		if(input.startsWith(Command.SUSPEND)) {
			taskEngine.setSendIsAlive(false);
		}
		else if(input.startsWith(Command.ACTIVATE)) {
			taskEngine.setSendIsAlive(true);
		}
		else {
			System.out.println(Message.UNKNOWN_COMMAND);
		}
	}

	@Override
	public void run() {
		try {
			socket = new DatagramSocket(port);
		} catch (IOException e) {
			System.out.println("Could not listen on port " + port);
			return;
		}
		
		while(listening) {
			try {
				byte[] buf = new byte[256];
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				socket.receive(packet);
				
				processInput(new String(packet.getData()));
				
			} catch (IOException e) {}
		}
	}
}
