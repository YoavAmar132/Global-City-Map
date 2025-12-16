package gcm.server.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbManager {

    // TODO: the sql we will use(adam)
    private static final String URL  = "jdbc:mysql://localhost:3306/GCM_DB";
    private static final String USER = "root";
    private static final String PASS = "Adam199*";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL driver not found", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
