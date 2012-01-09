package at.keiblinger.tu.vs.engine;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
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
	
	private TaskEngine taskEngine;
	private ServerSocket socket;
	private int port;
	private boolean listening = true;
	private File taskDir;
	private ExecutorService threadPool;
	
	/**
	 * Shutdown thread pool, stop listening and close Socket
	 */
	public void closeConnection() {
		try {
			threadPool.shutdown();
			listening = false;
			socket.close();
		} catch (IOException e) {}	
	}
	
	public TCPListener(TaskEngine taskEngine, int port, File taskDir) {
		this.taskEngine = taskEngine;
		this.port = port;
		this.taskDir = taskDir;
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
				threadPool.execute(new TCPWorker(taskEngine, socket.accept(), taskDir));
			} catch (IOException e) {}
		}
	}
}
