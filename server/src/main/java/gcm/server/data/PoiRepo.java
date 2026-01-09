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
    public boolean insertPoi(Poi poi) throws SQLException {
        String sql = """
        INSERT INTO pois (name, description, category, x, y, is_accessible,cityID,is_approved)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
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
        SELECT id, name, description, category, x, y, is_accessible, cityID,is_approved
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
                            rs.getBoolean("is_approved")
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
                rs.getBoolean("is_approved")
        );
    }



}
