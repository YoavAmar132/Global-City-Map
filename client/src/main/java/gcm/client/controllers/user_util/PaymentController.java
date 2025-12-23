package gcm.client.controllers.user_util;

import gcm.client.utill.ClientApp;
import gcm.client.network.GcmClient;
import gcm.client.controllers.menu.UserMenuController;
import common.messages.RequestType;
import common.messages.BuyMapPayload;
import common.messages.GcmRequest;
import common.messages.GcmResponse;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;

public class PaymentController {

    @FXML private Label priceLabel;
    @FXML private Label itemLabel;

    private GcmClient client;

    @FXML
    public void initialize() {
        this.client = ClientApp.getClient();
        client.setResponseHandler(this::handleResponse);

        // 1. READ data from the Session
        PurchaseSession session = PurchaseSession.getInstance();

        // 2. Display it
        itemLabel.setText("City: " + session.getCityName());
        priceLabel.setText("$" + session.getPrice());
    }

    @FXML
    public void handleBuy() {
        PurchaseSession session = PurchaseSession.getInstance();

        // 3. Use session data to create the Payload
        // Note: passing "STORED_CARD" so server looks up the user's CC
        BuyMapPayload payload = new BuyMapPayload(
                session.getUserId(),
                session.getCityName(),
                session.getMapsList(),
                session.getPrice(),
                "STORED_CARD"
        );

        GcmRequest request = new GcmRequest(RequestType.BUY_MAP, payload);
        client.sendRequest(request);
    }

    private void handleResponse(GcmResponse response) {
        Platform.runLater(() -> {
            if (response.isSuccess()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setContentText("Purchase Successful!");
                alert.showAndWait();

                // Clear session and go home
                PurchaseSession.getInstance().clear();
                ClientApp.getNavigator().show(UserMenuController.class);
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setContentText(response.getErrorMessage());
                alert.showAndWait();
            }
        });
    }
}