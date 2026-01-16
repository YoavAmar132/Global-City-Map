package common.messages;

import java.io.Serializable;

public class CreateCityPayload implements Serializable {
    private final String cityName;
    private final String baseMapPath;

    public CreateCityPayload(String cityName, String baseMapPath) {
        this.cityName = cityName;
        this.baseMapPath = baseMapPath;
    }

    public String getCityName() {
        return cityName;
    }

    public String getBaseMapPath() {
        return baseMapPath;
    }
}

