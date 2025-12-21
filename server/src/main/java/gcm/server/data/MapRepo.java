package gcm.server.data;

import common.messages.ApprovePayload;
import common.model.POI_Category;
import common.model.Poi ;
import common.model.Route;
import common.model.MapSheet;

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
            this.cityRepo=new CityRepo();
            System.out.println("map repo created");
        }

    public RouteRepo getRouteRepo() {
        return routeRepo;
    }

    public PoiRepo getPoirepo() {
        return poirepo;
    }

    public boolean insertPendingMap(MapSheet map) {

        String sql = """
    INSERT INTO pending_maps
    (version, price, name, description, path, poi_array,route_array)
    VALUES (?, ?, ?, ?, ?, ?,?)
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

            int affected = stmt.executeUpdate();   // ✅ only once
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
                    //load arrays
                    String poiJson = rs.getString("poi_array");
                    String routeJson = rs.getString("route_array");

                    ArrayList<Poi> pois= JsonUtil.jsonToPoiList(poiJson);
                    ArrayList<Route> routes= JsonUtil.jsonToRouteList(routeJson);


                    for (Route r : routes) {
                        var pts = r.getBasePoints(); // List<double[]>

                        System.out.println("route id: " + r.getId());
                        System.out.println("points size: " + pts.size());

                        if (!pts.isEmpty()) {
                            System.out.println("first: " + Arrays.toString(pts.get(0)));
                            System.out.println("second: " + (pts.size() > 1 ? Arrays.toString(pts.get(1)) : "n/a"));
                            System.out.println("last: " + Arrays.toString(pts.get(pts.size() - 1)));
                        }
                    }


                    return new MapSheet(
                            rs.getInt("version"),
                            rs.getDouble("price"),
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getString("path"),
                            routes,pois

                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null; // not found
    }
    //delete pending map on approval
    public boolean deletePendingMap(int version) {
        String sql = "DELETE FROM pending_maps WHERE version = ?";

        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, version);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;   // true if something was deleted

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    //inserting maps to city
    public boolean insertApprovedMap(ApprovePayload ApprovedMap) {
            MapSheet map =ApprovedMap.getMap();
            String CityName=ApprovedMap.getCityName();
            boolean delete= deletePendingMap(map.getVersion());
            if(!delete) System.out.println("faild to delete the pending map");
        if (map == null) return false;

        String sql = """
            INSERT INTO maps (price, cityID, mapName, map)
            VALUES (?, ?, ?, ?)
        """;


        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            int cityId =cityRepo.idByCityName(CityName) ;         // <-- change this if your MapSheet uses a different getter
            String mapName = map.getName();

            // Use your JsonUtil MapSheet -> JSON method
            String mapJson = JsonUtil.mapSheetToJsonWithEmbeddedArrays(map);
            // If you used the “real arrays” version (obj.add(...)), it’s still a string here and MySQL JSON accepts it.

            stmt.setDouble(1, map.getPrice());
            stmt.setInt(2, cityId);
            stmt.setString(3, mapName);
            stmt.setString(4, mapJson);


            int affected = stmt.executeUpdate();
            if (affected == 0) return false;

            // Optional: read generated mapID if you want it
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int generatedMapId = rs.getInt(1);
                    // If MapSheet has setMapId(...), you can store it:
                    // map.setMapId(generatedMapId);
                }
            }

            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }



    public List<MapSheet> loadAllPendingMaps() {
        List<MapSheet> maps = new ArrayList<>();

        String sql = """
        SELECT version, price, name, description, path, poi_array, route_array
        FROM pending_maps
        ORDER BY name, version
        """;


        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {

                // load arrays
                String poiJson = rs.getString("poi_array");
                String routeJson = rs.getString("route_array");

                ArrayList<Poi> pois = JsonUtil.jsonToPoiList(poiJson);
                ArrayList<Route> routes = JsonUtil.jsonToRouteList(routeJson);

                // (optional debug – same as yours)
                for (Route r : routes) {
                    var pts = r.getBasePoints();

                    System.out.println("route id: " + r.getId());
                    System.out.println("points size: " + pts.size());

                    if (!pts.isEmpty()) {
                        System.out.println("first: " + Arrays.toString(pts.get(0)));
                        System.out.println("second: " + (pts.size() > 1 ? Arrays.toString(pts.get(1)) : "n/a"));
                        System.out.println("last: " + Arrays.toString(pts.get(pts.size() - 1)));
                    }
                }

                MapSheet map = new MapSheet(
                        rs.getInt("version"),
                        rs.getDouble("price"),
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

        String sql = """
        SELECT map
        FROM maps
        WHERE cityID = ?
    """;

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





}
