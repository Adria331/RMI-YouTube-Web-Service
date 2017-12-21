package myRest;



import java.rmi.*;
import java.rmi.RemoteException;
import java.util.*;


public interface InterfaceServer extends Remote{


	public void registerClient(User user,  InterfaceClient client) throws RemoteException;
	public void discardClient(InterfaceClient client) throws RemoteException; 
	public void login(User user, InterfaceClient client) throws RemoteException;

	public void uploadContent(String title, String desc, String filename, String path, byte[] data, InterfaceClient client) throws RemoteException;
	public void getContent(String description, InterfaceClient client) throws RemoteException; 
	public byte[] downloadContent(String title, InterfaceClient client) throws RemoteException; 

	public void modifyContentTitle(String oldTitle, String newTitle, InterfaceClient client) throws RemoteException;
	public void deleteContent(String title, InterfaceClient client) throws RemoteException;

	public void listAllMyContent(InterfaceClient client) throws RemoteException;
	public void listAllContent(InterfaceClient client) throws RemoteException;
	        
    public byte[] getBytesFile(String title) throws RemoteException;
    public Map<String,Content> getMapContent() throws RemoteException;

    public void deleteOwnContent(String title) throws RemoteException;
    public void updateOwnContentTitle(String old, String newTitle) throws RemoteException;
    }
