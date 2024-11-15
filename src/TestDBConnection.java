
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TestDBConnection {
    private static final String DB_URL = "jdbc:oracle:thin:@localhost:1521:YOURDB"; // Replace YOURDB
    private static final String USER = "SYSTEM"; // Your MySQL username
    private static final String PASSWORD = "nishitha"; // Your MySQL password

    public static void main(String[] args) {
        try {
            // Load the Oracle JDBC driver
            Class.forName("oracle.jdbc.OracleDriver");
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
            System.out.println("Connected to the database!");
            conn.close(); // Close the connection after testing
        } catch (SQLException e) {
            System.out.println("SQL Exception: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.out.println("Driver not found: " + e.getMessage());
        }
    }
}
