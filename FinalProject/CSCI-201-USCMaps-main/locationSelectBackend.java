// locationSelectBackend.java
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.*;

@WebServlet("/locationSelectBackend")
public class locationSelectBackend extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        // STEP ONE
        // Get and store all information user entered on HTML
        String address = request.getParameter("address");

        // STEP TWO
        // look up lat/lon by address
        String result = findLocationByAddress(
            "localhost",
            "3306",
            "lab_09",
            "Location",
            address
        );

        response.setContentType("text/plain");
        response.getWriter().println(result);
    }

    public static String findLocationByAddress(
        String server,
        String port,
        String schema,
        String table,
        String address
    ) {
        Connection        con = null;
        PreparedStatement ps  = null;
        ResultSet         rs  = null;
        String            msg = "Hello World";

        try {
            // 1) connect
            con = DriverManager.getConnection(
                "jdbc:mysql://" + server + ":" + port + "/" + schema,
                "root",
                "root"
            );
            // 2) look up latitude & longitude by address
            ps = con.prepareStatement(
                "SELECT latitude, longitude FROM " + table + " WHERE address = ?"
            );
            ps.setString(1, address);
            rs = ps.executeQuery();

            if (rs.next()) {
                double lat = rs.getDouble("latitude");
                double lon = rs.getDouble("longitude");
                return lat + "," + lon;
            } else {
                return "Address not found";
            }
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
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

    public static String addLocationToDataBase(
        String server,
        String port,
        String schema,
        String table,
        String username,
        String address,
        double latitude,
        double longitude
    ) {
        Connection        con = null;
        PreparedStatement ps  = null;
        ResultSet         rs  = null;
        String            msg = "Hello World";

        try {
            // 1) connect
            con = DriverManager.getConnection(
                "jdbc:mysql://" + server + ":" + port + "/" + schema,
                "root",
                "root"
            );
            // 2) check for existing address
            ps = con.prepareStatement(
                "SELECT 1 FROM " + table + " WHERE address = ?"
            );
            ps.setString(1, address);
            rs = ps.executeQuery();
            if (rs.next()) {
                return "Address already exists";
            }
            ps.close();
            rs.close();

            // 3) insert new location (locationID auto‐increments)
            ps = con.prepareStatement(
                "INSERT INTO " + table +
                " (username, address, latitude, longitude)" +
                " VALUES (?, ?, ?, ?)"
            );
            ps.setString(1, username);
            ps.setString(2, address);
            ps.setDouble(3, latitude);
            ps.setDouble(4, longitude);
            int impact = ps.executeUpdate();
            if (impact == 1) {
                return "Success";
            } else if (impact == 0) {
                return "Add Fails";
            }
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
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
