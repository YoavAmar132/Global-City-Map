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

        // Use table aliases (c and p) to be strictly clear which table we refer to
        String sql = """
            SELECT * FROM Cities c
            WHERE c.CityName IN (
                SELECT p.CityName FROM Purchases p WHERE p.UserID = ?
            )
        """;

        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // FIX: Use exact column names from your database screenshot
                    City city = new City(
                            rs.getInt("CityID"),       // Changed "CityId" -> "CityID"
                            rs.getString("CityName"),  // Matches DB
                            rs.getString("baseMap"),
                            rs.getDouble("CityPrice")// Matches DB
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

    /**
     * Records a purchase.
     * REQUIRES: ALTER TABLE Purchases ADD COLUMN CityName VARCHAR(255);
     */
    public boolean addPurchase(int userId, String cityName, double price) {
        String sql = "INSERT INTO Purchases (UserID, CityName, PurchaseDate, DownloadUsed) VALUES (?, ?, NOW(), 0)";

        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, cityName);
            // We ignore 'price' here as per your DB structure

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
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
            (version, price, name, description, path, poi_array, route_array)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            poirepo.insertAllPoi(map.getPois());
            routeRepo.insertAllRoutes(map.getRoutes());

            stmt.setInt(1, map.getVersion());
            stmt.setDouble(2, map.getPrice());
            stmt.setString(3, map.getName());
            stmt.setString(4, map.getDescription());
            stmt.setString(5, map.getPath());
            stmt.setString(6, JsonUtil.poiListToJson(map.getPois()));
            stmt.setString(7, JsonUtil.routeListToJson(map.getRoutes()));

            int affected = stmt.executeUpdate();
            return affected == 1;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public MapSheet loadPendingMap(int version, String name) {
        String sql = """
            SELECT version, price, name, description, path, poi_array, route_array
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

                    return new MapSheet(
                            rs.getInt("version"),
                            rs.getDouble("price"),
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getString("path"),
                            routes, pois
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean deletePendingMap(int version) {
        String sql = "DELETE FROM pending_maps WHERE version = ?";
        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, version);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean insertApprovedMap(ApprovePayload ApprovedMap) {
        MapSheet map = ApprovedMap.getMap();
        String CityName = ApprovedMap.getCityName();
        boolean delete = deletePendingMap(map.getVersion());
        if (!delete) System.out.println("failed to delete the pending map");
        if (map == null) return false;

        String sql = "INSERT INTO maps (price, cityID, mapName, map) VALUES (?, ?, ?, ?)";

        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            int cityId = cityRepo.idByCityName(CityName);
            String mapName = map.getName();
            String mapJson = JsonUtil.mapSheetToJsonWithEmbeddedArrays(map);

            stmt.setDouble(1, map.getPrice());
            stmt.setInt(2, cityId);
            stmt.setString(3, mapName);
            stmt.setString(4, mapJson);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<MapSheet> loadAllPendingMaps() {
        List<MapSheet> maps = new ArrayList<>();
        String sql = "SELECT version, price, name, description, path, poi_array, route_array FROM pending_maps ORDER BY name, version";

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
                        rs.getDouble("price"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("path"),
                        routes, pois
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

    // Getters for other repos
    public RouteRepo getRouteRepo() { return routeRepo; }
    public PoiRepo getPoirepo() { return poirepo; }
}