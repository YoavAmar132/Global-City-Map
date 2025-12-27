package gcm.server.data;

import common.model.City;
import common.model.CityPricingItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * nthg to comment everything simple and does what it is called
 */
public class CityRepo {



    public List<CityPricingItem> getAllCityPrices() throws SQLException {
        List<CityPricingItem> prices = new ArrayList<>();

        String sql = "SELECT CityID, CityPrice FROM Cities ORDER BY CityName";

        try (Connection conn = DbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                prices.add(new CityPricingItem(
                        rs.getInt("CityID"),
                        rs.getDouble("CityPrice")
                ));
            }
        }
        return prices;
    }

    public boolean updateCityPrice(CityPricingItem item) throws SQLException {
        String sql = "UPDATE Cities SET CityPrice = ? WHERE CityID = ?";

        try (Connection conn = DbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, item.getPrice());
            ps.setInt(2, item.getCityId());
            return ps.executeUpdate() == 1;
        }
    }


    public List<City> getAllCities() throws SQLException {
        String sql = "SELECT CityID, CityName,baseMap,CityPrice FROM Cities ORDER BY CityName";

        List<City> cities = new ArrayList<>();

        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                cities.add(new City(
                        rs.getInt("CityID"),
                        rs.getString("CityName"),
                        rs.getString("baseMap"),
                        rs.getDouble("CityPrice")
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
