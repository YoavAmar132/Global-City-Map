package gcm.client.controllers.catalog;

import common.messages.*;
import common.model.City;
import gcm.client.controllers.menu.UserMenuController;
import gcm.client.controllers.user_util.BuyMapScreenController;
import gcm.client.network.GcmClient;
import gcm.client.utill.ClientApp;
import gcm.client.controllers.user_util.PurchaseSession; // Import the session
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;

public class BuyMapCatalogController {
    private GcmClient client;

    @FXML private VBox Citylist;
    @FXML private Label statusLabel;

    @FXML
    private void initialize() {
        client = ClientApp.getClient();
        client.setResponseHandler(this::handleResponse);
        loadCities();
    }

    private void loadCities() {
        EmptyPayload payload = new EmptyPayload();
        GcmRequest request = new GcmRequest(RequestType.LIST_CITIES, payload);
        client.sendRequest(request);
    }

    private HBox createMapRow(City city) {
        Label name = new Label(city.getName());
        name.setStyle("-fx-text-fill: white; -fx-font-size: 14;");

        Button buyBtn = new Button("Buy");
        buyBtn.setPrefSize(90, 30);
        buyBtn.setStyle(
                "-fx-background-color: linear-gradient(to right, #11998e, #38ef7d); " +
                        "-fx-text-fill: white; -fx-font-size: 13; -fx-background-radius: 8; -fx-cursor: hand;"
        );
        buyBtn.setOnAction(e -> navigateToBuyScreen(city));

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        HBox row = new HBox(12, name, spacer, buyBtn);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: rgba(255,255,255,0.14); -fx-background-radius: 12;");
        row.setPadding(new javafx.geometry.Insets(10, 12, 10, 12));

        return row;
    }

    private void navigateToBuyScreen(City city) {
        // 1. Clear old session data
        PurchaseSession.getInstance().clear();

        // 2. Set the new city in the singleton
        PurchaseSession.getInstance().setSelectedCity(city);
        PurchaseSession.getInstance().setUserID(ClientApp.getCurrentUser().getId());

        // 3. Navigate
        ClientApp.getNavigator().show(BuyMapScreenController.class);
    }

    private void handleResponse(GcmResponse response) {
        Platform.runLater(() -> {
            if (!response.isSuccess()) {
                statusLabel.setText("Error loading cities.");
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setContentText(response.getErrorMessage());
                alert.showAndWait();
            } else {
                Object t = response.getData();
                if (t instanceof ArrayList<?>) {
                    ArrayList<?> list = (ArrayList<?>) t;
                    if (!list.isEmpty() && list.get(0) instanceof City) {
                        Citylist.getChildren().clear();
                        for (Object o : list) {
                            Citylist.getChildren().add(createMapRow((City) o));
                        }
                        statusLabel.setText("Select a city to buy.");
                    }
                }
            }
        });
    }

    public void onBackClicked(ActionEvent actionEvent) {
        ClientApp.getNavigator().show(UserMenuController.class);
    }

    public void onRefreshClicked(ActionEvent actionEvent) {
        loadCities();
    }

    public void handleClose(ActionEvent actionEvent) {
        client.closeConnectionSafe();
        Platform.exit();
    }
}