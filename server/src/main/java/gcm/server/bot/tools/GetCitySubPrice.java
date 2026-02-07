package gcm.server.bot.tools;

import common.model.City;
import gcm.server.data.CityRepo;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class GetCitySubPrice implements BotTool {

    @Override
    public String getName() {
        return "getCitySubPrice";
    }

    @Override
    public String getDescription() {
        return "- getCitySubPrice requires EXACT argument name: cityName\nreturn the price for a subscription for the city";
    }

    @Override
    public String execute(Map<String, String> args) throws Exception {
        String cityName = args.get("cityName");
        if (cityName == null) throw new Exception("CityName not provided");

        cityName = normalize(cityName);
        CityRepo cityRepo = new CityRepo();

        int cityId = cityRepo.idByCityName(cityName);
        City city = cityRepo.getCity(cityId);
        if(city == null) throw new Exception("City not found");

        return "The price for a subscription for the city of " + cityName + " is " + city.getPrice() +"$";
    }

    private static String normalize(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}

