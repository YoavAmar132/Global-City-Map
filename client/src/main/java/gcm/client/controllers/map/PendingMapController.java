package gcm.client.controllers.map;

import common.messages.*;
import common.model.City;
import common.model.MapSheet;
import gcm.client.controllers.catalogPublic.GuestCatalogController;
import gcm.client.controllers.menu.ContentManagerController;
import gcm.client.controllers.menu.ContentWorkerMenuController;
import gcm.client.network.GcmClient;
import gcm.client.utill.ClientApp;
import gcm.client.utill.SceneNavigator;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PendingMapController {
    private GcmClient client;
    private City currentCity;

    private boolean active = false;

    @FXML
    private VBox pendingList;

    private ArrayList<City> cities = new ArrayList<>(); // Initialize to avoid NPE

    @FXML
    private void initialize() {
        active = true;

        client = ClientApp.getClient();
        client.setResponseHandler(this::handleResponse);

        // 1. Send request for Pending Maps
        MaPayload maPayload = new MaPayload(0, "");
        GcmRequest mapRequest = new GcmRequest(RequestType.GET_PENDING_MAPS, maPayload);
        client.sendRequest(mapRequest);

        // 2. Send request for Cities
        EmptyPayload cityPayload = new EmptyPayload();
        GcmRequest cityRequest = new GcmRequest(RequestType.LIST_CITIES, cityPayload);
        client.sendRequest(cityRequest);
    }

    public void setCities(ArrayList<City> cities) {
        this.cities = cities;
    }

    private HBox createMapRow(MapSheet map) {
        Label name = new Label(map.getName());
        name.setStyle("-fx-text-fill: white; -fx-font-size: 14;");

        Button open = new Button("Open");
        open.setPrefSize(90, 30);
        open.setStyle("-fx-background-color: linear-gradient(to right, #00c6ff, #0072ff); -fx-text-fill: white; -fx-font-size: 13; -fx-background-radius: 8; -fx-cursor: hand;");
        open.setOnAction(e -> openPendingMap(map));

        Button approve = new Button("Approve");
        approve.setPrefSize(90, 30);
        approve.setStyle("-fx-background-color: rgba(255,255,255,0.20); -fx-text-fill: white; -fx-font-size: 13; -fx-background-radius: 8; -fx-cursor: hand;");
        approve.setOnAction(e -> ApproveMap(map));

        Button reject = new Button("Reject");
        reject.setPrefSize(90, 30);
        reject.setStyle("""
    -fx-background-color: rgba(255,80,80,0.35);
    -fx-text-fill: white;
    -fx-font-size: 13;
    -fx-background-radius: 8;
    -fx-cursor: hand;
""");

        reject.setOnAction(e -> rejectMap(map));


        HBox spacer = new HBox();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        HBox row = new HBox(12, name, spacer, open, approve, reject);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: rgba(255,255,255,0.14); -fx-background-radius: 12;");
        row.setPadding(new javafx.geometry.Insets(10, 12, 10, 12));

        return row;
    }

    private void rejectMap(MapSheet map) {
        if (map == null) return;

        client.sendRequest(
                new GcmRequest(
                        RequestType.REJECT_PENDING_MAP,
                        map
                )
        );

        client.sendRequest(
                new GcmRequest(
                        RequestType.GET_PENDING_MAPS,
                        new MaPayload(0, "")
                )
        );
    }



    private void ApproveMap(MapSheet map) {
        if (map == null) return;

        if (cities == null || cities.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Please Wait");
            alert.setContentText("City list is still loading. Try again in a moment.");
            alert.showAndWait();
            return;
        }

        List<String> cityNames = cities.stream()
                .map(City::getName)
                .collect(Collectors.toList());

        ChoiceDialog<String> dialog =
                new ChoiceDialog<>(cityNames.get(0), cityNames);

        dialog.setTitle("Approve Map");
        dialog.setHeaderText("Choose a city for this map");
        dialog.setContentText("City:");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) return;

        String chosenCity = result.get();

        // Send approval
        client.sendRequest(
                new GcmRequest(
                        RequestType.APPROVE_MAP_VERSION,
                        new ApprovePayload(map, chosenCity)
                )
        );

        client.sendRequest(
                new GcmRequest(
                        RequestType.GET_PENDING_MAPS,
                        new MaPayload(0, "")
                )
        );
    }

    public void openPendingMap(MapSheet map) {
        SceneNavigator.LoadedView<UserMapViewerController> view =
                ClientApp.getNavigator().get(UserMapViewerController.class);

        view.controller.setVals(map);
        ClientApp.getNavigator().showLoaded(view.root);
    }

    private void handleResponse(GcmResponse response) {
        if (!active) return;

        Platform.runLater(() -> {
            if (!active) return;

            if (!response.isSuccess()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setContentText(response.getErrorMessage());
                alert.showAndWait();
                return;
            }

            Object data = response.getData();

            if (data instanceof ArrayList<?> list && !list.isEmpty()) {
                Object first = list.get(0);

                if (first instanceof City) {
                    cities = (ArrayList<City>) list;
                }
                else if (first instanceof MapSheet) {
                    pendingList.getChildren().clear();
                    for (MapSheet map : (List<MapSheet>) list) {
                        pendingList.getChildren().add(createMapRow(map));
                    }
                }
            }
        });
    }


    private void detach() {
        active = false;
        client.setResponseHandler(null);
    }



    public void onBackClicked(ActionEvent actionEvent) {
        detach();
        ClientApp.getNavigator().show(ContentManagerController.class);
    }

    public void handleClose(ActionEvent actionEvent) {
        detach();
        ClientApp.getNavigator().show(ContentManagerController.class);
    }

    public void onRefreshClicked(ActionEvent actionEvent) {
        // Re-initialize to fetch fresh data
        ClientApp.getNavigator().show(PendingMapController.class);
    }
}