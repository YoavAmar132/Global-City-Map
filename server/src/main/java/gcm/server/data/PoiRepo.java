package gcm.server.data;
import common.model.POI_Category;
import common.model.Poi ;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PoiRepo {
    public int getLastPoiId() throws SQLException {
        String sql = "SELECT MAX(id) AS max_id FROM pois";

        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("max_id"); // returns 0 if table is empty
            }
            return 0;
        }
    }

    public void updatePoiContent(
            Connection conn,
            int poiId,
            String name,
            String description,
            POI_Category category,
            boolean accessible,
            int recommendedMinutes
    ) throws SQLException {

        String sql = """
        UPDATE pois
        SET
            name = ?,
            description = ?,
            category = ?,
            is_accessible = ?,
            recommended_minutes = ?
        WHERE id = ?
    """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, description);
            stmt.setString(3, category.name());
            stmt.setBoolean(4, accessible);
            stmt.setInt(5, recommendedMinutes);
            stmt.setInt(6, poiId);
            stmt.executeUpdate();
        }
    }


    public boolean insertPoi(Poi poi) throws SQLException {
        String sql = """
        INSERT INTO pois (name, description, category, x, y, is_accessible,cityID,is_approved, recommended_minutes)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;


        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, poi.getName());
            stmt.setString(2, poi.getDescription());
            stmt.setString(3, poi.getCategory().name());
            stmt.setDouble(4, poi.getNWorldX());
            stmt.setDouble(5, poi.getNWorldY());
            stmt.setBoolean(6, poi.isAccessible());
            stmt.setInt(7,poi.getCityID());
            stmt.setBoolean(8, poi.isApproved());
            stmt.setInt(9, poi.getRecommendedMinutes());

            int rows = stmt.executeUpdate();
            return rows > 0;

            // get generated id

        }catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean insertAllPoi(ArrayList <Poi> pois) throws SQLException
    {
        for (Poi poi : pois) {
         if(!insertPoi(poi))
         {
             System.out.println("faild to insert poi");
             return false;
         }
        }

       return true;
    }
    public ArrayList<Poi> loadAllPois(int first, int last) {
        ArrayList<Poi> pois = new ArrayList<>();

        String sql = """
        SELECT id, name, description, category, x, y, is_accessible, cityID,is_approved, recommended_minutes
        FROM pois
        WHERE id BETWEEN ? AND ?
        ORDER BY id
        """;


        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, first);
            stmt.setInt(2, last);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    POI_Category category = POI_Category.valueOf(rs.getString("category"));
                    Poi poi = new Poi(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getDouble("x"),
                            rs.getDouble("y"),
                            category,
                            rs.getBoolean("is_accessible"),
                            rs.getInt("cityID"),
                            rs.getBoolean("is_approved"),
                            rs.getInt("recommended_minutes")
                    );



                    pois.add(poi);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return pois;
    }

    public List<Poi> loadPoisByCity(int cityId) {

        List<Poi> pois = new ArrayList<>();

        String sql = """
        SELECT id, name, description, category,
               x, y,
               is_accessible, cityID, is_approved, recommended_minutes
        FROM pois
        WHERE cityID = ? AND is_approved = 1
    """;

        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, cityId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {

                    Poi poi = new Poi(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("description"),

                            rs.getDouble("x"),   // ← maps to nWorldX
                            rs.getDouble("y"),   // ← maps to nWorldY

                            POI_Category.valueOf(rs.getString("category")),
                            rs.getBoolean("is_accessible"),
                            rs.getInt("cityID"),
                            rs.getBoolean("is_approved"),
                            rs.getInt("recommended_minutes")
                    );

                    pois.add(poi);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return pois;
    }



    public static Poi fromResultSet(ResultSet rs) throws SQLException {

        return new Poi(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getDouble("x"),
                rs.getDouble("y"),
                POI_Category.valueOf(rs.getString("category")),
                rs.getBoolean("is_accessible"),
                rs.getInt("cityID"),
                rs.getBoolean("is_approved"),
                rs.getInt("recommended_minutes")
        );
    }

    public int insertPoiAndReturnId(Poi poi) throws SQLException {

        String sql = """
        INSERT INTO pois
        (name, description, category, x, y, is_accessible, cityID, is_approved,recommended_minutes)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?,?)
    """;

        try (Connection conn = DbManager.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, poi.getName());
            ps.setString(2, poi.getDescription());
            ps.setString(3, poi.getCategory().name());

            ps.setDouble(4, poi.getNWorldX());   // → maps to `x`
            ps.setDouble(5, poi.getNWorldY());   // → maps to `y`

            ps.setBoolean(6, poi.isAccessible());
            ps.setInt(7, poi.getCityID());
            ps.setBoolean(8, true);              // approved POI
            ps.setInt(9, poi.getRecommendedMinutes());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        throw new SQLException("Failed to insert POI, no ID returned");
    }


    public boolean deletePoiById(int poiId) {

        String sql = "DELETE FROM pois WHERE id = ?";

        try (Connection conn = DbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, poiId);
            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }






}
