
package myRest;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.rmi.*;

import java.net.MalformedURLException;

import java.util.List;
import java.util.Scanner;

public class Client{

	public static String filePath;
	public static boolean registered = false;
	public static ClientImp client;
	public static InterfaceServer server;
	public static void main(String args[]){
        filePath = "C:\\Users\\Adrià\\Desktop\\RMI-Project-master\\content\\";
		String ip = scanner("Select the Ip address of the server (default is localhost)");
		String port = scanner("Select the port of the server (default is 4000)");
		
		if(port == null || port.equals(""))
			port = "4000";

		if(ip == null || ip.equals(""))
			ip = "localhost";

		String url = "rmi://" + ip + ":" + port + "/mytube";

		try{
			server = (InterfaceServer) Naming.lookup(url);
		}catch(NotBoundException ex){
			System.out.println("The url is not currently bound");
			System.exit(0);
		}catch(MalformedURLException ex){
			System.out.println("Registry has not an appropiate url");
			System.exit(0);
		}catch(RemoteException ex){
			System.out.println("Registry cannot be contacted");
			System.exit(0);
		}

		// All the options
		try{
			client = new ClientImp();
			choice();

		}catch(RemoteException ex){
			System.out.println("A RemoteException has been caught");
			System.out.println(ex);
		}

	}

	public static String scanner(String message){
		System.out.println(message);
		Scanner scan = new Scanner(System.in);
		String text = scan.nextLine();
		return text;
	}

	public static void choice() throws RemoteException{
		while(true){
			if(registered == false){
				while(true){
					String opcions = "\n///////////////////////////////////// \n"+
					"1 = Register \n" + // Done
					"2 = Exit client \n" + // Done
					"3 = Login or switch user\n " + // Done
					"4 = If already registered\n" + // Done
					"///////////////////////////////////// \n";

					String escollit = scanner(opcions);
					
					// Done
					if(escollit.equals("1")){
						System.out.println("You have chosed to register");
						register();
						
					// Done
					}else if(escollit.equals("2")){
						System.out.println("You are going to be desconnected from the client");
						server.discardClient(client);
						System.exit(0);
						
					// Done
					}else if(escollit.equals("3")){
						System.out.println("You chosed to login in or switch user");
						String username = scanner("Username?");
						while(username == null || username.equals(""))
							username = scanner("Choose a valid username?");
						
						String password = scanner("Password?");
						while(password == null || password.equals(""))
							password = scanner("Choose a valid password?");
						
						server.login(new User(username, password), client);
						
					// Done	
					}else if(escollit.equals("4")){
						registered = true;
						
					}else{
						System.out.println("Not a valid Option");
					}

					System.out.println("/////////////////////////////////////");
					if(registered){
						System.out.println("Those are your options:\n");
						break;
					}
				}

			}else{
				while(true){
					String opcions = "\n///////////////////////////////////// \n"+
						"1 = Exit client \n" + // Done
						"2 = Upload Content \n"+ // Done
						"3 = Get Content by description  \n"+ // Done
						"4 = Download Content by Title \n"+
						"5 = Modify Title of your content  \n"+ // Done
						"6 = Delete content of yours \n"+ // Done
						"7 = Remove your client from the server (logout)\n"+ // Done
						"8 = List your content \n"+ // //Done
						"9 = List all contents of the server \n"+ 
						"0 = Go back \n"+ // Done
						"////////////////////////////////// \n";

					String escollit = scanner(opcions);
					// Done
					if(escollit.equals("1")){
						System.out.println("You are going to be desconnected from the client");
						server.discardClient(client);
						System.exit(0);

					}else if(escollit.equals("2")){
						upload();
							
					}else if(escollit.equals("3")){
						System.out.println("You have chosed to list content with your description");
						String desc = scanner("Write a description to search content");
						server.getContent(desc, client);

					}else if(escollit.equals("4")){
						System.out.println("You have chosed to download content with your title");
						downloadContentFile();

					}else if(escollit.equals("5")){
						System.out.println("You have chosed to modify a title of your content");
						String oldtitle = scanner("Title of the content you want to change?");
						String newtitle = scanner("New title for the content?");
						server.modifyContentTitle(oldtitle, newtitle, client);

					}else if(escollit.equals("6")){
						System.out.println("You have chosed to delete a content of yours");
						String title = scanner("Title of the content you want to delete?");
						server.deleteContent(title, client);

					}else if(escollit.equals("7")){
						System.out.println("You have choosed to discard your client");
						server.discardClient(client);
						System.out.println("Your client was removed from the server");
						registered = false;

					}else if(escollit.equals("8")){
						System.out.println("You have chosed to list your content uploaded");
						server.listAllMyContent(client);

					}else if(escollit.equals("9")){
						server.listAllContent(client);

					}else if(escollit.equals("0")){
						registered = false;
					}else{
						System.out.println("Not a valid Option");
					}

					if(!registered){
						break;
					}

				}//While true
			}//Else si estas loged
		}
	}

	public static void register() throws RemoteException{
		
		String name = scanner("Username?");
		while(name == null || name.equals(""))
			name = scanner("Choose a valid username");
		
		String password = scanner("Password?");
		while(password == null || password.equals(""))
			password = scanner("Choose a valid password");
		
		User newUser = new User(name, password);
		server.registerClient(newUser,  client);
	}

	public static void upload() throws RemoteException{
		String title = scanner("Title of the content?");
		while(title == null || title.equals(""))
			title = scanner("Choose a valid Title");
		
		String desc = scanner("Description of the content?");
		while(desc == null || desc.equals(""))
			desc = scanner("Choose a valid Description");
		
		String filename = scanner("File name? (extension included)");
		while(filename == null || filename.equals(""))
			filename = scanner("Choose a valid file name (With its extension)");
		
		try{
			byte[] data = Files.readAllBytes(new File(filePath + filename).toPath());
			server.uploadContent(title, desc, filename, filePath + filename, data, client);

		}catch(IOException ex){
			System.out.println("Invalid file");
			System.out.println(ex);
		}
	}

	public static void downloadContentFile(){
		String title = scanner("Title of the content you want to download?");
		
		try{
			byte[] data = server.downloadContent(title, client);
			if(data==null){
				System.out.println("Search finished");
				return;
			}
			System.out.println("File was found");
			new File(filePath+title).mkdir();
			FileOutputStream fos = new FileOutputStream(filePath+title+"\\"+title+".pdf");
			fos.write(data);
			fos.close();
			System.out.println("File downloaded at: " + filePath+title+"\\");
		}catch(IOException ex){
			System.out.println("File couldn't be downloaded");
			System.out.println(ex);
		}
	}
	
}
