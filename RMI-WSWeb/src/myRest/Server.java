package myRest;


import java.rmi.*;
import java.net.MalformedURLException;
import java.util.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class Server{

	private static int port;
	private static String ip;

        static boolean addServer;
        
	public static void main(String args[]) throws RemoteException, MalformedURLException, AccessException{

		try{
			ip = scanner("Select the Ip address of the server (default is localhost)");
			String port2 = scanner("Select the port of the server (default is 4000)");

			if(port2 == null || port2.equals(""))
				port = 4000;
			else{
				port = Integer.parseInt(port2);
			}

			if(ip == null || ip.equals(""))
				ip = "localhost";

     
                        

			ServerImp obj = new ServerImp();
			startRegistry();
			String url = "rmi://" + ip + ":" + Integer.toString(port) + "/mytube";
			Naming.rebind(url, obj);
			obj.setIp(ip);
			obj.setPort(port);
			
//			addServer = true;
//	        while(addServer){
//                String si = scanner("Do you want to connect with other server? y/n");
//                
//                if(si.equals("y")){
//                    String ipserver2 = scanner("Which ip");
//                    String portserver2 = scanner("Which port?");
//                    if(ipserver2.equals("") || portserver2.equals("")){
//                        System.out.println("Port or ip not valid"); 
//                    }else{
//                            String urlserver2 = "rmi://" + ipserver2 + ":" + portserver2 + "/mytube";
//                            try{
//                                InterfaceServer s  = (InterfaceServer) Naming.lookup(urlserver2);
//                                s.addServer((InterfaceServer) Naming.lookup(url));
//                                obj.addServer(s);
//                            }catch(NotBoundException ex){
//                                    System.out.println("The url is not currently bound");
//                            }catch(MalformedURLException ex){
//                                    System.out.println("Registry has not an appropiate url");
//                            }catch(RemoteException ex){
//                                    System.out.println("Registry cannot be contacted");
//                            }
//                        }
//                }else if(si.equals("n")){
//                        addServer = false;
//                }else{
//                        System.out.println("Not a valid response");
//                }             
//	        }

	        System.out.println("Server ready");

		}catch(RemoteException ex){
			System.out.println("Server not ready");
		}catch(MalformedURLException ex){
			System.out.println("Registry has not an appropiate url");
		}
		/*catch(AccessException ex){
			System.out.println("Operation not permited");
		}*/
	}


	public static void startRegistry() throws RemoteException{
		try{
			Registry registry= LocateRegistry.getRegistry(port);
        	registry.list();
        }catch(RemoteException ex){
        	System.out.println("RMI registry is not located at port " + port);
        	LocateRegistry.createRegistry(port);
        	System.out.println("RMI registry created at port " + port);
        }
	}

	public static String scanner(String message){
		System.out.println(message);
		Scanner scan = new Scanner(System.in);
		String text = scan.nextLine();
		return text;
	}

	

}
