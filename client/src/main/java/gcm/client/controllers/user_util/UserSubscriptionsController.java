package gcm.client.controllers.user_util;

import common.messages.*;
import common.model.City;
import gcm.client.network.GcmClient;
import gcm.client.utill.ClientApp;
import gcm.client.utill.SceneNavigator;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import java.util.ArrayList;

public class UserSubscriptionsController {
    private GcmClient client;

    @FXML private VBox contentList;
    @FXML private Label statusLabel;

    @FXML
    private void initialize() {
        client = ClientApp.getClient();
        client.setResponseHandler(this::handleResponse);
        loadSubscriptions();
    }

    private void loadSubscriptions() {
        int userId = ClientApp.getCurrentUser().getId();
        // Ensure RequestType.LIST_USER_SUBSCRIPTIONS exists in your enum!
        GcmRequest request = new GcmRequest(RequestType.LIST_USER_SUBSCRIPTIONS, userId);
        client.sendRequest(request);
        statusLabel.setText("Loading subscriptions...");
    }

    // In gcm/client/controllers/user_util/UserSubscriptionsController.java

    private HBox createCityRow(City city) {
        // 1. City Name
        Label name = new Label(city.getName());
        name.setStyle("-fx-text-fill: white; -fx-font-size: 16; -fx-font-weight: bold;");

        // 2. Expiration Date Label (NEW)
        // We read the date we stored in 'description'
        String expDate = (city.getDescription() != null) ? city.getDescription() : "Unknown";
        Label dateLabel = new Label("Exp: " + expDate);
        dateLabel.setStyle("-fx-text-fill: #ff9800; -fx-font-size: 12;");

        VBox infoBox = new VBox(2, name, dateLabel); // Stack name and date

        // 3. Action Button
        Button viewMapsBtn = new Button("Browse Maps");
        viewMapsBtn.setPrefHeight(32);
        viewMapsBtn.setStyle(
                "-fx-background-color: #00c6ff; -fx-text-fill: white; " +
                        "-fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand;"
        );
        viewMapsBtn.setOnAction(e -> openSubscriptionCity(city));

        // 4. Layout
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(12, infoBox, spacer, viewMapsBtn); // Use infoBox instead of name
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 8; -fx-padding: 10;");

        return row;
    }

    private void openSubscriptionCity(City city) {
        // Navigate to SubscriptionMapsController
        SceneNavigator.LoadedView<SubscriptionMapsController> view =
                ClientApp.getNavigator().get(SubscriptionMapsController.class);

        view.controller.setCity(city);
        ClientApp.getNavigator().showLoaded(view.root);
    }

    private void handleResponse(GcmResponse response) {
        Platform.runLater(() -> {
            if (response.isSuccess() && response.getData() instanceof ArrayList<?>) {
                ArrayList<?> list = (ArrayList<?>) response.getData();
                contentList.getChildren().clear();

                if (list.isEmpty()) {
                    statusLabel.setText("You have no active subscriptions.");
                } else {
                    statusLabel.setText(""); // Clear text when data exists
                    for (Object o : list) {
                        if (o instanceof City) {
                            contentList.getChildren().add(createCityRow((City) o));
                        }
                    }
                }
            } else {
                statusLabel.setText("Error: " + response.getErrorMessage());
            }
        });
    }

    @FXML
    public void onBackClicked(ActionEvent e) {
        ClientApp.getNavigator().show(MyMapsController.class);
    }
}