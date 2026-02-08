package gcm.server.bot.tools;

import gcm.server.data.CityRepo;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class GetCityExistTool implements BotTool {

    @Override
    public String getName() {
        return "getCityExist";
    }

    @Override
    public String getDescription() {
        return "- getCityExist requires EXACT argument name: cityName\nreturn whether or not the city <cityName> exists";
    }

    @Override
    public String execute(Map<String, String> args) {
        String city = args.get("cityName");
        if (city == null) return "City name missing";
        city = normalize(city);
        CityRepo cityRepo = new CityRepo();
        try{
            if(cityRepo.existsByCityName(city)) return "The city of "+city+" exists";
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return "The city of "+city+" doesn't exists";
    }

    private static String normalize(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
