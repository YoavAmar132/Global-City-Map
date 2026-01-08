package gcm.server.data;

import common.model.CityReportData;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class StatsRepo {

    public List<CityReportData> generateReport(LocalDate from, LocalDate to, int cityId) {
        List<CityReportData> reports = new ArrayList<>();

        // יצירת טווח תאריכים שמכסה את כל היום (00:00 עד 23:59)
        LocalDateTime fromDateTime = from.atStartOfDay();
        LocalDateTime toDateTime = to.atTime(LocalTime.MAX);

        String sqlCities = (cityId == 0) ?
                "SELECT CityID, CityName FROM Cities" :
                "SELECT CityID, CityName FROM Cities WHERE CityID = ?";

        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlCities)) {

            if (cityId != 0) stmt.setInt(1, cityId);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int currentCityId = rs.getInt("CityID");
                String cityName = rs.getString("CityName");

                try {
                    // 1. ספירת מפות (לפי ID)
                    int maps = count(conn, "SELECT COUNT(*) FROM Maps WHERE cityID = ?", currentCityId);

                    // 2. ספירת רכישות (לפי שם העיר - כפי שראינו בטבלה שלך)
                    int purchases = 0;
                    String purchaseSql = "SELECT COUNT(*) FROM Purchases WHERE CityName = ? AND PurchaseDate BETWEEN ? AND ?";
                    try (PreparedStatement pStmt = conn.prepareStatement(purchaseSql)) {
                        pStmt.setString(1, cityName);
                        pStmt.setTimestamp(2, Timestamp.valueOf(fromDateTime));
                        pStmt.setTimestamp(3, Timestamp.valueOf(toDateTime));
                        ResultSet pRs = pStmt.executeQuery();
                        if (pRs.next()) {
                            purchases = pRs.getInt(1);
                        }
                    }

                    // 3. ספירת מנויים (לפי CityID ו-StartDate) -> התיקון כאן!
                    int subs = countDateRange(conn,
                            "SELECT COUNT(*) FROM Subscriptions WHERE CityID = ? AND StartDate BETWEEN ? AND ?",
                            currentCityId, fromDateTime, toDateTime);

                    // 4. ספירת צפיות (לפי CityID)
                    int views = countDateRange(conn,
                            "SELECT COUNT(*) FROM ViewLogs WHERE CityID = ? AND viewDate BETWEEN ? AND ?",
                            currentCityId, fromDateTime, toDateTime);

                    // הוספת השורה לדו"ח
                    reports.add(new CityReportData(cityName, maps, purchases, subs, views));

                } catch (SQLException e) {
                    System.err.println("Error calculating stats for city: " + cityName);
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reports;
    }

    // פונקציות עזר
    private int count(Connection conn, String sql, int id) throws SQLException {
        try (PreparedStatement s = conn.prepareStatement(sql)) {
            s.setInt(1, id);
            ResultSet r = s.executeQuery();
            return r.next() ? r.getInt(1) : 0;
        }
    }

    private int countDateRange(Connection conn, String sql, int id, LocalDateTime from, LocalDateTime to) throws SQLException {
        try (PreparedStatement s = conn.prepareStatement(sql)) {
            s.setInt(1, id);
            s.setTimestamp(2, java.sql.Timestamp.valueOf(from));
            s.setTimestamp(3, java.sql.Timestamp.valueOf(to));
            ResultSet r = s.executeQuery();
            return r.next() ? r.getInt(1) : 0;
        }
    }

    public boolean expDate(int userId) throws SQLException {

        String sql = """
        SELECT EndDate
        FROM Subscriptions
        WHERE UserID = ?
    """;

        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {

                if (!rs.next()) {
                    return false; // no subscription found
                }

                Timestamp endTimestamp = rs.getTimestamp("EndDate");
                if (endTimestamp == null) {
                    return false;
                }

                LocalDateTime endDate = endTimestamp.toLocalDateTime();
                LocalDateTime now = LocalDateTime.now();

                // already expired → false
                if (endDate.isBefore(now)) {
                    return false;
                }

                // difference in hours
                long hoursLeft = ChronoUnit.HOURS.between(now, endDate);

                return hoursLeft <= 72; // 3 days = 72 hours
            }
        }
    }
    public ArrayList<String> getHistory(int userId) {

        ArrayList<String> history = new ArrayList<>();

        // 1) Purchases
        String purchasesSql = """
        SELECT PurchaseDate, CityName, mapVersion, DownloadUsed
        FROM Purchases
        WHERE UserID = ?
        ORDER BY PurchaseDate DESC
    """;

        // 2) Subscriptions (join Cities to get a readable city name)
        String subsSql = """
        SELECT s.StartDate, s.EndDate, s.DownloadsRemaining, c.CityName
        FROM Subscriptions s
        JOIN Cities c ON c.CityID = s.CityID
        WHERE s.UserID = ?
        ORDER BY s.StartDate DESC
    """;

        try (Connection conn = DbManager.getConnection()) {

            // ---- Purchases ----
            try (PreparedStatement stmt = conn.prepareStatement(purchasesSql)) {
                stmt.setInt(1, userId);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String date = rs.getTimestamp("PurchaseDate").toString();
                        String city = rs.getString("CityName");
                        int version = rs.getInt("mapVersion");
                        boolean used = rs.getBoolean("DownloadUsed");

                        String row = String.format(
                                "PURCHASE | %s | City: %s | Version: %d | Download used: %s",
                                date,
                                city,
                                version,
                                used ? "Yes" : "No"
                        );

                        history.add(row);
                    }
                }
            }

            // ---- Subscriptions ----
            try (PreparedStatement stmt = conn.prepareStatement(subsSql)) {
                stmt.setInt(1, userId);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String start = rs.getTimestamp("StartDate").toString();
                        String end = rs.getTimestamp("EndDate").toString();
                        int remaining = rs.getInt("DownloadsRemaining");
                        String city = rs.getString("CityName");

                        String row = String.format(
                                "SUBSCRIPTION | City: %s | %s → %s | Downloads remaining: %d",
                                city,
                                start,
                                end,
                                remaining
                        );

                        history.add(row);
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return history;
    }





}