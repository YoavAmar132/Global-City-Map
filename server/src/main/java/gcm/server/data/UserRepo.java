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
    public int getMaxUserId() {
        String sql = "SELECT MAX(id) AS max_id FROM users";

        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("max_id");  // returns max id, or 0 if table is empty
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0; // safe fallback if no users exist
    }
    public boolean insertUser(User user) {
        String sql = "INSERT INTO users (id, username, password, role) VALUES (?, ?, ?, ?)";

        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, user.getId());
            stmt.setString(2, user.getUsername());
            stmt.setString(3, user.getPassword());
            stmt.setString(4, user.getRole());

            int rows = stmt.executeUpdate();
            return rows > 0;   // true if inserted successfully

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }



}
