package gcm.client.controllers.map;

import common.messages.*;
import common.model.City;
import common.model.CityPricingItem;
import gcm.client.controllers.WelcomeController;
import gcm.client.network.GcmClient;
import gcm.client.utill.ClientApp;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.*;

import java.util.List;
import java.util.Optional;

public class EditPricesController {

    @FXML
    private VBox cityList;

    private GcmClient client;

    @FXML
    private void initialize() {
        client = ClientApp.getClient();
        client.setResponseHandler(this::handleResponse);

        client.sendRequest(new GcmRequest(
                RequestType.LIST_CITIES,
                new EmptyPayload()
        ));
    }

    private void handleResponse(GcmResponse response) {
        if (!response.isSuccess()) {
            showError(response.getErrorMessage());
            return;
        }

        Object data = response.getData();
        if (!(data instanceof List<?> list)) {
            return;
        }

        @SuppressWarnings("unchecked")
        List<City> cities = (List<City>) list;

        cityList.getChildren().clear();
        for (City city : cities) {
            cityList.getChildren().add(createCityRow(city));
        }
    }

    private HBox createCityRow(City city) {

        Label name = new Label(city.getName());
        name.setStyle(
                "-fx-text-fill: #1F2937;" +
                        "-fx-font-size: 16;" +
                        "-fx-font-weight: bold;"
        );

        Label price = new Label("Price: " + city.getPrice());
        price.setStyle(
                "-fx-text-fill: #4B5563;" +
                        "-fx-font-size: 13;"
        );

        VBox textBox = new VBox(4, name, price);

        Button edit = new Button("Edit");
        edit.setPrefSize(90, 34);
        edit.setStyle(
                "-fx-background-color: linear-gradient(to right, #00c6ff, #0072ff);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 13;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;"
        );
        edit.setOnAction(e -> editPrice(city, price));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(16, textBox, spacer, edit);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(14, 18, 14, 18));
        row.setStyle(
                "-fx-background-color: #F9FAFB;" +
                        "-fx-background-radius: 14;"
        );

        return row;
    }

    private void editPrice(City city, Label priceLabel) {
        TextInputDialog dialog =
                new TextInputDialog(String.valueOf(city.getPrice()));

        dialog.setTitle("Edit Price");
        dialog.setHeaderText(city.getName());
        dialog.setContentText("Enter new price:");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) return;

        try {
            double newPrice = Double.parseDouble(result.get());

            if (newPrice < 0) {
                showError("Price must be non-negative");
                return;
            }

            CityPricingItem item =
                    new CityPricingItem(city.getId(), newPrice);

            client.setResponseHandler(r -> {
                if (!r.isSuccess()) {
                    showError(r.getErrorMessage());
                    return;
                }

                city.setPrice(newPrice);
                priceLabel.setText("Price: " + newPrice);
            });

            client.sendRequest(new GcmRequest(
                    RequestType.UPDATE_CITY_PRICE,
                    item
            ));

        } catch (NumberFormatException e) {
            showError("Invalid number");
        }
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    @FXML
    private void handleClose() {
        ClientApp.getNavigator().show(WelcomeController.class);
    }
}
