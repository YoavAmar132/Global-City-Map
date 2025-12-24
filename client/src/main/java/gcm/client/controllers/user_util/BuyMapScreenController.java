package gcm.client.controllers.user_util;

import common.messages.*;
import common.model.City;
import gcm.client.controllers.catalog.BuyMapCatalogController;
import gcm.client.controllers.menu.UserMenuController; // Or wherever you want to go after success
import gcm.client.network.GcmClient;
import gcm.client.utill.ClientApp;
import gcm.client.controllers.user_util.PurchaseSession;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;

public class BuyMapScreenController {

    @FXML private Label cityNameLabel;
    @FXML private Label priceLabel;
    @FXML private RadioButton radioOTP;
    @FXML private RadioButton radioSub;
    @FXML private ToggleGroup purchaseGroup;

    private GcmClient client;

    // Hardcoded prices (You might want to move these to the City object later)
    private static final double OTP_PRICE = 19.99;
    private static final double SUB_PRICE = 9.99;

    @FXML
    public void initialize() {
        // 1. Setup Client
        this.client = ClientApp.getClient();
        client.setResponseHandler(this::handleResponse);

        // 2. Get data from Session
        PurchaseSession session = PurchaseSession.getInstance();
        City city = session.getSelectedCity();

        if (city == null) {
            cityNameLabel.setText("No City Selected");
            return;
        }

        cityNameLabel.setText(city.getName());

        // 3. Setup listeners for price updates
        purchaseGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> updatePriceDisplay());

        // Initial check
        updatePriceDisplay();
    }

    private void updatePriceDisplay() {
        if (radioOTP.isSelected()) {
            priceLabel.setText("$" + OTP_PRICE);
        } else {
            priceLabel.setText("$" + SUB_PRICE);
        }
    }

    public void onConfirmPurchaseClicked(ActionEvent actionEvent) {
        PurchaseSession session = PurchaseSession.getInstance();
        City city = session.getSelectedCity();

        // 1. Update Session with final choice
        double finalPrice;
        if (radioOTP.isSelected()) {
            session.setPurchaseType(PurchaseSession.PurchaseType.ONE_TIME_PURCHASE);
            finalPrice = OTP_PRICE;
        } else {
            session.setPurchaseType(PurchaseSession.PurchaseType.SUBSCRIPTION);
            finalPrice = SUB_PRICE;
        }
        session.setPrice(finalPrice);

        // 2. Create Payload & Send Request
        // IMPORTANT: Ensure your BuyMapPayload constructor supports these arguments.
        // You might need to update BuyMapPayload to accept 'PurchaseType' or a boolean for isSubscription.
        BuyMapPayload payload = new BuyMapPayload(
                session.getUserID(),
                city.getName(),
                null, // mapsList - assuming null is fine if we buy by City Name, or fetch from city.getMaps()
                finalPrice,
                "STORED_CARD" // Payment Method
        );

        // If you updated BuyMapPayload to handle subscription types, pass that here too!

        GcmRequest request = new GcmRequest(RequestType.BUY_MAP, payload);
        client.sendRequest(request);
    }

    // 3. Handle the Server Response
    private void handleResponse(GcmResponse response) {
        Platform.runLater(() -> {
            if (response.isSuccess()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText("Purchase Successful!");
                alert.setContentText("Thank you for your purchase.");
                alert.showAndWait();

                // Clear session and go to Main Menu
                PurchaseSession.getInstance().clear();
                ClientApp.getNavigator().show(UserMenuController.class);
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Purchase Failed");
                alert.setContentText(response.getErrorMessage());
                alert.showAndWait();
            }
        });
    }

    public void onCancelClicked(ActionEvent actionEvent) {
        PurchaseSession.getInstance().clear();
        ClientApp.getNavigator().show(BuyMapCatalogController.class);
    }

    public void handleClose(ActionEvent actionEvent) {
        Platform.exit();
    }
}