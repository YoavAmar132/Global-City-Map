package gcm.server.data;

import common.messages.RegisterPayload;
import common.model.User;
import gcm.server.model.UserEntity;

import java.sql.*;
import java.util.ArrayList;

/**
 *  removed loadAllUsers not needed now since we use dp (inefficient when users number grow)
 *  removed getMaxUserId idk why its there xD
 */

public class UserRepo {


    // Find user by username (LOGIN)
    public UserEntity findByUsername(String username) throws SQLException {

        String sql = """
            SELECT UserID, UserName, Password, Role,
                   failedAttempts, isLocked, lockedUntil
            FROM Users
            WHERE UserName = ?
        """;

        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) return null;

                return new UserEntity(
                        rs.getInt("UserID"),
                        rs.getString("UserName"),
                        rs.getString("Password"),   // password HASH
                        rs.getString("Role"),
                        rs.getInt("failedAttempts"),
                        rs.getBoolean("isLocked"),
                        rs.getTimestamp("lockedUntil")
                );
            }
        }
    }

    // Check if username exists
    public boolean existsByUsername(String username) throws SQLException {

        String sql = "SELECT 1 FROM Users WHERE UserName = ?";

        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Insert new user (REGISTER)
    public boolean insertUser(RegisterPayload payload, String role)
            throws SQLException {

        String sql = """
        INSERT INTO Users (UserName, Password, Role, name, surname, phoneNum)
        VALUES (?, ?, ?, ?, ?, ?)
    """;

        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, payload.getUsername());
            stmt.setString(2, payload.getPassword());
            stmt.setString(3, role);
            stmt.setString(4, payload.getFirstname());
            stmt.setString(5, payload.getLastname());

            // phoneNum is INT in DB
            stmt.setInt(6, Integer.parseInt(payload.getPhonenum()));

            return stmt.executeUpdate() == 1;
        }
    }


    // Login SUCCESS
    public void recordLoginSuccess(int userId) throws SQLException {

        String sql = """
            UPDATE Users
            SET lastLogin = NOW(),
                failedAttempts = 0,
                isLocked = FALSE,
                lockedUntil = NULL
            WHERE UserID = ?
        """;

        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }

    // Login FAILURE
    public void recordLoginFailure(int userId, int maxAttempts, int lockMinutes)
            throws SQLException {

        String sql = """
        UPDATE Users
        SET failedAttempts = failedAttempts + 1,
            isLocked = CASE
                WHEN failedAttempts  >= ? THEN TRUE
                ELSE isLocked
            END,
            lockedUntil = CASE
                WHEN failedAttempts  >= ?
                     AND lockedUntil IS NULL
                THEN DATE_ADD(NOW(), INTERVAL ? MINUTE)
                ELSE lockedUntil
            END
        WHERE UserID = ?
    """;

        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, maxAttempts);
            stmt.setInt(2, maxAttempts);
            stmt.setInt(3, lockMinutes);
            stmt.setInt(4, userId);

            stmt.executeUpdate();
        }
    }


    //does as it says :D
    public void unlockIfExpired(int userId) throws SQLException {

        String sql = """
        UPDATE Users
        SET isLocked = FALSE,
            failedAttempts = 0,
            lockedUntil = NULL
        WHERE UserID = ?
          AND isLocked = TRUE
          AND (
                lockedUntil IS NULL
                OR lockedUntil <= NOW()
              )
    """;

        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }
    public ArrayList<Integer> getAllId() throws SQLException {

        ArrayList<Integer> userIds = new ArrayList<>();

        String sql = "SELECT UserID FROM Users";

        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                userIds.add(rs.getInt("UserID"));
            }
        }

        return userIds;
    }
    public ArrayList<RegisterPayload> getAllUsers() throws SQLException {

        ArrayList<RegisterPayload> users = new ArrayList<>();

        String sql = """
    SELECT UserID, UserName, Password, name, surname, phoneNum,role
    FROM Users
    WHERE role = 'Customer'
""";


        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {

                RegisterPayload user = new RegisterPayload(

                        rs.getString("UserName"),
                        rs.getString("Password"),
                        rs.getString("name"),
                        rs.getString("surname"),
                        rs.getString("phoneNum"),
                        "",""



                );
               user.setUserid(rs.getInt("UserID"));
               user.setRole(rs.getString("role"));

                users.add(user);
            }
        }

        return users;
    }
    public ArrayList<RegisterPayload> getAllWorkers() throws SQLException {

        ArrayList<RegisterPayload> users = new ArrayList<>();

        String sql = """
    SELECT UserID, UserName, Password, name, surname, phoneNum,role
    FROM Users
    WHERE role <> 'Customer'
""";



        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {

                RegisterPayload user = new RegisterPayload(

                        rs.getString("UserName"),
                        rs.getString("Password"),
                        rs.getString("name"),
                        rs.getString("surname"),
                        rs.getString("phoneNum"),
                        "",""



                );
                user.setUserid(rs.getInt("UserID"));
                user.setRole(rs.getString("role"));

                users.add(user);
            }
        }

        return users;
    }
    public int gotMail(int userId) {

        String sql = """
        SELECT COUNT(*) AS msgCount
        FROM Messages
        WHERE UserID = ?
    """;

        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("msgCount");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0; // no messages or error
    }





}

