package gcm.server.service;

import common.messages.*;
import common.model.*;
import gcm.server.data.MapRepo;
import gcm.server.data.PoiRepo;
import gcm.server.data.UserRepo;
import gcm.server.data.RouteRepo;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MapService {

    private final MapRepo mapRepository;
    private final UserRepo userRepository;
    private final RouteRepo routeRepository;

    private ArrayList<Integer> users;

    public MapSheet map;

    public MapService(
            MapRepo mapRepository,
            UserRepo userRepository,
            RouteRepo routeRepository
    ) {
        this.mapRepository = mapRepository;
        this.userRepository = userRepository;
        this.routeRepository = routeRepository;
    }


    public List<Poi> getPoisForCity(int cityId) {
        return mapRepository.getPoisForCity(cityId);
    }

    /* ================= MAPS ================= */





    public List<Poi> getpois() {
        return mapRepository.getAllPoi();
    }

    public boolean PendMap(MapSheet map) {
        return mapRepository.insertPendingMap(map);
    }

    public boolean sendApprovedMap(ApprovePayload approvePayload) {
        return mapRepository.insertApprovedMap(approvePayload);
    }

    public MapSheet PullMap(int version, String name) {
        return mapRepository.loadPendingMap(version, map.getCityID(), name);
    }

    public List<MapSheet> PullAllMap() {
        return mapRepository.loadAllPendingMaps();
    }

    public List<MapSheet> PullAllCityMaps(String CityName) {
        return mapRepository.loadAllMapsFromCity(CityName);
    }

    public int getPoiIndex() throws SQLException {
        PoiRepo poirepository = mapRepository.getPoirepo();
        return poirepository.getLastPoiId();
    }

    /* ================= PURCHASES ================= */

    public boolean isCityPurchased(int userId, String cityName) {
        return mapRepository.isCityPurchased(userId, cityName);
    }

    public boolean isUserSubscribed(int userId, String cityName) throws SQLException {
        return mapRepository.isUserSubscribed(userId, cityName);
    }

    public boolean isMapVersionPurchased(int userId, String cityName, int version) {
        return mapRepository.isMapVersionPurchased(userId, cityName, version);
    }

    public boolean addPurchase(int userId, String cityName, double price,
                               boolean isSubscription, int months) throws SQLException {
        return mapRepository.addPurchase(userId, cityName, price, isSubscription, months);
    }

    public List<City> getSubscribedCities(int userId) {
        return mapRepository.getSubscribedCities(userId);
    }

    public ArrayList<MapSheet> getPurchasedMaps(int userId) {
        return mapRepository.getPurchasedMapsByUserId(userId);
    }

    public GcmResponse handleGetPurchasedCities(int userId) {
        try {
            List<City> cities = mapRepository.getPurchasedCitiesByUserId(userId);
            return GcmResponse.ok(cities);
        } catch (Exception e) {
            return GcmResponse.error("Server Error: " + e.getMessage());
        }
    }

    public void SendMessage(String City) throws SQLException {
        users = userRepository.getAllId();
        for (int id : users) {
            if (mapRepository.isUserSubscribed(id, City)) {
                mapRepository.writeMessage(id, "new version of " + City + " is now available");
            }
        }
    }

    public ArrayList<Message> getMessage(int id) throws SQLException {
        ArrayList<Message> messages = new ArrayList<>();
        ArrayList<String> strings = mapRepository.getaAllMessages(id);

        for (String s : strings) {
            messages.add(new Message("Map update", s));
        }

        return messages;
    }

    /* ================= ROUTES ================= */

    public boolean submitPendingRoute(PendingRoute route) throws SQLException {
        if (route == null) return false;
        if (route.getStops() == null || route.getStops().size() < 2) return false;
        return routeRepository.insertPendingRoute(route);
    }

    public boolean approveRoute(int routeId) throws SQLException {
        return routeRepository.approveRoute(routeId);
    }


    public ArrayList<Integer> getPopup(String City) throws SQLException {
        users = userRepository.getAllId();
        ArrayList<Integer> pop = new ArrayList<>();
        for (int id : users) {
            if (mapRepository.isUserSubscribed(id, City)) {
                pop.add(id);

            }
        }
        return pop;
    }

    public List<PendingRoute> getPendingRoutes() throws SQLException {
        return routeRepository.getPendingRoutes();
    }


    public RouteSheet getApprovedRouteSheet(int routeId) throws SQLException {
        return routeRepository.loadRouteSheet(routeId);
    }

    public RouteSheet getPendingRouteSheet(int routeId) throws SQLException {
        return routeRepository.loadPendingRouteSheet(routeId);
    }

    public List<RouteSheet> getApprovedRoutesForCity(int cityId) throws SQLException {
        return routeRepository.loadApprovedRoutesForCity(cityId);
    }

    public boolean approveEditedRoute(int pendingRouteId, int oldRouteId)
            throws SQLException {

        // delete old approved route
        routeRepository.deleteApprovedRoute(oldRouteId);

        // approve pending route (existing logic)
        return routeRepository.approveRoute(pendingRouteId);
    }
    public boolean approveRouteWithEditCheck(int pendingRouteId)
            throws SQLException {

        PendingRoute pr =
                routeRepository.getPendingRouteById(pendingRouteId);

        if (pr == null) return false;

        // EDIT case → remove old approved route
        if (pr.getSourceRouteId() != null) {
            routeRepository.deleteApprovedRoute(
                    pr.getSourceRouteId()
            );
        }

        // Always approve pending
        return routeRepository.approveRoute(pendingRouteId);
    }

    public List<MapSheet> getApprovedMapsForCity(int cityId) throws SQLException {
        return mapRepository.getApprovedMapsForCity(cityId);
    }

    public boolean deleteMap(int mapId) throws SQLException {

        // delete approved map
        mapRepository.deleteApprovedMap(mapId);

        // delete all pending versions (if exist)
        mapRepository.deletePendingMapsBySourceMapId(mapId);

        return true;
    }

    public boolean deleteRoute(int routeId) throws SQLException {

        // delete approved route
        routeRepository.deleteApprovedRoute2(routeId);

        // delete pending edits if exist
        routeRepository.deletePendingRouteBySourceRouteId(routeId);

        return true;
    }

    public boolean deletePoiIfUnused2(int poiId) throws SQLException {

        // used in maps?
        if (mapRepository.isPoiUsedInAnyMap(poiId)) {
            return false;
        }

        // used in routes?
        if (routeRepository.isPoiUsedInAnyRoute(poiId)) {
            return false;
        }

        // safe to delete
        return mapRepository
                .getPoirepo()
                .deletePoiById(poiId);
    }



    public boolean deletePoiIfUnused(int poiId) throws SQLException {

        if (mapRepository.isPoiUsedInAnyMap(poiId)) {
            return false;
        }

        if (routeRepository.isPoiUsedInAnyRoute(poiId)) {
            return false;
        }

        return mapRepository
                .getPoirepo()
                .deletePoiById(poiId);
    }


    public boolean deleteRoute2(int routeId) throws SQLException {

        // delete pending edits
        routeRepository.deletePendingRouteBySourceRouteId(routeId);

        // delete approved route
        routeRepository.deleteApprovedRoute(routeId);

        return true;
    }

    public List<MapSheet> getApprovedMapsForCity2(int cityId)
            throws SQLException {

        return mapRepository.getApprovedMapsForCity(cityId);
    }


    public List<MapDeleteItem> getApprovedMapsForDelete(int cityId) {
        return mapRepository.getApprovedMapsForDelete(cityId);
    }

    public boolean rejectPendingRoute(int routeId) throws SQLException {
        return routeRepository.deletePendingRoute(routeId);
    }

    public boolean rejectPendingMap(MapSheet map) throws SQLException {
        return mapRepository.deletePendingMap(
                map.getCityID(),
                map.getName(),
                map.getVersion()
        );
    }


}
