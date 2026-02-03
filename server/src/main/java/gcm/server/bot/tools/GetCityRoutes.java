package gcm.server.bot.tools;

import common.model.Poi;
import common.model.Route;
import common.model.RouteSheet;
import gcm.server.data.CityRepo;
import gcm.server.data.RouteRepo;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;


public class GetCityRoutes implements BotTool {

    @Override
    public String getName() {
        return "getCityRoutes";
    }

    @Override
    public String getDescription() {
        return "- getCityRoutes requires EXACT argument name: cityName (cityName has to be name of a city that exist in the system)\n" +
                "return the list of routes of the city in the following form:\n" +
                "routes for <cityName>\n" +
                "route 1: <route 1>\n" +
                "....\n" +
                "route n: <route n>\n"+
                "<route n>\n" +
                "where every <route i> is described in the form:\n" +
                "<name of poi 1 in route i>\n"+
                "....\n" +
                "<name of poi m in route i>\n" +
                "\n" +
                "for example:\n" +
                "if <cityName> = paris\n"+
                "you might get:\n"+
                "route 1: route1\n" +
                "Eifel tower\n"+
                "Arc de Triomphe\n"+
                "route 2: visiting the louvre museum\n" +
                "louvre museum\n";
    }

    @Override
    public String execute(Map<String, String> args) throws Exception {
        String city = args.get("cityName");
        if (city == null) throw new Exception("CityName not provided");

        city = normalize(city);

        CityRepo cityRepo = new CityRepo();
        int cityId = cityRepo.idByCityName(city);
        if(cityId == -1) throw new Exception("City not found");

        RouteRepo routeRepo = new RouteRepo();
        List<RouteSheet> routeSheetList = routeRepo.loadApprovedRoutesForCity(cityId);
        StringBuilder answer = new StringBuilder();
        answer.append("\n");
        int i  = 1;
        for (RouteSheet routeSheet : routeSheetList) {

            Route route = routeSheet.getRoute();
            answer.append("route ").append(i).append(": ").append(route.getName()).append("\n");
            List<Poi> pois= routeSheet.getOrderedPois();
            for (Poi poi : pois) {
                answer.append(poi.getName()).append("\n");;
            }
        }

        return answer.toString();
    }

    private static String normalize(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
