import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@WebServlet("/loginPageBackend")
public class loginPageBackend extends HttpServlet {
	private static final long serialVersionUID = 1L;

    private static final Map<String, String> credentialCache = new ConcurrentHashMap<>();

	@Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // STEP ONE
        // Get and store all information user entered on HTML
        String logValue   = request.getParameter("log");
        String user       = request.getParameter("user");
        String pass       = request.getParameter("pass");
        String msg        = "Unspecified Error";

        if ("Login".equals(logValue)) {
            // verify credentials
            msg = checkPass(user, pass);
        }
        else if ("Register".equals(logValue)) {
            // collect extra fields
            String firstName    = request.getParameter("firstName");
            String lastName     = request.getParameter("lastName");
            String email  = request.getParameter("emailAdress");
            String phoneNumber  = request.getParameter("phoneNumber");
            msg = addUserToDataBase(user, pass, firstName, lastName, email, phoneNumber);
        }
        else if ("Forgot Password".equals(logValue)) {
        }

        response.setContentType("text/plain");
        response.getWriter().println(msg);
    }

    private String checkPass(String user, String pass) {

        String stored = credentialCache.get(user);
        if (stored != null) {
            return stored.equals(pass) ? "Success": "Password incorrect";
        }

        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = databaseConnection.getConnection();
            ps = con.prepareStatement("SELECT password FROM userInfo WHERE username = ?");
            ps.setString(1, user);
            rs = ps.executeQuery();
            
            if (!rs.next()) {
                return "User not found";
            }
            
            String storedPass = rs.getString("password");
            return storedPass.equals(pass) ? "Success" : "Password incorrect";
            
        } catch(SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            return "Database error: " + e.getMessage();
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
   
    private String addUserToDataBase(
        String user, 
        String pass, 
        String firstName, 
        String lastName, 
        String email, 
        String phoneNumber) {
        
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            con = databaseConnection.getConnection();
            
            // First check if user exists
            ps = con.prepareStatement("SELECT 1 FROM userInfo WHERE username = ?");
            ps.setString(1, user);
            rs = ps.executeQuery();
            
            if (rs.next()) {
                return "User already exists";
            }
            
            // Insert new user
            ps = con.prepareStatement(
                "INSERT INTO userInfo (username, password, firstName, lastName, email, phoneNumber) " +
                "VALUES (?, ?, ?, ?, ?, ?)"
            );
            
            ps.setString(1, user);
            ps.setString(2, pass);
            ps.setString(3, firstName);
            ps.setString(4, lastName);
            ps.setString(5, email);
            ps.setString(6, phoneNumber);
            
            int result = ps.executeUpdate();
            if (result == 1) {
                    credentialCache.put(user, pass);
                    return "Success";
                } else {
                    return "Registration failed";
            }
            
        } catch(SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            return "Database error: " + e.getMessage();
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}

