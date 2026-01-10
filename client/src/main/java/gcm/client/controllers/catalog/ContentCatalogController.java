package gcm.client.controllers.catalog;

import gcm.client.controllers.map.MapLoaderController;
import gcm.client.controllers.map.RouteLoaderController;
import gcm.client.controllers.map.UserMapViewerController;
import javafx.event.ActionEvent;

import common.messages.*;
import common.model.City;
import common.model.MapSheet;
import gcm.client.controllers.menu.ContentWorkerMenuController;
import gcm.client.network.GcmClient;
import gcm.client.utill.ClientApp;
import gcm.client.utill.SceneNavigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.ChoiceDialog;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

import java.util.List;

public class ContentCatalogController {


    public enum OpenIntent { VIEW, EDIT_MAP ,EDIT_ROUTE}
    private OpenIntent openIntent = OpenIntent.VIEW;

    private GcmClient client;
    @FXML
    private VBox Citylist;
    private ArrayList<City> cities; // loaded earlier

    @FXML
    private void initialize() {
        client = ClientApp.getClient();
        client.setResponseHandler(this::handleResponse);

        EmptyPayload payload=new EmptyPayload();
        GcmRequest request = new GcmRequest(RequestType.LIST_CITIES, payload);
        client.sendRequest(request);
    }


    public void setOpenIntent(OpenIntent intent) {
        this.openIntent = intent;
        System.out.println("ContentCatalog openIntent set to: " + intent);
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

        System.out.println("City clicked: " + city.getName()
                + " | openIntent = " + openIntent);


        if (openIntent == OpenIntent.EDIT_MAP) {

            SceneNavigator.LoadedView<MapLoaderController> view =
                    ClientApp.getNavigator().get(MapLoaderController.class);

            view.controller.setVals(city.getName(), true);
            ClientApp.getNavigator().showLoaded(view.root);
            return;
        }

        if (openIntent == OpenIntent.EDIT_ROUTE) {

            SceneNavigator.LoadedView<RouteLoaderController> view =
                    ClientApp.getNavigator().get(RouteLoaderController.class);

            view.controller.setCity(city);
            ClientApp.getNavigator().showLoaded(view.root);
            return;
        }

        SceneNavigator.LoadedView<MapLoaderController> view =
                ClientApp.getNavigator().get(MapLoaderController.class);

        view.controller.setVals(city.getName(), false);
        ClientApp.getNavigator().showLoaded(view.root);
    }



    private void handleResponse(GcmResponse response) {
        if (!response.isSuccess()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("loading all maps Failed");
            alert.setContentText(response.getErrorMessage());
            alert.showAndWait();
        }else {

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
            if(t instanceof ArrayList<?>)
            {
                ArrayList<?> list = (ArrayList<?>) t;

                if (!list.isEmpty() && list.get(0) instanceof City) {
                    @SuppressWarnings("unchecked")
                    ArrayList<City> cities = (ArrayList<City>) list;
                    setCities(cities);
                    Citylist.getChildren().clear();
                    for (City city  : cities) {
                        Citylist.getChildren().add(createMapRow(city));
                    }
                    System.out.println("City list success");
                }

                }

            }


        }


    public void onBackClicked(ActionEvent actionEvent) {
        ClientApp.getNavigator().show(ContentWorkerMenuController.class);
    }

    public void handleClose(ActionEvent actionEvent) {
        ClientApp.getNavigator().show(ContentWorkerMenuController.class);
    }

    public void onRefreshClicked(ActionEvent actionEvent) {
        ClientApp.getNavigator().show(gcm.client.controllers.map.PendingMapController.class);
    }
}
