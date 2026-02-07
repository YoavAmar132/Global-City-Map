package gcm.client.controllers.map;

import common.messages.*;
import common.model.City;
import common.model.Route;
import common.model.RouteSheet;
import gcm.client.controllers.catalog.ContentCatalogController;
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

import java.util.ArrayList;
import java.util.List;

public class RouteLoaderController {

    private GcmClient client;
    private City city;

    @FXML
    private VBox RouteList;

    @FXML
    private Label statusLabel;

    @FXML
    private void initialize() {
        // nothing yet
    }

    public void setCity(City city) {
        this.city = city;
        this.client = ClientApp.getClient();
        client.setResponseHandler(this::handleResponse);

        statusLabel.setText("Loading routes for " + city.getName() + "...");

        GcmRequest request = new GcmRequest(
                RequestType.GET_APPROVED_ROUTES_FOR_CITY,
                new CityIdPayload(city.getId())
        );

        client.sendRequest(request);
    }

    private HBox createRouteRow(RouteSheet sheet) {

        Label name = new Label(sheet.getRoute().getName());
        name.setStyle("-fx-text-fill: white; -fx-font-size: 14;");

        Button open = new Button("Open");
        open.setPrefSize(90, 30);
        open.setStyle(
                "-fx-background-color: linear-gradient(to right, #00c6ff, #0072ff);" +
                        "-fx-text-fill: white; -fx-font-size: 13; -fx-background-radius: 8;"
        );

        open.setOnAction(e -> openApprovedRoute(sheet));

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        HBox row = new HBox(12, name, spacer, open);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: rgba(255,255,255,0.14); -fx-background-radius: 12;");
        row.setPadding(new javafx.geometry.Insets(10, 12, 10, 12));

        return row;
    }


    private void openApprovedRoute(RouteSheet sheet) {

        System.out.println("Open route (EDIT): " +
                sheet.getRoute().getName());

        SceneNavigator.LoadedView<MapViewerController> view =
                ClientApp.getNavigator().get(MapViewerController.class);

        view.controller.setValsForEditRoute(sheet);
        ClientApp.getNavigator().showLoaded(view.root);
    }


    private void handleResponse(GcmResponse response) {

        if (!response.isSuccess()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Failed loading routes");
            alert.setContentText(response.getErrorMessage());
            alert.showAndWait();
            return;
        }

        Object data = response.getData();

        if (data instanceof ArrayList<?> list &&
                !list.isEmpty() &&
                list.get(0) instanceof RouteSheet) {

            @SuppressWarnings("unchecked")
            List<RouteSheet> routes = (List<RouteSheet>) data;

            RouteList.getChildren().clear();
            for (RouteSheet sheet : routes) {
                RouteList.getChildren().add(createRouteRow(sheet));
            }

            statusLabel.setText("Loaded " + routes.size() + " routes");
        }
    }


    public void onBackClicked(ActionEvent actionEvent) {
        ClientApp.getNavigator().show(ContentCatalogController.class);
    }

    public void handleClose(ActionEvent actionEvent) {
        GcmRequest request = new GcmRequest(
                RequestType.LOGOUT,
                ClientApp.getCurrentUser()
        );
        client.sendRequest(request);
    }

    public void onRefreshClicked(ActionEvent actionEvent) {
        setCity(city);
    }
}
