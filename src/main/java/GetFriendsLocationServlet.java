package ServletPack;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.*;

//IN: look for JSON with an email
//OUT: return JSON array with all friends "username", "longitude", "lattitude"

@WebServlet("/getFriendsLocation")
public class GetFriendsLocationServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String JDBC_URL  = "jdbc:mysql://localhost:3306/trojanMapsDB";
    private static final String JDBC_USER = "root";
    private static final String JDBC_PASS = "root";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    	
        JsonObject body = new Gson().fromJson(req.getReader(), JsonObject.class);
        
        String email = body.get("email").getAsString().trim();
        if (email.isEmpty()) {
            resp.sendError(400, "Missing email in JSON");
            return;
        }

        String sql =
            "SELECT f.username, f.latitude, f.longitude " +
            "FROM userInfo me " +
            "JOIN userFriendGroup mine ON me.userID = mine.userID " +
            "JOIN userFriendGroup fg   ON mine.groupID = fg.groupID " +
            "JOIN userInfo f           ON fg.userID = f.userID " +
            "WHERE me.email = ? " +
            "  AND f.userID <> me.userID";

        JsonArray arr = new JsonArray();
        try{
        	Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/trojanMapsDB", "root", "root");
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    JsonObject friend = new JsonObject();
                    friend.addProperty("username",  rs.getString("username"));
                    friend.addProperty("latitude",  rs.getDouble("latitude"));
                    friend.addProperty("longitude", rs.getDouble("longitude"));
                    arr.add(friend);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            resp.sendError(500, "Database error");
            return;
        }

        resp.setContentType("application/json");
        resp.getWriter().write(new Gson().toJson(arr));
    }
}
