package gcm.server.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import common.model.MapSheet;
import common.model.Poi;
import common.model.Route;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class JsonUtil {

    private static final Gson gson = new GsonBuilder().create();

    public static String poiListToJson(ArrayList<Poi> pois) {
        return gson.toJson(pois);
    }


    public static ArrayList<Poi> jsonToPoiList(String json) {
        if (json == null || json.isBlank()) return new ArrayList<>();
        Type listType = new TypeToken<ArrayList<Poi>>() {}.getType();
        return gson.fromJson(json, listType);
    }


    // --- MapSheet (with embedded arrays as STRING fields) ---

    public static String mapSheetToJsonWithEmbeddedArrays(MapSheet map) {
        if (map == null) return null;

        String poiJson = poiListToJson(new ArrayList<>(map.getPois()));

        JsonObject obj = new JsonObject();
        obj.addProperty("version", map.getVersion());
        obj.addProperty("cityID", map.getCityID());
        obj.addProperty("name", map.getName());
        obj.addProperty("description", map.getDescription());
        obj.addProperty("path", map.getPath());

        obj.addProperty("poi_array", poiJson);

        return gson.toJson(obj);
    }

    public static MapSheet jsonToMapSheetWithEmbeddedArrays(String json) {
        if (json == null || json.isBlank()) return null;

        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();

        int version = obj.get("version").getAsInt();
        int cityID = obj.get("cityID").getAsInt();
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


        ArrayList<Poi> pois = jsonToPoiList(poiJson);

        return new MapSheet(
                version,
                cityID,
                name,
                description,
                path,
                pois
        );
    }
}
