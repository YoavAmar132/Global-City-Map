package gcm.server.data;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;
import java.util.ArrayList;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import common.model.MapSheet;
import common.model.Poi;
import common.model.Route;

public class JsonUtil {

    private static final Gson gson = new GsonBuilder().create();

    public static String poiListToJson(ArrayList<Poi> pois) {
        return gson.toJson(pois);
    }
    public static String routeListToJson(ArrayList<Route> routes) {
        return gson.toJson(routes);
    }

    public static ArrayList<Poi> jsonToPoiList(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }

        Type listType = new TypeToken<ArrayList<Poi>>() {}.getType();
        return gson.fromJson(json, listType);
    }

    public static ArrayList<Route> jsonToRouteList(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }

        Type listType = new TypeToken<ArrayList<Route>>() {}.getType();
        return gson.fromJson(json, listType);
    }

    //--MapSheet

    public static String mapSheetToJsonWithEmbeddedArrays(MapSheet map) {
        if (map == null) return null;

        String poiJson = poiListToJson(new ArrayList<>(map.getPois()));
        String routeJson = routeListToJson(new ArrayList<>(map.getRoutes()));

        JsonObject obj = new JsonObject();
        obj.addProperty("version", map.getVersion());
        obj.addProperty("name", map.getName());
        obj.addProperty("description", map.getDescription());
        obj.addProperty("path", map.getPath());

        // Embedded JSON as STRING fields:
        obj.addProperty("poi_array", poiJson);
        obj.addProperty("route_array", routeJson);

        return gson.toJson(obj);
    }
    public static MapSheet jsonToMapSheetWithEmbeddedArrays(String json) {
        if (json == null || json.isBlank()) return null;

        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();

        int version = obj.get("version").getAsInt();
        String name = obj.get("name").getAsString();
        String description = obj.has("description") && !obj.get("description").isJsonNull()
                ? obj.get("description").getAsString()
                : null;
        String path = obj.has("path") && !obj.get("path").isJsonNull()
                ? obj.get("path").getAsString()
                : null;

        String poiJson = obj.has("poi_array") && !obj.get("poi_array").isJsonNull()
                ? obj.get("poi_array").getAsString()
                : "[]";

        String routeJson = obj.has("route_array") && !obj.get("route_array").isJsonNull()
                ? obj.get("route_array").getAsString()
                : "[]";

        ArrayList<Poi> pois = jsonToPoiList(poiJson);
        ArrayList<Route> routes = jsonToRouteList(routeJson);

        MapSheet map = new MapSheet(
                version,
                name,
                description,
                path,
                routes,
                pois
        );


        return map;
    }

}
