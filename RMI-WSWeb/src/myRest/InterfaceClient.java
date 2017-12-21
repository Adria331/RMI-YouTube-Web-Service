package myRest;
import java.rmi.*;

public interface InterfaceClient extends Remote{
	public void sendMessage(String message) throws RemoteException;
	public void setUser(User user) throws RemoteException;
	public User getUser() throws RemoteException;
	public void saveContentKey(String key) throws RemoteException;
	public void removeContentKey(String key) throws RemoteException;
}