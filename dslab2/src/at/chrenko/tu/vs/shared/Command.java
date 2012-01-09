package at.chrenko.tu.vs.shared;

/**
 * @author Thomas Chrenko - 0728121
 *
 * Command holds the commands for the communication.
 * 
 */
public class Command {

	public static final String LOGIN = "!login";
	public static final String LOGOUT = "!logout";
	public static final String EXIT = "!exit";
	public static final String COMPANIES = "!companies";
	public static final String LIST = "!list";
	public static final String LOAD = "!load";
	public static final String ALIVE = "!alive";
	public static final String SUSPEND = "!suspend";
	public static final String ACTIVATE = "!activate";
	public static final String EXECUTETASK = "!executeTask";
	public static final String REQUESTENGINE = "!requestEngine";
	public static final String PREPARE = "!prepare";
	public static final String INFO = "!info";
	public static final String ENGINES = "!engines";
	public static final String FINISH = "!finish";
	public static final String TOOBUSY = "!toobusy";
	public static final String READY = "!ready";
	public static final String ABORT = "!abort";
	public static final String USERS = "!users";
	public static final String GETPRICINGCURVE = "!getPricingCurve";
	public static final String SETPRICESTEP = "!setPriceStep";
	public static final String CREDITS = "!credits";
	public static final String BUY = "!buy";
	public static final String GETOUTPUT = "!getOutput";
	
	private Command() {}
}
