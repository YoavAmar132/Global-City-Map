package common.messages;

import java.io.Serializable;

public class PendingCityPricePayload implements Serializable {

    private final int cityId;
    private final double cityPrice;
    private final double subPrice;

    public PendingCityPricePayload(int cityId, double cityPrice, double subPrice) {
        this.cityId = cityId;
        this.cityPrice = cityPrice;
        this.subPrice = subPrice;
    }

    public int getCityId() { return cityId; }
    public double getCityPrice() { return cityPrice; }
    public double getSubPrice() { return subPrice; }
}
