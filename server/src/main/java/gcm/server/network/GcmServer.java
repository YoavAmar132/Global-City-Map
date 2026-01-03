package gcm.server.network;

import common.messages.GcmRequest;
import common.messages.GcmResponse;
import common.messages.Reload;
import gcm.server.controllers.RequestHandler;
import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;

public class GcmServer extends AbstractServer {

    private final RequestHandler requestHandler;

    public GcmServer(int port, RequestHandler requestHandler) {
        super(port);
        this.requestHandler = requestHandler;
    }

    @Override
    protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
        try {
            // 1. Make sure it's a GcmRequest
            if (!(msg instanceof GcmRequest request)) {
                client.sendToClient(GcmResponse.error("Invalid message type"));
                return;
            }

            // 2. Delegate to RequestHandler (for now: only LOGIN)
            GcmResponse response = requestHandler.handle(request);
            if(response.getRefresh()==1)
            {
                Reload reload =new Reload();
                response.setRefresh(0);
                sendToAllClients(GcmResponse.ok(reload));
            }
            // 3. Send response back to this client
            client.sendToClient(response);

        } catch (Exception e) {
            e.printStackTrace();
            try {
                client.sendToClient(GcmResponse.error("Internal server error"));
            } catch (Exception ignored) {
                // ignore failure to send error
            }
        }
    }

    @Override
    protected void serverStarted() {
        System.out.println("GCM Server started on port " + getPort());
    }

    @Override
    protected void serverStopped() {
        System.out.println("GCM Server stopped.");
    }

    @Override
    protected void listeningException(Throwable exception) {
        System.err.println("Listening exception in GCM Server: " + exception.getMessage());
        exception.printStackTrace();
    }

    @Override
    protected void clientConnected(ConnectionToClient client) {
        System.out.println("Client connected: " + client);
    }

    @Override
    protected void clientDisconnected(ConnectionToClient client) {
        System.out.println("Client disconnected: " + client);
    }
}
