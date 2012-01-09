package at.chrenko.tu.vs.scheduler;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Thomas Chrenko - 0728121
 *
 * EngineListener listens for incoming udp datagrams from the engines
 * and administers a thread pool to execute new EngineWorkers on incoming datagrams. 
 *
 */
public class EngineListener extends Thread {

	private Scheduler scheduler;
	private DatagramSocket socket;
	private int port;
	private int timeout;
	private boolean listening = true;
	private ExecutorService threadPool;
	
	public void closeConnection() {
		threadPool.shutdown();
		listening = false;
		socket.close();	
	}
	
	public EngineListener(Scheduler scheduler, int port, int timeout) {
		this.scheduler = scheduler;
		this.port = port;
		this.timeout = timeout;
	}
	
	@Override
	public void run() {
		try {
			socket = new DatagramSocket(port);
		} catch (SocketException e) {
			System.out.println("Could not listen on port " + port);
			return;
		}
		
		threadPool = Executors.newCachedThreadPool();

		while(listening) {
			try {
				byte[] buf = new byte[256];
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				socket.receive(packet);
				threadPool.execute(new EngineWorker(scheduler, packet, timeout));
			} catch (IOException e) {}
		}
	}
}
