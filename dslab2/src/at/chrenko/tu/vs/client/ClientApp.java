package at.chrenko.tu.vs.client;

import java.io.File;

/**
 * @author Thomas Chrenko - 0728121
 *
 */
public class ClientApp {

	/**
	 * Main method for starting the Client.
	 * 
	 * Checks the arguments and starts the Client.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length != 2) {
			Usage();
			return;
		}
		
		String managementComponent = args[0];
		
		String taskDir = args[1];
		
		File dir = new File(taskDir);
		
		if(!dir.isAbsolute()) {
			File current = new File(".");
			dir = new File(current.getAbsolutePath() + File.separatorChar + taskDir);
		}
		
		if(!dir.exists()) {
			boolean success = dir.mkdir();
			
			if(!success) {
				Usage();
				return;
			}
		}

		Thread t = new Thread(new Client(managementComponent, dir));
		t.start();
	}
	
	private static void Usage() {
		System.out.println("Usage: run-client <managementComponent> <taskdir>");
	}

}
