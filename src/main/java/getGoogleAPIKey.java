import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Servlet implementation class ArtsyTokenServlet
 */
@WebServlet("/getGoogleAPIKey")
public class getGoogleAPIKey extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public getGoogleAPIKey() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// Update token if it is out of date or generate one if there is none to be
		// found
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/plain");
		PrintWriter clientIn = response.getWriter();
		Connection DBConnection = null;

		try {
			// Necessary due to newer version, referenced here: https://stackoverflow.com/questions/63122449/class-fornamecom-mysql-jdbc-driver-returns-a-classnotfound-exception-in-ecli/63122627#63122627
			Class.forName("com.mysql.cj.jdbc.Driver");
			DBConnection = DriverManager.getConnection("jdbc:mysql://localhost/trojanMapsDB", "root", "Philbert1108");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		String tokenQuery = "SELECT keyVal FROM googleMapKey WHERE keyID=1";
		String token = null;

		try {
			Statement tokenStatement = DBConnection.createStatement();
			ResultSet returnQuery = tokenStatement.executeQuery(tokenQuery);
			if (returnQuery.next()) {
				token = returnQuery.getString("keyVal");
			}
			tokenStatement.close();
			returnQuery.close();
			DBConnection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		clientIn.print(token);
		clientIn.flush();
		clientIn.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}
}
