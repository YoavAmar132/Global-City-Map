package gcm.server.data;

import common.model.City;
import common.model.CityPricingItem;
import common.model.Complaint;
import common.model.PendingCityPriceItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * nthg to comment everything simple and does what it is called
 */
public class CityRepo {



    public boolean createCity(String cityName, String baseMapPath, String description) throws SQLException {

        String sql = """
        INSERT INTO Cities (CityName, baseMap, CityPrice, SubPrice, Description)
        VALUES (?, ?, 0, 0, ?)
    """;

        try (Connection conn = DbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, cityName);
            ps.setString(2, baseMapPath);
            ps.setString(3, description);

            return ps.executeUpdate() == 1;
        }
    }

    public City getCity(int cityId) throws SQLException {
        String sql = """
            SELECT c.CityID, c.CityName, c.baseMap, c.CityPrice, c.SubPrice, c.Description
            FROM Cities c
            WHERE CityID = ?
        """;
        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);){
            stmt.setInt(1, cityId);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) return null;

            City city = new City(
                    rs.getInt("CityID"),
                    rs.getString("CityName"),
                    rs.getString("baseMap"),
                    rs.getDouble("CityPrice"),
                    rs.getDouble("SubPrice"),
                    rs.getString("description"));
            return city;
        }
    }


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
        String sql = "SELECT CityID, CityName, baseMap, CityPrice, SubPrice, description FROM Cities ORDER BY CityName";

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
                        rs.getDouble("SubPrice"),
                        rs.getString("description")
                ));
            }
        }
        return cities;
    }

    public List<City> getAllCitiesWithMaps() throws SQLException {

        String sql = """
        SELECT c.CityID, c.CityName, c.baseMap, c.CityPrice, c.SubPrice, c.Description
        FROM Cities c
        WHERE EXISTS (
            SELECT 1
            FROM Maps m
            WHERE m.cityID = c.CityID
        )
        ORDER BY c.CityName
    """;

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
                        rs.getDouble("SubPrice"),
                        rs.getString("description")
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

    public Integer getCityIdByPath(String mapPath) throws SQLException {
        // Looks up the CityID where the baseMap column matches the path provided
        String sql = "SELECT CityID FROM GCM_DB.Cities WHERE baseMap = ?";

        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, mapPath);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("CityID");
                }
            }
        }
        return null; // Return null if no city matches this path
    }

    public boolean upsertPendingCityPrice(
            int cityId,
            double cityPrice,
            double subPrice
    ) throws SQLException {

        String sql = """
        INSERT INTO pending_city_prices (cityID, newCityPrice, newSubPrice)
        VALUES (?, ?, ?)
        ON DUPLICATE KEY UPDATE
            newCityPrice = VALUES(newCityPrice),
            newSubPrice = VALUES(newSubPrice)
    """;

        try (Connection conn = DbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, cityId);
            ps.setDouble(2, cityPrice);
            ps.setDouble(3, subPrice);
            return ps.executeUpdate() >= 1;
        }
    }

    public List<PendingCityPriceItem> getPendingCityPrices()
            throws SQLException {

        String sql = """
        SELECT c.CityID, c.CityName,
               p.newCityPrice, p.newSubPrice
        FROM pending_city_prices p
        JOIN Cities c ON c.CityID = p.cityID
    """;

        List<PendingCityPriceItem> list = new ArrayList<>();

        try (Connection conn = DbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new PendingCityPriceItem(
                        rs.getInt("CityID"),
                        rs.getString("CityName"),
                        rs.getDouble("newCityPrice"),
                        rs.getDouble("newSubPrice")
                ));
            }
        }
        return list;
    }

    public PendingCityPriceItem getPendingCityPrice(int cityId)
            throws SQLException {

        String sql = """
        SELECT c.CityName, p.newCityPrice, p.newSubPrice
        FROM pending_city_prices p
        JOIN Cities c ON c.CityID = p.cityID
        WHERE p.cityID = ?
    """;

        try (Connection conn = DbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, cityId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                return new PendingCityPriceItem(
                        cityId,
                        rs.getString("CityName"),
                        rs.getDouble("newCityPrice"),
                        rs.getDouble("newSubPrice")
                );
            }
        }
    }


    public boolean updateCityPrice(
            int cityId,
            double cityPrice,
            double subPrice
    ) throws SQLException {

        String sql = """
        UPDATE Cities
        SET CityPrice = ?, SubPrice = ?
        WHERE CityID = ?
    """;

        try (Connection conn = DbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, cityPrice);
            ps.setDouble(2, subPrice);
            ps.setInt(3, cityId);
            return ps.executeUpdate() == 1;
        }
    }


    public boolean deletePendingCityPrice(int cityId)
            throws SQLException {

        String sql = "DELETE FROM pending_city_prices WHERE cityID = ?";

        try (Connection conn = DbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, cityId);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean hasPendingPriceForCity(int cityId) {
        String sql = "SELECT 1 FROM pending_city_prices WHERE cityID = ?";

        try (Connection conn = DbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, cityId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return true; // fail-safe: block insert
        }
    }

    public boolean insertPendingPriceChange(CityPricingItem item) {

        if (hasPendingPriceForCity(item.getCityId())) {
            return false;
        }

        String sql = """
        INSERT INTO pending_city_prices (cityID, newCityPrice, newSubPrice)
        VALUES (?, ?, ?)
    """;

        try (Connection conn = DbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, item.getCityId());
            ps.setDouble(2, item.getPrice());
            ps.setDouble(3, item.getSubPrice());

            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }



}
