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
                        rs.getDouble("SubPrice")
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
}
