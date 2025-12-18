package gcm.server.data;

import common.model.City;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * nthg to comment everything simple and does what it is called
 */
public class CityRepo {

    public List<City> getAllCities() throws SQLException {
        String sql = "SELECT CityID, CityName FROM Cities ORDER BY CityName";

        List<City> cities = new ArrayList<>();

        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                cities.add(new City(
                        rs.getInt("CityID"),
                        rs.getString("CityName")
                ));
            }
        }

        return cities;
    }

    public boolean existsByCityName(String cityName) throws SQLException {
        String sql = "SELECT 1 FROM Cities WHERE CityName = ?";

        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cityName);
            return stmt.executeQuery().next();
        }
    }

    public boolean insertCity(String cityName) throws SQLException {
        String sql = "INSERT INTO Cities (CityName) VALUES (?)";

        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cityName);
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
