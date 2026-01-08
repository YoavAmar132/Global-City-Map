
package common.model;

import java.io.Serializable;

public class Poi implements Serializable {
    public static final int BASE_ZOOM = 16;

    private final int id;
    private final String name;
    private final String description;
    private final POI_Category category;
    private final boolean accessible;
    private final int cityID;
    private final Boolean is_approved;

    // Stored in BASE_ZOOM world-pixels
    private final double baseWorldX;
    private final double baseWorldY;

    public Poi(int id, String name, String description,
               double baseWorldX, double baseWorldY,
               POI_Category category,
               boolean accessible, int cityID, boolean is_approved) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.baseWorldX = baseWorldX;
        this.baseWorldY = baseWorldY;
        this.category = category;
        this.accessible = accessible;
        this.cityID = cityID;
        this.is_approved = is_approved;
    }


    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public POI_Category getCategory() { return category; }
    public boolean isAccessible() {
        return accessible;
    }
    public int getCityID() { return cityID; }
    public boolean isApproved() { return is_approved; }



    // Convert BASE_ZOOM world-pixels -> requested zoom world-pixels
    public double getWorldX(int zoomLevel) {
        double scale = Math.pow(2, BASE_ZOOM - zoomLevel);
        return baseWorldX / scale;
    }

    public double getWorldY(int zoomLevel) {
        double scale = Math.pow(2, BASE_ZOOM - zoomLevel);
        return baseWorldY/ scale;
    }
    // Convert BASE_ZOOM world-pixels -> requested zoom world-pixels
    public double getNWorldX() {
        return baseWorldX ;
    }

    public double getNWorldY() {
        return baseWorldY;
    }

    // Optional: if you ever need raw base coords
    public double getBaseWorldX() { return baseWorldX; }
    public double getBaseWorldY() { return baseWorldY; }

}

