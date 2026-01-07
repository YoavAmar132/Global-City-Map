package common.model;

import java.io.Serializable;

public class CityCatalogItem implements Serializable {

    private final int cityId;
    private final String cityName;
    private final int mapCount;
    private final double cityPrice;
    private final double subPrice;

    public CityCatalogItem(int cityId, String cityName, int mapCount,
                           double cityPrice, double subPrice) {
        this.cityId = cityId;
        this.cityName = cityName;
        this.mapCount = mapCount;
        this.cityPrice = cityPrice;
        this.subPrice = subPrice;
    }

    public int getCityId() { return cityId; }
    public String getCityName() { return cityName; }
    public int getMapCount() { return mapCount; }
    public double getCityPrice() { return cityPrice; }
    public double getSubPrice() { return subPrice; }
}
