package at.chrenko.tu.vs.rmi;

import java.rmi.RemoteException;

import at.chrenko.tu.vs.manager.Manager;
import at.chrenko.tu.vs.manager.User;
import at.chrenko.tu.vs.shared.Message;

/**
 * @author Thomas Chrenko - 0728121
 *
 */
public class LoginHandler implements LoginRMI {

	private Manager manager;
	
	public LoginHandler(Manager manager) {
		this.manager = manager;
	}
	
	@Override
	public UserRMI login(String name, String password) throws RemoteException {
		if(!manager.usersContains(name)) 
			throw new RemoteException("Wrong name or password.");
		
		User user = manager.getUser(name);
		
		if(!user.checkPassword(password))
			throw new RemoteException("Wrong name or password.");
		
		user.getLock().lock();
		
		if(user.isLoggedIn()) {
			user.getLock().unlock();
			throw new RemoteException(Message.ALREADY_LOGGED_IN_ELSEWHERE);
		} 
		
		user.setLoggedIn(true);
		
		user.setRemote(user);
		
		user.getLock().unlock();
		
		return user.getRemote();
	}

}
