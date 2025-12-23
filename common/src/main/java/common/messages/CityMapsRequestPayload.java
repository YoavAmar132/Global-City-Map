package common.messages;

import java.io.Serializable;

public class CityMapsRequestPayload implements Serializable {
    private int cityId;

    public CityMapsRequestPayload(int cityId) {
        this.cityId = cityId;
    }

    public int getCityId() {
        return cityId;
    }
}
