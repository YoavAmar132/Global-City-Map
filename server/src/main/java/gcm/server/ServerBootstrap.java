package gcm.server;

import gcm.server.controllers.RequestHandler;
import gcm.server.data.*;
import gcm.server.network.GcmServer;
import gcm.server.service.*;

public class ServerBootstrap {

    public static void startServer() throws Exception {

        UserRepo userRepo = new UserRepo();
        AuthService authService = new AuthService(userRepo);

        MapRepo mapRepo = new MapRepo();
        MapService mapService = new MapService(mapRepo,userRepo);

        CityRepo cityRepo = new CityRepo();
        CityService cityService = new CityService(cityRepo);

        CatalogRepo catalogRepo = new CatalogRepo();
        CatalogService catalogService = new CatalogService(catalogRepo);

        StatsRepo statsRepo = new StatsRepo();
        StatsService statsService = new StatsService(statsRepo);

        RequestHandler handler =
                new RequestHandler(authService, mapService, cityService, catalogService, statsService);

        GcmServer server = new GcmServer(5555, handler);

        new Thread(() -> {
            try {
                server.listen();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "GCM-Server-Thread").start();
    }
}
