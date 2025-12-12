
package common.model;

public class Poi {
    private final int id;
    private final String name;
    private final String description;
    private final double worldX ;   // or x in map coords
    private final double worldy ;   // or y in map coords
    private final POI_Category category;

    public Poi(int id, String name, String description,double worldX , double worldy, POI_Category category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.worldX  = worldX ;
        this.worldy = worldy;
        this.category = category;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public double getWorldX () { return worldX ; }
    public double getWorldy() { return worldy; }
    public POI_Category getCategory() { return category; }
}
