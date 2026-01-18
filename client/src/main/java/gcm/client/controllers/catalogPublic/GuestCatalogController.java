package gcm.client.controllers.catalogPublic;

import common.messages.*;
import common.model.CityCatalogItem;
import common.model.MapCatalogItem;
import gcm.client.controllers.WelcomeController;
import gcm.client.controllers.menu.UserMenuController;
import gcm.client.network.GcmClient;
import gcm.client.utill.ClientApp;
import gcm.client.utill.SceneNavigator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.Region;

import javax.management.Descriptor;
import java.util.List;
import java.util.Objects;

public class GuestCatalogController {

    @FXML
    private VBox cityList;

    @FXML
    private TextField searchField;

    private GcmClient client;

    @FXML
    private void initialize() {
        client = ClientApp.getClient();
        client.setResponseHandler(this::handleResponse);
        loadCatalog();
    }

    private void loadCatalog() {
        client.sendRequest(new GcmRequest(
                RequestType.GET_CITY_CATALOG,
                new EmptyPayload()
        ));
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText();

        if (query == null || query.trim().isEmpty()) {
            loadCatalog();
            return;
        }

        client.sendRequest(new GcmRequest(
                RequestType.SEARCH_CITY,
                new SearchPayload(query)
        ));
    }

    private void handleResponse(GcmResponse response) {
        if (!response.isSuccess()) {
            showError(response.getErrorMessage());
            return;
        }

        Object data = response.getData();
        if (data instanceof Reload) {
            Platform.runLater(() -> ClientApp.getNavigator().show(GuestCatalogController.class));
            return;
        }

        if (!(data instanceof List<?> list)) {
            return;
        }

        Platform.runLater(() -> {
            cityList.getChildren().clear();

            @SuppressWarnings("unchecked")
            List<CityCatalogItem> cities = (List<CityCatalogItem>) list;

            if (cities.isEmpty()) {
                cityList.getChildren().add(new Label("No cities found matching your search."));
            } else {
                for (CityCatalogItem city : cities) {
                    cityList.getChildren().add(createCityRow(city));
                }
            }
        });
    }

    private HBox createCityRow(CityCatalogItem city) {
        // 1. City Name
        Label name = new Label(city.getCityName());
        name.setStyle("-fx-text-fill: #1F2937; -fx-font-size: 16; -fx-font-weight: bold;");

        String description = city.getCityDescription();
        if (description == null || description.isBlank()) {
            description = "No description";
        }

        String statsText = String.format(
                "Maps: %d  |  POIs: %d  |  Tours: %d  | Description: %s",
                city.getMapCount(),
                city.getPoiCount(),
                city.getToursCount(),
                description
        );



        Label statsLabel = new Label(statsText);
        statsLabel.setStyle("-fx-text-fill: #0072ff; -fx-font-weight: bold; -fx-font-size: 12;");

        // REMOVED: Description Label logic

        VBox textBox = new VBox(4, name, statsLabel);

        // 3. Action Button
        Button open = new Button("Open");
        open.setPrefSize(90, 34);
        open.setStyle(
                "-fx-background-color: linear-gradient(to right, #00c6ff, #0072ff);" +
                        "-fx-text-fill: white; -fx-font-size: 13; -fx-font-weight: bold;" +
                        "-fx-background-radius: 8; -fx-cursor: hand;"
        );
        open.setOnAction(e -> openCity(city));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(16, textBox, spacer, open);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(14, 18, 14, 18));
        row.setStyle("-fx-background-color: #F9FAFB; -fx-background-radius: 14; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        return row;
    }

    public static void forcedRefresh() {
        ClientApp.getNavigator().show(GuestCatalogController.class);
    }

    private void openCity(CityCatalogItem city) {
        int userID = 0;
        if (ClientApp.getCurrentUser() != null) {
            userID = ClientApp.getCurrentUser().getId();
        }
        CityMapsRequestPayload payload =
                new CityMapsRequestPayload(city.getCityId(), userID);

        GcmRequest request =
                new GcmRequest(RequestType.GET_CITY_MAPS, payload);

        client.setResponseHandler(response -> {
            if (!response.isSuccess()) {
                showError(response.getErrorMessage());
                return;
            }

            @SuppressWarnings("unchecked")
            List<MapCatalogItem> maps = (List<MapCatalogItem>) response.getData();

            SceneNavigator.LoadedView<GuestCityMapsController> view =
                    ClientApp.getNavigator().get(GuestCityMapsController.class);

            view.controller.setCity(city);
            view.controller.setMaps(maps);

            ClientApp.getNavigator().showLoaded(view.root);
        });

        client.sendRequest(request);
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setContentText(msg);
        a.showAndWait();
    }

    @FXML
    private void handleClose() {
        if (ClientApp.getCurrentUser() != null && Objects.equals(ClientApp.getCurrentUser().getRole(), "Customer")) {
            ClientApp.getNavigator().show(UserMenuController.class);
        } else {
            ClientApp.getNavigator().show(WelcomeController.class);
        }
    }
}