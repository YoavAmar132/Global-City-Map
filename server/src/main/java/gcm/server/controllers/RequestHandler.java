package gcm.server.controllers;

import common.messages.*;
import common.model.*;
import gcm.server.network.GcmServer;
import gcm.server.service.*;
import common.messages.CityMapsRequestPayload;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RequestHandler {

    private final AuthService authService;
    private final MapService mapService;
    private final CityService cityService;
    private final CatalogService catalogService;
    private final StatsService statsService;
    private static final List<User> online_users = new ArrayList<>();
    private User loggedInUser = null;
     public Message message=new Message("new map added","");;
    public RequestHandler(AuthService authService, MapService mapservice, CityService cityService,
                          CatalogService catalogService, StatsService statsService) {
        this.authService = authService;
        this.mapService = mapservice;
        this.cityService = cityService;
        this.catalogService = catalogService;
        this.statsService = statsService;
    }

    /**
     * Main entry point for handling a request from a client.
     * For now, we only support LOGIN.
     */
    public GcmResponse handle(GcmRequest request) throws SQLException {

        RequestType type = request.getType();

        if (type == RequestType.LIST_POIS) {
            try {
                return handlePoi(request);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        if (type == RequestType.LOGOUT) {
            if(request.getPayload()!=null) {
                User user = (User) request.getPayload();
                online_users.removeIf(u ->
                        u.getUsername().equals(user.getUsername())
                );
            }
            return GcmResponse.ok(null);
        }


        if (type == RequestType.LIST_ROUTES) {
            try {
                return handleRoute(request);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        if (type == RequestType.LOGIN) {
            try {
                return handleLogin(request);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        if (type == RequestType.REGISTER) {
            try {
                return handleRegistration(request);
            } catch (SQLException e) {
                throw new RuntimeException("failed to register user", e);
            }
        }
        if(type==RequestType.PEND_MAP) {
            System.out.println("map handler created");
            try {
                System.out.println("request detected");
                return handleMapPending(request);
            }  catch (SQLException e) {
                throw new RuntimeException("failed to register user", e);
            }
        }

        if(type==RequestType.GET_POI_INDEX) {
            System.out.println("poi index created");
            try {
                System.out.println("request detected");
                return handlePoiIndex(request);
            }  catch (SQLException e) {
                throw new RuntimeException("failed to register user", e);
            }
        }
        if(type==RequestType.GET_ROUTE_INDEX) {
            System.out.println("route index created");
            try {
                System.out.println("request detected");
                return handleRouteIndex(request);
            }  catch (SQLException e) {
                throw new RuntimeException("failed to register user", e);
            }
        }
        if(type==RequestType.GET_MAP) {
            System.out.println("map request proccesed");
            try {
                System.out.println("request detected");
                return handleMapRequest(request);
            }  catch (SQLException e) {
                throw new RuntimeException("failed to register user", e);
            }
        }
        if (type == RequestType.GET_PENDING_MAPS) {
            try {
                return handleAllMapRequest(request);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        if (type == RequestType.APPROVE_MAP_VERSION) {
            try {
                return handleMapApproval(request);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        if (type == RequestType.LIST_CITIES) {
            try {
                return handleListCities(request);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        if (type == RequestType.LIST_MAPS_FOR_CITY) {
            try {
                return handleAllCityMaps(request);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        if (type == RequestType.GET_CITY_CATALOG) {
            try {
                return handleGetCityCatalog(request);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        if (type == RequestType.GET_CITY_MAPS) {
            try {
                return handleGetCityMaps(request);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        if (type == RequestType.BUY_MAP){
            return handleBuyMap(request);
        }

        if (type == RequestType.GET_ALL_CITY_PRICES) {
            try {
                return handleGetAllCityPrices(request);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        if (type == RequestType.UPDATE_CITY_PRICE) {
            try {
                GcmResponse r =handleUpdateCityPrice(request);
                if(r.isSuccess()){
                    r.setRefresh(1);
                }
                return r;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }


        if (type == RequestType.LIST_USER_PURCHASES) {
            try {
                return handleGetCityPurchases(request);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        if (type == RequestType.LIST_USER_SUBSCRIPTIONS) {
            try {
                return handleGetUserSubscriptions(request);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        if (type == RequestType.LIST_USER_MAPS) {
            try {
                return handleGetUserMaps(request);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        if (type == RequestType.GET_REPORT) {
            try {
                return handleGetReport(request);
            }
            catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        if (type == RequestType.GET_MESSAGES) {
            try {
                return handleMessages(request);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        if (type == RequestType.LIST_ALL_USERS) {
          return authService.getUsers();
        }
        if (type == RequestType.LIST_ALL_WORKERS) {
            return authService.getWorkers();
        }
        if (type == RequestType.LIST_USER_PURCHASES_HISTORY) {
            return handleHistory(request);
        }

        if (type == RequestType.SEARCH_CITY) {
            try {
                return handleSearch(request);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }


        // later you'll add more cases for other RequestTypes
        return GcmResponse.error("Unsupported request type: " + type);
    }
    /**
     * Handles LOGIN requests.
     * Expects payload = LoginPayload
     * Returns: GcmResponse.ok(User) on success, or GcmResponse.error(...) on failure.
     * overall perfect stuff from yoav just added try-catch for the exception
     */
    private GcmResponse handleLogin(GcmRequest request) throws SQLException {
        System.out.println("login request received");
        // 1. Validate and cast payload
        Object rawPayload = request.getPayload();
        if (!(rawPayload instanceof LoginPayload payload)) {
            return GcmResponse.error("Invalid payload for LOGIN request");
        }

        String username = payload.getUsername();
        String password = payload.getPassword();

        try {
            // 2. Delegate to AuthService to check DB / users list
            User user = authService.login(username, password);
            // 3. Handle failure
            if (user == null) {
                return GcmResponse.error(authService.getErrormsg());   // we have error function so use it :D
            }
            for(User u:online_users)
            {
                System.out.println(u.getUsername());
                if(u.getUsername().equals(user.getUsername()))
                {
                    return GcmResponse.error("user is already logged in");
                }

            }
            online_users.add(user);
            int x=authService.PendingMessage(user.getId());
          return GcmResponse.okm(user,String.valueOf(x));


        } catch (SQLException e) {
            e.printStackTrace(); // server log don't really care :D
            return GcmResponse.error("Server error during registration");
        }
    }

    public static synchronized void removeOnlineUser(User user) {
        online_users.removeIf(u ->
                u.getUsername().equals(user.getUsername())
        );
    }


    private GcmResponse handlePoi(GcmRequest request) throws SQLException {
        System.out.println("get all poi request");
        return GcmResponse.ok(mapService.getpois());
    }
    private GcmResponse handleRoute(GcmRequest request) throws SQLException {
        System.out.println("get all route request");
        return GcmResponse.ok(mapService.getroutes());
    }


    private GcmResponse handleGetAllCityPrices(GcmRequest request) throws SQLException {
        System.out.println("GET_ALL_CITY_PRICES request received");

        Object rawPayload = request.getPayload();
        if (!(rawPayload instanceof EmptyPayload)) {
            return GcmResponse.error("Invalid payload for GET_ALL_CITY_PRICES");
        }

        List<CityPricingItem> prices =
                cityService.getAllCityPrices();

        if (prices == null) {
            return GcmResponse.error("Failed to get city prices");
        }

        return GcmResponse.ok(prices);
    }

    private GcmResponse handleUpdateCityPrice(GcmRequest request) throws SQLException {
        System.out.println("UPDATE_CITY_PRICE request received");

        Object rawPayload = request.getPayload();
        if (!(rawPayload instanceof CityPricingItem item)) {
            return GcmResponse.error("Invalid payload for UPDATE_CITY_PRICE");
        }
        boolean success = cityService.updateCityPrice(item);

        if (!success) {
            return GcmResponse.error("Failed to update city price");
        }

        return GcmResponse.ok(null);

    }


    // map pending handeler
    private GcmResponse handleMapPending(GcmRequest request) throws SQLException {
        System.out.println("map pending request received");
        Object rawPayload = request.getPayload();
        if (!(rawPayload instanceof MapSheet mapSheet)) {
            return GcmResponse.error("Invalid payload for map pending request");
        }
        if(!mapService.PendMap(mapSheet)){ return GcmResponse.error("faild to pend");}
        return GcmResponse.ok(mapSheet);
    }
    //map aprroved handler
    private GcmResponse handleMapApproval(GcmRequest request) throws SQLException {
        System.out.println("map addition request received");
        Object rawPayload = request.getPayload();
        if (!(rawPayload instanceof ApprovePayload approvePayload)) {
            return GcmResponse.error("Invalid payload for map pending request");
        }
        if(!mapService.sendApprovedMap(approvePayload)){ return GcmResponse.error("faild to pend");}
         mapService.SendMessage(((ApprovePayload) rawPayload).getCityName());
        Popup p=new Popup(mapService.getPopup(((ApprovePayload) rawPayload).getCityName()));
        return GcmResponse.ok(p);
    }
    //map request handler
    private GcmResponse handleMapRequest(GcmRequest request) throws SQLException {
        System.out.println("map  request received");
        Object rawPayload = request.getPayload();
        if (!(rawPayload instanceof MaPayload maPayload)) {
            return GcmResponse.error("Invalid payload for map  request");
        }
        MapSheet map=mapService.PullMap(maPayload.getVersion(),maPayload.getName());
        if(map==null){ return GcmResponse.error("faild to get map");}
        return GcmResponse.ok(map);
    }
    private GcmResponse handleAllMapRequest(GcmRequest request) throws SQLException {
        System.out.println("map  request received");
        Object rawPayload = request.getPayload();
        if (!(rawPayload instanceof MaPayload maPayload)) {
            return GcmResponse.error("Invalid payload for map  request");
        }
        List<MapSheet> map=mapService.PullAllMap();
        if(map==null){ return GcmResponse.error("faild to get all map");}
        return GcmResponse.ok(map);
    }
    //get all maps of certin city
    private GcmResponse handleAllCityMaps(GcmRequest request) throws SQLException {
        System.out.println("map  request received");
        Object rawPayload = request.getPayload();
        if (!(rawPayload instanceof MaPayload maPayload)) {
            return GcmResponse.error("Invalid payload for map  request");
        }
        List<MapSheet> maps=mapService.PullAllCityMaps(maPayload.getName());
        if(maps==null){ return GcmResponse.error("faild to get all map");}
        return GcmResponse.ok(maps);
    }
    // get list of cities handler
    private GcmResponse handleListCities(GcmRequest request) throws SQLException {
        System.out.println("list of citis request received");
        Object rawPayload = request.getPayload();
        if (!(rawPayload instanceof EmptyPayload empty)) {
            return GcmResponse.error("Invalid payload for map pending request");
        }
        List<City> cities = cityService.getAllCities();
        if (cities == null) return GcmResponse.error("faild to get list");
        return GcmResponse.ok(cities);

    }
    // get poi index
    private GcmResponse handlePoiIndex(GcmRequest request) throws SQLException {
        System.out.println("poi index request received");
        Object rawPayload = request.getPayload();
        if (!(rawPayload instanceof IndexPayload indexPayload)) {
            return GcmResponse.error("Invalid payload for map pending request");
        }
        IndexPayload index=new IndexPayload(mapService.getPoiIndex());
        if(index!=null) {return GcmResponse.ok(index);}
        return GcmResponse.error("faild to pend");

    }
    private GcmResponse handleRouteIndex(GcmRequest request) throws SQLException {
        System.out.println("route index request received");
        Object rawPayload = request.getPayload();
        if (!(rawPayload instanceof RouteIndexPayload indexPayload)) {
            return GcmResponse.error("Invalid payload for map pending request");
        }
        RouteIndexPayload index=new RouteIndexPayload(mapService.getRouteIndex());
        if(index!=null) {return GcmResponse.ok(index);}
        return GcmResponse.error("faild to pend");

    }

    //registration handler (perfect from yoav just added try-catch)
    private GcmResponse handleRegistration(GcmRequest request) throws SQLException {
        System.out.println("registration request received");
        // 1. Validate and cast payload
        Object rawPayload = request.getPayload();
        if (!(rawPayload instanceof RegisterPayload payload)) {
            return GcmResponse.error("Invalid payload for register request");
        }

        String username = payload.getUsername();
        String password = payload.getPassword();

        try {
            // 2. Delegate to AuthService to check DB / users list
            User user = authService.register(payload);
            // 3. Handle failure
            if (user == null) {
                return GcmResponse.error(authService.getErrormsg());
            }
            // 4. Success → return the User directly
            return GcmResponse.ok(user);

        } catch (SQLException e) {
            e.printStackTrace(); // server log
            return GcmResponse.error("Server error during registration");
        }
    }


    private GcmResponse handleGetCityCatalog(GcmRequest request) throws SQLException {
        System.out.println("GET_CITY_CATALOG request received");

        Object rawPayload = request.getPayload();
        if (!(rawPayload instanceof EmptyPayload)) {
            return GcmResponse.error("Invalid payload for city catalog");
        }

        List<CityCatalogItem> cities =
                catalogService.searchCities("");

        if (cities == null) {
            return GcmResponse.error("Failed to load city catalog");
        }

        return GcmResponse.ok(cities);
    }

    private GcmResponse handleGetCityMaps(GcmRequest request) throws SQLException {
        System.out.println("GET_CITY_MAPS request received");

        Object rawPayload = request.getPayload();
        if (!(rawPayload instanceof CityMapsRequestPayload payload)) {
            return GcmResponse.error("Invalid payload for city maps");
        }

        List<MapCatalogItem> maps =
                catalogService.loadMapsForCity(payload.getCityId());

        if (maps == null) {
            return GcmResponse.error("Failed to load maps for city");
        }

        return GcmResponse.ok(maps);
    }

    // --- RequestHandler.java ---

    private GcmResponse handleBuyMap(GcmRequest request) {
        Object rawPayload = request.getPayload();
        if (!(rawPayload instanceof BuyMapPayload payload)) {
            return GcmResponse.error("Invalid payload");
        }

        try {
            boolean alreadyOwns = false;

            if (payload.isSubscription()) {
                // Check for active subscription
                alreadyOwns = mapService.isUserSubscribed(payload.getUserId(), payload.getCityName());
            } else {
                // OTP: Check specific version
                int versionToCheck = payload.getVersion();

                // If client sent 0 (standard buy), we need to check against the LATEST version
                // (Assuming standard buy always targets the latest)
                if (versionToCheck == 0) {
                    // We need to resolve the version to check ownership correctly
                    // Ideally, MapService should expose getLatestVersionForCity(cityName)
                    // For now, let's assume if they send 0, we rely on the repo's internal check or assume they want the latest.
                    // A safer way:
                    // alreadyOwns = mapService.isCityPurchased(...); // BLOCKS DUPLICATES

                    // BUT, since we want to allow V1 and V2, we should ideally resolve the version here.
                    // Simplified Logic: If version is 0, we fall back to "Do you own the city?" to prevent accidental double buys of the "main" map.
                    alreadyOwns = mapService.isCityPurchased(payload.getUserId(), payload.getCityName());
                } else {
                    // Client requested a specific version (e.g. from Subscription Claim)
                    alreadyOwns = mapService.isMapVersionPurchased(payload.getUserId(), payload.getCityName(), versionToCheck);
                }
            }

            if (alreadyOwns) {
                return GcmResponse.error("You already own this specific map version!");
            }

            boolean success = mapService.addPurchase(
                    payload.getUserId(),
                    payload.getCityName(),
                    payload.getPrice(),
                    payload.isSubscription(),
                    payload.getVersion() // Pass the version
            );

            return success ? GcmResponse.ok("Success") : GcmResponse.error("Database Error");

        } catch (Exception e) {
            return GcmResponse.error("Server Error: " + e.getMessage());
        }
    }

    private GcmResponse handleGetCityPurchases(GcmRequest request) throws SQLException {
        System.out.println("LIST_USER_PURCHASES request received");
        if (request.getPayload() instanceof Integer userId) {
            return mapService.handleGetPurchasedCities(userId);
        }
        return GcmResponse.error("Invalid Payload for LIST_USER_PURCHASES");
    }

    private GcmResponse handleGetUserSubscriptions(GcmRequest request) throws SQLException {
        int userId = (int) request.getPayload();
        List<City> cities = mapService.getSubscribedCities(userId);
        return GcmResponse.ok(cities);
    }
    // In RequestHandler.java
    private GcmResponse handleGetUserMaps(GcmRequest request) throws SQLException {
        try {
            // 1. Extract User ID from the payload
            if (!(request.getPayload() instanceof Integer)) {
                return GcmResponse.error("Invalid payload. Expected User ID (int).");
            }
            int userId = (int) request.getPayload();

            // 2. Fetch maps using the service method we created
            // (This gets both OTP maps and Subscription maps)
            List<MapSheet> maps = mapService.getPurchasedMaps(userId);

            // 3. Return the list
            return GcmResponse.ok(maps);

        } catch (Exception e) {
            e.printStackTrace();
            return GcmResponse.error("Server Error fetching user maps: " + e.getMessage());
        }
    }

    public void cleanupOnDisconnect() {
        if (loggedInUser != null) {
            online_users.removeIf(u ->
                    u.getUsername().equals(loggedInUser.getUsername())
            );
            System.out.println(
                    "User removed due to disconnect: " + loggedInUser.getUsername()
            );
            loggedInUser = null;
        }
    }

    private GcmResponse handleGetReport(GcmRequest request) throws SQLException {
        System.out.println("GET_REPORT request received");

        Object rawPayload = request.getPayload();

        // 1. Validate Payload
        if (!(rawPayload instanceof ReportPayload payload)) {
            return GcmResponse.error("Invalid payload for reports");
        }

        // 2. Delegate to StatsService (which calls StatsRepo)
        // Make sure you use 'statsService', not 'catalogService'
        List<CityReportData> reports =
                statsService.generateReport(payload.getFromDate(), payload.getToDate(), payload.getCityId());

        // 3. Handle Errors
        if (reports == null) {
            return GcmResponse.error("Failed to generate report");
        }

        // 4. Return Success
        return GcmResponse.ok(reports);
    }
    //yoav
    private GcmResponse handleMessages(GcmRequest request) throws SQLException {
        System.out.println("get messages request received");

        Object rawPayload = request.getPayload();
        if (!(rawPayload instanceof User)) {
            return GcmResponse.error("Invalid payload for messages");
        }
       int id =((User) rawPayload).getId();
        ArrayList<Message> messages = mapService.getMessage(id);
        Message m=statsService.getMessages(id);
        if (m!=null)
        {
            messages.add(0,m);
        }



        return GcmResponse.ok(messages);
    }

    private GcmResponse handleHistory(GcmRequest request) throws SQLException {
        System.out.println("get history request received");

        Object rawPayload = request.getPayload();
        if (!(rawPayload instanceof RegisterPayload)) {
            return GcmResponse.error("Invalid payload for history");
        }
        int id =((RegisterPayload) rawPayload).getUserid();
        ArrayList<String> history = statsService.getHistory(id);
       if(history.isEmpty())
       {
           return GcmResponse.error("faild to get history from db");
       }

        return GcmResponse.ok(history);
    }

    private GcmResponse handleSearch(GcmRequest request) throws SQLException {
        System.out.println("search request received");
        Object rawPayload = request.getPayload();
        if (!(rawPayload instanceof SearchPayload)) {
            return GcmResponse.error("Invalid payload for search");
        }

        SearchPayload payload = (SearchPayload) request.getPayload();

        // Logic is delegated to the Service
        // If this fails, the SQLException propagates up immediately
        List<CityCatalogItem> results = catalogService.searchCities(payload.getQuery());

        return GcmResponse.ok(results);
    }























    //yoav
    //adam



}