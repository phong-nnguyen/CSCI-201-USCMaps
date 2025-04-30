import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;

@WebServlet("/api/locations")
public class googleMapsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        List<Location> locations = new ArrayList<>();
        
        try (Connection conn = databaseConnection.getConnection()) {
            String sql = "SELECT * FROM locations";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    Location location = new Location(
                        rs.getInt("locationID"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getString("address"),
                        rs.getDouble("lattitude"),
                        rs.getDouble("longitude")
                    );
                    locations.add(location);
                }
            }
            
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Database error\"}");
            return;
        }
        
        Gson gson = new Gson();
        String jsonResponse = gson.toJson(locations);
        response.getWriter().write(jsonResponse);
    }
    
    private static class Location {
        private int locationID;
        private String name;
        private String category;
        private String address;
        private double latitude;
        private double longitude;
        
        public Location(int locationID, String name, String category, String address, 
                       double latitude, double longitude) {
            this.locationID = locationID;
            this.name = name;
            this.category = category;
            this.address = address;
            this.latitude = latitude;
            this.longitude = longitude;
        }
        
        // Getters
        public int getLocationID() { return locationID; }
        public String getName() { return name; }
        public String getCategory() { return category; }
        public String getAddress() { return address; }
        public double getLatitude() { return latitude; }
        public double getLongitude() { return longitude; }
    }
} 