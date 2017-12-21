package myRest;


import java.rmi.server.UnicastRemoteObject;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Scanner;

import javax.ws.rs.core.MediaType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.beans.XMLEncoder;
import java.beans.XMLDecoder;
import sun.net.www.protocol.http.HttpURLConnection;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


public class ServerImp extends UnicastRemoteObject implements InterfaceServer{

	private static String filePath;
	private static String xmlFilePath;

	private String ip;
	private String port;
	
//	private static Map<InterfaceClient, String> users;
	private static List<InterfaceClient> users;
	private static Map<String, Content> content;

	public ServerImp() throws RemoteException{
		super();
//		users = new HashMap<InterfaceClient, String>();
		content = new HashMap<String, Content>(); // key / Content
		new ArrayList<InterfaceServer>();
		users = new ArrayList<InterfaceClient>();
		
		filePath = "C:\\Users\\Adrià\\Desktop\\RMI-Project-master\\servercontent\\";
		xmlFilePath = "C:\\Users\\Adrià\\Desktop\\RMI-Project-master\\xml\\";
		
		decodeXML();
		createfolders();
	}

	public void setIp(String ip){
		this.ip = ip;
	}
	public void setPort(int port){
		this.port = Integer.toString(port);
	}
	
    public void createfolders(){
    	if(!(new File("C:\\Users\\Adrià\\Desktop\\RMI-Project-master\\content").exists()))
    		new File("C:\\Users\\Adrià\\Desktop\\RMI-Project-master\\content").mkdir();
    	
    	if(!(new File("C:\\Users\\Adrià\\Desktop\\RMI-Project-master\\servercontent").exists()))
    		new File("C:\\Users\\Adrià\\Desktop\\RMI-Project-master\\servercontent").mkdir();
    	
    	if(!(new File("C:\\Users\\Adrià\\Desktop\\RMI-Project-master\\xml").exists()))
    		new File("C:\\Users\\Adrià\\Desktop\\RMI-Project-master\\xml").mkdir();
    }
	
////// USER MANAGEMENT /////

	public void registerClient(User user, InterfaceClient client) throws RemoteException{
		
		try{
			URL url = new URL("http://localhost:8080/RMI-WSWeb/rest/user");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			
			String input = new Gson().toJson(user);

			conn.setDoOutput(true);
			OutputStream os = conn.getOutputStream();
            os.write(input.getBytes());
            os.flush();

            int status = conn.getResponseCode();
            conn.disconnect();
            // 201 = Created // 409 = Conflict
            if(status==201){
    			client.setUser(user);
    			client.sendMessage("You have registered and loged in correctly");
    			if(!users.contains(client))
    				users.add(client);
            }else if(status==409){
            	client.sendMessage("This username has been already used");
            }
            return;
            
		}catch(MalformedURLException e){
			System.out.println(e);
		}catch(IOException e){
			System.out.println(e);
		}	
	}

	public void discardClient(InterfaceClient client) throws RemoteException{
		
		if(users.contains(client)){
			users.remove(client);
			client.setUser(null);
			System.out.println("Client deleted");
			client.sendMessage("Deleted succesfully");
		}else{
			client.sendMessage("Couldn't be removed");
		}
	}

	public void login(User user, InterfaceClient client) throws RemoteException{
		try{
			URL url = new URL("http://localhost:8080/RMI-WSWeb/rest/user/"+user.getUsername()+"/"+user.getPassword());
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", MediaType.APPLICATION_JSON);
			
			if(conn.getResponseCode() != 200){
				client.sendMessage("Your user or password are incorrect");
				return;
//				throw new RuntimeException("Failed: HTTP error code: " + conn.getResponseCode());
			}
			
			conn.disconnect();
			
			client.setUser(user);
			if(!users.contains(client))
				users.add(client);
			client.sendMessage("Login successful");
	        return;
	        
		}catch(Exception e){
			System.out.println(e);
		}
	}
	
	
	
////////////////////////////////////


	// K 
	public void uploadContent(String title, String desc, String filename, String path, byte[] data, InterfaceClient client) throws RemoteException{
		
		User owner = client.getUser();
		if(owner == null){
			client.sendMessage("You have to be logged in to upload something");
			return;
		}
		
		String key = UUID.randomUUID().toString();
		
		Content c = new Content(title, desc, filename);
		//Add attributes
		c.setContentKey(key);
		c.setOwnerName(owner.getUsername());
		c.setServerURL("rmi://" + ip + ":" + port + "/mytube");
//		client.sendMessage("ABANS DE PILLAR URL");
		try {
            URL url = new URL ("http://localhost:8080/RMI-WSWeb/rest/file");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection(); 
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
//            client.sendMessage("SE CONECTE");
            
            String input = new Gson().toJson(c);
//            client.sendMessage("NEW GSON OK");
            
			conn.setDoOutput(true);
            OutputStream os = conn.getOutputStream();
            os.write(input.getBytes());
//            client.sendMessage("WRITED");
            
            os.flush();
            
            int status = conn.getResponseCode();
            conn.disconnect();
//            client.sendMessage("Desconectat");
            if(status == 409){ 
                client.sendMessage("Title already used, chose another title");
                return;
            }else if(status != 201){
            	client.sendMessage("File could not be uploaded");
                return;
            }else{
            	System.out.println("Uploading file...\n");
            	content.put(c.getTitle(), c);
            	new File(filePath+key).mkdir();
            	FileOutputStream fos = new FileOutputStream(filePath+key+"\\"+filename);
				fos.write(data);
				fos.close();
				
				encodeXML(c);
				client.saveContentKey(c.getKey());
				client.sendMessage("Uploaded successfuly \n");
				System.out.println("File uploaded: " + filename);
				notifyAllClients(client);
            }
        } catch (IOException e) {
            System.out.println(e);
        }  			
	}

	public void getContent(String description, InterfaceClient client) throws RemoteException{
		List<String> titles = new ArrayList<String>();
	    try{
			URL url = new URL("http://localhost:8080/RMI-WSWeb/rest/file/" + description);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", MediaType.APPLICATION_JSON);
		
			int status = conn.getResponseCode();
			
			if(status == 200){
				BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				String output = br.readLine();
				String[] outputlist = new Gson().fromJson(output, String[].class);
				titles = new ArrayList<>(Arrays.asList(outputlist));
				
	    	}else{
				client.sendMessage("Problem connecting with the server");
			}
			
			conn.disconnect();
	    }catch(IOException e){
	    	System.out.println(e);
	    }
		
      
		if(titles.size() == 0)
			client.sendMessage("There is no title coincident with that description");
		else{
			client.sendMessage("These are the contents relationed with that description:\n");
			for(String contentTitle : titles){
				client.sendMessage("- " + contentTitle);
			}
		}
	}


	public byte[] downloadContent(String title, InterfaceClient client) throws RemoteException{
		if(content.containsKey(title)){
			try{
				System.out.println(filePath + content.get(title).getKey() + "\\"+content.get(title).getFilename());
				return Files.readAllBytes(new File(filePath + content.get(title).getKey() + "\\"+content.get(title).getFilename()).toPath());
			}catch(IOException ex){
				System.out.println(ex);
				client.sendMessage("Error downloading the file from the server");
				return null;
			}
		}else{
			InterfaceServer s = searchServerByTitle(title);
			System.out.println("File ready");
			if(s==null){
				client.sendMessage("Could not find any content with that title");
				return null;
			}
			return s.getBytesFile(title);
		}
	} 


	public void notifyAllClients(InterfaceClient client) throws RemoteException{
		for(InterfaceClient clientsito : users){
			clientsito.sendMessage("New content has been added");
		}
	}


	public void modifyContentTitle(String oldTitle, String newTitle,  InterfaceClient client) throws RemoteException{
	    try{
			URL url = new URL("http://localhost:8080/RMI-WSWeb/rest/file/" + oldTitle +"/" + client.getUser().getUsername() + "/change/" + newTitle);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("PUT");
			conn.setRequestProperty("Content-Type", "application/json");
		
			int status = conn.getResponseCode();
			
			if(status == 401){
				client.sendMessage("You are not authorized to do this");
			}else if(status == 406){
				client.sendMessage("The new title is already used");
			}else if(status == 500){
				client.sendMessage("Title could not be changed");
	    	}else{
	    		if(content.containsKey(oldTitle))
	    				updateOwnContentTitle(oldTitle, newTitle);
	    		else{
	    			InterfaceServer s = searchServerByTitle(newTitle);
	    			s.updateOwnContentTitle(oldTitle, newTitle);
	    		}
				client.sendMessage("Title modified");
			}
			
			conn.disconnect();
	    }catch(IOException e){
	    	System.out.println(e);
	    }
	}

	public void updateOwnContentTitle(String old, String newTitle) throws RemoteException{

		Content c = content.get(old);
		c.setTitle(newTitle);
		content.remove(old);
		content.put(newTitle, c);
		File xml = new File(xmlFilePath + old + ".xml");
		System.out.println("Changing " + xmlFilePath + c.getTitle() + ".xml");
		xml.delete();
		encodeXML(c);

	}


	// K 
	public void deleteContent(String title, InterfaceClient client) throws RemoteException{ 
		InterfaceServer s = null;
		try{
			
			if(!content.containsKey(title)){
				s = searchServerByTitle(title);
			}
			URL url = new URL("http://localhost:8080/RMI-WSWeb/rest/file/delete/"+title+"/"+client.getUser().getUsername());
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("DELETE");
			conn.setRequestProperty("Content-Type", "application/json");
		
			int status = conn.getResponseCode();
			
			conn.disconnect();
			
			if(status == 401){
				client.sendMessage("Title unexistent");
				return;
				
			}else if(status == 200){
				if(content.containsKey(title)){
					Content c = content.get(title);
					content.remove(title);
					File pdf = new File(filePath + c.getKey() + "\\" + c.getFilename());
					File folder = new File(filePath + c.getKey());
					File xml = new File(xmlFilePath + c.getTitle() + ".xml");
					pdf.delete();
					folder.delete();
					xml.delete();
					client.sendMessage("Deletion succesful");
					
					return;
				}else{
					s.deleteOwnContent(title);
				}
				
			}else{
				client.sendMessage("Could not be deleted");
				return;
			}
			
		}catch(IOException ex){
			System.out.println(ex);
			client.sendMessage("Problem deleting de content");
		}catch(IllegalArgumentException ex){
			System.out.println(ex);
			client.sendMessage("Problem deleting de content");
		}
	}

	public void deleteOwnContent(String title) throws RemoteException{
		Content c = content.get(title);
		content.remove(title);
		File pdf = new File(filePath + c.getKey() + "\\" + c.getFilename());
		File folder = new File(filePath + c.getKey());
		File xml = new File(xmlFilePath + c.getTitle() + ".xml");
		pdf.delete();
		folder.delete();
		xml.delete();
	}
	
	public void listAllMyContent(InterfaceClient client) throws RemoteException{
		List<String> titles = new ArrayList<String>();
	    try{
			URL url = new URL("http://localhost:8080/RMI-WSWeb/rest/file/list/" + client.getUser().getUsername());
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", MediaType.APPLICATION_JSON);
		
			int status = conn.getResponseCode();
			
			if(status == 200){
				BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				String output = br.readLine();
				String[] outputlist = new Gson().fromJson(output, String[].class);
				titles = new ArrayList<>(Arrays.asList(outputlist));
				
	    	}else{
				client.sendMessage("Problem connecting with the server");
			}
			
			conn.disconnect();
	    }catch(IOException e){
	    	System.out.println(e);
	    }
		
      
		if(titles.size() == 0)
			client.sendMessage("You have no contents uploaded");
		else{
			client.sendMessage("These are your content:\n");
			for(String contentTitle : titles){
				client.sendMessage("- " + contentTitle);
			}
		}
	}
	
	public void listAllContent(InterfaceClient client) throws RemoteException{
		for(Content c: content.values()){
			client.sendMessage("- " + c.getTitle() + " " + c.getKey() + " by " + c.getOwnerName());
		}
	}
	

    public byte[] getBytesFile(String title) throws RemoteException{
    	try{
    		System.out.println("Preparing file");
    		String path = filePath + content.get(title).getKey() + "\\" + content.get(title).getFilename();
    				return Files.readAllBytes(new File(path).toPath());
		}catch(IOException ex){
			return null;
		}
    }

    public void encodeXML(Content c) throws RemoteException{
    	try{
    		System.out.println("Generating "+ xmlFilePath + c.getTitle() + ".xml");
    		XMLEncoder e = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(xmlFilePath + c.getTitle() + ".xml")));
    		e.writeObject(c);
    		e.close();
    	}catch(IOException ex){
    	}
    }

    public void decodeXML() throws RemoteException{
    	XMLDecoder e;

    	if(!(new File("C:\\Users\\Adrià\\Desktop\\RMI-Project-master\\xml").exists()))
    		return;

    	try{
	    	for(File f : new File(xmlFilePath).listFiles()){
				e = new XMLDecoder(new BufferedInputStream(new FileInputStream(f)));
				Object result = e.readObject();
				Content c = (Content) result;
				content.put(c.getTitle(), c);
				e.close();
	    	}
    	}catch(IOException ex){
    		System.out.println("Can't load previous files");
    	}
    }
    
    public Map<String,Content> getMapContent() throws RemoteException{
        return content;
    }
    
    public String getContentFilePath(Content c){
    	return filePath+c.getKey()+"\\"+c.getFilename();
    }
    
    public InterfaceServer searchServerByTitle(String title) throws RemoteException{
    	System.out.println("Looking for in others servers");
    	
    	try{
			URL url = new URL("http://localhost:8080/RMI-WSWeb/rest/"+ title +"/server");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", MediaType.APPLICATION_JSON);
		
			int status = conn.getResponseCode();
			
			if(status == 200){
				BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				String output = br.readLine();
				String urlrmi = output.replace(" ", "");
				try{
					System.out.println("Server located");
					conn.disconnect();
					return (InterfaceServer) Naming.lookup(urlrmi);
				}catch(Exception e){
					conn.disconnect();
					return null;
				}
			}else{
				return null;
			}
			
	    }catch(IOException e){
	    	System.out.println(e);
	    }
    	
    	return null;
    }

    

    
}
