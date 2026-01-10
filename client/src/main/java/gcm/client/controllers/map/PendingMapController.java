package gcm.client.controllers.map;

import common.messages.*;
import common.model.City;
import common.model.MapSheet;
import gcm.client.controllers.catalogPublic.GuestCatalogController;
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

    @FXML
    private VBox pendingList;

    private ArrayList<City> cities = new ArrayList<>(); // Initialize to avoid NPE

    @FXML
    private void initialize() {
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

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        HBox row = new HBox(12, name, spacer, open, approve);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: rgba(255,255,255,0.14); -fx-background-radius: 12;");
        row.setPadding(new javafx.geometry.Insets(10, 12, 10, 12));

        return row;
    }

    private void ApproveMap(MapSheet map) {
        if (map == null) return;

        // Safety check if cities haven't loaded yet
        if (cities == null || cities.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Please Wait");
            alert.setContentText("City list is still loading. Try again in a moment.");
            alert.showAndWait();
            return;
        }

        List<String> cityNames = cities.stream().map(City::getName).collect(Collectors.toList());

        ChoiceDialog<String> dialog = new ChoiceDialog<>(cityNames.get(0), cityNames);
        dialog.setTitle("Approve Map");
        dialog.setHeaderText("Choose a city for this map");
        dialog.setContentText("City:");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) return;

        String chosenCity = result.get();
        System.out.println("approved for city: " + chosenCity);

        // Send approval request
        ApprovePayload payload = new ApprovePayload(map, chosenCity);
        GcmRequest request = new GcmRequest(RequestType.APPROVE_MAP_VERSION, payload);
        client.sendRequest(request);


    }

    public void openPendingMap(MapSheet map) {
        SceneNavigator.LoadedView<UserMapViewerController> view =
                ClientApp.getNavigator().get(UserMapViewerController.class);

        view.controller.setVals(map);
        ClientApp.getNavigator().showLoaded(view.root);
    }

    private void handleResponse(GcmResponse response) {
        // ALWAYS use Platform.runLater for UI updates from network threads
        Platform.runLater(() -> {
            if (!response.isSuccess()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setContentText(response.getErrorMessage());
                alert.showAndWait();
                return;
            }

            Object t = response.getData();
            if (t instanceof Popup) {
                // Likely the result of "APPROVE_MAP_VERSION" success
                System.out.println("Map approval success (or empty response)");
                // Optional: refresh list automatically
                onRefreshClicked(null);
                return;
            }

            if (t instanceof ArrayList<?>) {
                ArrayList<?> list = (ArrayList<?>) t;

                // Case 1: Empty list. We must check which request type it was.
                // Since we can't easily track request IDs here without complex logic,
                // we rely on the object type if the list ISN'T empty.
                // If it IS empty, we can't distinguish, but clearing the map list is usually safer.

                if (list.isEmpty()) {
                    // It's ambiguous, but usually safe to clear the UI list just in case it was a map search result
                    // If it was an empty city list, clearing the pending map list is a harmless side effect (unless you expected maps).
                    // Ideally, your server response should include the "RequestType" it is answering.
                    System.out.println("Received empty list.");
                    return;
                }

                // Case 2: List has items, check the first item type
                Object firstItem = list.get(0);

                if (firstItem instanceof City) {
                    @SuppressWarnings("unchecked")
                    ArrayList<City> loadedCities = (ArrayList<City>) list;
                    setCities(loadedCities);
                    System.out.println("City list loaded: " + loadedCities.size());
                }
                else if (firstItem instanceof MapSheet) {
                    @SuppressWarnings("unchecked")
                    List<MapSheet> pendingMaps = (List<MapSheet>) list;
                    pendingList.getChildren().clear();
                    for (MapSheet map : pendingMaps) {
                        pendingList.getChildren().add(createMapRow(map));
                    }
                    System.out.println("Pending maps loaded: " + pendingMaps.size());
                }
            }
        });
    }

    public void onBackClicked(ActionEvent actionEvent) {
        ClientApp.getNavigator().show(ContentWorkerMenuController.class);
    }

    public void handleClose(ActionEvent actionEvent) {
        ClientApp.getNavigator().show(ContentWorkerMenuController.class);
    }

    public void onRefreshClicked(ActionEvent actionEvent) {
        // Re-initialize to fetch fresh data
        ClientApp.getNavigator().show(PendingMapController.class);
    }
}