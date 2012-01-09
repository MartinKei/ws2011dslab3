package at.chrenko.tu.vs.manager;

import java.io.Serializable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import at.chrenko.tu.vs.rmi.UserRMI;

/**
 * @author Thomas Chrenko - 0728121
 *
 */
public abstract class User implements Serializable {

	private static final long serialVersionUID = 2909373603342727380L;
	
	protected Manager manager;

	private String name;
	
	private String password;
	
	private boolean loggedIn;
	
	protected Lock lock;
	
	protected UserRMI remote;
	
	public String getName() {
		return this.name;
	}
	
	public boolean isLoggedIn() {
		return this.loggedIn;
	}
	
	public void setLoggedIn(boolean loggedIn) {
		this.loggedIn = loggedIn;
	}
	
	public Lock getLock() {
		return lock;
	}
	
	public UserRMI getRemote() {
		return remote;
	}
	
	public boolean checkPassword(String password) {
		return this.password.equals(password);
	}
	
	public User(Manager manager, String name, String password, boolean admin) {
		this.manager = manager;
		this.name = name;
		this.password = password;
		this.loggedIn = false;
		this.lock = new ReentrantLock();
		this.remote = null;
	}
	
	public User(Manager manager, String name, String password) {
		this(manager, name, password, false);
	}
	
	public abstract void setRemote(User user);
}
