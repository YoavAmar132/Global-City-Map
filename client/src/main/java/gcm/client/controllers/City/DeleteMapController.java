package gcm.client.controllers.City;

import common.messages.*;
import common.model.CityCatalogItem;
import common.model.MapDeleteItem;
import gcm.client.network.GcmClient;
import gcm.client.utill.ClientApp;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.fxml.FXML;

import java.util.List;

public class DeleteMapController {

    private CityCatalogItem city;
    private GcmClient client;

    @FXML
    private VBox listBox;


    public void setCity(CityCatalogItem city) {
        this.city = city;
        loadMaps();
    }


    private void loadMaps() {
        client = ClientApp.getClient();
        client.setResponseHandler(this::handleResponse);

        client.sendRequest(new GcmRequest(
                RequestType.DELETE_GET_CITY_MAPS,
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
            if (!(o instanceof MapDeleteItem map)) continue;
            listBox.getChildren().add(buildRow(map));
        }

        if (list.isEmpty()) {
            Label empty = new Label("No maps available for deletion.");
            empty.setStyle("-fx-text-fill: white; -fx-opacity: 0.8;");
            listBox.getChildren().add(empty);
        }
    }


    private HBox buildRow(MapDeleteItem map) {

        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 12, 10, 12));
        row.setStyle("""
            -fx-background-color: rgba(255,255,255,0.14);
            -fx-background-radius: 12;
        """);

        Label name = new Label(map.getName());
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

        delete.setOnAction(e -> confirmAndDelete(map));

        row.getChildren().addAll(name, spacer, delete);
        return row;
    }


    private void confirmAndDelete(MapDeleteItem map) {

        if (!confirm("Delete map: " + map.getName() + "?")) return;

        client.sendRequest(new GcmRequest(
                RequestType.DELETE_MAP,
                new MapIdPayload(map.getMapId())
        ));

        // refresh list after delete
        loadMaps();
    }


    @FXML
    private void onRefresh() {
        loadMaps();
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
