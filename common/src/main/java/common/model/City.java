package common.model;

import java.io.Serializable;

/**
 * nthg special here
 */
public class City implements Serializable {

    private int id;
    private String name;
    private String basemap;
    private double price;

    public City(int id, String name,String BaseMap, double price) {
        this.id = id;
        this.name = name;
        this.basemap=BaseMap;
        this.price=price;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getBasemap() {
        return basemap;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) { this.price=price; }
}
