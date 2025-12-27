package common.model;

import java.io.Serializable;

public class CityPricingItem implements Serializable {

    private int cityId;
    private double price;

    public CityPricingItem(int cityId, double price) {
        this.cityId = cityId;
        this.price = price;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
