package at.chrenko.tu.vs.manager;

import java.io.File;
/**
 * @author Thomas Chrenko - 0728121
 *
 */
public class ManagerApp {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length != 5) {
			usage();
			return;
		}
		
		String bindingName, schedulerHost, taskDir;
		
		int schedulerTCPPort, preparationCosts;
		
		try {
			bindingName = args[0];
			schedulerHost= args[1];
			schedulerTCPPort = Integer.parseInt(args[2]);
			preparationCosts = Integer.parseInt(args[3]);
			taskDir = args[4];
		} catch (NumberFormatException e) {
			usage();
			return;
		}
		
		if(schedulerTCPPort < 0 || schedulerTCPPort > 65535 || preparationCosts < 0) {
			usage();
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
				usage();
				return;
			}
		}
		
		Thread t = new Thread(new Manager(bindingName, schedulerHost, schedulerTCPPort, preparationCosts, dir));
		t.start();
	}
	
	private static void usage() {
		System.out.println("Usage: run-manager <bindingName> <schedulerHost> <schedulerTCPPort> <preparationCosts> <taskDir>");
	}

}
