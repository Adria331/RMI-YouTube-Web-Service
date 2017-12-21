import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.ArrayList;
import javax.enterprise.context.RequestScoped;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import myRest.*;



@RequestScoped
@Path("")
@Produces({ "application/xml", "application/json" })
@Consumes({ "application/xml", "application/json" })
public class RestApp {
	
//	https://en.wikipedia.org/wiki/List_of_HTTP_status_codes
	
	
//////////////////////////////////////// USER MANAGEMENT
	@POST
	@Path("/user")
	public Response registerUser(User user){
		Statement db = takeDatabase();
		try{
			/*  SELECT nom FROM users WHERE name='username';   */
			ResultSet result = db.executeQuery(
					"SELECT username FROM users "
							+ "WHERE username='" + user.getUsername() + "';"
					);
			//If the cursor is just before an item on the set
			//If it is means there are an item on the set.
			if (result.isBeforeFirst())
				return Response.status(409).entity("Username not available").build();
			
			db.executeUpdate("INSERT INTO users(username,password) VALUES("
							+ "'" + user.getUsername() + "'," 
							+ "'" + user.getPassword() + "');");
			System.out.println("User "+ user.getUsername() + " created");

			return Response.status(201).build();
			
		}catch(SQLException e){
			System.out.println(e);
		}
		return Response.status(500).entity("Error").build();
	}
	
	@GET
	@Path("/user/{username}/{password}")
	public Response equalsUserPassword(@PathParam("username") String username, @PathParam("password") String password){
		Statement db = takeDatabase();
		try{
			ResultSet result = db.executeQuery(
					"SELECT username FROM users "
							+ "WHERE username='" + username + "'" 
							+ " AND password='" + password + "';"
					);

			if (result.isBeforeFirst())
				return Response.status(200).build();
			else
				return Response.status(409).build();
			
		}catch(SQLException e){
			System.out.println(e);
		}
		
		
		return Response.status(500).entity("Error").build();
	}
	
//////////////////////////////////////// CONTENT MANAGEMENT
	
	@POST
	@Path("/file")
	public Response uploadFile(Content content){
		System.out.println("\n\n  asdfas \n\n");
		Statement db = takeDatabase();
		try{
			ResultSet result = db.executeQuery(
					"SELECT title FROM contents "
							+ "WHERE title='" + content.getTitle() + "';"
					);
			if (result.isBeforeFirst())
				return Response.status(409).entity("Title not available").build();
			
			db.executeUpdate("INSERT INTO contents(key, title, description, owner, filename, serverurl) "
							+ "VALUES("
							+ "'" + content.getKey() + "',"
							+ "'" + content.getTitle() + "',"
							+ "'" + content.getDescription() + "',"
							+ "'" + content.getOwnerName() + "',"
							+ "'" + content.getFilename() + "',"	
							+ "'" + content.getServerURL() + "');"
							);
			
			System.out.println("Content "+ content.getTitle() + " from "+ content.getServerURL() +" created");
			return Response.status(201).build();
			
		}catch(SQLException e){
			System.out.println(e);
		}
		return Response.status(500).entity("Error").build();
	}
		
	
	@GET
	@Path("/file/{desc}")
	public Response getContentByDescription(@PathParam("desc") String description){
		Statement db = takeDatabase();
		try{
			ResultSet result = db.executeQuery(
					"SELECT title FROM contents "
							+ "WHERE description LIKE '%" + description + "%';" 
					);
			

			List<String> titles = new ArrayList<String>();
			while(result.next())
				titles.add(result.getString("title"));
			
			return Response.status(200).entity(titles).build();
			
		}catch(SQLException e){
			System.out.println(e);
		}
		
		return Response.status(500).entity("Error").build();
		
	}
	
	@DELETE
	@Path("/file/delete/{title}/{owner}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteFile(@PathParam("title") String title, @PathParam("owner") String owner){
		Statement db = takeDatabase();
		try{
			ResultSet result = db.executeQuery(
					"SELECT title FROM contents "
							+ "WHERE title = '" + title + "'"
							+ " AND owner = '" + owner + "';" 
					);
			
			if (!result.isBeforeFirst())
				return Response.status(401).entity("Title not available").build();
			
			db.executeUpdate("DELETE FROM contents WHERE title = '" + title + "';");
			return Response.status(200).build();
			
		}catch(SQLException ex){
			return Response.status(500).entity("Error").build();
		}
	}
	
	@PUT
	@Path("/file/{title}/{owner}/change/{newtitle}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateTitle(@PathParam("title") String oldTitle, @PathParam("owner") String owner, @PathParam("newtitle") String newTitle){
		Statement db = takeDatabase();
		try{
			// IF YOU ARE THE OWNER OF THE CONTENT
			ResultSet result = db.executeQuery(
					"SELECT title FROM contents "
							+ "WHERE title = '" + oldTitle + "'"
							+ " AND owner = '" + owner + "';" 
					);
			
			if (!result.isBeforeFirst())
				return Response.status(401).entity("Unauthorized").build();
			
			// IF THE NEW TITLE EXIST
			result = db.executeQuery(
					"SELECT title FROM contents "
							+ "WHERE title = '" + newTitle + "';"
					);
			
			if (result.isBeforeFirst())
				return Response.status(406).entity("New Title already used").build();
			
			// UPDATE
			db.executeUpdate("UPDATE contents SET title = '"+ newTitle +"'"
					      + " WHERE title = '" + oldTitle + "';"
					      );

			return Response.status(200).build();
			
			
			
		}catch(SQLException ex){
			return Response.status(500).entity("Error").build();
		}
	}
	
	@GET
	@Path("/file/list/{owner}")
	public Response listOwnContent(@PathParam("owner") String owner){
		Statement db = takeDatabase();
		try{
			ResultSet result = db.executeQuery(
					"SELECT title FROM contents "
							+ "WHERE owner ='" + owner + "';" 
					);
			

			List<String> titles = new ArrayList<String>();
			while(result.next())
				titles.add(result.getString("title"));
			
			return Response.status(200).entity(titles).build();
			
		}catch(SQLException e){
			System.out.println(e);
		}
		
		return Response.status(500).entity("Error").build();
		
	}
	
	@GET
	@Path("/{title}/server")
	public Response getServerByTitle(@PathParam("title") String title){
		Statement db = takeDatabase();
		try{
			ResultSet result = db.executeQuery(
					"SELECT serverURL FROM contents "
							+ "WHERE title ='" + title + "';" 
					);
			if (!result.isBeforeFirst())
				return Response.status(404).entity("No server found").build();
				
			result.next();
			return Response.status(200).entity(result.getString("serverURL")).build();
			
		}catch(SQLException e){
			System.out.println(e);
		}
		
		return Response.status(500).entity("Error").build();
		
	}
	
	
///////////////////////////////////////////////////////////
	public Statement takeDatabase(){
		try {
			DataSource database = (DataSource)  new InitialContext().lookup("java:/PostgresXADS");
			Connection connection = database.getConnection();
			Statement statement = connection.createStatement();
			return statement;
		} catch (Exception e) {
			System.out.println("Database could not be loaded");
			return null;
		}
	}
}