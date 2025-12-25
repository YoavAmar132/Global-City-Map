package gcm.server.service;

import common.messages.ApprovePayload;
import common.messages.BuyMapPayload;
import common.messages.GcmResponse;
import common.model.City;
import common.model.MapSheet;
import common.model.Poi;
import common.model.User;
import gcm.server.data.MapRepo;
import gcm.server.data.PoiRepo;
import gcm.server.data.RouteRepo;
import gcm.server.data.UserRepo;
import common.messages.IndexPayload;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class MapService {
    private final MapRepo mapRepository;

    public MapSheet map;

    public MapService(MapRepo mapRepository) {
        this.mapRepository = mapRepository;
    }

    //store
    public boolean PendMap(MapSheet map)
    {
        System.out.println("map service created");
        return mapRepository.insertPendingMap(map);
    }
    public boolean sendApprovedMap(ApprovePayload approvePayload)
    {
        System.out.println("inserting map service created");
        return mapRepository.insertApprovedMap(approvePayload);
    }
    //load map
    public MapSheet PullMap(int version,String name)
    {
        return  mapRepository.loadPendingMap(version,name);
    }
    public List<MapSheet> PullAllMap()
    {
        return  mapRepository.loadAllPendingMaps();
    }
    public List<MapSheet> PullAllCityMaps(String CityName)
    {
        return  mapRepository.loadAllMapsFromCity(CityName);
    }

    public int getPoiIndex() throws SQLException {
        PoiRepo poirepository=mapRepository.getPoirepo();
        return poirepository.getLastPoiId();
    }
    public int getRouteIndex() throws SQLException {
        RouteRepo routerepository=mapRepository.getRouteRepo();
        return routerepository.getLastRouteId();
    }

    public GcmResponse handleBuyMap(BuyMapPayload payload) {
        System.out.println("Service: Processing BuyMap for user " + payload.getUserId());

        // 1. Validation
        if (payload.getCityName() == null || payload.getCityName().isEmpty()) {
            return GcmResponse.error("Invalid City Name");
        }

        // 2. Check if already purchased (Optional, prevents double buy)
        if (mapRepository.isCityPurchased(payload.getUserId(), payload.getCityName())) {
            return GcmResponse.error("You already own this city!");
        }

        // 3. Perform Purchase
        boolean success = mapRepository.addPurchase(
                payload.getUserId(),
                payload.getCityName(),
                payload.getPrice()
                // or derive from payload if you update it
        );

        if (success) {
            // 4. Return Success
            // (Later, you can change this to return the actual MapSheet object if you want immediate viewing)
            return GcmResponse.ok("Purchase successful");
        } else {
            return GcmResponse.error("Database Error: Could not complete purchase.");
        }
    }

    public GcmResponse handleGetPurchasedCities(int userId) {
        try {
            List<City> cities = mapRepository.getPurchasedCitiesByUserId(userId);
            return GcmResponse.ok(cities); // Returns ArrayList<City>
        } catch (Exception e) {
            return GcmResponse.error("Server Error: " + e.getMessage());
        }
    }
}