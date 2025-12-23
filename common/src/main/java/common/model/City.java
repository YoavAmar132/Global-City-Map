package common.model;

import java.io.Serializable;

/**
 * nthg special here
 */
public class City implements Serializable {

    private final int id;
    private final String name;
    private final String   basemap;

    public City(int id, String name,String BaseMap) {
        this.id = id;
        this.name = name;
        this.basemap=BaseMap;
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
}
