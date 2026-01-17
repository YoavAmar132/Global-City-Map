package common.messages;

import java.io.Serializable;

public class CreateCityPayload implements Serializable {
    private final String cityName;
    private final String baseMapPath;
    private final String description;

    public CreateCityPayload(String cityName, String baseMapPath, String description) {
        this.cityName = cityName;
        this.baseMapPath = baseMapPath;
        this.description = description;
    }

    public String getCityName() {
        return cityName;
    }

    public String getBaseMapPath() {
        return baseMapPath;
    }
    public String getDescription() {
        return description;
    }
}

