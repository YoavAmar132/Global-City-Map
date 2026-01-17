package gcm.server.data;

import common.messages.ApprovePayload;
import common.model.*;
import common.model.Route;
import gcm.server.data.JsonUtil; // Make sure this import matches your project structure

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapRepo {

    private final PoiRepo poirepo;
    private final CityRepo cityRepo;

    public MapRepo() {
        this.poirepo = new PoiRepo();
        this.cityRepo = new CityRepo();
        System.out.println("map repo created");
    }

    // ==========================================
    //  NEW METHODS FOR BUY MAP
    // ==========================================

    public List<Poi> getPoisForCity(int cityId) {
        return poirepo.loadPoisByCity(cityId);
    }

    public List<City> getPurchasedCitiesByUserId(int userId) {
        List<City> cities = new ArrayList<>();

        // עדכון השאילתה לשליפת עמודות ספציפיות כולל SubPrice
        String sql = """
            SELECT c.CityID, c.CityName, c.baseMap, c.CityPrice, c.SubPrice, c.description
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
                            rs.getDouble("SubPrice"),
                            rs.getString("description")
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
        (version, cityID, name, description, path, poi_array, is_edit, source_map_id)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
    """;

        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, map.getVersion());
            stmt.setInt(2, map.getCityID());
            stmt.setString(3, map.getName());
            stmt.setString(4, map.getDescription());
            stmt.setString(5, map.getPath());
            stmt.setString(6, JsonUtil.poiListToJson(map.getPois()));
            stmt.setBoolean(7, map.isEdit());
            stmt.setObject(8, map.getSourceMapId());


            return stmt.executeUpdate() == 1;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }






    public MapSheet loadPendingMap(int version, int cityID, String name) {
        String sql = """
        SELECT version, cityID, name, description, path, poi_array, is_edit
        FROM pending_maps
        WHERE version = ? AND cityID = ? AND name = ?
    """;

        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, version);
            stmt.setInt(2, cityID);
            stmt.setString(3, name);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String poiJson = rs.getString("poi_array");
                    ArrayList<Poi> pois = JsonUtil.jsonToPoiList(poiJson);

                    MapSheet map = new MapSheet(
                            rs.getInt("version"),
                            rs.getInt("cityID"),
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getString("path"),
                            pois
                    );

                    map.setEdit(rs.getBoolean("is_edit"));
                    map.setSourceMapId((Integer) rs.getObject("source_map_id"));
                    return map;
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

    public boolean insertApprovedMap(ApprovePayload approvedMap) {

        if (approvedMap == null || approvedMap.getMap() == null)
            return false;

        MapSheet map = approvedMap.getMap();
        String cityName = approvedMap.getCityName();

        String insertMapSql = """
        INSERT INTO Maps (cityID, mapName, map, version)
        VALUES (?, ?, ?, ?)
    """;

        try (Connection conn = DbManager.getConnection()) {
            conn.setAutoCommit(false);

            int cityId = cityRepo.idByCityName(cityName);
            if (cityId == -1) {
                conn.rollback();
                return false;
            }

            for (Poi poi : map.getPois()) {

                if (poi.getId() < 0) {
                    Poi approvedPoi = new Poi(
                            0,
                            poi.getName(),
                            poi.getDescription(),
                            poi.getNWorldX(),
                            poi.getNWorldY(),
                            poi.getCategory(),
                            poi.isAccessible(),
                            poi.getCityID(),
                            true,
                            poi.getRecommendedMinutes()
                    );

                    int newPoiId = poirepo.insertPoiAndReturnId(approvedPoi);
                    poi.setId(newPoiId);

                } else {
                    poirepo.updatePoiContent(
                            conn,
                            poi.getId(),
                            poi.getName(),
                            poi.getDescription(),
                            poi.getCategory(),
                            poi.isAccessible(),
                            poi.getRecommendedMinutes()
                    );
                }
            }

            int newVersion;

            if (map.isEdit()) {
                if (map.getSourceMapId() == null) {
                    conn.rollback();
                    throw new SQLException("Edit map missing sourceMapId");
                }

                deleteApprovedMapById(conn, map.getSourceMapId());
                newVersion = getLatestVersionForCity(cityId) + 1;

            } else {
                newVersion = 1;
            }

            deletePendingMap(map.getVersion(), map.getName());

            try (PreparedStatement stmt = conn.prepareStatement(insertMapSql)) {
                stmt.setInt(1, cityId);
                stmt.setString(2, map.getName());
                stmt.setString(3, JsonUtil.mapSheetToJsonWithEmbeddedArrays(map));
                stmt.setInt(4, newVersion);
                stmt.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }







    private boolean deleteApprovedMapById(Connection conn, int mapId) throws SQLException {
        String sql = "DELETE FROM Maps WHERE mapID = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, mapId);
            return stmt.executeUpdate() == 1;
        }
    }



    public List<MapSheet> loadAllPendingMaps() {
        List<MapSheet> maps = new ArrayList<>();

        String sql = """
        SELECT version, cityID, name, description, path, poi_array, is_edit, source_map_id
        FROM pending_maps
        ORDER BY name, version
    """;

        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String poiJson = rs.getString("poi_array");
                ArrayList<Poi> pois = JsonUtil.jsonToPoiList(poiJson);

                MapSheet map = new MapSheet(
                        rs.getInt("version"),
                        rs.getInt("cityID"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("path"),
                        pois
                );

                map.setEdit(rs.getBoolean("is_edit"));
                map.setSourceMapId((Integer) rs.getObject("source_map_id"));
                maps.add(map);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return maps;
    }





    public List<MapSheet> loadAllMapsFromCity(String cityName) {
        List<MapSheet> maps = new ArrayList<>();
        String sql = "SELECT mapID, map FROM maps WHERE cityID = ?";

        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int cityId = cityRepo.idByCityName(cityName);
            stmt.setInt(1, cityId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String mapJson = rs.getString("map");
                    int mapId = rs.getInt("mapID");
                    MapSheet map = JsonUtil.jsonToMapSheetWithEmbeddedArrays(mapJson);
                    if (map != null){
                        map.setSourceMapId(mapId);
                        maps.add(map);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return maps;
    }

    // Helper: Get latest version (Required for OTP)
    public int getLatestVersionForCity(int cityId) {
        String sql = "SELECT MAX(version) AS maxVer FROM Maps WHERE cityID = ?";
        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, cityId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("maxVer"); // returns 0 if NULL
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
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
        SELECT c.CityID, c.CityName, c.baseMap, c.CityPrice, c.SubPrice, c.description s.EndDate
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
                            rs.getDouble("SubPrice"),
                            rs.getString("description")
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
    public void writeMessage(int id, String message) throws SQLException {

        String sql = """
        INSERT INTO Messages (UserID, Message, CreatedAt)
        VALUES (?, ?, CURRENT_TIMESTAMP)
    """;

        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.setString(2, message);

            stmt.executeUpdate();
        }
    }
    public ArrayList<String> getaAllMessages(int id) throws SQLException {

        ArrayList<String> messages = new ArrayList<>();

        String selectSql = """
        SELECT Message
        FROM Messages
        WHERE UserID = ?
        ORDER BY CreatedAt
    """;

        String deleteSql = """
        DELETE FROM Messages
        WHERE UserID = ?
    """;

        try (Connection conn = DbManager.getConnection()) {

            // Important: make this atomic
            conn.setAutoCommit(false);

            try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                selectStmt.setInt(1, id);

                try (ResultSet rs = selectStmt.executeQuery()) {
                    while (rs.next()) {
                        messages.add(rs.getString("Message"));
                    }
                }
            }

            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                deleteStmt.setInt(1, id);
                deleteStmt.executeUpdate();
            }

            // commit only if everything succeeded
            conn.commit();

        } catch (SQLException e) {
            // rollback on error
            throw e;
        }

        return messages;
    }


    public boolean insertPendingRoute(PendingRoute route) throws SQLException {

        Connection conn = null;

        try {
            conn = DbManager.getConnection();
            conn.setAutoCommit(false); //

            // Insert into pending_routes
            String routeSql = """
            INSERT INTO pending_routes (name, description, cityID, is_edit, sourceRouteID)
            VALUES (?, ?, ?, ?, ?)
        """;

            int routeId;

            try (PreparedStatement ps = conn.prepareStatement(
                    routeSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, route.getName());
                ps.setString(2, route.getDescription());
                ps.setInt(3, route.getCityId());
                ps.setBoolean(4, route.isEdit());
                ps.setObject(5, route.getSourceRouteId(), Types.INTEGER);


                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                if (!rs.next()) {
                    conn.rollback();
                    return false;
                }

                routeId = rs.getInt(1);
            }

            // Insert pending_route_stops
            String stopSql = """
            INSERT INTO pending_route_stops
            (routeID, poiID, stop_order, recommended_minutes)
            VALUES (?, ?, ?, ?)
        """;

            try (PreparedStatement ps = conn.prepareStatement(stopSql)) {
                int order = 1;
                for (int poiId : route.getStops()) {
                    ps.setInt(1, routeId);
                    ps.setInt(2, poiId);
                    ps.setInt(3, order++);
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            conn.commit(); // success
            return true;

        } catch (Exception e) {
            if (conn != null) conn.rollback();
            e.printStackTrace();
            return false;

        } finally {
            if (conn != null) conn.setAutoCommit(true);
            if (conn != null) conn.close();
        }
    }

    public boolean insertApprovedRoute(int routeId) {

        String insertRouteSql = """
        INSERT INTO routes (cityID, name, description, createdBy)
        SELECT cityID, name, description, createdBy
        FROM pending_routes
        WHERE routeID = ?
    """;

        String insertStopsSql = """
        INSERT INTO route_stops (routeID, poiID, stop_order, recommended_minutes)
        SELECT ?, poiID, stop_order, recommended_minutes
        FROM pending_route_stops
        WHERE routeID = ?
    """;

        String deleteStopsSql = """
        DELETE FROM pending_route_stops WHERE routeID = ?
    """;

        String deleteRouteSql = """
        DELETE FROM pending_routes WHERE routeID = ?
    """;

        try (Connection conn = DbManager.getConnection()) {
            conn.setAutoCommit(false);

            int newRouteId;

            // 1️⃣ Insert route
            try (PreparedStatement ps =
                         conn.prepareStatement(insertRouteSql, Statement.RETURN_GENERATED_KEYS)) {

                ps.setInt(1, routeId);
                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                if (!rs.next()) {
                    conn.rollback();
                    return false;
                }
                newRouteId = rs.getInt(1);
            }

            // 2️⃣ Insert stops
            try (PreparedStatement ps = conn.prepareStatement(insertStopsSql)) {
                ps.setInt(1, newRouteId);
                ps.setInt(2, routeId);
                ps.executeUpdate();
            }

            // 3️⃣ Cleanup pending
            try (PreparedStatement ps = conn.prepareStatement(deleteStopsSql)) {
                ps.setInt(1, routeId);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(deleteRouteSql)) {
                ps.setInt(1, routeId);
                ps.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public List<PendingRoute> getPendingRoutes() throws SQLException {

        List<PendingRoute> routes = new ArrayList<>();

        String routesSql = """
        SELECT routeId, name, description, cityId
        FROM pending_routes
        ORDER BY routeId
    """;

        String stopsSql = """
        SELECT poiId
        FROM pending_route_stops
        WHERE routeId = ?
        ORDER BY stop_order
    """;

        try (Connection conn = DbManager.getConnection()) {

            try (PreparedStatement routePs = conn.prepareStatement(routesSql);
                 ResultSet rs = routePs.executeQuery()) {

                while (rs.next()) {

                    int routeId = rs.getInt("routeId");
                    String name = rs.getString("name");
                    String description = rs.getString("description");
                    int cityId = rs.getInt("cityId");

                    // ---- Load stops (ORDER MATTERS) ----
                    ArrayList<Integer> stops = new ArrayList<>();

                    try (PreparedStatement stopPs = conn.prepareStatement(stopsSql)) {
                        stopPs.setInt(1, routeId);

                        try (ResultSet rsStops = stopPs.executeQuery()) {
                            while (rsStops.next()) {
                                stops.add(rsStops.getInt("poiId"));
                            }
                        }
                    }

                    routes.add(new PendingRoute(
                            routeId,
                            name,
                            description,
                            cityId,
                            stops
                    ));
                }
            }
        }

        return routes;
    }


    public boolean isPoiUsedInAnyMap(int poiId) {

        try (Connection conn = DbManager.getConnection()) {

            String approvedSql = "SELECT map FROM Maps";
            try (PreparedStatement ps = conn.prepareStatement(approvedSql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    String json = rs.getString("map");

                    MapSheet map =
                            JsonUtil.jsonToMapSheetWithEmbeddedArrays(json);

                    if (map == null || map.getPois() == null) continue;

                    for (Poi p : map.getPois()) {
                        if (p.getId() == poiId) {
                            return true;
                        }
                    }
                }
            }

            String pendingSql = "SELECT poi_array FROM pending_maps";
            try (PreparedStatement ps = conn.prepareStatement(pendingSql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    String json = rs.getString("poi_array");
                    ArrayList<Poi> pois = JsonUtil.jsonToPoiList(json);

                    for (Poi p : pois) {
                        if (p.getId() == poiId) {
                            return true;
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return true; // FAIL SAFE
        }

        return false;
    }






    // Getters for other repos
    public PoiRepo getPoirepo() { return poirepo; }
}