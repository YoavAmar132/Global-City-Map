package gcm.client.controllers.user_util;

import gcm.client.controllers.map.MapLoaderController;
import common.messages.*;
import common.model.City;
import gcm.client.controllers.map.MapViewerController;
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
import javafx.scene.layout.VBox;
import java.util.ArrayList;

public class MyMapsController {
    private GcmClient client;

    @FXML
    private VBox Citylist;
    @FXML
    private Label CstatusLabel;

    @FXML
    private void initialize() {
        client = ClientApp.getClient();
        client.setResponseHandler(this::handleResponse);

        loadMyMaps();
    }

    private void loadMyMaps() {
        int userId = ClientApp.getCurrentUser().getId();
        // Send request with just the User ID
        GcmRequest request = new GcmRequest(RequestType.LIST_USER_PURCHASES, userId);
        client.sendRequest(request);
        CstatusLabel.setText("Fetching your maps...");
    }

    private HBox createMapRow(City city) {
        // 1. Name Label
        Label name = new Label(city.getName());
        name.setStyle("-fx-text-fill: white; -fx-font-size: 16; -fx-font-weight: bold;");

        // 2. BaseMap/Info Label (Since we don't have description, we show BaseMap or ID)
        Label subInfo = new Label("BaseMap: " + city.getBasemap());
        subInfo.setStyle("-fx-text-fill: #b0b5bd; -fx-font-size: 12;");

        VBox textBox = new VBox(2, name, subInfo);

        // 3. Open Button
        Button open = new Button("Open Map");
        open.setPrefSize(100, 30);
        open.setStyle(
                "-fx-background-color: linear-gradient(to right, #00c6ff, #0072ff); " +
                        "-fx-text-fill: white; -fx-font-size: 13; -fx-background-radius: 20; -fx-cursor: hand;"
        );
        open.setOnAction(e -> openCity(city));

        // 4. Layout
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        HBox row = new HBox(12, textBox, spacer, open);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 12;");
        row.setPadding(new javafx.geometry.Insets(10, 15, 10, 15));

        return row;
    }

    public void openCity(City city) {
        System.out.println("gets list of cities");
        SceneNavigator.LoadedView<MapViewerController> view =
                ClientApp.getNavigator().get(MapViewerController.class);
        // set values BEFORE showing
        view.controller.setVals(city.getBasemap());

        // now show
        ClientApp.getNavigator().showLoaded(view.root);

        System.out.println("opened: " + city.getBasemap());
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

                    if (list.isEmpty()) {
                        CstatusLabel.setText("You haven't purchased any maps yet.");
                    } else {
                        CstatusLabel.setText(""); // Clear loading text
                        // Safe casting
                        for (Object obj : list) {
                            if (obj instanceof City) {
                                Citylist.getChildren().add(createMapRow((City) obj));
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