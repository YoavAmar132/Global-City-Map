package gcm.server.data;

import common.model.Complaint;

import java.sql.*;
import java.util.ArrayList;
import java.util.Optional;

public class ComplaintRepo {

    // 1. Create complaint and return id
    public int createComplaint(int userId, String message) throws SQLException {
        String sql = """
            INSERT INTO SupportTickets (userID, message, ticketStatus)
            VALUES (?, ?, 'Open');
        """;

        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);){
             stmt.setInt(1, userId);
             stmt.setString(2, message);

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

    public boolean hasInProgressComplaint() throws SQLException {
        String sql = """
                    SELECT 1
                    FROM SupportTickets
                    WHERE ticketStatus = 'InProgress'
                    LIMIT 1       
        """;

        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery();){

            return rs.next();
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

            return Optional.of(createComplaintFromResSet(rs));
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


    public boolean closeWithHumanAnswer(int ticketId, String response) throws SQLException {
        String sql = """
            UPDATE SupportTickets
            SET response = ?, ticketStatus = 'Closed', responseBy = 'Human'
            WHERE ticketID = ? AND ticketStatus = 'WaitingForHuman'
        """;
        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);){
            stmt.setString(1, response);
            stmt.setInt(2, ticketId);
            int updatedRows = stmt.executeUpdate();
            return updatedRows > 0;
        }
    }

    public Complaint getComplaint(int complaintId) throws SQLException {
        String sql = """
            SELECT * FROM SupportTickets
            WHERE ticketID = ?
        """;
        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);){
            stmt.setInt(1, complaintId);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) return null;
            return createComplaintFromResSet(rs);
        }
    }
    /**
    * set all complaints that either in status == WaitingForBot or in status == InProgress to waitForCustomerSupport
    */
    public void reassignHangingComplaintsForCustomerSupport() throws SQLException {
        String sql = """
            UPDATE SupportTickets
            SET ticketStatus = 'WaitingForHuman'
            WHERE ticketStatus = 'WaitingForBot' OR ticketStatus = 'InProgress'
        """;
        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);){
            stmt.executeUpdate();
        }
    }

    public ArrayList<Complaint> getUserComplaints(int id) throws SQLException{
        String sql = """
            SELECT * FROM SupportTickets
            WHERE userID = ?
            ORDER BY createdAt
        """;
        ArrayList<Complaint> resultList = new ArrayList<>();;

        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);){
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                resultList.add(createComplaintFromResSet(rs));
            }
            return resultList;
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
                resultList.add(createComplaintFromResSet(rs));
            }
            return resultList;
        }
    }


        private Complaint createComplaintFromResSet(ResultSet rs) throws SQLException {

        Complaint complaint =  new Complaint(
                rs.getInt("userID"),
                rs.getString("message"));
        complaint.setId(rs.getInt("ticketID"));
        complaint.setResponse(rs.getString("response"));//if it's null then response = 0
        complaint.setResponseBy(rs.getString("responseBy"));//if it's null then responseBy = 0
        String status = rs.getString("ticketStatus");
        if(status.equals("Open")){
            complaint.setStatus(Complaint.Status.OPEN);
        }
        else if(status.equals("WaitingForBot")){
            complaint.setStatus(Complaint.Status.WAITING_FOR_BOT);
        }
        else if(status.equals("WaitingForHuman")){
            complaint.setStatus(Complaint.Status.WAITING_FOR_HUMAN);
        }
        else if(status.equals("InProgress")){
            complaint.setStatus(Complaint.Status.IN_PROGRESS);
        }
        else if(status.equals("Closed")){
            complaint.setStatus(Complaint.Status.CLOSED);
        }
        else{
            System.err.println("complaintRepo - Unknown complaint status: " + status);
        }
            return complaint;
    }

}
