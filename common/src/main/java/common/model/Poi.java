
package common.model;

public class Poi {
    private final int id;
    private final String name;
    private final double lon;   // or x in map coords
    private final double lat;   // or y in map coords
    private final String category;

    public Poi(int id, String name, double lon, double lat, String category) {
        this.id = id;
        this.name = name;
        this.lon = lon;
        this.lat = lat;
        this.category = category;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public double getLon() { return lon; }
    public double getLat() { return lat; }
    public String getCategory() { return category; }
}
