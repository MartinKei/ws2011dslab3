package at.keiblinger.tu.vs.engine;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.TimerTask;

import at.keiblinger.tu.vs.shared.Command;

/**
 * @author Thomas Chrenko - 0728121
 *
 * SendIsAliveTimer is a timertask for sending alive packets
 *
 */
public class SendIsAliveTimer extends TimerTask {

	private TaskEngine taskEngine;
	private String schedulerHost;
	private int minConsumption;
	private int maxConsumption;
	private int schedulerUDPPort;
	private int port;
	
	public SendIsAliveTimer(TaskEngine taskEngine, String schedulerHost, int minConsumption, int maxConsumption, int schedulerUDPPort, int port) {
		this.taskEngine = taskEngine;
		this.schedulerHost = schedulerHost;
		this.minConsumption = minConsumption;
		this.maxConsumption = maxConsumption;
		this.schedulerUDPPort = schedulerUDPPort;
		this.port = port;
	}
	
	@Override
	public void run() {
		sendIsAlivePacket();
	}
	
	/**
	 * Creates an alive packet and sends it to the scheduler
	 */
	private synchronized void sendIsAlivePacket() {
		if(taskEngine.sendIsAlive()) {
			StringBuffer aliveMessage = new StringBuffer(Command.ALIVE);
			aliveMessage.append(" ");
			aliveMessage.append(port);
			aliveMessage.append(" ");
			aliveMessage.append(minConsumption);
			aliveMessage.append(" ");
			aliveMessage.append(maxConsumption);
			aliveMessage.append(" ");
			
			try {
				DatagramSocket socket = new DatagramSocket();
				byte[] buf = new byte[aliveMessage.toString().length()];
				buf = aliveMessage.toString().getBytes();
				DatagramPacket packet = new DatagramPacket(buf, buf.length,
						InetAddress.getByName(schedulerHost), schedulerUDPPort);
				socket.send(packet);
			} catch (SocketException e) {
				System.out.println(e.getMessage());
			} catch (UnknownHostException e) {
				System.out.println(e.getMessage());
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		}
	}
}
