package gcm.server.data;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;
import java.util.ArrayList;

import com.google.gson.reflect.TypeToken;
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

    public static ArrayList<Route> jsonTorouteList(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }

        Type listType = new TypeToken<ArrayList<Route>>() {}.getType();
        return gson.fromJson(json, listType);
    }
}
