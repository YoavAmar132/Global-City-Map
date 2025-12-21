package common.model;

import java.io.Serializable;

public class CityCatalogItem implements Serializable {


    private final int cityId;
    private final String cityName;
    private final int mapCount;
    private final double minPrice;
    private final double maxPrice;

    public CityCatalogItem(int cityId, String cityName,
                           int mapCount, double minPrice, double maxPrice) {
        this.cityId = cityId;
        this.cityName = cityName;
        this.mapCount = mapCount;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
    }

    public int getCityId() { return cityId; }
    public String getCityName() { return cityName; }
    public int getMapCount() { return mapCount; }
    public double getMinPrice() { return minPrice; }
    public double getMaxPrice() { return maxPrice; }
}
