package gcm.server;

import gcm.server.bot.BotAgent;
import gcm.server.bot.BotConfig;
import gcm.server.bot.BotToolRegistry;
import gcm.server.bot.OllamaClient;
import gcm.server.bot.tools.*;
import gcm.server.controllers.RequestHandler;
import gcm.server.data.*;
import gcm.server.network.GcmServer;
import gcm.server.service.*;

public class ServerBootstrap {

    public static void startServer() throws Exception {

        UserRepo userRepo = new UserRepo();
        AuthService authService = new AuthService(userRepo);

        MapRepo mapRepo = new MapRepo();

        RouteRepo routeRepo = new RouteRepo();

        MapService mapService = new MapService(mapRepo, userRepo, routeRepo);

        CityRepo cityRepo = new CityRepo();
        CityService cityService = new CityService(cityRepo);

        CatalogRepo catalogRepo = new CatalogRepo();
        CatalogService catalogService = new CatalogService(catalogRepo);

        StatsRepo statsRepo = new StatsRepo();
        StatsService statsService = new StatsService(statsRepo);

        ComplaintRepo complaintRepo = new ComplaintRepo();
        ComplaintService complaintService = new ComplaintService(complaintRepo);

        initializeBot(complaintRepo);

        RequestHandler handler =
                new RequestHandler(authService, mapService, cityService, catalogService, statsService,complaintService);

        GcmServer server = new GcmServer(5555, handler);

        new Thread(() -> {
            try {
                server.listen();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "GCM-Server-Thread").start();
    }

    private static void initializeBot(ComplaintRepo complaintRepo) {

        BotConfig botConfig = BotConfig.getInstance();
        if (botConfig.isEnabled()) {
            System.out.println("bot config enabled");
            OllamaClient ollamaClient = new OllamaClient();
            ollamaClient.warmUp();

            //safety measure in case that warmup couldn't connect to the bot and change BotConfig.isEnabled to false
            if(!botConfig.isEnabled()){
                System.out.println("bot config disabled");
                /*
                 useful for reassigning complaints that were waiting for a bot or during a response from a bot and the server had to restart
                 but it didn't restart with the bot
                 more generally,it's used in order to failure proof this part of the system
                */
                try{
                    complaintRepo.reassignHangingComplaintsForCustomerSupport();
                }
                catch (Exception e){
                    System.out.println("failed to reassign hanging complaints for customer support");
                    e.printStackTrace();
                }
                return;
            }

            BotToolRegistry botToolRegistry = new BotToolRegistry();

            //registering all the tools the bot can access
            botToolRegistry.register(new GetCityExistTool());

            //letting the bot run in the background
            BotAgent botAgent = new BotAgent(complaintRepo, ollamaClient, botToolRegistry);
            Thread botThread = new Thread(botAgent);
            botThread.setDaemon(true);
            botThread.start();
        }
        else{
            /*
                 useful for reassigning complaints that were waiting for a bot or during a response from a bot and the server had to restart
                 but it didn't restart with the bot
                 more generally,it's used in order to failure proof this part of the system
                */
            try{
                complaintRepo.reassignHangingComplaintsForCustomerSupport();
            }
            catch (Exception e){
                System.out.println("failed to reassign hanging complaints for customer support");
                e.printStackTrace();
            }
        }
    }

}
