package common.model;

import java.io.Serializable;

public class CityPricingItem implements Serializable {

    private int cityId;
    private double price;
    private double subPrice;

    public CityPricingItem(int cityId, double price, double subPrice) {
        this.cityId = cityId;
        this.price = price;
        this.subPrice = subPrice;
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

    public double getSubPrice() {
        return subPrice;
    }
    public void setSubPrice(double subPrice) {
        this.subPrice = subPrice;
    }
}

