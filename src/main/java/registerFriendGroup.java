import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Servlet implementation class registerFriendGroup
 */
@WebServlet("/registerFriendGroup")
public class registerFriendGroup extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public registerFriendGroup() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/json");
		PrintWriter clientIn = response.getWriter();
		BufferedReader reader = request.getReader();
		JsonObject jsonObj = JsonParser.parseReader(reader).getAsJsonObject();
		Connection DBConnection = null;
		
		try {
	        Class.forName("com.mysql.cj.jdbc.Driver");
	    } catch (ClassNotFoundException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    }
		
		try {
			DBConnection = DriverManager.getConnection("jdbc:mysql://localhost/trojanMapsDB", "root", "password");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		try {
			// Get the main user
			String getUserIDQuery = "SELECT userInfo.userID FROM userInfo WHERE username = ?";
			PreparedStatement userIDStatement = DBConnection.prepareStatement(getUserIDQuery);
			userIDStatement.setString(1, jsonObj.get("emailOne").getAsString());
			
			ResultSet mainUserResult = userIDStatement.executeQuery();
			
			PreparedStatement emailTwoIDStatement = DBConnection.prepareStatement(getUserIDQuery);
			emailTwoIDStatement.setString(1, jsonObj.get("emailTwo").getAsString());
			ResultSet emailTwoUserResult = emailTwoIDStatement.executeQuery();
			PreparedStatement emailThreeIDStatement = DBConnection.prepareStatement(getUserIDQuery);
			emailThreeIDStatement.setString(1, jsonObj.get("emailThree").getAsString());
			ResultSet emailThreeUserResult = emailThreeIDStatement.executeQuery();
			
			// User doesn't exist
			if(!mainUserResult.next() || !emailTwoUserResult.next() || !emailThreeUserResult.next()) {
				clientIn.print("{\"Error\": \"One of the emails do not exist.\"}");
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				clientIn.flush();
				reader.close();
				DBConnection.close();
				return;
			}
			
			// Check if the group has already been created
			String checkGroupNameQuery = "SELECT * FROM friendGroups WHERE groupName = ?";
			PreparedStatement checkGroupName = DBConnection.prepareStatement(checkGroupNameQuery);
			checkGroupName.setString(1, jsonObj.get("groupName").getAsString());
			ResultSet checkGroupNameRS = checkGroupName.executeQuery();
			
			// Check if the main user belongs to the group
//			String checkMainUserBelongQuery = "SELECT userFriendGroup.groupName FROM userFriendGroup WHERE userID = ? AND groupName = ?";
//			PreparedStatement checkMainUserBelong = DBConnection.prepareStatement(checkMainUserBelongQuery);
//			checkMainUserBelong.setString(1, mainUserResult.getString("userID"));
//			checkMainUserBelong.setString(2, jsonObj.get("groupName").getAsString());
//			ResultSet checkMainUserBelongRS = checkMainUserBelong.executeQuery();
			
			boolean groupExists = false;
			
			// Check for if the main user belongs to the group if it exists and if the group actually exists
			if(checkGroupNameRS.next()) {
				groupExists = true;
				clientIn.print("{\"Error\": \"Group name already exists\"}");
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				groupExists = true;
				
				clientIn.flush();
				reader.close();
				DBConnection.close();
				return;
			}
//			else if(checkMainUserBelongRS.next()) {
//				// For front-end: check the status code of the response and print out the error of the returned JSON Object that only has Error in it
//				clientIn.print("{\"Error\": \"Group name already exists\"}");
//				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//				groupExists = true;
//				
//				clientIn.flush();
//				reader.close();
//				DBConnection.close();
//				return;
//			}
			
//			if(groupExists) {
//				// Create group and add in new user
//				String createGroupAndAddUsersQuery = "INSERT INTO userFriendGroup (userID, groupName) VALUES (?,?)";
//				PreparedStatement createGroupAndAddUsers = DBConnection.prepareStatement(createGroupAndAddUsersQuery);
//				createGroupAndAddUsers.setString(1, emailTwoUserResult.getString("userID"));
//				createGroupAndAddUsers.setString(2, jsonObj.get("groupName").getAsString());
//				
//				response.setStatus(HttpServletResponse.SC_OK);
//			}else {
//				// Create group and add in new user
//				String createGroupAndAddUsersQuery = "INSERT INTO userFriendGroup (userID, groupName) VALUES (?,?), (?,?)";
//				PreparedStatement createGroupAndAddUsers = DBConnection.prepareStatement(createGroupAndAddUsersQuery);
//				createGroupAndAddUsers.setString(1, mainUserResult.getString("userID"));
//				createGroupAndAddUsers.setString(2, jsonObj.get("groupName").getAsString());
//				createGroupAndAddUsers.setString(3, emailTwoUserResult.getString("userID"));
//				createGroupAndAddUsers.setString(4, jsonObj.get("groupName").getAsString());
//				response.setStatus(HttpServletResponse.SC_OK);
//			}
			
			// Insert friendGroup
			String createFriendGroupQuery = "INSERT INTO friendGroups (groupName) VALUES (?)";
			PreparedStatement createFriendGroup = DBConnection.prepareStatement(createFriendGroupQuery);
			createFriendGroup.setString(1, jsonObj.get("groupName").getAsString());
			createFriendGroup.executeUpdate();
			
			// Get groupID
			String groupIDQuery = "SELECT * FROM friendGroups WHERE groupName = ?";
			PreparedStatement groupID = DBConnection.prepareStatement(groupIDQuery);
			groupID.setString(1, jsonObj.get("groupName").getAsString());
			ResultSet groupIDRS = groupID.executeQuery();
			groupIDRS.next();
			
			// Inputs
			int groupIDInput = groupIDRS.getInt("groupID");
			int emailOne = mainUserResult.getInt("userID");
			int emailTwo = emailTwoUserResult.getInt("userID");
			int emailThree = emailThreeUserResult.getInt("userID");
			
			String createGroupAndAddUsersQuery = "INSERT INTO userFriendGroup (groupID, userID) VALUES (?,?), (?,?), (?,?)";
			PreparedStatement createGroupAndAddUsers = DBConnection.prepareStatement(createGroupAndAddUsersQuery);
			createGroupAndAddUsers.setInt(1, groupIDInput);
			createGroupAndAddUsers.setInt(2, emailOne);
			createGroupAndAddUsers.setInt(3, groupIDInput);
			createGroupAndAddUsers.setInt(4, emailTwo);
			createGroupAndAddUsers.setInt(5, groupIDInput);
			createGroupAndAddUsers.setInt(6, emailThree);
			createGroupAndAddUsers.executeUpdate();
			response.setStatus(HttpServletResponse.SC_OK);
			
			
			clientIn.print("{\"Success\": \"Successful registration\"}");
			clientIn.flush();
			reader.close();
			DBConnection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
