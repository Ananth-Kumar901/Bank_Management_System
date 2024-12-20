import java.sql.Connection;
import java.sql.DriverManager;

class DatabaseConnection {
	    private static final String URL = "jdbc:mysql://localhost:3306/bank_system";
	    private static final String USER = "root";
	    private static final String PASSWORD = "Ananth@2025";

	    public static Connection getConnection() throws Exception {
	        return DriverManager.getConnection(URL, USER, PASSWORD);
	    }
	}

