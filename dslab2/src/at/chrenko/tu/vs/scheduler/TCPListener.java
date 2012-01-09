package at.chrenko.tu.vs.scheduler;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Thomas Chrenko - 0728121
 *
 * TCPListener listens for incoming tcp connections and
 * administers a thread pool to execute new TCPWorkers on incoming connections.
 *
 */
public class TCPListener extends Thread {
	
	private Scheduler scheduler;
	private ServerSocket socket;
	private int port;
	private boolean listening = true;
	private ExecutorService threadPool;
	private List<TCPWorker> workers;
	
	public void closeConnection() {
		try {
			threadPool.shutdown();
			listening = false;
			socket.close();
			
			for(TCPWorker worker : workers) {
				worker.closeConnection();
			}
		} catch (IOException e) {}
	}
	
	public TCPListener(Scheduler scheduler, int port) {
		this.scheduler = scheduler;
		this.port = port;
		this.workers = new ArrayList<TCPWorker>();
	}

	@Override
	public void run() {
		try {
			socket = new ServerSocket(port);
		} catch (IOException e) {
			System.out.println("Could not listen on port " + port);
			return;
		}
		
		threadPool = Executors.newCachedThreadPool();
		
		while(listening) {
			try {
				TCPWorker worker = new TCPWorker(scheduler, socket.accept());
				workers.add(worker);
				threadPool.execute(worker);
			} catch (IOException e) {}
		}
	}
}
