package gcm.client.controllers.user_util;

import common.messages.*;
import common.model.City;
import common.model.MapSheet;
import gcm.client.network.GcmClient;
import gcm.client.utill.ClientApp;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import java.util.List;

public class SubscriptionMapsController {
    private GcmClient client;
    private City currentCity;

    @FXML private VBox mapsList;
    @FXML private Label titleLabel;
    @FXML private Label statusLabel;

    @FXML
    private void initialize() {
        client = ClientApp.getClient();
    }

    // Called by the previous screen (UserSubscriptionsController)
    public void setCity(City city) {
        this.currentCity = city;
        titleLabel.setText(city.getName() + " - Available Maps");

        // Request available maps for this city
        client.setResponseHandler(this::handleListResponse);
        MaPayload payload = new MaPayload(0, city.getName());
        client.sendRequest(new GcmRequest(RequestType.LIST_MAPS_FOR_CITY, payload));
    }

    private HBox createMapRow(MapSheet map) {
        // 1. Name and Version
        Label name = new Label(map.getName());
        name.setStyle("-fx-text-fill: white; -fx-font-size: 15; -fx-font-weight: bold;");

        Label version = new Label("Version " + map.getVersion());
        version.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 12;");

        VBox infoBox = new VBox(2, name, version);

        // 2. Add Button
        Button addBtn = new Button("Add to My Maps");
        addBtn.setPrefHeight(32);
        addBtn.setStyle(
                "-fx-background-color: #4CAF50; " + // Green
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 5; " +
                        "-fx-cursor: hand;"
        );
        addBtn.setOnAction(e -> addMapToLibrary(map));

        // 3. Layout
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(15, infoBox, spacer, addBtn);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.setStyle(
                "-fx-background-color: rgba(255,255,255,0.08); " +
                        "-fx-background-radius: 8; " +
                        "-fx-padding: 10 15 10 15;"
        );

        return row;
    }

    private void addMapToLibrary(MapSheet map) {
        if (currentCity == null) return;

        BuyMapPayload payload = new BuyMapPayload(
                ClientApp.getCurrentUser().getId(),
                currentCity.getName(),
                0.0,   // Free
                false, // Treated as OTP
                map.getVersion() // <--- SEND THE SPECIFIC VERSION
        );

        client.setResponseHandler(this::handleAddResponse);
        client.sendRequest(new GcmRequest(RequestType.BUY_MAP, payload));
    }

    private void handleListResponse(GcmResponse response) {
        Platform.runLater(() -> {
            if (response.isSuccess() && response.getData() instanceof List<?>) {
                List<?> list = (List<?>) response.getData();
                mapsList.getChildren().clear();

                if (list.isEmpty()) {
                    statusLabel.setText("No maps available for this city.");
                } else {
                    statusLabel.setText(""); // Clear loading text
                    for (Object obj : list) {
                        if (obj instanceof MapSheet) {
                            mapsList.getChildren().add(createMapRow((MapSheet) obj));
                        }
                    }
                }
            } else {
                statusLabel.setText("Error loading maps.");
            }
        });
    }

    private void handleAddResponse(GcmResponse response) {
        Platform.runLater(() -> {
            Alert alert = new Alert(response.isSuccess() ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
            alert.setTitle(response.isSuccess() ? "Success" : "Error");
            alert.setHeaderText(null);
            alert.setContentText(response.isSuccess() ? "Map successfully added to your library!" : response.getErrorMessage());
            alert.showAndWait();

            // Optional: Switch handler back to list if you want to support auto-refresh,
            // but usually we stay on this page.
        });
    }

    @FXML
    public void onBackClicked(ActionEvent e) {
        ClientApp.getNavigator().show(UserSubscriptionsController.class);
    }
}