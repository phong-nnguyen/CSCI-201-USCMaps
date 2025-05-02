package ServletPack;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.*;

@WebServlet("/getLocation")
public class getLocationServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final String JDBC_URL  = "jdbc:mysql://localhost:3306/uscmapsdb";
    private static final String JDBC_USER = "root";
    private static final String JDBC_PASS = "root";

    private final Gson gson = new Gson();

    @Override
    public void init() throws ServletException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new ServletException("JDBC Driver not found", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException
    {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        BufferedReader reader = req.getReader();
        JsonObject json = gson.fromJson(reader, JsonObject.class);

        String adress;
        if(json.has("adress")){
            adress = json.get("address").getAsString();
        }else{
            adress = null;
        }
        
        if (address == null || address.trim().isEmpty()) {
            resp.setStatus(400);
            JsonObject err = new JsonObject();
            err.addProperty("error", "Missing or empty 'address' parameter");
            resp.getWriter().print(gson.toJson(err));
            return;
        }

        String sql = "SELECT lattitude, longitude FROM locations WHERE address = ?";
        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS);
             PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setString(1, address.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double lat  = rs.getDouble("lattitude");
                    double lon  = rs.getDouble("longitude");

                    JsonObject result = new JsonObject();
                    result.addProperty("address", address);
                    result.addProperty("lattitude", lat);
                    result.addProperty("longitude", lon);

                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().print(gson.toJson(result));
                } else {
                    // not found
                    resp.setStatus(400);
                    JsonObject err = new JsonObject();
                    err.addProperty("error", "No entry for address: " + address);
                    resp.getWriter().print(gson.toJson(err));
                }
            }
        } catch (SQLException e) {
            resp.setStatus(400);
            JsonObject err = new JsonObject();
            err.addProperty("error", "Database error: " + e.getMessage());
            resp.getWriter().print(gson.toJson(err));
        }
    }
}
