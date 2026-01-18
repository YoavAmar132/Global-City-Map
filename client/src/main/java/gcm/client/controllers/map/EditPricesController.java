package gcm.client.controllers.map;

import common.messages.*;
import common.model.City;
import common.model.CityPricingItem;
import gcm.client.controllers.menu.ContentWorkerMenuController;
import gcm.client.network.GcmClient;
import gcm.client.utill.ClientApp;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.*;
import javafx.util.Pair;

import java.util.List;
import java.util.Optional;

public class EditPricesController {

    @FXML
    private VBox cityList;

    private GcmClient client;

    @FXML
    private void initialize() {
        client = ClientApp.getClient();
        client.setResponseHandler(this::handleListResponse);

        client.sendRequest(new GcmRequest(
                RequestType.LIST_CITIES,
                new EmptyPayload()
        ));
    }

    private void handleListResponse(GcmResponse response) {
        Platform.runLater(() -> {
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
        });
    }

    private HBox createCityRow(City city) {
        Label name = new Label(city.getName());
        name.setStyle("-fx-text-fill: #1F2937; -fx-font-size: 16; -fx-font-weight: bold;");

        Label price = new Label(
                String.format("One-time: %.2f | Sub: %.2f",
                        city.getPrice(),
                        city.getSubPrice())
        );
        price.setStyle("-fx-text-fill: #4B5563; -fx-font-size: 13;");

        VBox textBox = new VBox(4, name, price);

        Button edit = new Button("Edit");
        edit.setPrefSize(90, 34);
        edit.setStyle(
                "-fx-background-color: linear-gradient(to right, #00c6ff, #0072ff);" +
                        "-fx-text-fill: white; -fx-font-size: 13; -fx-font-weight: bold;" +
                        "-fx-background-radius: 8; -fx-cursor: hand;"
        );

        edit.setOnAction(e -> showEditDialog(city));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(16, textBox, spacer, edit);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(14, 18, 14, 18));
        row.setStyle("-fx-background-color: #F9FAFB; -fx-background-radius: 14;");

        return row;
    }

    private void showEditDialog(City city) {
        Dialog<Pair<Double, Double>> dialog = new Dialog<>();
        dialog.setTitle("Edit Prices");
        dialog.setHeaderText("Request price change for " + city.getName());

        ButtonType saveButtonType = new ButtonType("Send Request", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField priceField = new TextField(String.valueOf(city.getPrice()));
        TextField subPriceField = new TextField(String.valueOf(city.getSubPrice()));

        grid.add(new Label("One-time Price:"), 0, 0);
        grid.add(priceField, 1, 0);
        grid.add(new Label("Subscription Price:"), 0, 1);
        grid.add(subPriceField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> {
            if (button == saveButtonType) {
                try {
                    double p = Double.parseDouble(priceField.getText());
                    double sp = Double.parseDouble(subPriceField.getText());
                    return new Pair<>(p, sp);
                } catch (Exception e) {
                    return null;
                }
            }
            return null;
        });

        Optional<Pair<Double, Double>> result = dialog.showAndWait();

        result.ifPresent(prices -> {
            double newPrice = prices.getKey();
            double newSubPrice = prices.getValue();

            if (newPrice < 0 || newSubPrice < 0) {
                showError("Prices must be non-negative");
                return;
            }

            sendPriceChangeRequest(city, newPrice, newSubPrice);
        });
    }

    private void sendPriceChangeRequest(City city,
                                        double newPrice,
                                        double newSubPrice) {

        CityPricingItem payload =
                new CityPricingItem(city.getId(), newPrice, newSubPrice);

        client.setResponseHandler(response -> Platform.runLater(() -> {
            if (!response.isSuccess()) {
                showError(response.getErrorMessage());
                return;
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText(null);
            alert.setContentText("Price change request sent for approval.");
            alert.showAndWait();

            ClientApp.getNavigator()
                    .show(ContentWorkerMenuController.class);
        }));

        client.sendRequest(new GcmRequest(
                RequestType.REQUEST_CITY_PRICE_CHANGE,
                payload
        ));
    }

    private void showError(String msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(msg);
            alert.showAndWait();
        });
    }

    @FXML
    private void handleClose() {
        ClientApp.getNavigator().show(ContentWorkerMenuController.class);
    }
}
