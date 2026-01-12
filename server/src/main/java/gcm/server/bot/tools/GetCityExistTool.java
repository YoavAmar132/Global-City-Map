package gcm.server.bot.tools;

import gcm.server.data.CityRepo;

import java.util.Map;

public class GetCityExistTool implements BotTool {

    @Override
    public String getName() {
        return "getCityExist";
    }

    @Override
    public String getDescription() {
        return "Returns whatever or not the city exist";
    }

    @Override
    public String execute(Map<String, String> args) {
        String city = args.get("cityName");
        if (city == null) return "City name missing";

        CityRepo cityRepo = new CityRepo();
        try{
            if(cityRepo.existsByCityName(city)) return "The city of "+city+" exists";
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return "The city of "+city+"doesn't exists";
    }
}
