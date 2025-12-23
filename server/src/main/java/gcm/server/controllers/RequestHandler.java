package gcm.server.controllers;

import common.messages.*;
import common.model.MapSheet;
import common.model.User;
import gcm.server.data.MapRepo;
import gcm.server.service.AuthService;
import gcm.server.service.CatalogService;
import gcm.server.service.CityService;
import gcm.server.service.MapService;

import java.sql.SQLException;

public class RequestHandler {

    private final AuthService authService;
    private final MapService mapservice;
    private final CityService cityService;
    private final CatalogService catalogService;

    public RequestHandler(AuthService authService, MapService mapservice, CityService cityService, CatalogService catalogService) {
        this.authService = authService;
        this.mapservice = mapservice;
        this.cityService = cityService;
        this.catalogService = catalogService;
    }

    /**
     * Main entry point for handling a request from a client.
     * For now, we only support LOGIN.
     */
    public GcmResponse handle(GcmRequest request) {

        RequestType type = request.getType();

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
            // 4. Success → return the User directly
            return GcmResponse.ok(user);

        } catch (SQLException e) {
            e.printStackTrace(); // server log don't really care :D
            return GcmResponse.error("Server error during registration");
        }
    }

    // map pending handeler
    private GcmResponse handleMapPending(GcmRequest request) throws SQLException {
        System.out.println("map pending request received");
        Object rawPayload = request.getPayload();
        if (!(rawPayload instanceof MapSheet mapSheet)) {
            return GcmResponse.error("Invalid payload for map pending request");
        }
        if(!mapservice.PendMap(mapSheet)){ return GcmResponse.error("faild to pend");}
        return GcmResponse.ok(mapSheet);
    }

    // get poi index
    private GcmResponse handlePoiIndex(GcmRequest request) throws SQLException {
        System.out.println("poi index request received");
        Object rawPayload = request.getPayload();
        if (!(rawPayload instanceof IndexPayload indexPayload)) {
            return GcmResponse.error("Invalid payload for map pending request");
        }
        IndexPayload index=new IndexPayload(mapservice.getPoiIndex());
       if(index!=null) {return GcmResponse.ok(index);}
        return GcmResponse.error("faild to pend");

}
    private GcmResponse handleRouteIndex(GcmRequest request) throws SQLException {
        System.out.println("route index request received");
        Object rawPayload = request.getPayload();
        if (!(rawPayload instanceof RouteIndexPayload indexPayload)) {
            return GcmResponse.error("Invalid payload for map pending request");
        }
        RouteIndexPayload index=new RouteIndexPayload(mapservice.getRouteIndex());
        if(index!=null) {return GcmResponse.ok(index);}
        return GcmResponse.error("faild to pend");

    }

    //registration handler (perfect from yoav just added try-catch)
    private GcmResponse handleRegistration(GcmRequest request) throws SQLException {
        System.out.println("registration request received");
        // 1. Validate and cast payload
        Object rawPayload = request.getPayload();
        if (!(rawPayload instanceof LoginPayload payload)) {
            return GcmResponse.error("Invalid payload for register request");
        }

        String username = payload.getUsername();
        String password = payload.getPassword();

        try {
            // 2. Delegate to AuthService to check DB / users list
            User user = authService.register(username, password);
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
}