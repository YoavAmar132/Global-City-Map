package gcm.client.controllers.user_util;

import common.messages.*;
import common.model.City;
import gcm.client.controllers.catalog.BuyMapCatalogController;
import gcm.client.controllers.menu.UserMenuController;
import gcm.client.network.GcmClient;
import gcm.client.utill.ClientApp;
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
    @FXML private RadioButton radioOTP; // One Time Purchase
    @FXML private RadioButton radioSub; // Subscription
    @FXML private ToggleGroup purchaseGroup;

    private GcmClient client;
    private City selectedCity; // שומרים את העיר כשדה במחלקה לגישה נוחה

    @FXML
    public void initialize() {
        // 1. Setup Client
        this.client = ClientApp.getClient();
        client.setResponseHandler(this::handleResponse);

        // 2. Get data from Session
        PurchaseSession session = PurchaseSession.getInstance();
        this.selectedCity = session.getSelectedCity();

        if (selectedCity == null) {
            cityNameLabel.setText("Error: No City Selected");
            return;
        }

        // 3. Update UI with City Data
        cityNameLabel.setText("Purchase: " + selectedCity.getName());

        // עדכון הטקסט של הכפתורים שיראה את המחיר ליד האופציה
        radioOTP.setText(String.format("One Time Purchase ($%.2f)", selectedCity.getPrice()));
        radioSub.setText(String.format("Subscription (6 Months) ($%.2f)", selectedCity.getSubPrice()));

        // 4. Setup listeners for price updates
        purchaseGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> updatePriceDisplay());

        // Initial check
        updatePriceDisplay();
    }

    private void updatePriceDisplay() {
        if (selectedCity == null) return;

        if (radioOTP.isSelected()) {
            priceLabel.setText(String.format("Total: $%.2f", selectedCity.getPrice()));
        } else {
            priceLabel.setText(String.format("Total: $%.2f", selectedCity.getSubPrice()));
        }
    }

    public void onConfirmPurchaseClicked(ActionEvent actionEvent) {
        if (selectedCity == null) return;

        PurchaseSession session = PurchaseSession.getInstance();

        // 1. Determine final price and type based on selection
        double finalPrice;
        if (radioOTP.isSelected()) {
            session.setPurchaseType(PurchaseSession.PurchaseType.ONE_TIME_PURCHASE);
            finalPrice = selectedCity.getPrice(); // מחיר רגיל מהעיר
        } else {
            session.setPurchaseType(PurchaseSession.PurchaseType.SUBSCRIPTION);
            finalPrice = selectedCity.getSubPrice(); // מחיר מנוי מהעיר
        }
        session.setPrice(finalPrice);

        // 2. Create Payload & Send Request
        // כרגע אנחנו שולחים את המחיר שנבחר לשרת
        BuyMapPayload payload = new BuyMapPayload(
                session.getUserID(),
                selectedCity.getName(),
                null, // mapsList (לא רלוונטי ברכישת עיר מלאה)
                finalPrice,
                "STORED_CARD" // Payment Method
        );

        // הערה: אם תרצה בעתיד לשמור ב-DB את סוג הרכישה (מנוי/רגיל),
        // תצטרך להוסיף שדה ל-BuyMapPayload ולעדכן את ה-Repo בשרת.

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
                alert.setContentText("You have successfully purchased access to " + selectedCity.getName());
                alert.showAndWait();

                // Clear session and go to Main Menu
                PurchaseSession.getInstance().clear();
                ClientApp.getNavigator().show(UserMenuController.class);
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Purchase Failed");
                alert.setHeaderText("Transaction Declined");
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
        client.closeConnectionSafe();
        Platform.exit();
    }
}