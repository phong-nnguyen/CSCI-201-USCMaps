import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.*;

@WebServlet("/loginPageBackend")
public class loginPageBackend {
	
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public loginPageBackend() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
        // STEP ONE
        // Get and store all information user entered on HTML
        String logValue   = request.getParameter("log");
        String user       = request.getParameter("user");
        String pass       = request.getParameter("pass");
        String msg        = "Unspecified Error";

        if ("Login".equals(logValue)) {
            // verify credentials
            msg = checkPass(
                "localhost",
                "3306",
                "USCMapsDB",
                "users",
                user,
                pass
            );
        }
        else if ("Register".equals(logValue)) {
            // collect extra fields
            String firstName    = request.getParameter("firstName");
            String lastName     = request.getParameter("lastName");
            String emailAdress  = request.getParameter("emailAdress");
            String phoneNumber  = request.getParameter("phoneNumber");
            msg = addUserToDataBase(
                "localhost",
                "3306",
                "lab_09",
                "users",
                user,
                pass,
                firstName,
                lastName,
                emailAdress,
                phoneNumber
            );
        }
        else if ("Forgot Password".equals(logValue)) {
        }

        response.setContentType("text/plain");
        response.getWriter().println(msg);
    }

    
    public static String checkPass(
        String server, 
        String port, 
        String schema, 
        String table, 
        String user, 
        String pass) {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String msg = "Hello World";
		try {
			//Execute SQL statement in DB
			con = DriverManager.getConnection("jdbc:mysql://" + server + ":" + port + "/" + schema, "root", "root");
			ps = con.prepareStatement("SELECT password FROM " 
	                   + table 
	                   + " WHERE username = ?"
	                   );
			ps.setString(1, user);
			rs = ps.executeQuery();
			
			//If not found/If found
			if (!rs.next()) {
	            msg = "User not found";
	            return msg;
	        }else {
	        	String answer = rs.getString("password");
	        	if (!answer.equals(pass)) {
	                msg = "Password incorrect";
	            }
	            else {
	                msg = "Password correct";
	            }
	        }
			return msg;
		}
		catch(SQLException e){
			System.out.println (e.getMessage());
		}
		finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (ps != null) {
					ps.close();
				}
				if (con != null) {
					con.close();
				}
				} catch (SQLException sqle) {
					System.out.println(sqle.getMessage());
				}
		}
	   return "Unspecified Error";
    }
   
    public static String addUserToDataBase(
		   String server, 
		   String port, 
		   String schema, 
		   String table, 
		   String user, 
		   String pass, 
		   String firstName, 
		   String lastName, 
		   String emailAdress, 
		   String phoneNumber) {
	   	Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String msg = "Hello World";
		try {
			//Execute SQL statement in DB
			con = DriverManager.getConnection("jdbc:mysql://" + server + ":" + port + "/" + schema, "root", "root");
			ps = con.prepareStatement("SELECT 1 FROM " + table + " WHERE username = "+ user);
			rs = ps.executeQuery();
			if (rs.next()) {
	            msg = "User already exists";
	        }
			ps.close();
			rs.close();
			
			ps = con.prepareStatement("INSERT INTO " + table + " (username, password, firstName, lastName, phoneNumber) VALUES (?, ?, ?, ?, ?, ?)");
			 ps.setString(1, user);
	         ps.setString(2, pass);
	         ps.setString(3, firstName);
	         ps.setString(4, lastName);
	         ps.setString(5, emailAdress);
	         ps.setString(6, phoneNumber);
			int impact = ps.executeUpdate();
			if(impact == 1) {
				return "Success";
			}else if(impact == 0){
				return "Add Fails";
			}
		}
		catch(SQLException e){
			System.out.println (e.getMessage());
		}
		finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (ps != null) {
					ps.close();
				}
				if (con != null) {
					con.close();
				}
				} catch (SQLException sqle) {
					System.out.println(sqle.getMessage());
				}
		}
	   return "Unspecified Error";
    }
}

