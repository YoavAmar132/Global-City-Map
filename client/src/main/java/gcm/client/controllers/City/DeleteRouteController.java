package gcm.client.controllers.City;

import common.messages.*;
import common.model.CityCatalogItem;
import common.model.RouteSheet;
import gcm.client.network.GcmClient;
import gcm.client.utill.ClientApp;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

public class DeleteRouteController {

    private CityCatalogItem city;
    private GcmClient client;

    @FXML
    private VBox listBox;


    public void setCity(CityCatalogItem city) {
        this.city = city;
        loadRoutes();
    }


    private void loadRoutes() {
        client = ClientApp.getClient();
        client.setResponseHandler(this::handleResponse);

        client.sendRequest(new GcmRequest(
                RequestType.GET_APPROVED_ROUTES_FOR_CITY,
                new CityIdPayload(city.getCityId())
        ));
    }


    private void handleResponse(GcmResponse res) {
        if (!res.isSuccess()) {
            showError(res.getErrorMessage());
            return;
        }

        if (!(res.getData() instanceof List<?> list)) return;

        listBox.getChildren().clear();

        for (Object o : list) {
            if (!(o instanceof RouteSheet route)) continue;
            listBox.getChildren().add(buildRow(route));
        }

        if (list.isEmpty()) {
            Label empty = new Label("No routes available for deletion.");
            empty.setStyle("-fx-text-fill: white; -fx-opacity: 0.8;");
            listBox.getChildren().add(empty);
        }
    }


    private HBox buildRow(RouteSheet route) {

        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 12, 10, 12));
        row.setStyle("""
            -fx-background-color: rgba(255,255,255,0.14);
            -fx-background-radius: 12;
        """);

        Label name = new Label(route.getRoute().getName());
        name.setStyle("-fx-text-fill: white; -fx-font-size: 14;");

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button delete = new Button("Delete");
        delete.setPrefWidth(90);
        delete.setStyle("""
            -fx-background-color: linear-gradient(to right, #ff416c, #ff4b2b);
            -fx-text-fill: white;
            -fx-font-size: 13;
            -fx-background-radius: 8;
            -fx-cursor: hand;
        """);

        delete.setOnAction(e -> confirmAndDelete(route));

        row.getChildren().addAll(name, spacer, delete);
        return row;
    }


    private void confirmAndDelete(RouteSheet route) {

        if (!confirm("Delete route: " + route.getRoute().getName() + "?"))
            return;

        client.sendRequest(new GcmRequest(
                RequestType.DELETE_ROUTE,
                new RouteIdPayload(route.getRouteId())
        ));

        loadRoutes();
    }


    @FXML
    private void onRefresh() {
        loadRoutes();
    }

    @FXML
    private void onBack() {
        ClientApp.getNavigator().show(DeleteContentMenuController.class);
    }


    private boolean confirm(String msg) {
        ButtonType deleteBtn = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.getButtonTypes().setAll(deleteBtn, cancelBtn);

        return alert.showAndWait().orElse(cancelBtn) == deleteBtn;
    }

    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).showAndWait();
    }
}
