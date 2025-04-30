package ServletPack;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.*;

@WebServlet("/register")
public class registerServlet extends HttpServlet {
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

        String username    = json.has("username") ? json.get("username").getAsString() : null;
        String password    = json.has("password") ? json.get("password").getAsString() : null;
        String firstName   = json.has("firstName") ? json.get("firstName").getAsString() : null;
        String lastName    = json.has("lastName") ? json.get("lastName").getAsString() : null;
        String phoneNumber = json.has("phoneNumber") ? json.get("phoneNumber").getAsString() : null;


        if (username == null || username.isEmpty() ||
            password == null || password.isEmpty())
        {
            resp.setStatus(400);
            JsonObject err = new JsonObject();
            err.addProperty("error", "username and password are required");
            resp.getWriter().print(gson.toJson(err));
            return;
        }

        String checkSql = "SELECT userID FROM userinfo WHERE username = ?";
        String insertSql = "INSERT INTO userinfo (username, password, firstName, lastName, phoneNumber) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS);
             PreparedStatement checkPs = conn.prepareStatement(checkSql))
        {
            checkPs.setString(1, username);
            try (ResultSet rs = checkPs.executeQuery()) {
                if (rs.next()) {
                    resp.setStatus(400);
                    JsonObject err = new JsonObject();
                    err.addProperty("error", "Username already exists");
                    resp.getWriter().print(gson.toJson(err));
                    return;
                }
            }

            try (PreparedStatement insertPs = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                insertPs.setString(1, username);
                insertPs.setString(2, password);
                insertPs.setString(3, firstName);
                insertPs.setString(4, lastName);
                insertPs.setString(5, phoneNumber);

                int affected = insertPs.executeUpdate();
                if (affected == 0) {
                    resp.setStatus(400);
                    JsonObject err = new JsonObject();
                    err.addProperty("error", "Failed to register user");
                    resp.getWriter().print(gson.toJson(err));
                    return;
                }

                int newId = -1;
                try (ResultSet keys = insertPs.getGeneratedKeys()) {
                    if (keys.next()) {
                        newId = keys.getInt(1);
                    }
                }

                resp.setStatus(200);
                JsonObject data = new JsonObject();
                data.addProperty("userID", newId);
                data.addProperty("username", username);
                data.addProperty("firstName", firstName);
                data.addProperty("lastName", lastName);
                data.addProperty("phoneNumber", phoneNumber);
                resp.getWriter().print(gson.toJson(data));
            }

        } catch (SQLException e) {
            resp.setStatus(400);
            JsonObject err = new JsonObject();
            err.addProperty("error", "SQL error: " + e.getMessage());
            resp.getWriter().print(gson.toJson(err));
        }
    }
}
