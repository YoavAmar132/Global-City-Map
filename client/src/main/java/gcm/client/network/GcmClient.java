package gcm.client.network;

import common.messages.GcmRequest;
import common.messages.GcmResponse;
import common.messages.RequestType;
import gcm.client.utill.ClientApp;
import javafx.application.Platform;
import ocsf.client.AbstractClient;

import java.io.IOException;
import java.util.function.Consumer;

public class GcmClient extends AbstractClient {

    /**
     * This is like ChatIF: a callback that UI can set to handle responses.
     */
    private Consumer<GcmResponse> responseHandler;

    /**
     * Constructs an instance of the GCM client.
     *
     * @param host The server to connect to.
     * @param port The port number to connect on.
     */
    public GcmClient(String host, int port) throws IOException {
        super(host, port);   // store host/port inside AbstractClient
        openConnection();
    }

    /**
     * Let the UI (e.g., LoginController) set how to handle responses.
     */
    public void setResponseHandler(Consumer<GcmResponse> handler) {
        this.responseHandler = handler;
    }

    /**
     * This is your version of handleMessageFromClientUI in ChatClient:
     * UI calls this to send a request to the server.
     */
    public void sendRequest(GcmRequest request) {
        try {
            sendToServer(request);
        } catch (IOException e) {
            e.printStackTrace();
            // You COULD also call responseHandler with an error GcmResponse here
        }
    }

    /**
     * This is called automatically when data comes from the server.
     * It's like handleMessageFromServer in ChatClient.
     */
    @Override
    protected void handleMessageFromServer(Object msg) {
        if (!(msg instanceof GcmResponse response)) {
            System.err.println("Received non-GcmResponse from server: " + msg);
            return;
        }

        Consumer<GcmResponse> handler = this.responseHandler;
        if (handler != null) {
            // 🔴 OCSF thread → 🔵 JavaFX thread
            Platform.runLater(() -> handler.accept(response));
        }
    }

    @Override
    protected void connectionEstablished() {
        System.out.println("Connected to GCM server: " + getHost() + ":" + getPort());
    }

    @Override
    protected void connectionException(Exception exception) {
        System.err.println("Connection exception: " + exception.getMessage());
        exception.printStackTrace();
    }

    @Override
    protected void connectionClosed() {
        System.out.println("Connection to GCM server closed.");
    }

    public void closeConnectionSafe() {
        try {
            closeConnection();
            GcmRequest request = new GcmRequest(RequestType.LOGOUT, ClientApp.getCurrentUser());
            sendRequest(request);
            System.out.println("GCM Client: connection closed.");
        } catch (Exception e) {
            System.err.println("GCM Client: error closing connection: " + e.getMessage());
        }
    }

}
