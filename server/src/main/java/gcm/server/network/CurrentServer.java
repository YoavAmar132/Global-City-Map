package gcm.server.network;

/**
 * A singleton class which act as a wrapper over the gcm class
 * allow classes in the server side to use important methods inside gcm server
 * in our case,it allows complaintService to send update to client so they'll know that their complaint has been updated
* */

public final class CurrentServer {

    private GcmServer server = null;
    private static CurrentServer instance;

    private CurrentServer() {}

    public static synchronized CurrentServer getInstance() {
        if (instance == null) {
            instance = new CurrentServer();
        }
        return instance;
    }

    public void setServer(GcmServer server) {
        this.server = server;
    }

    public GcmServer getServer() {
        return server;
    }
}
