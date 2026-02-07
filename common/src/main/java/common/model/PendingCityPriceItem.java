package common.model;

import java.io.Serializable;

public class PendingCityPriceItem implements Serializable {

    private final int cityId;
    private final String cityName;
    private final double cityPrice;
    private final double subPrice;

    public PendingCityPriceItem(
            int cityId,
            String cityName,
            double cityPrice,
            double subPrice
    ) {
        this.cityId = cityId;
        this.cityName = cityName;
        this.cityPrice = cityPrice;
        this.subPrice = subPrice;
    }

    public int getCityId() { return cityId; }
    public String getCityName() { return cityName; }
    public double getCityPrice() { return cityPrice; }
    public double getSubPrice() { return subPrice; }
}
