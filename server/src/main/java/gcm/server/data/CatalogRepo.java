package gcm.server.data;

import common.model.CityCatalogItem;
import common.model.MapCatalogItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CatalogRepo {

    /**
     * Load all cities with:
     *  - number of maps
     *  - min/max price (from JSON)
     */
    public List<CityCatalogItem> loadCityCatalog() {

        List<CityCatalogItem> result = new ArrayList<>();

        String sql = """
           SELECT
            c.CityID,
            c.CityName,
            c.CityPrice,
            c.SubPrice,
            COUNT(m.mapID) AS mapCount
            FROM Cities c
            LEFT JOIN Maps m ON m.cityID = c.CityID
            GROUP BY c.CityID, c.CityName, c.CityPrice, c.SubPrice
        """;

        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                result.add(new CityCatalogItem(
                        rs.getInt("CityID"),
                        rs.getString("CityName"),
                        rs.getInt("mapCount"),
                        rs.getDouble("CityPrice"),
                        rs.getDouble("SubPrice"),
                        rs.getInt("PoiCount"),
                        rs.getInt("RouteCount")
                ));

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Load all maps of a city
     */
    public List<MapCatalogItem> loadMapsForCity(int cityId) {

        List<MapCatalogItem> result = new ArrayList<>();

        String sql = """
            SELECT
                mapID,
                mapName,
                JSON_EXTRACT(map, '$.description') AS description,
                JSON_EXTRACT(map, '$.price') AS price
            FROM Maps
            WHERE cityID = ?
        """;

        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, cityId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {

                    result.add(new MapCatalogItem(
                            rs.getInt("mapID"),
                            rs.getString("mapName"),
                            rs.getString("description"),
                            rs.getDouble("price")
                    ));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Logs a user view for a specific city.
     * If the user is a Guest, userId should be 0.
     */
    public void logCityView(int cityId, int userId) {

        String sql = "INSERT INTO ViewLogs (userID, CityID, viewDate) VALUES (?, ?, NOW())";

        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, cityId);

            stmt.executeUpdate();
            System.out.println("Logged view for CityID: " + cityId + ", UserID: " + userId);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<CityCatalogItem> searchCities(String queryText) throws SQLException {
        // NEW LOGIC:
        // 1. The WHERE IN (...) clause finds the valid CityIDs based on your search.
        // 2. The main SELECT joins everything again to get the TOTAL counts for those cities.

        String sql = "SELECT " +
                "  c.CityID, " +
                "  c.CityName, " +
                "  c.CityPrice, " +
                "  c.SubPrice, " +
                "  COUNT(DISTINCT m.mapID) AS MapCount, " +
                "  COUNT(DISTINCT p.id) AS PoiCount, " +
                "  COUNT(DISTINCT r.id) AS RouteCount " +
                "FROM GCM_DB.Cities c " +
                "LEFT JOIN GCM_DB.Maps m ON c.CityID = m.cityID " +
                "LEFT JOIN GCM_DB.pois p ON c.CityID = p.city_id " +
                "LEFT JOIN GCM_DB.routes r ON c.CityID = r.city_id " +
                "WHERE c.CityID IN ( " +
                "    SELECT DISTINCT subC.CityID " +
                "    FROM GCM_DB.Cities subC " +
                "    LEFT JOIN GCM_DB.pois subP ON subC.CityID = subP.city_id " +
                "    WHERE " +
                "      subC.CityName LIKE ? " +
                "      OR subP.name LIKE ? " +
                "      OR subP.description LIKE ? " +
                ") " +
                "GROUP BY c.CityID, c.CityName, c.CityPrice, c.SubPrice";

        List<CityCatalogItem> resultList = new ArrayList<>();
        String searchPattern = "%" + queryText + "%";

        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // We still have 3 question marks inside the subquery
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultList.add(new CityCatalogItem(
                            rs.getInt("CityID"),
                            rs.getString("CityName"),
                            rs.getInt("MapCount"),
                            rs.getDouble("CityPrice"),
                            rs.getDouble("SubPrice"),
                            rs.getInt("PoiCount"),
                            rs.getInt("RouteCount")
                    ));
                }
            }
        }
        return resultList;
    }


}
