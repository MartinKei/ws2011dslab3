package at.keiblinger.tu.vs.engine;

import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;

import at.keiblinger.tu.vs.shared.Command;
import at.keiblinger.tu.vs.shared.FileTask;
import at.keiblinger.tu.vs.shared.InputHandler;
import at.keiblinger.tu.vs.shared.Message;
import at.keiblinger.tu.vs.shared.StreamGobbler;
import at.keiblinger.tu.vs.shared.TaskType;

/**
 * @author Thomas Chrenko - 0728121
 *
 * TCPWorker administers connections to the scheduler or to a client and
 * processes load request from scheduler and execute request from clients
 *
 */
public class TCPWorker implements Runnable, InputHandler  {
	
	private TaskEngine taskEngine;
	private Socket socket;
	private BufferedReader inReader;
	private ObjectInputStream inStream;
	private PrintWriter out;
	private File taskDir;
	private Process proc;
	
	// executionCounter is used to create unique directory names
	private static AtomicInteger executionCounter = new AtomicInteger(0);
	
	public TCPWorker(TaskEngine taskEngine, Socket socket, File taskDir) {
		this.taskEngine = taskEngine;
		this.socket = socket;
		this.taskDir = taskDir;
	}
	
	@Override
	public void processInput(String input) {
		if(input.startsWith(Command.LOAD)) {
			out.println(handleLoad(input));
		}
		else if(input.startsWith(Command.EXECUTETASK)) {
			handleExecute(input);
		}
		else {
			out.println(Message.UNKNOWN_COMMAND);
		}
	}
	
	@Override
	public void run() {
		try {
			out = new PrintWriter(socket.getOutputStream(), true);
			inReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			String input;
			
			input = inReader.readLine();
			if(input != null) {
				processInput(input);
			}
		} catch (IOException e) {
			System.out.println("Error processing input!");
		} finally {
			closeConnection();
		}
	}
	
	/**
	 * Handles load command
	 * 
	 * @param input
	 * @return The load of the engine if input is a valid load command and an error message otherwise
	 */
	private String handleLoad(String input) {
		StringBuilder response = new StringBuilder(Command.LOAD);
		response.append(" ");
		
		if(!input.equals(Command.LOAD)) {
			response.append(Message.LOAD_COMMAND_ERROR);
			return response.toString();
		}

		response.append(taskEngine.getLoad());
		
		return response.toString();
	}
	
	
	
	/**
	 * Handles execute command
	 * 
	 * If input is a valid execute command and the task can be executed it sends
	 * a ready command to the client to signal the engine is ready to receive the FileTask,
	 * executes it after transfer, redirects the tasks output to the client through StreamGobblers,
	 * and sends a finish message to the client after finishing execution, or after abort command from client(new thread checks for command)
	 * 
	 * @param input
	 */
	private void handleExecute(String input) {
		input = input.replaceFirst(Command.EXECUTETASK, "");
		
		Scanner scanner = new Scanner(input);
		
		if(!scanner.hasNext()) {
			out.println("Error!");
			closeConnection();
			return;
		}
		
		String type = scanner.next();
		
		TaskType taskType;
		try {
			taskType = TaskType.valueOf(type);
		} catch (IllegalArgumentException e) {
			out.println("Error!");
			closeConnection();
			return;
		}

		if(scanner.hasNext()) {
			out.println("Error!");
			closeConnection();
			return;
		}
		
		if(taskEngine.getAndAddLoad(taskType.getLoad()) == -1) {
			out.println(Command.TOOBUSY + " " + "Too Busy to execute the task!");
			closeConnection();
			return;
		}
		
		File file = null;
		File dir = null;
		
		try {
			//signal that the engine is ready for the FileTask (serialization)
			out.println(Command.READY);
			
			//get the serialized object
			inStream = new ObjectInputStream(socket.getInputStream());
			
			FileTask fileTask = null;

			fileTask = (FileTask)inStream.readObject();
			
			String dirName = taskDir.getAbsolutePath() + File.separatorChar + socket.getLocalPort() + "_" + executionCounter.incrementAndGet();
			String fileName = dirName + File.separatorChar + fileTask.getFileName();
			
			dir = new File(dirName);
			
			if(!dir.exists()) {
				boolean success = new File(dirName).mkdir();
				
				if(!success) {
					System.out.println("Error creating the directory for execution.");
					closeConnection();
					return;
				}
			}
			
			// create a file from FileTask's byte array
			file = new File(fileName);
			
			FileOutputStream fos = new FileOutputStream(file);
			
			fos.write(fileTask.getFile());
			
			fos.close();
			
			// execute the task and messure execution time
			long start = System.currentTimeMillis();
			
			proc = Runtime.getRuntime().exec(fileTask.getStartScript(), null, dir);
			
			// create StreamGobbler for redirecting the tasks output
			StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), socket.getOutputStream());
			StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), socket.getOutputStream());
			
			errorGobbler.start();
			outputGobbler.start();
			
			// if client sends abort command stop execution
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					try {
						String input = null;
						
						while((input = inReader.readLine()) != null) {
							if(input.equals(Command.ABORT))
								proc.destroy();
						}
					} catch (IOException e) {}
				}
			}).start();
			
			int exitVal = proc.waitFor();
			
			taskEngine.getAndAddLoad(-fileTask.getTaskType().getLoad());
			
			long end = System.currentTimeMillis();
			
			double duration = ((end-start)/1000d);
			
			out.println(Command.FINISH + " " + exitVal + " " + Double.toString(duration));
			
		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
		} catch (IOException e) {
			System.out.println(e.getMessage());
		} catch (InterruptedException e) {
			System.out.println(e.getMessage());
		} catch (ClassNotFoundException e) {
			System.out.println(e.getMessage());
		} finally {
			if(file != null)
				file.delete();
			if(dir != null)
				dir.delete();
		}
	}
	
	/**
	 * Close the socket => terminate
	 */
	private void closeConnection() {
		try {
			if(socket != null)
				socket.close();
		} catch (IOException e) {}
	}
	
	
}
