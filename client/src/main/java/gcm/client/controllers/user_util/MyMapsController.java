package gcm.client.controllers.user_util;

import common.messages.*;
import common.model.MapSheet;
import gcm.client.controllers.map.UserMapViewerController;
import gcm.client.controllers.menu.UserMenuController;
import gcm.client.network.GcmClient;
import gcm.client.utill.ClientApp;
import gcm.client.utill.SceneNavigator;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import java.util.ArrayList;

public class MyMapsController {
    private GcmClient client;

    @FXML private VBox Citylist;
    @FXML private Label CstatusLabel;

    @FXML
    private void initialize() {
        client = ClientApp.getClient();
        client.setResponseHandler(this::handleResponse);
        loadMyMaps();
    }

    private void loadMyMaps() {
        int userId = ClientApp.getCurrentUser().getId();
        // RequestType.LIST_USER_PURCHASES must be handled in RequestHandler
        // to return List<MapSheet> (OTP maps + active subscription maps)
        // In MyMapsController.java
        GcmRequest request = new GcmRequest(RequestType.LIST_USER_MAPS, userId);
        client.sendRequest(request);
        CstatusLabel.setText("Fetching your maps...");
    }

    /**
     * Creates the "View Subscriptions" button dynamically.
     * We add this to the top of the list every time we reload.
     */
    private Button createSubscriptionButton() {
        Button subBtn = new Button("View My Subscriptions");
        subBtn.setMaxWidth(Double.MAX_VALUE);
        subBtn.setPrefHeight(40);
        subBtn.setStyle(
                "-fx-background-color: #ff9800; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-size: 14; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand;"
        );

        // Navigate to the Subscriptions Controller (Ensure you created this class!)
        subBtn.setOnAction(e -> ClientApp.getNavigator().show(UserSubscriptionsController.class));
        return subBtn;
    }

    private HBox createMapRow(MapSheet map) {
        // 1. Map Name
        Label name = new Label(map.getName());
        name.setStyle("-fx-text-fill: white; -fx-font-size: 16; -fx-font-weight: bold;");

        // 2. Version Info
        Label subInfo = new Label("Version: " + map.getVersion());
        subInfo.setStyle("-fx-text-fill: #b0b5bd; -fx-font-size: 12;");

        VBox textBox = new VBox(2, name, subInfo);

        // 3. Open Button
        Button open = new Button("Open Map");
        open.setPrefSize(100, 30);
        open.setStyle(
                "-fx-background-color: linear-gradient(to right, #00c6ff, #0072ff); " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 13; " +
                        "-fx-background-radius: 20; " +
                        "-fx-cursor: hand;"
        );
        open.setOnAction(e -> openMap(map));

        // 4. Layout
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(12, textBox, spacer, open);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 12;");
        row.setPadding(new javafx.geometry.Insets(10, 15, 10, 15));

        return row;
    }

    public void openMap(MapSheet map) {
        SceneNavigator.LoadedView<UserMapViewerController> view =
                ClientApp.getNavigator().get(UserMapViewerController.class);

        // Pass the MapSheet object to the viewer
        view.controller.setVals(map);

        ClientApp.getNavigator().showLoaded(view.root);
        System.out.println("Opened map: " + map.getName() + " v" + map.getVersion());
    }

    private void handleResponse(GcmResponse response) {
        Platform.runLater(() -> {
            if (!response.isSuccess()) {
                CstatusLabel.setText("Error loading maps.");
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setContentText(response.getErrorMessage());
                alert.showAndWait();
            } else {
                Object t = response.getData();
                if (t instanceof ArrayList<?>) {
                    ArrayList<?> list = (ArrayList<?>) t;

                    Citylist.getChildren().clear();

                    // 1. ALWAYS Add the Subscription Button at the top
                    Citylist.getChildren().add(createSubscriptionButton());
                    // Add a small spacer after the button
                    Label spacer = new Label("");
                    spacer.setMinHeight(10);
                    Citylist.getChildren().add(spacer);

                    // 2. Add the maps
                    if (list.isEmpty()) {
                        CstatusLabel.setText("You don't have any maps yet.");
                    } else {
                        CstatusLabel.setText("");
                        for (Object obj : list) {
                            if (obj instanceof MapSheet) {
                                Citylist.getChildren().add(createMapRow((MapSheet) obj));
                            }
                        }
                    }
                }
            }
        });
    }

    public void onBackClicked(ActionEvent actionEvent) {
        ClientApp.getNavigator().show(UserMenuController.class);
    }

    public void handleClose(ActionEvent actionEvent) {
        Platform.exit();
    }

    public void onRefreshClicked(ActionEvent actionEvent) {
        loadMyMaps();
    }
}