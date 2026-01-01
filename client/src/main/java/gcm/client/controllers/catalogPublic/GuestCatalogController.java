package gcm.client.controllers.catalogPublic;

import common.messages.*;
import common.model.CityCatalogItem;
import common.model.MapCatalogItem;
import gcm.client.controllers.WelcomeController;
import gcm.client.network.GcmClient;
import gcm.client.utill.ClientApp;
import gcm.client.utill.SceneNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import common.messages.GcmRequest;
import common.messages.GcmResponse;
import common.messages.RequestType;

import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.scene.layout.Region;

import java.util.List;


public class GuestCatalogController {

    @FXML
    private VBox cityList;

    private GcmClient client;

    @FXML
    private void initialize() {
        client = ClientApp.getClient();
        client.setResponseHandler(this::handleResponse);

        // request catalog
        client.sendRequest(new GcmRequest(
                RequestType.GET_CITY_CATALOG,
                new EmptyPayload()
        ));
    }

    private void handleResponse(GcmResponse response) {
        if (!response.isSuccess()) {
            showError(response.getErrorMessage());
            return;
        }

        Object data = response.getData();
        if (!(data instanceof List<?> list) || list.isEmpty()) {
            return;
        }

        cityList.getChildren().clear();

        @SuppressWarnings("unchecked")
        List<CityCatalogItem> cities = (List<CityCatalogItem>) list;

        for (CityCatalogItem city : cities) {
            cityList.getChildren().add(createCityRow(city));
        }
    }

    private HBox createCityRow(CityCatalogItem city) {

        Label name = new Label(city.getCityName());

        name.setStyle(
                "-fx-text-fill: #1F2937;" +
                        "-fx-font-size: 16;" +
                        "-fx-font-weight: bold;"
        );

        /*
        String priceText = city.getMapCount() == 0
                ? "0 maps | No prices"
                : city.getMapCount() + " maps | " +
                city.getMinPrice() + " – " + city.getMaxPrice();
        */
        String priceText =
                city.getMapCount() + " maps | Price: " + city.getCityPrice();


        Label info = new Label(priceText);
        info.setStyle(
                "-fx-text-fill: #4B5563;" +
                        "-fx-font-size: 13;"
        );

        VBox textBox = new VBox(4, name, info);


        Button open = new Button("Open");
        open.setPrefSize(90, 34);
        open.setStyle(
                "-fx-background-color: linear-gradient(to right, #00c6ff, #0072ff);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 13;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;"
        );
        open.setOnAction(e -> openCity(city));


        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(16, textBox, spacer, open);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(14, 18, 14, 18));
        row.setStyle(
                "-fx-background-color: #F9FAFB;" +   // light card
                        "-fx-background-radius: 14;"
        );
        return row;
    }


    public static void forcedRefresh(){
        ClientApp.getNavigator().show(GuestCatalogController.class);

    }

    private void openCity(CityCatalogItem city) {

        CityMapsRequestPayload payload =
                new CityMapsRequestPayload(city.getCityId());

        GcmRequest request =
                new GcmRequest(RequestType.GET_CITY_MAPS, payload);

        client.setResponseHandler(response -> {
            if (!response.isSuccess()) {
                showError(response.getErrorMessage());
                return;
            }

            @SuppressWarnings("unchecked")
            List<MapCatalogItem> maps =
                    (List<MapCatalogItem>) response.getData();

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
        ClientApp.getNavigator().show(WelcomeController.class);
    }


}
