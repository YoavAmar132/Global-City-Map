package gcm.client.controllers.catalog;

import common.messages.*;
import common.model.City;
import gcm.client.controllers.menu.UserMenuController;
import gcm.client.controllers.user_util.BuyMapScreenController;
import gcm.client.network.GcmClient;
import gcm.client.utill.ClientApp;
import gcm.client.controllers.user_util.PurchaseSession;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;

public class BuyMapCatalogController {
    private GcmClient client;

    @FXML private VBox Citylist;
    @FXML private Label statusLabel;

    @FXML
    private void initialize() {
        client = ClientApp.getClient();
        client.setResponseHandler(this::handleResponse);
        loadCities();
    }

    private void loadCities() {
        EmptyPayload payload = new EmptyPayload();
        GcmRequest request = new GcmRequest(RequestType.LIST_CITIES, payload);
        client.sendRequest(request);
    }

    private HBox createMapRow(City city) {
        // 1. שם העיר - גדול ומודגש
        Label name = new Label(city.getName());
        name.setStyle("-fx-text-fill: white; -fx-font-size: 16; -fx-font-weight: bold;");

        // 2. תצוגת המחירים (רגיל + מנוי) - קטן יותר ובצבע בהיר אך שונה
        // אנו משתמשים ב-String.format כדי להציג 2 ספרות אחרי הנקודה
        String priceText = String.format("Buy: $%.2f | Sub: $%.2f", city.getPrice(), city.getSubPrice());
        Label prices = new Label(priceText);
        prices.setStyle("-fx-text-fill: #dddddd; -fx-font-size: 12;");

        // 3. VBox שיחזיק את השם והמחיר אחד מתחת לשני
        VBox infoBox = new VBox(4, name, prices);
        infoBox.setAlignment(Pos.CENTER_LEFT);

        // 4. כפתור הקנייה
        Button buyBtn = new Button("Buy");
        buyBtn.setPrefSize(90, 34);
        buyBtn.setStyle(
                "-fx-background-color: linear-gradient(to right, #11998e, #38ef7d); " +
                        "-fx-text-fill: white; -fx-font-size: 13; -fx-font-weight: bold; " +
                        "-fx-background-radius: 8; -fx-cursor: hand;"
        );
        buyBtn.setOnAction(e -> navigateToBuyScreen(city));

        // 5. מרווח גמיש לדחיפת הכפתור שמאלה
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // 6. הרכבת השורה כולה
        HBox row = new HBox(12, infoBox, spacer, buyBtn);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: rgba(255,255,255,0.14); -fx-background-radius: 12;");
        row.setPadding(new javafx.geometry.Insets(10, 15, 10, 15));

        return row;
    }

    private void navigateToBuyScreen(City city) {
        // 1. Clear old session data
        PurchaseSession.getInstance().clear();

        // 2. Set the new city in the singleton
        PurchaseSession.getInstance().setSelectedCity(city);
        PurchaseSession.getInstance().setUserID(ClientApp.getCurrentUser().getId());

        // 3. Navigate
        ClientApp.getNavigator().show(BuyMapScreenController.class);
    }

    private void handleResponse(GcmResponse response) {
        Platform.runLater(() -> {
            if (!response.isSuccess()) {
                statusLabel.setText("Error loading cities.");
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setContentText(response.getErrorMessage());
                alert.showAndWait();
            } else {
                Object t = response.getData();
                if (t instanceof Popup) {
                    if((((Popup) t).isInList(ClientApp.getCurrentUser().getId())))
                    {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("");
                        alert.setContentText("you have a new message");
                        alert.showAndWait();
                    }
                }
                if (t instanceof ArrayList<?>) {
                    ArrayList<?> list = (ArrayList<?>) t;
                    // בדיקה שהרשימה לא ריקה ושהאיבר הראשון הוא אכן City
                    if (!list.isEmpty() && list.get(0) instanceof City) {
                        Citylist.getChildren().clear();
                        for (Object o : list) {
                            Citylist.getChildren().add(createMapRow((City) o));
                        }
                        statusLabel.setText("Select a city to buy.");
                    } else if (list.isEmpty()) {
                        // טיפול במצב שאין ערים בכלל
                        Citylist.getChildren().clear();
                        statusLabel.setText("No cities available.");
                    }
                }

            }
        });
    }

    public void onBackClicked(ActionEvent actionEvent) {

        ClientApp.getNavigator().show(UserMenuController.class);

    }

    public void onRefreshClicked(ActionEvent actionEvent) {
        loadCities();
    }

    public void handleClose(ActionEvent actionEvent) {
        ClientApp.getNavigator().show(UserMenuController.class);
    }
}