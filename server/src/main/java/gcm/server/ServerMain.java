// server/src/main/java/gcm/server/ServerMain.java

package gcm.server;

import gcm.server.controllers.RequestHandler;
import gcm.server.data.MapRepo;
import gcm.server.data.UserRepo;
import gcm.server.network.GcmServer;
import gcm.server.service.AuthService;
import gcm.server.service.MapService;

public class ServerMain {

    /**
     * removed loadUSersFromDb
     * @param args
     */
    public static void main(String[] args) {
        try {
            UserRepo UserRepository = new UserRepo();
            AuthService authService = new AuthService(UserRepository);
            MapRepo mapRepository=new MapRepo();
            MapService mapService=new MapService(mapRepository);
            //authService.loadUsersFromDb(); // fills the ArrayList<User>
            RequestHandler handler = new RequestHandler(authService,mapService);

            GcmServer server = new GcmServer(5555, handler);
            server.listen(); // starts listening (blocks current thread)

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to start GCM Server.");
        }
    }
}
