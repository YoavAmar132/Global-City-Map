package gcm.server.data;

import common.model.City;

import javafx.scene.image.Image;
import java.io.InputStream;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * nthg to comment everything simple and does what it is called
 */
public class CityRepo {

    //add to mysql: CREATE INDEX idx_city_name ON maps(CityName);
    public List<City> getAllCities(int offset , int count,String searchText) throws SQLException {
        String sql = """
        SELECT CityID, CityName
        FROM Cities
        WHERE LOWER(CityName) LIKE LOWER(CONCAT('%', ?, '%'))
        ORDER BY CityName
        LIMIT ? OFFSET ?
        """;

        List<City> cities = new ArrayList<>();

        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);)
        {
            stmt.setString(1, searchText);
            stmt.setInt(2, count);
            stmt.setInt(3, offset);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {

                cities.add(new City(
                        rs.getInt("CityID"),
                        rs.getString("CityName"),
                        "",
                        null
                ));
            }
            return cities;
        }
    }

    public int getCitiesCount(String searchText) throws SQLException {
        String sql = """
        SELECT count(*) AS Count
        FROM Cities
        WHERE (? IS NULL OR LOWER(CityName) LIKE LOWER(CONCAT('%', ?, '%')));
        """;
        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);)
        {
            stmt.setString(1, searchText);
            stmt.setString(2, searchText);

            ResultSet rs = stmt.executeQuery();
            rs.next();

            return rs.getInt(1);

        }
    }


    public boolean existsByCityName(String cityName) throws SQLException {
        String sql = "SELECT 1 FROM Cities WHERE CityName = ?";

        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cityName);
            return stmt.executeQuery().next();
        }
    }
    public int idByCityName(String cityName) throws SQLException {
        String sql = "SELECT CityId FROM Cities WHERE CityName = ?";

        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cityName);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("CityId");
                }
            }
        }

        // City not found
        return -1;
    }


    public boolean insertCity(String cityName,String imagePath) throws SQLException {
        String sql = "INSERT INTO Cities (CityName,ImagePath) VALUES (?,?)";

        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cityName);
            stmt.setString(2, imagePath);

            return stmt.executeUpdate() == 1;
        }
    }


    //note that because of cascade, mysql deletes all maps/tours/tourstops that have this map ID
    public boolean deleteCity(int cityId) throws SQLException {
        String sql = "DELETE FROM Cities WHERE CityID = ?";

        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, cityId);
            return stmt.executeUpdate() == 1;

        }
    }
}
