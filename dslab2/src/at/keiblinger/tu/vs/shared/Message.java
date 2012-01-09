package at.keiblinger.tu.vs.shared;

/**
 * @author Thomas Chrenko - 0728121
 *
 * Message holds some of the messages used in the communication.
 *
 */
public class Message {
	
	public static final String LOGGED_IN = "Successfully logged in.";
	public static final String LOGGED_OUT = "Successfully logged out.";
	public static final String LOGGED_OUT_FORCED = "Scheduler is shutting down. Forced log out.";
	public static final String ALREADY_LOGGED_IN_ELSEWHERE = "User is already logged in elsewhere";
	public static final String ALREADY_LOGGED_IN = "You are already logged in!";
	public static final String WRONG_USER_PASSWORD = "Wrong username or password.";
	public static final String LOGIN_COMMAND_ERROR = "!login command requires two arguments: username and password.";
	public static final String UNKNOWN_COMMAND = "Unknown command!";
	public static final String LOGIN_FIRST = "You have to login first!";
	public static final String LOGOUT_COMMAND_ERROR = "!logout command does not take arguments.";
	public static final String LOAD_COMMAND_ERROR = "!load command does not take arguments.";
	public static final String EXECUTETASK_COMMAND_ERROR = "!executeTask command requires two arguments: taskID and startScript.";
	public static final String UNKNOWN_RESPONSE = "Unknown response!";
	public static final String CURRENT_LOAD = "Current load: %d%%";
	public static final String PREPARE_COMMAND_ERROR = "!prepare command requires two arguments: taskName and type.";
	public static final String EXIT_COMMAND_ERROR = "!exit command does not take arguments.";
	public static final String REQUESTENGINE_SCHEDULER_COMMAND_ERROR = "!requestEngine command requires two arguments: type and taskID";
	public static final String REQUESTENGINE_CLIENT_COMMAND_ERROR = "!requestEngine command requires one argument: taskID";
	public static final String SETPRICESTEP_COMMAND_ERROR = "!setPriceStep command requires two arguments: taskCount and percent.";
	public static final String CREDITS_COMMAND_ERROR = "!credits command does not take arguments.";
	public static final String LIST_COMMAND_ERROR = "!list command does not take arguments.";
	public static final String BUY_COMMAND_ERROR = "!buy command requires one argument: amount";
	public static final String INFO_COMMAND_ERROR = "!info command requires one argument: taskId";
	public static final String GETOUTPUT_COMMAND_ERROR = "!getOutput command requires one argument: taskId";
	
	private Message() {}
}
