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

        // הוספת SubPrice לשליפה
        String sql = "SELECT CityID, CityPrice, SubPrice FROM Cities ORDER BY CityName";

        try (Connection conn = DbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                prices.add(new CityPricingItem(
                        rs.getInt("CityID"),
                        rs.getDouble("CityPrice"), // מחיר רגיל
                        rs.getDouble("SubPrice")   // מחיר מנוי (צריך לוודא שהבנאי תומך בזה)
                ));
            }
        }
        return prices;
    }

    public boolean updateCityPrice(CityPricingItem item) throws SQLException {
        // הוספת SubPrice לשאילתת העדכון
        String sql = "UPDATE Cities SET CityPrice = ?, SubPrice = ? WHERE CityID = ?";

        try (Connection conn = DbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, item.getPrice());    // מחיר חד פעמי
            ps.setDouble(2, item.getSubPrice()); // מחיר מנוי (החדש)
            ps.setInt(3, item.getCityId());

            return ps.executeUpdate() == 1;
        }
    }


    public List<City> getAllCities() throws SQLException {
        // הוספנו את SubPrice לשאילתה
        String sql = "SELECT CityID, CityName, baseMap, CityPrice, SubPrice FROM Cities ORDER BY CityName";

        List<City> cities = new ArrayList<>();

        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                cities.add(new City(
                        rs.getInt("CityID"),
                        rs.getString("CityName"),
                        rs.getString("baseMap"),
                        rs.getDouble("CityPrice"),
                        rs.getDouble("SubPrice") // עכשיו זה יעבוד כי ביקשנו את העמודה בשאילתה
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

    public String cityNameById(int cityId) {
        return null;
    }
}
