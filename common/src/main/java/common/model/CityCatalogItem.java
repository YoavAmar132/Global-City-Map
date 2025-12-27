package common.model;

import java.io.Serializable;

public class CityCatalogItem implements Serializable {


    private final int cityId;
    private final String cityName;
    private final int mapCount;
    private final double minPrice;
    private final double maxPrice;
    private final double cityPrice;

    public CityCatalogItem(int cityId, String cityName,
                           int mapCount, double minPrice, double maxPrice,double cityPrice) {
        this.cityId = cityId;
        this.cityName = cityName;
        this.mapCount = mapCount;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.cityPrice = cityPrice;
    }

    public int getCityId() { return cityId; }
    public String getCityName() { return cityName; }
    public int getMapCount() { return mapCount; }
    public double getMinPrice() { return minPrice; }
    public double getMaxPrice() { return maxPrice; }
    public double getCityPrice() { return cityPrice; }
}
