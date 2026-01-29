package gcm.server.controllers;

import common.messages.*;
import common.model.*;
import gcm.server.data.RouteRepo;
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

        if (type == RequestType.LIST_CITIES_WITH_MAPS) {
            try {
                return handleListCitiesWithMaps(request);
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

        if (type == RequestType.SUBMIT_ROUTE) {
            try {
                return handleSubmitRoute(request);
            } catch (SQLException e) {
                e.printStackTrace();
                return GcmResponse.error("Database error while submitting route");
            }
        }


        if (type == RequestType.APPROVE_ROUTE) {
            try {
                return handleApproveRoute(request);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        if (type == RequestType.GET_PENDING_ROUTES) {
            try {
                return handleGetPendingRoutes(request);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        if (type == RequestType.GET_ROUTE_SHEET) {
            return GcmResponse.ok(
                    mapService.getApprovedRouteSheet(
                            ((GetRouteSheetPayload) request.getPayload()).getRouteId()
                    )
            );
        }


        if (type == RequestType.GET_PENDING_ROUTE_SHEET) {
            return GcmResponse.ok(
                    mapService.getPendingRouteSheet(
                            ((GetRouteSheetPayload) request.getPayload()).getRouteId()
                    )
            );
        }


        if (type == RequestType.GET_APPROVED_ROUTES_FOR_CITY) {
            try {
                return handleGetApprovedRoutesForCity(request);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        if (type == RequestType.GET_USER_BY_ID) {
            try {
                System.out.println("called get user info");
                return handleUserInfo(request);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }




        if (type == RequestType.CREATE_CITY) {
            try {
                return handleCreateCity(request);
            } catch (SQLException e) {
                e.printStackTrace();
                return GcmResponse.error("Database error while creating city");
            }
        }


        if (type == RequestType.CREATE_CITY_PRICE_CHANGE) {
            try {
                return handleCreateCityPriceChange(request);
            } catch (SQLException e) {
                return GcmResponse.error("Failed to request city price change");
            }
        }

        if (type == RequestType.GET_PENDING_CITY_PRICES) {
            try {
                return GcmResponse.ok(cityService.getPendingCityPrices());
            } catch (SQLException e) {
                return GcmResponse.error("Failed to load pending city prices");
            }
        }

        if (type == RequestType.APPROVE_CITY_PRICE) {
            try {
                return cityService.approveCityPrice(
                        ((CityIdPayload) request.getPayload()).getCityId()
                )
                        ? GcmResponse.ok(null)
                        : GcmResponse.error("Approval failed");
            } catch (SQLException e) {
                return GcmResponse.error("Database error");
            }
        }

        if (type == RequestType.REJECT_CITY_PRICE) {
            try {
                return cityService.rejectCityPrice(
                        ((CityIdPayload) request.getPayload()).getCityId()
                )
                        ? GcmResponse.ok(null)
                        : GcmResponse.error("Reject failed");
            } catch (SQLException e) {
                return GcmResponse.error("Database error");
            }
        }

        if (type == RequestType.REQUEST_CITY_PRICE_CHANGE) {
            CityPricingItem item = (CityPricingItem) request.getPayload();
            boolean ok = cityService.requestCityPriceChange(item);
            return ok ? GcmResponse.ok(null) :
                        GcmResponse.error("There is already a pending price change for this city"
                        );
        }



        if (type == RequestType.DELETE_POI) {
            try {
                return handleDeletePoi(request);
            } catch (SQLException e) {
                e.printStackTrace();
                return GcmResponse.error("Database error while deleting POI");
            }
        }

        if (type == RequestType.DELETE_MAP) {
            try {
                return handleDeleteMap(request);
            } catch (SQLException e) {
                e.printStackTrace();
                return GcmResponse.error("Database error while deleting map");
            }
        }

        if (type == RequestType.DELETE_ROUTE) {
            try {
                return handleDeleteRoute(request);
            } catch (SQLException e) {
                e.printStackTrace();
                return GcmResponse.error("Database error while deleting route");
            }
        }

        if (type == RequestType.DELETE_GET_CITY_MAPS) {
            return handleDeleteGetCityMaps(request);
        }

        if (type == RequestType.REJECT_PENDING_ROUTE) {
            System.out.println("reject pending route request processed");
            try {
                return handleRejectPendingRoute(request);
            } catch (SQLException e) {
                throw new RuntimeException("failed to reject pending route", e);
            }
        }

        if (type == RequestType.REJECT_PENDING_MAP) {
            System.out.println("reject pending map request processed");
            try {
                return handleRejectPendingMap(request);
            } catch (SQLException e) {
                throw new RuntimeException("failed to reject pending map", e);
            }
        }
        if (type == RequestType.CHANGE_INFO) {
            System.out.println("requesting  info change");
            try {
                return handleChangeInfo(request);
            } catch (SQLException e) {
                throw new RuntimeException("failed to reject pending map", e);
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
        System.out.println("LIST_POIS request received");

        Object payload = request.getPayload();

        // 1) city-only
        if (payload instanceof CityIdPayload cityPayload) {
            int cityId = cityPayload.getCityId();
            return GcmResponse.ok(mapService.getPoisForCity(cityId));
        }

        // 2) all pois (for older screens)
        return GcmResponse.ok(mapService.getpois());
    }


    /*
    private GcmResponse handleRoute(GcmRequest request) throws SQLException {
        System.out.println("get all route request");
        return GcmResponse.ok(mapService.getroutes());
    }*/


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
    private GcmResponse handleListCitiesWithMaps(GcmRequest request) throws SQLException {
        System.out.println("list of citis with maps request received");
        Object rawPayload = request.getPayload();
        if (!(rawPayload instanceof EmptyPayload empty)) {
            return GcmResponse.error("Invalid payload for map pending request");
        }
        List<City> cities = cityService.getAllCitiesWithMaps();
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
    /*
    private GcmResponse handleRouteIndex(GcmRequest request) throws SQLException {
        System.out.println("route index request received");
        Object rawPayload = request.getPayload();
        if (!(rawPayload instanceof RouteIndexPayload indexPayload)) {
            return GcmResponse.error("Invalid payload for map pending request");
        }
        RouteIndexPayload index=new RouteIndexPayload(mapService.getRouteIndex());
        if(index!=null) {return GcmResponse.ok(index);}
        return GcmResponse.error("faild to pend");

    }*/

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
        if (!(rawPayload instanceof CityMapsRequestPayload)) {
            return GcmResponse.error("Invalid payload for city maps");
        }

        CityMapsRequestPayload payload1 = (CityMapsRequestPayload) request.getPayload();

        if(payload1.getUserRole().equals("Customer") || payload1.getUserRole().equals("Guest")){
            catalogService.newLogCityView(payload1.getCityId(), payload1.getUserId());
        }

        List<MapCatalogItem> maps =
                catalogService.loadMapsForCity(payload1.getCityId());

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
            boolean alreadySub = false;
            boolean alreadyOTP = false;
            alreadySub = mapService.isUserSubscribed(payload.getUserId(), payload.getCityName());
            alreadyOTP = mapService.isCityPurchased(payload.getUserId(), payload.getCityName());
            if (payload.isSubscription()) {
                if (alreadySub) {
                    return GcmResponse.error("You have this City Subscription");
                }
            }else { // trying to otp
                if (alreadySub) {
                    return GcmResponse.error("You ARE Subscribed to this City, all City content is Available in my maps");
                }
                if (alreadyOTP) {
                    return GcmResponse.error("You have Purchase this City Before try a Subscription");
                }
            }


            boolean success = mapService.addPurchase(
                    payload.getUserId(),
                    payload.getCityName(),
                    payload.getPrice(),
                    payload.isSubscription(),
                    payload.getVersion(), // Pass the version
                    payload.getCredit()
            );

            return success ? GcmResponse.ok(payload) : GcmResponse.error("Database Error");

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
            ArrayList<MapSheet> maps = mapService.getPurchasedMaps(userId);
            if(maps.isEmpty())
            {
                System.out.println("got the maps");
            }


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
































    //Adam
    private GcmResponse handleSubmitRoute(GcmRequest request) throws SQLException {

        System.out.println("SUBMIT_ROUTE request received");

        Object rawPayload = request.getPayload();
        if (!(rawPayload instanceof PendingRoute route)) {
            return GcmResponse.error("Invalid payload for submit route");
        }

        System.out.println("---- DEBUG PENDING ROUTE ----");
        System.out.println("isEdit = " + route.isEdit());
        System.out.println("sourceRouteId = " + route.getSourceRouteId());
        System.out.println("cityId = " + route.getCityId());
        System.out.println("name = " + route.getName());
        System.out.println("-----------------------------");

        boolean success = mapService.submitPendingRoute(route);
        return success ? GcmResponse.ok(null)
                : GcmResponse.error("Failed to submit route");
    }




    private GcmResponse handleApproveRoute(GcmRequest request) throws SQLException {

        System.out.println("route approval request received");

        Object rawPayload = request.getPayload();
        if (!(rawPayload instanceof ApproveRoutePayload payload)) {
            return GcmResponse.error("Invalid payload for APPROVE_ROUTE");
        }

        boolean success;

        if (payload.getSourceRouteId() != null) {
            success = mapService.approveEditedRoute(
                    payload.getRouteId(),
                    payload.getSourceRouteId()
            );
        } else {
             success =
                    mapService.approveRouteWithEditCheck(
                            payload.getRouteId()
                    );

        }


        if (!success) {
            return GcmResponse.error("Failed to approve route");
        }

        return GcmResponse.ok(null);
    }


    private GcmResponse handleGetPendingRoutes(GcmRequest request) throws SQLException {

        System.out.println("GET_PENDING_ROUTES request received");

        List<PendingRoute> routes = mapService.getPendingRoutes();

        if (routes == null) {
            return GcmResponse.error("Failed to load pending routes");
        }

        return GcmResponse.ok(routes);
    }

    private GcmResponse handleGetApprovedRoutesForCity(GcmRequest request) throws SQLException {

        Object raw = request.getPayload();
        if (!(raw instanceof CityIdPayload payload)) {
            return GcmResponse.error("Invalid payload for GET_APPROVED_ROUTES_FOR_CITY");
        }

        List<RouteSheet> routes = mapService.getApprovedRoutesForCity(payload.getCityId());
        return GcmResponse.ok(routes);
    }
    private GcmResponse handleUserInfo(GcmRequest request) throws SQLException {

        Object raw = request.getPayload();
        if (!(raw instanceof Integer id)) {
            System.out.println("faild x");
            return GcmResponse.error("Invalid payload for get user info");
        }
        GcmResponse response=authService.getUsers();
        if(response.isSuccess())
        {
            Object t=response.getData();
            ArrayList<RegisterPayload> users= (ArrayList<RegisterPayload>)t;
            for (RegisterPayload rp : users) {
                if (rp.getUserid() == id) {
                   return GcmResponse.ok(rp);

                }
            }

        }
        return GcmResponse.error("Invalid payload for get user info");


    }

    private GcmResponse handleCreateCity(GcmRequest request) throws SQLException {

        Object rawPayload = request.getPayload();

        if (!(rawPayload instanceof CreateCityPayload payload)) {
            return GcmResponse.error("Invalid payload for CREATE_CITY");
        }

        boolean success = cityService.createCity(payload);

        return success
                ? GcmResponse.ok(null)
                : GcmResponse.error("Failed to create city");
    }


    private GcmResponse handleCreateCityPriceChange(GcmRequest request)
            throws SQLException {

        PendingCityPricePayload payload =
                (PendingCityPricePayload) request.getPayload();

        boolean ok = cityService.requestCityPriceChange(payload);

        return ok
                ? GcmResponse.ok(null)
                : GcmResponse.error("Failed to request city price change");
    }


    private GcmResponse handleDeletePoi(GcmRequest request) throws SQLException {

        System.out.println("DELETE_POI request received");

        Object rawPayload = request.getPayload();
        if (!(rawPayload instanceof PoiIdPayload payload)) {
            return GcmResponse.error("Invalid payload for DELETE_POI");
        }

        int poiId = payload.getPoiId();

        System.out.println("Deleting POI id = " + poiId);

        boolean success = mapService.deletePoiIfUnused2(poiId);

        return success
                ? GcmResponse.ok(poiId)
                : GcmResponse.error("Cannot delete POI. It is used in a map or route");
    }



    private GcmResponse handleDeleteRoute(GcmRequest request) throws SQLException {

        System.out.println("DELETE_ROUTE request received");

        Object rawPayload = request.getPayload();
        if (!(rawPayload instanceof RouteIdPayload payload)) {
            return GcmResponse.error("Invalid payload for DELETE_ROUTE");
        }

        int routeId = payload.getRouteId();

        System.out.println("Deleting route id = " + routeId);

        boolean success = mapService.deleteRoute2(routeId);

        return success
                ? GcmResponse.ok(routeId)
                : GcmResponse.error("Failed to delete route");
    }


    private GcmResponse handleDeleteGetCityMaps(GcmRequest request) {

        if (!(request.getPayload() instanceof CityIdPayload payload)) {
            return GcmResponse.error("Invalid payload for DELETE_GET_CITY_MAPS");
        }

        List<MapDeleteItem> maps =
                mapService.getApprovedMapsForDelete(payload.getCityId());

        return GcmResponse.ok(maps);
    }

    private GcmResponse handleDeleteMap(GcmRequest request) throws SQLException {

        System.out.println("DELETE_MAP request received");

        Object rawPayload = request.getPayload();
        if (!(rawPayload instanceof MapIdPayload payload)) {
            return GcmResponse.error("Invalid payload for DELETE_MAP");
        }

        int mapId = payload.getMapId();

        System.out.println("Deleting map id = " + mapId);

        boolean success = mapService.deleteMap(mapId);

        return success
                ? GcmResponse.ok(mapId)
                : GcmResponse.error("Failed to delete map");
    }


    private GcmResponse handleRejectPendingRoute(GcmRequest request) throws SQLException {

        Object rawPayload = request.getPayload();
        if (!(rawPayload instanceof RouteIdPayload payload)) {
            return GcmResponse.error("Invalid payload for REJECT_PENDING_ROUTE");
        }

        boolean success = mapService.rejectPendingRoute(payload.getRouteId());

        return success
                ? GcmResponse.ok(null)
                : GcmResponse.error("Failed to reject pending route");
    }

    private GcmResponse handleRejectPendingMap(GcmRequest request) throws SQLException {

        Object rawPayload = request.getPayload();
        if (!(rawPayload instanceof MapSheet map)) {
            return GcmResponse.error("Invalid payload for REJECT_PENDING_MAP");
        }

        boolean success = mapService.rejectPendingMap(map);

        return success
                ? GcmResponse.ok(null)
                : GcmResponse.error("Failed to reject pending map");
    }
    private GcmResponse handleChangeInfo(GcmRequest request) throws SQLException {

        Object rawPayload = request.getPayload();
        if (!(rawPayload instanceof RegisterPayload payload)) {
            return GcmResponse.error("Invalid payload for info change");
        }

        boolean validMail= authService.validEmail(payload.getEmail());
        boolean EmailUsed=authService.EmailInUse(payload.getEmail());
        boolean validPhone= authService.validatePhone(payload.getPhonenum());
        boolean validName=payload.getFirstname().length()>1;
        boolean validSurename=payload.getLastname().length()>1;
        if(!validMail)
        {
            GcmResponse.error("Email is not valid");

        }
        if(EmailUsed)
        {
            GcmResponse.error("Email is Already Taken");
        }
        if(!validPhone)
        {
            GcmResponse.error("phone number is not valid");
        }
        if(!validName)
        {
            GcmResponse.error("name is not valid");
        }
        if(!validSurename)
        {
            GcmResponse.error("Surname is not valid");
        }
        boolean success= authService.AlterInfo(payload);

        return success
                ? GcmResponse.ok(null)
                : GcmResponse.error("faild to change information");
    }


}