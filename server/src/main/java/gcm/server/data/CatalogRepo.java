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

        String sql = """
        SELECT
            c.CityID,
            c.CityName,
            c.CityPrice,
            c.SubPrice,

            COUNT(DISTINCT m.mapID) AS MapCount,
            COUNT(DISTINCT p.id) AS PoiCount,
            COUNT(DISTINCT r.routeID) AS RouteCount

        FROM Cities c

        JOIN Maps m
            ON c.CityID = m.cityID

        LEFT JOIN pois p
             ON c.CityID = p.cityID
            AND p.is_approved = TRUE

        LEFT JOIN routes r
            ON c.CityID = r.cityID
            AND EXISTS (
                SELECT 1
                FROM route_stops rs
                JOIN pois rp ON rs.poiID = rp.id
                WHERE rs.routeID = r.routeID
                AND rp.is_approved = TRUE
            )

        WHERE c.CityID IN (
            SELECT DISTINCT subC.CityID
            FROM Cities subC
            LEFT JOIN pois subP
                ON subC.CityID = subP.cityID
            WHERE
                subC.CityName LIKE ?
                OR(
                    subP.is_approved = TRUE
                    AND(
                        subP.name LIKE ?
                        OR subP.description LIKE ?
                    )
                )
        )

        GROUP BY
            c.CityID,
            c.CityName,
            c.CityPrice,
            c.SubPrice
        """;


        List<CityCatalogItem> resultList = new ArrayList<>();
        String searchPattern = "%" + queryText + "%";

        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

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
