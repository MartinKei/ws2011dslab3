package at.chrenko.tu.vs.scheduler;

/**
 * @author Thomas Chrenko - 0728121
 *
 */
public class SchedulerApp {

	/**
	 * Main method for starting the Scheduler.
	 * 
	 * Checks the arguments and starts the Scheduler.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length != 6) {
			System.out.println("Usage: run-scheduler <tcpPort> <udpPort> <min> <max> <timeout> <checkPeriod>");
			return;
		}
		
		int tcpPort, udpPort, min, max, timeout, checkPeriod;
		
		try {
			tcpPort = Integer.parseInt(args[0]);
			udpPort = Integer.parseInt(args[1]);
			min = Integer.parseInt(args[2]);
			max = Integer.parseInt(args[3]);
			timeout = Integer.parseInt(args[4]);
			checkPeriod = Integer.parseInt(args[5]);
		} catch (NumberFormatException e) {
			System.out.println("Usage: run-scheduler <tcpPort> <udpPort> <min> <max> <timeout> <checkPeriod>");
			return;
		}
		
		if(tcpPort < 0 || tcpPort > 65535 || udpPort < 0 || udpPort > 65535 ||
				min < 0 || max < 0 || max < min || timeout <= 0 || checkPeriod <= 0) {
			System.out.println("Usage: run-scheduler <tcpPort> <udpPort> <min> <max> <timeout> <checkPeriod>");
			return;
		}
		
		Thread t = new Thread(new Scheduler(tcpPort, udpPort, min, max, timeout, checkPeriod));
		t.start();
	}
}
