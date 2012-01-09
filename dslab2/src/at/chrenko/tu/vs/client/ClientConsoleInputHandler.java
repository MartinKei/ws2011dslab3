package at.chrenko.tu.vs.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import at.chrenko.tu.vs.rmi.CompanyHandler;
import at.chrenko.tu.vs.rmi.LoginRMI;
import at.chrenko.tu.vs.rmi.NotificationHandler;
import at.chrenko.tu.vs.rmi.NotificationRMI;
import at.chrenko.tu.vs.rmi.UserRMI;
import at.chrenko.tu.vs.shared.Command;
import at.chrenko.tu.vs.shared.InputHandler;
import at.chrenko.tu.vs.shared.Message;
import at.chrenko.tu.vs.shared.TaskType;

/**
 * @author Thomas Chrenko - 0728121
 *
 * ConsoleInputHandler processes the user input from console
 * 
 */
public class ClientConsoleInputHandler implements InputHandler {

	private Client client;
	private File taskDir;
	private LoginRMI loginRemote;
	private UserRMI userRemote;
	private List<NotificationRMI> callbackObjects;
	private boolean admin;
	
	public ClientConsoleInputHandler(Client client, LoginRMI loginRemote, File taskDir) {
		this.client = client;
		this.loginRemote = loginRemote;
		this.taskDir = taskDir;
		this.userRemote = null;
		
		this.callbackObjects = new ArrayList<NotificationRMI>();
		this.admin = false;
	}
	
	@Override
	public void processInput(String input) {
		if(userRemote == null && !input.startsWith(Command.LOGIN) && !input.startsWith(Command.EXIT)) {
			System.out.println("You have to login first.");
			return;
		}
			
		if(input.startsWith(Command.LOGIN)) {
			handleLogin(input);
		}
		else if(input.startsWith(Command.GETPRICINGCURVE)) {
			handleGetPricingCurve(input);
		}
		else if(input.startsWith(Command.SETPRICESTEP)) {
			handleSetPriceStep(input);
		}
		else if(input.startsWith(Command.CREDITS)) {
			handleCredits(input);
		}
		else if(input.startsWith(Command.BUY)) {
			handleBuy(input);
		}
		else if(input.startsWith(Command.LOGOUT)) {
			handleLogout(input);		
		}
		else if(input.startsWith(Command.EXIT)) {
			handleExit(input);
		}
		else if(input.startsWith(Command.LIST)) {
			handleList(input);
		}
		else if(input.startsWith(Command.PREPARE)) {
			handlePrepare(input);
		}
		else if(input.startsWith(Command.INFO)) {
			handleInfo(input);
		}
		else if(input.startsWith(Command.GETOUTPUT)) {
			handleGetOutput(input);
		}
		else if(input.startsWith(Command.EXECUTETASK)) {
			handleExecute(input);
		}
		else {
			System.out.println(Message.UNKNOWN_COMMAND);
		}
	}
	
	/**
	 * Handles a login command.
	 * 
	 * If input is a valid login command it calls the remote objects login command
	 * 
	 * @param input
	 */
	private void handleLogin(String input) {
		input = input.replaceFirst(Command.LOGIN + " ", "");
		
		Scanner scanner = new Scanner(input);
		
		if(!scanner.hasNext()) {
			System.out.println(Message.LOGIN_COMMAND_ERROR);
			return;
		}
		
		String userName = scanner.next();
		
		if(!scanner.hasNext()) {
			System.out.println(Message.LOGIN_COMMAND_ERROR);
			return;
		}
		
		String password = scanner.next();
		
		if(scanner.hasNext()) {
			System.out.println(Message.LOGIN_COMMAND_ERROR);
			return;
		}
		
		if(userRemote != null) {
			System.out.println(Message.ALREADY_LOGGED_IN);
			return;
		}
		
		try {
			userRemote = loginRemote.login(userName, password);
		} catch (RemoteException e) {
			System.out.println(e.getCause().getMessage());
			return;
		}
		
		String mode = "";
		try {
			mode = userRemote.mode();
		} catch (RemoteException e) {
			System.out.println("Error getting mode!");
			userRemote = null;
			return;
		}
		
		if(mode.equals("admin"))
			admin = true;
		
		System.out.println("Successfully logged in. Using " + mode + " mode.");
	}
	
	/**
	 * Handles a getPricingCurve command.
	 * 
	 * If input is a valid getPricingCurve command it calls the remote objects getPricingCurve command
	 * 
	 * @param input
	 */
	public void handleGetPricingCurve(String input) {
		if(input.equals(Command.GETPRICINGCURVE)) {
			try {
				System.out.println(userRemote.getPricingCurve());
			} catch (RemoteException e) {
				System.out.println(e.getCause().getMessage());
				return;
			}
		} else {
			System.out.println(Message.CREDITS_COMMAND_ERROR);
		}
	}
	
	/**
	 * Handles a setPriceStep command.
	 * 
	 * If input is a valid setPriceStep command it calls the remote objects setPriceStep command
	 * 
	 * @param input
	 */
	private void handleSetPriceStep(String input) {
		input = input.replaceFirst(Command.SETPRICESTEP + " ", "");
		
		Scanner scanner = new Scanner(input);
		scanner.useLocale(Locale.ENGLISH);
		
		if(!scanner.hasNextInt()) {
			System.out.println(Message.SETPRICESTEP_COMMAND_ERROR);
			return;
		}
		
		Integer taskCount = scanner.nextInt();
		
		if(!scanner.hasNextFloat()) {
			System.out.println(Message.SETPRICESTEP_COMMAND_ERROR);
			return;
		}
		
		Float percent = scanner.nextFloat();
		
		if(scanner.hasNext()) {
			System.out.println(Message.SETPRICESTEP_COMMAND_ERROR);
			return;
		}
		
		try {
			System.out.println(userRemote.setPriceStep(taskCount, percent));
		} catch (RemoteException e) {
			System.out.println(e.getCause().getMessage());
			return;
		}
	}
	
	/**
	 * Handles a list command.
	 * 
	 * If input is a valid list command it calls the remote objects list command
	 * 
	 * @param input
	 */
	private void handleList(String input) {		
		if(input.equals(Command.LIST)) {
			if(!admin) {
				File[] files = taskDir.listFiles();
				
				if(files.length == 0)
					System.out.println("There are no files in the directory.");
				else {
					StringBuilder retValue = new StringBuilder();
					
					for(int i=0;i<files.length;i++) {
						if(files[i].isFile()) {
							retValue.append(files[i].getName());
							retValue.append("\n");
						}
					}
					
					retValue.setLength(retValue.length()-1);
					
					System.out.println(retValue.toString());
				}
			} else {
				System.out.println("Command not allowed. Your are not a company.");
			}
		}
		else {
			System.out.println(Message.LIST_COMMAND_ERROR);
		}
	}
	
	/**
	 * Handles a credits command.
	 * 
	 * If input is a valid credits command it calls the remote objects credits command
	 * 
	 * @param input
	 */
	private void handleCredits(String input) {		
		if(input.equals(Command.CREDITS)) {
			try {
				System.out.println(userRemote.credits());
			} catch (RemoteException e) {
				System.out.println("Error getting credits!");
				return;
			}
		}
		else {
			System.out.println(Message.CREDITS_COMMAND_ERROR);
		}
	}
	
	/**
	 * Handles a buy command.
	 * 
	 * If input is a valid buy command it calls the remote objects buy command
	 * 
	 * @param input
	 */
	private void handleBuy(String input) {		
		input = input.replaceFirst(Command.BUY + " ", "");
		
		Scanner scanner = new Scanner(input);
		
		if(!scanner.hasNextInt()) {
			System.out.println(Message.BUY_COMMAND_ERROR);
			return;
		}
		
		int amount = scanner.nextInt();
		
		if(scanner.hasNext()) {
			System.out.println(Message.BUY_COMMAND_ERROR);
			return;
		}
		
		try {
			System.out.println(userRemote.buy(amount));
		} catch (RemoteException e) {
			System.out.println(e.getCause().getMessage());
		}
	}
	
	/**
	 * Handles a logout command.
	 * 
	 * If input is a valid logout command it calls the remote objects logout command
	 * 
	 * @param input
	 */
	private void handleLogout(String input) {		
		if(input.equals(Command.LOGOUT)) {
			if(userRemote == null) {
				System.out.println(Message.LOGIN_FIRST);
			}
			
			try {
				removeNotificationRMIs();
				callbackObjects = new ArrayList<NotificationRMI>();
				System.out.println(userRemote.logout());
				userRemote = null;
				admin = false;
			} catch (RemoteException e) {
				System.out.println(e.getCause().getMessage());
			}
		} else {
			System.out.println(Message.LOGOUT_COMMAND_ERROR);
		}
	}
	
	/**
	 * Handles an exit command.
	 * 
	 * If input is a valid exit command it calls the remote objects exit command
	 * 
	 * @param input
	 */
	private void handleExit(String input) {		
		if(input.equals(Command.EXIT)) {
			if(userRemote != null) {
				try {
					System.out.println(userRemote.exit());
				} catch (RemoteException e) {
					System.out.println(e.getCause().getMessage());
				}
			}
			
			removeNotificationRMIs();
			client.setStop(true);
		} else {
			System.out.println(Message.LOGOUT_COMMAND_ERROR);
		}
	}
	
	/**
	 * Handles a prepare command
	 *  
	 *  If input is a valid prepare command it calls the remote objects prepare command
	 *  
	 * @param input
	 */
	private void handlePrepare(String input) {
		input = input.replaceFirst(Command.PREPARE + " ", "");
		
		Scanner scanner = new Scanner(input);
		
		if(!scanner.hasNext()) {
			System.out.println(Message.PREPARE_COMMAND_ERROR);
			return;
		}
		
		String name = scanner.next();
		
		if(!scanner.hasNext()) {
			System.out.println(Message.PREPARE_COMMAND_ERROR);
			return;
		}
		
		String type = scanner.next();
		
		TaskType taskType;
		try {
			taskType = TaskType.valueOf(type);
		} catch (IllegalArgumentException e) {
			System.out.println("Type must be LOW, MIDDLE or HIGH!");
			return;
		}

		if(scanner.hasNext()) {
			System.out.println(Message.PREPARE_COMMAND_ERROR);
			return;
		}
		
		if(userRemote == null) {
			System.out.println(Message.LOGIN_FIRST);
			return;
		}
		
		File file = null;
		
		for(File f : taskDir.listFiles()) {
			if(f.isFile() && f.getName().equals(name)) {
				file = f;
			}
		}
		
		if(file == null) {
			System.out.println("Task not found.");
			return;
		}
		
		byte[] fileBytes;
		try {
			fileBytes = getBytesFromFile(file);
		} catch (IOException e) {
			System.out.println("Error reading file.");
			return;
		}
		
		try {
			System.out.println(userRemote.prepare(name, taskType, fileBytes));
		} catch (RemoteException e) {
			System.out.println(e.getCause().getMessage());
		}
	}
	
	/**
	 * Handles an info command
	 *  
	 *  If input is a valid info command it calls the remote objects info command
	 *  
	 * @param input
	 */
	private void handleInfo(String input) {
		input = input.replaceFirst(Command.INFO + " ", "");
		
		Scanner scanner = new Scanner(input);
		
		if(!scanner.hasNextInt()) {
			System.out.println(Message.INFO_COMMAND_ERROR);
			return;
		}
		
		int taskId = scanner.nextInt();
		
		if(scanner.hasNext()) {
			System.out.println(Message.INFO_COMMAND_ERROR);
			return;
		}
		
		if(userRemote == null) {
			System.out.println(Message.LOGIN_FIRST);
			return;
		}
		
		try {
			System.out.println(userRemote.info(taskId));
		} catch (RemoteException e) {
			System.out.println(e.getCause().getMessage());
		}
	}
	
	/**
	 * Handles a getOutput command
	 *  
	 *  If input is a valid getOutput command it calls the remote objects getOutput command
	 *  
	 * @param input
	 */
	private void handleGetOutput(String input) {
		input = input.replaceFirst(Command.GETOUTPUT + " ", "");
		
		Scanner scanner = new Scanner(input);
		
		if(!scanner.hasNextInt()) {
			System.out.println(Message.GETOUTPUT_COMMAND_ERROR);
			return;
		}
		
		int taskId = scanner.nextInt();
		
		if(scanner.hasNext()) {
			System.out.println(Message.GETOUTPUT_COMMAND_ERROR);
			return;
		}
		
		if(userRemote == null) {
			System.out.println(Message.LOGIN_FIRST);
			return;
		}
		
		try {
			System.out.println(userRemote.getOutput(taskId));
		} catch (RemoteException e) {
			System.out.println(e.getCause().getMessage());
		}
	}
	
	/**
	 * Handles execute command
	 * 
	 * If input is a valid execute command it starts an ExecutionWorker for communicating with an engine
	 * 
	 * @param input
	 */
	private void handleExecute(String input) {
		input = input.replaceFirst(Command.EXECUTETASK + " ", "");
		
		Scanner scanner = new Scanner(input);
		
		if(!scanner.hasNextInt()) {
			System.out.println(Message.EXECUTETASK_COMMAND_ERROR);
			return;
		}
		
		int taskId = scanner.nextInt();
		
		String startScript;
		int start, end;
		
		if(input.contains("\"")) {
			start = input.indexOf('\"');
			end = input.lastIndexOf('\"');
			
			if(start == end) {
				System.out.println(Message.EXECUTETASK_COMMAND_ERROR);
				return;
			}
			
			startScript = input.substring(start+1, end);
		}
		else {
			System.out.println(Message.EXECUTETASK_COMMAND_ERROR);
			return;
		}
		
		NotificationRMI callback = new NotificationHandler();
		try {
			UnicastRemoteObject.exportObject(callback, 0);
		} catch (RemoteException e) {
			System.out.println("Error!");
			return;
		}
		
		callbackObjects.add(callback);
		
		String retValue;
		try {
			retValue = userRemote.executeTask(taskId, startScript, callback);
		} catch (RemoteException e) {
			System.out.println(e.getCause().getMessage());
			return;
		}
		
		if(retValue.startsWith("Command")) {
			try {
				UnicastRemoteObject.unexportObject(callback, true);
			} catch (NoSuchObjectException e) {}
		}
		
		System.out.println(retValue);
	}
	
	/**
	 * Reads a file, save it to a byte array and returns it
	 * 
	 * @param file - The file to read
	 * @return The byte array holding the file
	 * @throws IOException
	 */
	private byte[] getBytesFromFile(File file) throws IOException {
	    InputStream is = new FileInputStream(file);

	    long length = file.length();

	    if (length > Integer.MAX_VALUE) {
	    	is.close();
	        System.out.println("File is too large!");
	        return new byte[0];
	    }

	    byte[] bytes = new byte[(int)length];

	    int offset = 0;
	    int numRead = 0;
	    while (offset < bytes.length
	           && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
	        offset += numRead;
	    }

	    if (offset < bytes.length) {
	    	is.close();
	        throw new IOException("Could not completely read file "+file.getName());
	    }

	    is.close();
	    return bytes;
	}
	
	private void removeNotificationRMIs() {
		for(NotificationRMI callback : callbackObjects) {
			try {
				UnicastRemoteObject.unexportObject(callback, true);
			} catch (NoSuchObjectException e) {}
		}
	}
}
