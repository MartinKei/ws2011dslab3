package at.keiblinger.tu.vs.engine;

import java.io.File;

/**
 * @author Thomas Chrenko - 0728121
 *
 */
public class TaskEngineApp {

	/**
	 * Main method for starting the Engine.
	 * 
	 * Checks the arguments and starts the Engine.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length != 7) {
			System.out.println("Usage: run-gtengine <tcpPort> <schedulerHost> <schedulerUDPPort> <alivePeriod> <minConsumption> <maxConsumption> <taskDir>");
			return;
		}
		
		int port, alivePeriod, schedulerUDPPort, minConsumption, maxConsumption;
		String schedulerHost, taskDir;
		
		try {
			port = Integer.parseInt(args[0]);
			schedulerHost = args[1];
			schedulerUDPPort = Integer.parseInt(args[2]);
			alivePeriod = Integer.parseInt(args[3]);
			minConsumption = Integer.parseInt(args[4]);
			maxConsumption = Integer.parseInt(args[5]);
			taskDir = args[6];
		} catch (NumberFormatException e) {
			System.out.println("Usage: run-gtengine <tcpPort> <schedulerHost> <schedulerUDPPort> <alivePeriod> <minConsumption> <maxConsumption> <taskDir>");
			return;
		}
		
		if(port < 0 || port > 65535 || schedulerUDPPort < 0 || schedulerUDPPort > 65535 ||
				alivePeriod <= 0 || minConsumption <= 0 || maxConsumption <= 0 || maxConsumption < minConsumption) {
			System.out.println("Usage: run-gtengine <tcpPort> <schedulerHost> <schedulerUDPPort> <alivePeriod> <minConsumption> <maxConsumption> <taskDir>");
			return;
		}
		
		File dir = new File(taskDir);
		
		if(!dir.isAbsolute()) {
			File current = new File(".");
			dir = new File(current.getAbsolutePath() + File.separatorChar + taskDir);
		}
		
		if(!dir.exists()) {
			boolean success = dir.mkdir();
			
			if(!success) {
				System.out.println("Usage: run-gtengine <tcpPort> <schedulerHost> <schedulerUDPPort> <alivePeriod> <minConsumption> <maxConsumption> <taskDir>");
				return;
			}
		}
		
		Thread t = new Thread(new TaskEngine(port, alivePeriod, schedulerUDPPort, minConsumption,
												maxConsumption, schedulerHost, dir));
		t.start();
	}

}
