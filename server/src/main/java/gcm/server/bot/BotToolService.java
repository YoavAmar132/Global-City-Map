package gcm.server.bot;

import gcm.server.service.CityService;
import gcm.server.service.MapService;

public class BotToolService {

    private final CityService cityService;
    private final MapService mapService;

    public BotToolService(CityService cityService, MapService mapService) {
        this.cityService = cityService;
        this.mapService = mapService;
    }

    public boolean cityExists(String cityName) {
        try{
            return cityService.existsByCityName(cityName);
        }
        catch(Exception e){
            return false;
        }
    }

}
