package gcm.server.data;

import common.messages.ApprovePayload;
import common.model.Poi;
import common.model.Route;
import common.model.MapSheet;
import common.model.City;
import gcm.server.data.JsonUtil; // Make sure this import matches your project structure

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapRepo {

    private final PoiRepo poirepo;
    private final RouteRepo routeRepo;
    private final CityRepo cityRepo;

    public MapRepo() {
        this.poirepo = new PoiRepo();
        this.routeRepo = new RouteRepo();
        this.cityRepo = new CityRepo();
        System.out.println("map repo created");
    }

    // ==========================================
    //  NEW METHODS FOR BUY MAP
    // ==========================================

    public List<City> getPurchasedCitiesByUserId(int userId) {
        List<City> cities = new ArrayList<>();

        // עדכון השאילתה לשליפת עמודות ספציפיות כולל SubPrice
        String sql = """
            SELECT c.CityID, c.CityName, c.baseMap, c.CityPrice, c.SubPrice 
            FROM Cities c
            WHERE c.CityName IN (
                SELECT p.CityName FROM Purchases p WHERE p.UserID = ?
            )
        """;

        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // שימוש בבנאי החדש המקבל 5 פרמטרים
                    City city = new City(
                            rs.getInt("CityID"),
                            rs.getString("CityName"),
                            rs.getString("baseMap"),
                            rs.getDouble("CityPrice"),
                            rs.getDouble("SubPrice") // **הוספה חדשה**
                    );
                    cities.add(city);
                }
            }
        } catch (SQLException e) {
            System.err.println("Database Error in getPurchasedCitiesByUserId: " + e.getMessage());
            e.printStackTrace();
        }
        return cities;
    }

    public List<Poi> getAllPoi() {

          return poirepo.loadAllPois(0,10000);

        }
    public List<Route> getAllRoute() {

        return routeRepo.loadAllRoutes(0,10000);

    }

    // COPY THIS ENTIRE METHOD INTO MapRepo.java

    public List<MapSheet> getPurchasedMapsByUserId(int userId) {
        List<MapSheet> userMaps = new ArrayList<>();
        System.out.println("--- DEBUG: Fetching maps for User " + userId + " ---");

        // 1. FETCH SUBSCRIPTIONS (Get Latest Version)
        String subSql = "SELECT CityID FROM Subscriptions WHERE UserID = ? AND EndDate > NOW()";
        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(subSql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int cityId = rs.getInt("CityID");
                    String cityName = cityRepo.cityNameById(cityId);
                    int maxVer = getLatestVersionForCity(cityId);

                    System.out.println("DEBUG: Found Subscription -> CityID: " + cityId + " (" + cityName + ")");

                    if (cityName != null && maxVer > 0) {
                        MapSheet map = loadMapByVersion(cityName, maxVer);
                        if (map != null) userMaps.add(map);
                    }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }

        // 2. FETCH ONE-TIME PURCHASES (Get Specific Version)
        String otpSql = "SELECT CityName, mapVersion FROM Purchases WHERE UserID = ?";
        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(otpSql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String cityName = rs.getString("CityName");
                    int version = rs.getInt("mapVersion");

                    // Safety fix for null/0 versions
                    if (version <= 0) version = 1;

                    System.out.println("DEBUG: Found OTP Purchase -> City: " + cityName + ", Version: " + version);

                    // --- DIAGNOSTIC CHECK ---
                    int cityId = cityRepo.idByCityName(cityName);
                    if (cityId == -1) {
                        System.out.println("CRITICAL ERROR: 'idByCityName' returned -1 for " + cityName);
                        System.out.println("Action: Check your Cities table. Does '" + cityName + "' exist exactly?");
                    }
                    // ------------------------

                    MapSheet map = loadMapByVersion(cityName, version);
                    if (map != null) {
                        System.out.println("DEBUG: Successfully loaded map: " + map.getName());
                        userMaps.add(map);
                    } else {
                        System.out.println("ERROR: Map NOT found in DB. Verify 'Maps' table has CityID=" + cityId + " and Version=" + version);
                    }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }

        System.out.println("DEBUG: Returning " + userMaps.size() + " maps to client.");
        return userMaps;
    }

    /**
     * Records a purchase.
     * REQUIRES: ALTER TABLE Purchases ADD COLUMN CityName VARCHAR(255);
     */
    // Main Add Purchase Method
    // 1. New Helper: Check specific version ownership
    public boolean isMapVersionPurchased(int userId, String cityName, int version) {
        String sql = "SELECT 1 FROM Purchases WHERE UserID = ? AND CityName = ? AND mapVersion = ?";
        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, cityName);
            stmt.setInt(3, version);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // 2. Updated Add Purchase: Handle explicit versions
    public boolean addPurchase(int userId, String cityName, double price, boolean isSubscription, int requestedVersion) throws SQLException {
        int cityId = cityRepo.idByCityName(cityName);
        if (cityId == -1) return false;

        if (isSubscription) {
            // Subscription Logic (Unchanged)
            String sql = "INSERT INTO Subscriptions (UserID, CityID, StartDate, EndDate) VALUES (?, ?, NOW(), DATE_ADD(NOW(), INTERVAL 6 MONTH))";
            try (Connection conn = DbManager.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, cityId);
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) { e.printStackTrace(); return false; }

        } else {
            // OTP Logic
            int versionToBuy = requestedVersion;

            // If version is 0 (Normal Buy), fetch the latest one
            if (versionToBuy == 0) {
                versionToBuy = getLatestVersionForCity(cityId);
            }

            String sql = "INSERT INTO Purchases (UserID, CityName, PurchaseDate, mapVersion) VALUES (?, ?, NOW(), ?)";
            try (Connection conn = DbManager.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                stmt.setString(2, cityName);
                stmt.setInt(3, versionToBuy);
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) { e.printStackTrace(); return false; }
        }
    }

    /**
     * Checks if a user already owns a city
     */
    public boolean isCityPurchased(int userId, String cityName) {
        String sql = "SELECT 1 FROM Purchases WHERE UserID = ? AND CityName = ?";
        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, cityName);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ==========================================
    //  YOUR EXISTING METHODS
    // ==========================================

    public boolean insertPendingMap(MapSheet map) {
        String sql = """
            INSERT INTO pending_maps
            (version, name, description, path, poi_array, route_array)
            VALUES (?, ?, ?, ?, ?, ?)
        """;


        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            poirepo.insertAllPoi(map.getPois());
            routeRepo.insertAllRoutes(map.getRoutes());

            stmt.setInt(1, map.getVersion());
            stmt.setString(2, map.getName());
            stmt.setString(3, map.getDescription());
            stmt.setString(4, map.getPath());
            stmt.setString(5, JsonUtil.poiListToJson(map.getPois()));
            stmt.setString(6, JsonUtil.routeListToJson(map.getRoutes()));


            int affected = stmt.executeUpdate();
            return affected == 1;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public MapSheet loadPendingMap(int version, String name) {
        String sql = """
            SELECT version, name, description, path, poi_array, route_array
            FROM pending_maps
            WHERE version = ? AND name = ?
            """;


        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, version);
            stmt.setString(2, name);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String poiJson = rs.getString("poi_array");
                    String routeJson = rs.getString("route_array");

                    ArrayList<Poi> pois = JsonUtil.jsonToPoiList(poiJson);
                    ArrayList<Route> routes = JsonUtil.jsonToRouteList(routeJson);

                    new MapSheet(
                            rs.getInt("version"),
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getString("path"),
                            routes,
                            pois
                    );

                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Fix: Delete using Version AND Name to avoid deleting duplicates
    public boolean deletePendingMap(int version, String name) {
        String sql = "DELETE FROM pending_maps WHERE version = ? AND name = ?";
        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, version);
            stmt.setString(2, name);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean insertApprovedMap(ApprovePayload ApprovedMap) {
        MapSheet map = ApprovedMap.getMap();
        String CityName = ApprovedMap.getCityName();

        // 1. Delete from pending first (using Name + Version fix)
        deletePendingMap(map.getVersion(), map.getName());

        if (map == null) return false;

        String sql = """
            INSERT INTO Maps (cityID, mapName, map, version)
            VALUES (?, ?, ?, ?)
        """;


        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int cityId = cityRepo.idByCityName(CityName);
            String mapName = map.getName();
            String mapJson = JsonUtil.mapSheetToJsonWithEmbeddedArrays(map);

            stmt.setInt(1, cityId);
            stmt.setString(2, mapName);
            stmt.setString(3, mapJson);
            stmt.setInt(4, map.getVersion());


            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<MapSheet> loadAllPendingMaps() {
        List<MapSheet> maps = new ArrayList<>();
        String sql = """
            SELECT version, name, description, path, poi_array, route_array
            FROM pending_maps
            ORDER BY name, version
        """;


        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String poiJson = rs.getString("poi_array");
                String routeJson = rs.getString("route_array");
                ArrayList<Poi> pois = JsonUtil.jsonToPoiList(poiJson);
                ArrayList<Route> routes = JsonUtil.jsonToRouteList(routeJson);

                MapSheet map = new MapSheet(
                        rs.getInt("version"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("path"),
                        routes,
                        pois
                );

                maps.add(map);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return maps;
    }

    public List<MapSheet> loadAllMapsFromCity(String cityName) {
        List<MapSheet> maps = new ArrayList<>();
        String sql = "SELECT map FROM maps WHERE cityID = ?";

        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int cityId = cityRepo.idByCityName(cityName);
            stmt.setInt(1, cityId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String mapJson = rs.getString("map");
                    MapSheet map = JsonUtil.jsonToMapSheetWithEmbeddedArrays(mapJson);
                    if (map != null) maps.add(map);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return maps;
    }

    // Helper: Get latest version (Required for OTP)
    public int getLatestVersionForCity(int cityId) {
        // If your Maps table doesn't have a 'version' column yet, this will return 0 or fail.
        // Ensure you ran: ALTER TABLE Maps ADD COLUMN version INT;
        String sql = "SELECT MAX(version) as maxVer FROM Maps WHERE cityID = ?";
        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, cityId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("maxVer");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 1; // Default to 1 if not found
    }

    // 1. Helper: Loads a specific version of a map (NEW)
    public MapSheet loadMapByVersion(String cityName, int version) {
        String sql = "SELECT map FROM maps WHERE cityID = ? AND version = ?";
        // Note: You need to ensure your 'maps' table has a 'version' column!
        // If it doesn't, you have to extract it from the JSON, which is slower.

        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int cityId = cityRepo.idByCityName(cityName);
            stmt.setInt(1, cityId);
            stmt.setInt(2, version);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String mapJson = rs.getString("map");
                    return JsonUtil.jsonToMapSheetWithEmbeddedArrays(mapJson);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 2. Main Logic: Decides which map to give the user (NEW)
    public List<MapSheet> loadMapsForUser(int userId, String cityName) throws SQLException {
        List<MapSheet> result = new ArrayList<>();
        int cityId = cityRepo.idByCityName(cityName);

        // A. Check Subscription (Highest Priority)
        boolean isSubscribed = false;
        String subSql = "SELECT 1 FROM Subscriptions WHERE UserID = ? AND CityID = ? AND ExpirationDate > NOW()";
        try (Connection conn = DbManager.getConnection(); PreparedStatement stmt = conn.prepareStatement(subSql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, cityId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) isSubscribed = true;
        } catch (SQLException e) { e.printStackTrace(); }

        if (isSubscribed) {
            // User is subscribed -> Give them ALL versions (or just the latest)
            return loadAllMapsFromCity(cityName);
        }

        // B. Check OTP Purchase
        // If they aren't subscribed, check which specific versions they bought
        String otpSql = "SELECT mapVersion FROM Purchases WHERE UserID = ? AND CityName = ?";
        try (Connection conn = DbManager.getConnection(); PreparedStatement stmt = conn.prepareStatement(otpSql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, cityName);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int boughtVersion = rs.getInt("mapVersion");
                // Load ONLY the version they paid for
                MapSheet map = loadMapByVersion(cityName, boughtVersion);
                if (map != null) result.add(map);
            }
        } catch (SQLException e) { e.printStackTrace(); }

        return result;
    }

    // Helper: Check if subscription exists and is active
    public boolean isUserSubscribed(int userId, String cityName) throws SQLException {
        int cityId = cityRepo.idByCityName(cityName);
        if (cityId == -1) return false;

        String sql = "SELECT 1 FROM Subscriptions WHERE UserID = ? AND CityID = ? AND EndDate > NOW()";
        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, cityId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // In gcm/server/data/MapRepo.java

    public List<City> getSubscribedCities(int userId) {
        List<City> cities = new ArrayList<>();
        // Select the EndDate as well
        String sql = """
        SELECT c.CityID, c.CityName, c.baseMap, c.CityPrice, c.SubPrice, s.EndDate
        FROM Cities c
        JOIN Subscriptions s ON c.CityID = s.CityID
        WHERE s.UserID = ? AND s.EndDate > NOW()
    """;

        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    City city = new City(
                            rs.getInt("CityID"),
                            rs.getString("CityName"),
                            rs.getString("baseMap"),
                            rs.getDouble("CityPrice"),
                            rs.getDouble("SubPrice")
                    );

                    // HACK: Store the date string in the 'Description' field to avoid new classes
                    // Ensure your City class has setDescription(), or use a constructor that accepts it.
                    // If City doesn't have a description field, you might need to add one string field to it.
                    // Assuming City has a setDescription or we can just subclass it anonymously (messy).
                    // BEST OPTION IF NO FIELD: Add 'private String expirationDate;' to City.java model.

                    String dateStr = rs.getDate("EndDate").toString(); // Simple YYYY-MM-DD
                    city.setDescription(dateStr);

                    cities.add(city);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return cities;
    }

    // Getters for other repos
    public RouteRepo getRouteRepo() { return routeRepo; }
    public PoiRepo getPoirepo() { return poirepo; }
}