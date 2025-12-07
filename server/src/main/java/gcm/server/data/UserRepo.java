package gcm.server.data;

import common.model.User ;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserRepo {

    // Load ALL users into an ArrayList from MySQL
    public List<User> loadAllUsers() throws SQLException {
        String sql = "SELECT id, username, password, role FROM users"; // adjust column names

        List<User> users = new ArrayList<>();

        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id          = rs.getInt("id");
                String username = rs.getString("username");
                String password = rs.getString("password"); // or password_hash
                String role     = rs.getString("role");      // or default "CUSTOMER"

                users.add(new User(id, username, password, role));
            }
        }

        return users;
    }

    // Example: load one user by username (useful for login)
    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT id, username, password, role FROM users WHERE username = ?";

        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) return null;

                int id          = rs.getInt("id");
                String uname    = rs.getString("username");
                String password = rs.getString("password");
                String role     = rs.getString("role");

                return new User(id, uname, password, role);
            }
        }
    }
}
