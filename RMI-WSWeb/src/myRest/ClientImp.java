package myRest;



import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.util.*;


public class ClientImp extends UnicastRemoteObject implements InterfaceClient{

	public List<String> keys;
	public User user;
	
	public ClientImp() throws RemoteException{
		super();
		user = null;
		keys = new ArrayList<String>();
	}

	public void sendMessage(String message) throws RemoteException{
		System.out.println(message);
	}

	public void setUser(User user){
		this.user = user;
	}
	
	public User getUser(){
		return this.user;
	}
	
	public void saveContentKey(String key) throws RemoteException{
		keys.add(key);
	}

	public void removeContentKey(String key) throws RemoteException{
		keys.add(key);
	}

}