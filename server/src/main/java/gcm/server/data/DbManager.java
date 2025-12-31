package gcm.server.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbManager {

    private static String url;
    private static String user;
    private static String pass;

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL driver not found", e);
        }
    }

    //we will get those values from user (default will be root and jdbc:mysql://localhost:3306/GCM_DB)
    public static void init(String jdbcURL, String username, String password) {
        url = jdbcURL;
        user = username;
        pass = password;
    }

    public static Connection getConnection() throws SQLException {
        if (pass == null || pass.isBlank()) {
            throw new IllegalStateException("Database password not set");
        }
        return DriverManager.getConnection(url, user, pass);
    }
}
