package gcm.server.service;

import common.messages.*;
import common.model.*;
import gcm.server.data.MapRepo;
import gcm.server.data.PoiRepo;
import gcm.server.data.UserRepo;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class MapService {
    private final MapRepo mapRepository;
    private final UserRepo userRepository;
    private ArrayList<Integer> users;

    public MapSheet map;

    public MapService(MapRepo mapRepository ,UserRepo userRepository) {
        this.mapRepository = mapRepository ;
        this.userRepository=userRepository;
    }

    //store
    public List<Poi> getpois() {
      return mapRepository.getAllPoi();
    }

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
        return  mapRepository.loadPendingMap(version,map.getCityID(),name);
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


    // --- MapService.java ---

    // 1. Check if user already owns it (OTP)
    public boolean isCityPurchased(int userId, String cityName) {
        return mapRepository.isCityPurchased(userId, cityName);
    }

    // 2. Check if user is already subscribed (Subscription)
    public boolean isUserSubscribed(int userId, String cityName) throws SQLException {
        return mapRepository.isUserSubscribed(userId, cityName);
    }

    public boolean isMapVersionPurchased(int userId, String cityName, int version) {
        return mapRepository.isMapVersionPurchased(userId, cityName, version);
    }

    public int getLatestVersion(String cityName) {
        // You might need to add a helper in MapRepo to get ID from Name, then get Latest Version
        // Or just return 0 and let Repo handle it inside addPurchase
        return 0;
    }

    public boolean addPurchase(int userId, String cityName, double price, boolean isSubscription, int version) throws SQLException {
        return mapRepository.addPurchase(userId, cityName, price, isSubscription, version);
    }

    public List<City> getSubscribedCities(int userId) {
        return mapRepository.getSubscribedCities(userId);
    }

    // In MapService.java
    public List<MapSheet> getPurchasedMaps(int userId) {
        // OLD: return mapRepository.getPurchasedCitiesByUserId(userId);
        // NEW:
        return mapRepository.getPurchasedMapsByUserId(userId);
    }

    public GcmResponse handleGetPurchasedCities(int userId) {
        try {
            List<City> cities = mapRepository.getPurchasedCitiesByUserId(userId);
            return GcmResponse.ok(cities); // Returns ArrayList<City>
        } catch (Exception e) {
            return GcmResponse.error("Server Error: " + e.getMessage());
        }
    }
    public void SendMessage(String City) throws SQLException {
        users=userRepository.getAllId();
        for(int id:users)
        {
            if(mapRepository.isUserSubscribed(id,City))
            {
               mapRepository.writeMessage(id,"new version of"+City+" is now available");

            }
        }


    }
    public ArrayList<Message> getMessage(int id) throws SQLException {
        ArrayList<Message> messages=new ArrayList<Message>();
        ArrayList<String> strings=mapRepository.getaAllMessages(id);
        if(!strings.isEmpty())
        {
            for(String s:strings)
            {
                Message m =new Message("Map update",s);
                messages.add(m);
            }
        }

        return  messages;

    }
}