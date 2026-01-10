package gcm.server.data;

import common.model.PendingRoute;
import common.model.*;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RouteRepo {


    public boolean insertPendingRoute(PendingRoute route) throws SQLException {

        String insertRouteSql = """
        INSERT INTO pending_routes (name, description, cityID)
        VALUES (?, ?, ?)
    """;

        String insertStopSql = """
        INSERT INTO pending_route_stops (routeID, poiId, stop_order)
        VALUES (?, ?, ?)
    """;

        try (Connection conn = DbManager.getConnection()) {
            conn.setAutoCommit(false);

            int routeId;

            try (PreparedStatement ps = conn.prepareStatement(
                    insertRouteSql,
                    Statement.RETURN_GENERATED_KEYS
            )) {
                ps.setString(1, route.getName());
                ps.setString(2, route.getDescription());
                ps.setInt(3, route.getCityId());

                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                if (!rs.next()) {
                    conn.rollback();
                    return false;
                }
                routeId = rs.getInt(1);
            }

            try (PreparedStatement ps = conn.prepareStatement(insertStopSql)) {
                int order = 1;
                for (int poiId : route.getStops()) {
                    ps.setInt(1, routeId);
                    ps.setInt(2, poiId);
                    ps.setInt(3, order++);
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            conn.commit();
            return true;
        }
    }


    public boolean approveRoute(int routeId) throws SQLException {

        String insertRouteSql = """
            INSERT INTO Routes (name, description, cityID)
            SELECT name, description, cityID
            FROM pending_routes
            WHERE routeID= ?
        """;

        String insertStopsSql = """
            INSERT INTO route_stops (routeID, poiId, stop_order)
            SELECT ?, poiId, stop_order
            FROM pending_route_stops
            WHERE routeID = ?
        """;

        String deletePendingStopsSql =
                "DELETE FROM pending_route_stops WHERE routeID = ?";

        String deletePendingRouteSql =
                "DELETE FROM pending_routes WHERE routeID = ?";

        try (Connection conn = DbManager.getConnection()) {
            conn.setAutoCommit(false);

            //  Create approved route
            try (PreparedStatement ps = conn.prepareStatement(
                    insertRouteSql, Statement.RETURN_GENERATED_KEYS)) {

                ps.setInt(1, routeId);
                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                if (!rs.next()) {
                    conn.rollback();
                    return false;
                }

                int newRouteId = rs.getInt(1);

                //  Copy stops
                try (PreparedStatement psStops =
                             conn.prepareStatement(insertStopsSql)) {
                    psStops.setInt(1, newRouteId);
                    psStops.setInt(2, routeId);
                    psStops.executeUpdate();
                }
            }

            // Cleanup pending
            try (PreparedStatement ps = conn.prepareStatement(deletePendingStopsSql)) {
                ps.setInt(1, routeId);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(deletePendingRouteSql)) {
                ps.setInt(1, routeId);
                ps.executeUpdate();
            }

            conn.commit();
            return true;
        }
    }


    public List<PendingRoute> getPendingRoutes() throws SQLException {

        List<PendingRoute> routes = new ArrayList<>();

        String routeSql = """
        SELECT routeID, cityID, name, description
        FROM pending_routes
    """;

        String stopsSql = """
        SELECT poiID, stop_order
        FROM pending_route_stops
        WHERE routeID = ?
        ORDER BY stop_order
    """;

        try (Connection conn = DbManager.getConnection();
             PreparedStatement routeStmt = conn.prepareStatement(routeSql);
             ResultSet rs = routeStmt.executeQuery()) {

            while (rs.next()) {

                int routeId = rs.getInt("routeID");
                int cityId  = rs.getInt("cityID");
                String name = rs.getString("name");
                String desc = rs.getString("description");

                ArrayList<Integer> stops = new ArrayList<>();

                try (PreparedStatement stopsStmt = conn.prepareStatement(stopsSql)) {
                    stopsStmt.setInt(1, routeId);

                    try (ResultSet rsStops = stopsStmt.executeQuery()) {
                        while (rsStops.next()) {
                            stops.add(rsStops.getInt("poiID"));
                        }
                    }
                }

                PendingRoute route = new PendingRoute(
                        routeId,
                        name,
                        desc,
                        cityId,
                        stops
                );

                routes.add(route);
            }
        }

        return routes;
    }




    public RouteSheet loadRouteSheet(int routeId) throws SQLException {

        String routeSql = """
        SELECT r.routeID, r.cityID, r.name, r.description, c.baseMap
        FROM routes r
        JOIN Cities c ON r.cityID = c.CityID
        WHERE r.routeID = ?
    """;

        String stopsSql = """
        SELECT p.*
        FROM route_stops rs
        JOIN pois p ON rs.poiID = p.id
        WHERE rs.routeID = ?
        ORDER BY rs.stop_order
    """;

        try (Connection conn = DbManager.getConnection()) {

            Route route;
            int cityId;
            String tilePath;

            try (PreparedStatement ps = conn.prepareStatement(routeSql)) {
                ps.setInt(1, routeId);
                ResultSet rs = ps.executeQuery();

                if (!rs.next()) return null;

                cityId = rs.getInt("cityID");
                tilePath = rs.getString("baseMap");

                route = new Route(
                        rs.getString("name"),
                        rs.getString("description"),
                        null
                );
            }

            List<Poi> pois = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(stopsSql)) {
                ps.setInt(1, routeId);
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    pois.add(PoiRepo.fromResultSet(rs));
                }
            }

            return new RouteSheet(
                    routeId,
                    cityId,
                    tilePath,
                    route,
                    pois
            );
        }
    }

    public RouteSheet loadPendingRouteSheet(int routeId) throws SQLException {
        String routeSql = """
        SELECT pr.routeID, pr.cityID, pr.name, pr.description, c.baseMap
        FROM pending_routes pr
        JOIN Cities c ON pr.cityID = c.CityID
        WHERE pr.routeID = ?
    """;

        String stopsSql = """
        SELECT p.*
        FROM pending_route_stops prs
        JOIN pois p ON prs.poiID = p.id
        WHERE prs.routeID = ?
        ORDER BY prs.stop_order
    """;

        try (Connection conn = DbManager.getConnection()) {

            Route route;
            int cityId;
            String tilePath;

            try (PreparedStatement ps = conn.prepareStatement(routeSql)) {
                ps.setInt(1, routeId);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) return null;

                cityId = rs.getInt("cityID");
                tilePath = rs.getString("baseMap");

                route = new Route(
                        rs.getString("name"),
                        rs.getString("description"),
                        null
                );
            }

            List<Poi> pois = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(stopsSql)) {
                ps.setInt(1, routeId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    pois.add(PoiRepo.fromResultSet(rs));
                }
            }

            return new RouteSheet(routeId, cityId, tilePath, route, pois);
        }
    }

    public List<RouteSheet> loadApprovedRoutesForCity(int cityId) throws SQLException {

        String sql = """
        SELECT routeID
        FROM routes
        WHERE cityID = ?
    """;

        List<RouteSheet> result = new ArrayList<>();

        try (Connection conn = DbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, cityId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int routeId = rs.getInt("routeID");

                    // reuse existing method
                    RouteSheet sheet = loadRouteSheet(routeId);
                    if (sheet != null) result.add(sheet);
                }
            }
        }

        return result;
    }





}
