package gcm.server.data;

import common.model.Complaint;

import java.sql.*;
import java.util.ArrayList;
import java.util.Optional;

public class ComplaintRepo {

    // 1. Create complaint and return id
    public int createComplaint(int userId, String message,int previousComplaintId) throws SQLException {
        String sql = """
            INSERT INTO SupportTickets (userID, message, ticketStatus,previousComplaintId)
            VALUES (?, ?, 'Open',?);
        """;

        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);){
             stmt.setInt(1, userId);
             stmt.setString(2, message);
             if(previousComplaintId != 0){
                 stmt.setInt(3, previousComplaintId);
             }
             else{
                 stmt.setNull(3, Types.INTEGER);
             }

             stmt.executeUpdate();
             ResultSet generatedKeys = stmt.getGeneratedKeys();
             if (generatedKeys.next()) {
                 return generatedKeys.getInt(1);
             }
             return 0;
        }
    }

    // 5. Escalate
    public void setWaitingForBot(int ticketId) throws SQLException {
        String sql = """
            UPDATE SupportTickets
            SET ticketStatus = 'WaitingForBot'
            WHERE ticketID = ?
        """;
        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);){
            stmt.setInt(1, ticketId);
            stmt.executeUpdate();
        }
    }

    public void setWaitingForHuman(int ticketId) throws SQLException {
        String sql = """
            UPDATE SupportTickets
            SET ticketStatus = 'WaitingForHuman'
            WHERE ticketID = ?
        """;
        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);){
            stmt.setInt(1, ticketId);
            stmt.executeUpdate();
        }
    }



    // 2. Atomically claim next complaint
    public boolean claimNextComplaint() throws SQLException {
        String sql = """
        UPDATE SupportTickets st
        JOIN (
            SELECT ticketID
            FROM SupportTickets
            WHERE ticketStatus = 'WaitingForBot'
            ORDER BY createdAt
            LIMIT 1
        ) next
        ON st.ticketID = next.ticketID
        SET st.ticketStatus = 'InProgress'
    """;

        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            return stmt.executeUpdate() == 1;
        }
    }


    /*
    *
        if(!hasComplaintWaitingForBot())
            return false;
            * */
    // 2. Atomically claim next complaint
    public boolean hasComplaintWaitingForBot() throws SQLException {
        String sql = """
                SELECT ticketID FROM SupportTickets
                WHERE ticketStatus = 'WaitingForBot'
                ORDER BY createdAt
                LIMIT 1
        """;

        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery();){

            return rs.next();
        }
    }

    // 3. Fetch current complaint
    public Optional<Complaint> getNextInProgressComplaint() throws SQLException {
        String sql = """
            SELECT * FROM SupportTickets
            WHERE ticketStatus = 'InProgress'
            ORDER BY createdAt
            LIMIT 1
        """;

        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery();){

            if (!rs.next()) return Optional.empty();

            return Optional.of(mapRow(rs));
        }
    }

    // 4. Bot answered
    public void closeWithBotAnswer(int ticketId, String response) throws SQLException {
        String sql = """
            UPDATE SupportTickets
            SET response = ?, ticketStatus = 'Closed', responseBy = 'Bot'
            WHERE ticketID = ?
        """;
        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);){
            stmt.setString(1, response);
            stmt.setInt(2, ticketId);
            stmt.executeUpdate();
        }
    }


    public void closeWithHumanAnswer(int ticketId, String response) throws SQLException {
        String sql = """
            UPDATE SupportTickets
            SET response = ?, ticketStatus = 'Closed', responseBy = 'Human'
            WHERE ticketID = ?
        """;
        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);){
            stmt.setString(1, response);
            stmt.setInt(2, ticketId);
            stmt.executeUpdate();
        }
    }

    public Complaint getComplaint(int ticketId) throws SQLException {
        String sql = """
            SELECT * FROM SupportTickets
            WHERE ticketID = ?
        """;
        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);){
            stmt.setInt(1, ticketId);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) return null;
            return mapRow(rs);
        }
    }


    public ArrayList<Complaint> getComplaintsWaitingForHuman() throws SQLException {
        String sql = """
            SELECT * FROM SupportTickets
            WHERE ticketStatus = 'WaitingForHuman'
            ORDER BY createdAt
        """;
        ArrayList<Complaint> resultList = new ArrayList<>();;

        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery();){
            while (rs.next()) {
                resultList.add(mapRow(rs));
            }
            return resultList;
        }
    }


        private Complaint mapRow(ResultSet rs) throws SQLException {

        Complaint complaint =  new Complaint(
                rs.getInt("userID"),
                rs.getString("message"));
        complaint.setId(rs.getInt("ticketID"));
        complaint.setResponse(rs.getString("response"));//if it's null then response = 0
        complaint.setResponseBy(rs.getString("responseBy"));//if it's null then responseBy = 0
        complaint.setPreviousComplaintId(rs.getInt("previousComplaintId"));//if it's null then previousComplaintId = 0

        return complaint;
    }
}
