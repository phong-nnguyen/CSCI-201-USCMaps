import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class databaseConnection {
    
    public static Connection getConnection() throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        
        String sqlUrl = "jdbc:mysql://localhost:3306/trojanMapsDb";
        String sqlUser = "root";
        String sqlPassword = "root";
        
        return DriverManager.getConnection(sqlUrl, sqlUser, sqlPassword);
    }
} 