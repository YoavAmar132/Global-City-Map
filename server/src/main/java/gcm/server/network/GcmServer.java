package gcm.server.network;

import common.messages.GcmRequest;
import common.messages.GcmResponse;
import common.messages.Popup;
import common.messages.Reload;
import common.model.User;
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

            GcmResponse response = requestHandler.handle(request);

            if (request.getType() == common.messages.RequestType.LOGIN && response.isSuccess()) {
                User user = (User) response.getData();
                client.setInfo("user", user);
            }
             if(response.getData() instanceof Popup)
             {
                 sendToAllClients(response);
             }

            /* existing refresh logic */
            if (response.getRefresh() == 1) {
                Reload reload = new Reload();
                response.setRefresh(0);
                sendToAllClients(GcmResponse.ok(reload));
            }

            // send response back (client may have disconnected)
            try {
                client.sendToClient(response);
            } catch (Exception e) {
                System.out.println(
                        "Response not sent: client disconnected before reply. (disconnected)"
                );
            }



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
    protected synchronized void clientDisconnected(ConnectionToClient client) {
        System.out.println("Client disconnected: " + client);

        User user = (User) client.getInfo("user");

        if (user != null) {
            RequestHandler.removeOnlineUser(user);
            System.out.println("Removed user due to disconnect: " + user.getUsername());
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


}
