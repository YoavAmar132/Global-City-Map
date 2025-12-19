package gcm.server.data;

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

        public MapRepo() {
            this.poirepo = new PoiRepo();
            this.routeRepo = new RouteRepo();
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
    (version, name, description, path, poi_array,route_array)
    VALUES (?, ?, ?, ?, ?,?)
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
            stmt.executeUpdate();

            return stmt.executeUpdate() == 1;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public MapSheet loadPendingMap(int version, String name) {
        String sql = """
        SELECT version, name, description, path, poi_array,route_array
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
                    ArrayList<Route> routes= JsonUtil.jsonTorouteList(routeJson);


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

                // load arrays
                String poiJson = rs.getString("poi_array");
                String routeJson = rs.getString("route_array");

                ArrayList<Poi> pois = JsonUtil.jsonToPoiList(poiJson);
                ArrayList<Route> routes = JsonUtil.jsonTorouteList(routeJson);

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




}
