package common.model;


import java.io.Serializable;

/**
 * nthg special here
 */
public class City implements Serializable {

    private final int id;
    private final String name;
    private final String basemap;
    private final String CityImageKey;

    public City(int id, String name,String basemap,String CityImageKey) {
        this.id = id;
        this.name = name;
        this.basemap=basemap;
        this.CityImageKey=CityImageKey;
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

    public String getCityImageKey() {
        return CityImageKey;
    }
}
