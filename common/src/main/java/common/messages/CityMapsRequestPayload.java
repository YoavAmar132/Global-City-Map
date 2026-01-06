package common.messages;

import java.io.Serializable;

public class CityMapsRequestPayload implements Serializable {
    private int cityId;
    private int userId;

    public CityMapsRequestPayload(int cityId, int userId) {

        this.cityId = cityId;
        this.userId = userId;
    }

    public int getCityId() {
        return cityId;
    }
    public int getUserId() { return userId; }
}
