//package ServletPack;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.*;

//IN: return JSON array with all friends "email", "longitude", "lattitude"
//OUT: void, update database

@WebServlet("/reportLocation")
public class LocationReportServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    	
        JsonObject body = new Gson().fromJson(req.getReader(), JsonObject.class);
        
        String username = body.get("email").getAsString().trim();
        double lat   = body.get("latitude").getAsDouble();
        double lon   = body.get("longitude").getAsDouble();

        String sql = "UPDATE userInfo SET latitude = ?, longitude = ? WHERE username = ?";
        
        try {
        	Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/trojanMapsDB", "root", "password");
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setDouble(1, lat);
            ps.setDouble(2, lon);
            ps.setString(3, username);

            int updated = ps.executeUpdate();
            
            if (updated == 0) {
                resp.sendError(400, "No user with username " + username);
            } else {
                resp.setStatus(200);
            }
        } catch (SQLException e) {
        }
    }
}
