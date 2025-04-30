package ServletPack;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URLEncoder;
import java.sql.*;

@WebServlet("/login")
public class loginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String JDBC_URL  = "jdbc:mysql://localhost:3306/uscmapsdb";
    private static final String JDBC_USER = "root";
    private static final String JDBC_PASS = "root";

    private final Gson gson = new Gson();

    @Override
    public void init() throws ServletException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException cnfe) {
            throw new ServletException("JDBC Driver not found", cnfe);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException 
    {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        BufferedReader reader = req.getReader();
        JsonObject json = gson.fromJson(reader, JsonObject.class);

        String username = json.has("username") ? json.get("username").getAsString() : null;
        String password = json.has("password") ? json.get("password").getAsString() : null;


        if (username == null || username.isEmpty() ||
            password == null || password.isEmpty())
        {
            resp.setStatus(400);
            JsonObject err = new JsonObject();
            err.addProperty("error", "username and password are required");
            resp.getWriter().print(gson.toJson(err));
            return;
        }

        String sql = "SELECT userID, password FROM userinfo WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS);
             PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    resp.setStatus(400);
                    JsonObject err = new JsonObject();
                    err.addProperty("error", "Invalid username or password");
                    resp.getWriter().print(gson.toJson(err));
                    return;
                }

                String storedPw = rs.getString("password");
                int userID      = rs.getInt("userID");

                if (!storedPw.equals(password)) {
                    resp.setStatus(400);
                    JsonObject err = new JsonObject();
                    err.addProperty("error", "Invalid username or password");
                    resp.getWriter().print(gson.toJson(err));
                    return;
                }

                // success: set session & cookie
                HttpSession session = req.getSession();
                session.setAttribute("userID", userID);
                session.setAttribute("username", username);

                Cookie userCookie = new Cookie("username", URLEncoder.encode(username, "UTF-8"));
                userCookie.setPath("/");
                userCookie.setMaxAge(7 * 24 * 60 * 60);
                resp.addCookie(userCookie);

                // return JSON
                resp.setStatus(200);
                JsonObject data = new JsonObject();
                data.addProperty("userID", userID);
                data.addProperty("username", username);
                resp.getWriter().print(gson.toJson(data));
            }
        } catch (SQLException e) {
            resp.setStatus(400);
            JsonObject err = new JsonObject();
            err.addProperty("error", "Database error: " + e.getMessage());
            resp.getWriter().print(gson.toJson(err));
        }
    }
}
