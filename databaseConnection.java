import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    
    public static Connection getConnection() throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");
    
        String sqlUrl = "DATABASE_URL";
        String sqlUser = "DATABASE_USER";
        String sqlPassword = "DATABASE_PASSWORD";
        
        return DriverManager.getConnection(sqlUrl, sqlUser, sqlPassword);
    }
} 