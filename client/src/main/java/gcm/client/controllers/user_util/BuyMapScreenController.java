package gcm.client.controllers.user_util;

import common.messages.*;
import common.model.City;
import common.model.MapSheet;
import common.model.Poi;
import gcm.client.controllers.catalog.BuyMapCatalogController;
import gcm.client.controllers.map.MapViewerController;
import gcm.client.controllers.menu.ContentWorkerMenuController;
import gcm.client.controllers.menu.UserMenuController;
import gcm.client.network.GcmClient;
import gcm.client.utill.ClientApp;
import gcm.client.utill.SceneNavigator;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class BuyMapScreenController {

    private GcmClient client;
    @FXML
    private VBox Baselist;
    private ArrayList<City> cities; // loaded earlier
      private  double price;
      private  double subprice;
      private String cityname;


    @FXML
    private void initialize() {
        client = ClientApp.getClient();
        client.setResponseHandler(this::handleResponse);

        EmptyPayload payload=new EmptyPayload();
        GcmRequest request = new GcmRequest(RequestType.LIST_CITIES, payload);
        client.sendRequest(request);



    }

    public void setCities(ArrayList<City> cities) {
        this.cities = cities;
    }

    private HBox createMapRow(City city) {
        Label name = new Label(city.getName());
        name.setStyle("-fx-text-fill: white; -fx-font-size: 14;");

        Button open = new Button("Open");
        open.setPrefSize(90, 30);
        open.setStyle(
                "-fx-background-color: linear-gradient(to right, #00c6ff, #0072ff); " +
                        "-fx-text-fill: white; -fx-font-size: 13; -fx-background-radius: 8; -fx-cursor: hand;"
        );
        open.setOnAction(e -> openCity(city));


        HBox spacer = new HBox();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        // ✅ add BOTH buttons
        HBox row = new HBox(12, name, spacer, open);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: rgba(255,255,255,0.14); -fx-background-radius: 12;");
        row.setPadding(new javafx.geometry.Insets(10, 12, 10, 12));

        return row;
    }



    public void openCity(City city) {
        MaPayload payload=new MaPayload(0,city.getName());
        this.price=city.getPrice();
        this.subprice=city.getSubPrice();
        this.cityname=city.getName();
     GcmRequest request  = new GcmRequest(RequestType.LIST_MAPS_FOR_CITY, payload);
        client.sendRequest(request);

    }


    private void handleResponse(GcmResponse response) {
        if (!response.isSuccess()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("loading all maps Failed");
            alert.setContentText(response.getErrorMessage());
            alert.showAndWait();
        }else {

            Object t = response.getData();
            if(t instanceof BuyMapPayload)
            {
                boolean subscription =((BuyMapPayload) t).isSubscription();
                if(!subscription)
                {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("your transaction is compleate check your Messages");
                    alert.setContentText(response.getErrorMessage());
                    alert.showAndWait();
                }
            }
            if(t instanceof ArrayList<?>||t instanceof List<?>)
            {
                ArrayList<?> list = (ArrayList<?>) t;

                if (!list.isEmpty() && list.get(0) instanceof City) {
                    @SuppressWarnings("unchecked")
                    ArrayList<City> cities = (ArrayList<City>) list;
                    setCities(cities);
                    Baselist.getChildren().clear();
                    for (City city  : cities) {
                        Baselist.getChildren().add(createMapRow(city));
                    }
                    System.out.println("City list success");
                }
                if (!list.isEmpty() && list.get(0) instanceof MapSheet) {
                    ArrayList<MapSheet> map = (ArrayList<MapSheet>) list;
                    showMapsPopup(map,this.price,this.subprice);
                }


            }

        }


    }


    public void onBackClicked(ActionEvent actionEvent) {
        ClientApp.getNavigator().show(UserMenuController.class);
    }

    public void handleClose(ActionEvent actionEvent) {
        ClientApp.getNavigator().show(UserMenuController.class);
    }

    public void onRefreshClicked(ActionEvent actionEvent) {
        ClientApp.getNavigator().show(BuyMapScreenController.class);
    }
    //---------lazy popup screen no fxml----------------
    public  void showMapsPopup(
            ArrayList<MapSheet> maps,
            double otpPrice,
            double subscriptionPrice
    ) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Content in this City bundle");

        // Map list
        ListView<String> listView = new ListView<>();
        for (MapSheet sheet : maps) {
            listView.getItems().add(sheet.getName());
        }
        listView.setPrefHeight(250);

        // Buttons
        Button purchaseBtn = new Button("Purchase");
        Button subscribeBtn = new Button("Subscribe");
        Button closeBtn = new Button("Close");

        // Subscription duration selector (1–6 months)
        ComboBox<Integer> monthsBox = new ComboBox<>();
        monthsBox.getItems().addAll(1, 2, 3, 4, 5, 6);
        monthsBox.setValue(1); // default
        monthsBox.setPrefWidth(70);

        // Price labels
        Label otpLabel = new Label("One-time: ₪" + otpPrice);
        Label subLabel = new Label("Per month: ₪" + subscriptionPrice);

        // === Styling (same theme as before) ===
        listView.setStyle("""
        -fx-background-color: #2f2f2f;
        -fx-control-inner-background: #2f2f2f;
        -fx-text-fill: white;
    """);

        purchaseBtn.setStyle("""
        -fx-background-color: #3f8cff;
        -fx-text-fill: white;
        -fx-font-weight: bold;
        -fx-background-radius: 6;
        -fx-padding: 8 16;
    """);

        subscribeBtn.setStyle("""
        -fx-background-color: #4caf50;
        -fx-text-fill: white;
        -fx-font-weight: bold;
        -fx-background-radius: 6;
        -fx-padding: 8 16;
    """);

        closeBtn.setStyle("""
        -fx-background-color: #555555;
        -fx-text-fill: white;
        -fx-background-radius: 6;
        -fx-padding: 6 14;
    """);

        monthsBox.setStyle("""
        -fx-background-color: #0000FF;
        -fx-text-fill: white;
    """);

        otpLabel.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 11px;");
        subLabel.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 11px;");

        // === Actions ===
        purchaseBtn.setOnAction(e -> {
            BuyMapPayload payload=new BuyMapPayload(ClientApp.getCurrentUser().getId(),this.cityname,otpPrice,false,0);
            GcmRequest request  = new GcmRequest(RequestType.BUY_MAP, payload);
            client.sendRequest(request);

            System.out.println("Bought " + this.cityname + " For Total: ₪" + otpPrice);

            popup.close();
        });

        subscribeBtn.setOnAction(e -> {
            int months = monthsBox.getValue(); // 🔥 selected months (1–6)
            double totalPrice = months * subscriptionPrice;

            BuyMapPayload payload=new BuyMapPayload(ClientApp.getCurrentUser().getId(),this.cityname,totalPrice,true,months);
            GcmRequest request  = new GcmRequest(RequestType.BUY_MAP, payload);
            client.sendRequest(request);

            System.out.println("Subscribe for " + months + " months. Total: ₪" + totalPrice);

            popup.close();
        });

        closeBtn.setOnAction(e -> popup.close());

        // Layout
        VBox purchaseBox = new VBox(5, purchaseBtn, otpLabel);

        HBox subscribeRow = new HBox(5, subscribeBtn, monthsBox);
        subscribeRow.setStyle("-fx-alignment: center;");

        VBox subscribeBox = new VBox(5, subscribeRow, subLabel);

        HBox actionsBox = new HBox(30, purchaseBox, subscribeBox);
        actionsBox.setStyle("-fx-alignment: center;");

        VBox root = new VBox(15, listView, actionsBox, closeBtn);
        root.setStyle("""
        -fx-background-color: linear-gradient(to bottom, #1f1f1f, #2b2b2b);
        -fx-padding: 15;
    """);

        popup.setScene(new Scene(root, 420, 500));
        popup.showAndWait();
    }

}