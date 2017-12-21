package myRest;


import java.io.Serializable;


public class Content implements Serializable{

	private String key;
	
	private String title;
	private String description;
	
	private String filename;
	
	private String ownerName;
	
	private String serverURL;
	
	public Content(){
		
	}

	public Content(String title, String description, String filename) {
		this.title = title;
		this.description = description;
		this.filename = filename;
	}

	public String getTitle(){
		return this.title;
	}

	public String getServerURL(){
		return this.serverURL;
	}
	
	public String getKey(){
		return this.key;
	}

	public String getDescription(){
		return this.description;
	}

	public String getOwnerName(){
		return this.ownerName;
	}

	public String getFilename(){
		return this.filename;
	}

	public void setContentKey(String key){
		this.key = key;
	}


	public void setTitle(String newTitle){
		this.title = newTitle;
	}

	public void setDescription(String newDescription){
		this.description = newDescription;
	}

	public void setOwnerName(String name){
		this.ownerName = name;
	}

	public void setKey(String key){
		this.key = key;
	}
	public void setFilename(String filename){
		this.filename = filename;
	}
	public void setServerURL(String url){
		this.serverURL = url;
	}
}