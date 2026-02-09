package common.messages;

import java.io.Serializable;

public class CityIdPayload implements Serializable {
    private final int cityId;

    public CityIdPayload(int cityId) {
        this.cityId = cityId;
    }

    public int getCityId() {
        return cityId;
    }
}
