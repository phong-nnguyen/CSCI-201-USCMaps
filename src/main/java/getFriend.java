import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Servlet implementation class getFriend
 */
@WebServlet("/getFriend")
public class getFriend extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public getFriend() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/json");
		PrintWriter clientIn = response.getWriter();
		Connection DBConnection = null;
		
//		String emailAddress = request.getParameter("emailAddress");
		String username = request.getParameter("username");

		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			DBConnection = DriverManager.getConnection("jdbc:mysql://localhost/trojanMapsDB", "root", "password");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		JsonArray friends = new JsonArray();

		try {
//			String friendQuery = 
//				    "SELECT ui2.email " +
//				    "FROM userInfo ui1 " +
//				    "JOIN userFriendGroup ufg1 ON ui1.userID = ufg1.userID " +
//				    "JOIN userFriendGroup ufg2 ON ufg1.groupID = ufg2.groupID " +
//				    "JOIN userInfo ui2 ON ufg2.userID = ui2.userID " +
//				    "WHERE ui1.email = ? AND ui2.email != ui1.email";
			String friendQuery = 
				    "SELECT ui2.email " +
				    "FROM userInfo ui1 " +
				    "JOIN userFriendGroup ufg1 ON ui1.userID = ufg1.userID " +
				    "JOIN userFriendGroup ufg2 ON ufg1.groupID = ufg2.groupID " +
				    "JOIN userInfo ui2 ON ufg2.userID = ui2.userID " +
				    "WHERE ui1.username = ? AND ui2.email != ui1.email";
//			PreparedStatement friendStatement = DBConnection.prepareStatement(friendQuery);
//			friendStatement.setString(1, emailAddress);
			
			PreparedStatement friendStatement = DBConnection.prepareStatement(friendQuery);
			friendStatement.setString(1, username);
			
			ResultSet returnQuery = friendStatement.executeQuery();
			
			// Check if there are any favorites at all
			
			while (returnQuery.next()) {
			    JsonObject friend = new JsonObject();
			    friend.addProperty("email", returnQuery.getString("email"));
			    friends.add(friend);
			}
			
			friendStatement.close();
			returnQuery.close();
			DBConnection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		clientIn.print(friends);
		clientIn.flush();
		clientIn.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
