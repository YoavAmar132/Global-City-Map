package gcm.client.controllers.map;

import common.messages.*;
import common.model.City;
import common.model.CityPricingItem;
import gcm.client.controllers.WelcomeController;
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
        // הגדרת ה-Handler הראשוני לטעינת הרשימה
        client.setResponseHandler(this::handleListResponse);

        client.sendRequest(new GcmRequest(
                RequestType.LIST_CITIES,
                new EmptyPayload()
        ));
    }

    // פונקציה נפרדת לטיפול בתשובת הרשימה
    private void handleListResponse(GcmResponse response) {
        // עדכוני GUI חייבים לרוץ ב-Platform.runLater
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

        // הצגת שני המחירים למשתמש
        Label price = new Label(String.format("One-time: %.2f | Sub: %.2f", city.getPrice(), city.getSubPrice()));
        price.setStyle("-fx-text-fill: #4B5563; -fx-font-size: 13;");

        VBox textBox = new VBox(4, name, price);

        Button edit = new Button("Edit");
        edit.setPrefSize(90, 34);
        edit.setStyle(
                "-fx-background-color: linear-gradient(to right, #00c6ff, #0072ff);" +
                        "-fx-text-fill: white; -fx-font-size: 13; -fx-font-weight: bold;" +
                        "-fx-background-radius: 8; -fx-cursor: hand;"
        );

        // בעת לחיצה, פותחים דיאלוג עריכה מורחב
        edit.setOnAction(e -> showEditDialog(city, price));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(16, textBox, spacer, edit);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(14, 18, 14, 18));
        row.setStyle("-fx-background-color: #F9FAFB; -fx-background-radius: 14;");

        return row;
    }

    private void showEditDialog(City city, Label priceLabel) {
        // יצירת דיאלוג מותאם אישית שמאפשר החזרת זוג ערכים (Pair)
        Dialog<Pair<Double, Double>> dialog = new Dialog<>();
        dialog.setTitle("Edit Prices");
        dialog.setHeaderText("Update prices for " + city.getName());

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // יצירת טופס עם שני שדות
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

        // המרת התוצאה ל-Pair בעת לחיצה על שמירה
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    double newPrice = Double.parseDouble(priceField.getText());
                    double newSubPrice = Double.parseDouble(subPriceField.getText());
                    return new Pair<>(newPrice, newSubPrice);
                } catch (NumberFormatException e) {
                    return null; // יטופל בחוץ
                }
            }
            return null;
        });

        Optional<Pair<Double, Double>> result = dialog.showAndWait();

        // אם המשתמש לחץ Save והערכים תקינים
        result.ifPresent(prices -> {
            double newPrice = prices.getKey();
            double newSubPrice = prices.getValue();

            if (newPrice < 0 || newSubPrice < 0) {
                showError("Prices must be non-negative");
                return;
            }

            updatePricesOnServer(city, newPrice, newSubPrice, priceLabel);
        });
    }

    private void updatePricesOnServer(City city, double newPrice, double newSubPrice, Label priceLabel) {
        // הנחה: עדכנת את CityPricingItem שיהיה לו בנאי שמקבל את שני המחירים
        CityPricingItem item = new CityPricingItem(city.getId(), newPrice, newSubPrice);

        client.setResponseHandler(response -> Platform.runLater(() -> {
            if (!response.isSuccess()) {
                showError(response.getErrorMessage());
                return;
            }

            // עדכון המודל המקומי
            city.setPrice(newPrice);
            city.setSubPrice(newSubPrice);

            // עדכון התצוגה למשתמש
            priceLabel.setText(String.format("One-time: %.2f | Sub: %.2f", newPrice, newSubPrice));
        }));

        client.sendRequest(new GcmRequest(
                RequestType.UPDATE_CITY_PRICE,
                item
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