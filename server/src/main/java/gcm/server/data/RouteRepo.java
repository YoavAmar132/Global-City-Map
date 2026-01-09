package gcm.server.data;
import common.model.POI_Category;
import common.model.Poi ;
import common.model.Route;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RouteRepo {
    public int getLastRouteId() throws SQLException {
        String sql = "SELECT MAX(id) AS max_id FROM routes";

        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("max_id"); // returns 0 if table is empty
            }
            return 0;
        }
    }
    public boolean insertRoute(Route route) {
        List<double[]> points = route.getBasePoints();

        // LINESTRING requires at least 2 points
        if (points == null || points.size() < 2) {
            return false;
        }

        StringBuilder wkt = new StringBuilder("LINESTRING(");

        for (int i = 0; i < points.size(); i++) {
            double[] p = points.get(i);

            // safety check
            if (p == null || p.length != 2) {
                return false;
            }

            wkt.append(p[0]).append(" ").append(p[1]);

            if (i < points.size() - 1) {
                wkt.append(", ");
            }
        }
        wkt.append(")");

        String sql = """
        INSERT INTO routes (name, description, category, basePoints)
        VALUES (?, ?, ?, ST_GeomFromText(?))
        """;

        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, route.getName());
            stmt.setString(2, route.getDescription());
            stmt.setString(3, route.getCategory().name());
            stmt.setString(4, wkt.toString());

            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean insertAllRoutes(ArrayList <Route> routes) throws SQLException
    {
        for (Route route : routes) {
            if(!insertRoute(route))
            {
                System.out.println("faild to insert route");
                return false;
            }
        }

        return true;
    }
    public ArrayList<Route> loadAllRoutes(int first, int last) {
        ArrayList<Route> routes = new ArrayList<>();

        String sql = """
        SELECT id, name, description, category,
               ST_AsText(basePoints) AS wkt
        FROM routes
        WHERE id BETWEEN ? AND ?
        ORDER BY id
        """;

        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, first);
            stmt.setInt(2, last);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    String description = rs.getString("description");
                    String string_category = rs.getString("category");
                    POI_Category category = POI_Category.valueOf(string_category);


                    Route route = new Route(id, name, description, category, null, 0);

                    // Parse LINESTRING
                    String wkt = rs.getString("wkt");
                    parseLineString(wkt, route.getBasePoints());

                    routes.add(route);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return routes;
    }
    private void parseLineString(String wkt, List<double[]> basePoints) {
        if (wkt == null || !wkt.startsWith("LINESTRING")) {
            return;
        }

        // Remove "LINESTRING(" and ")"
        String pointsPart = wkt.substring(
                wkt.indexOf('(') + 1,
                wkt.lastIndexOf(')')
        );

        String[] points = pointsPart.split(",");

        for (String point : points) {
            String[] xy = point.trim().split("\\s+");
            double x = Double.parseDouble(xy[0]);
            double y = Double.parseDouble(xy[1]);

            basePoints.add(new double[]{x, y});
        }
    }




}
