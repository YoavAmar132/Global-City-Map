package gcm.client.controllers.map;

import common.messages.*;
import common.model.MapSheet;
import gcm.client.network.GcmClient;
import gcm.client.utill.ClientApp;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

public class PendingMapController {
    private GcmClient client;
    @FXML
    private VBox pendingList;
    @FXML
    private void initialize() {
        client = ClientApp.getClient();
        client.setResponseHandler(this::handleResponse);
        MaPayload maPayload=new MaPayload(0,"");
        GcmRequest request = new GcmRequest(RequestType.GET_PENDING_MAPS, maPayload);
        client.sendRequest(request);
    }

    private HBox createMapRow(MapSheet map) {
        Label name = new Label(map.getName());
        name.setStyle("-fx-text-fill: white; -fx-font-size: 14;");

        Button open = new Button("Open");
        open.setPrefSize(90, 30);
        open.setStyle("-fx-background-color: linear-gradient(to right, #00c6ff, #0072ff); " +
                "-fx-text-fill: white; -fx-font-size: 13; -fx-background-radius: 8; -fx-cursor: hand;");
        open.setOnAction(e -> openPendingMap(map)); // your method

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        HBox row = new HBox(12, name, spacer, open);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: rgba(255,255,255,0.14); -fx-background-radius: 12;");
        row.setPadding(new javafx.geometry.Insets(10, 12, 10, 12));

        return row;
    }
    public void openPendingMap(MapSheet map)
    {
        System.out.println("openes:"+map.getName());
    }
    private void handleResponse(GcmResponse response) {
        if (!response.isSuccess()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("loading all maps Failed");
            alert.setContentText(response.getErrorMessage());
            alert.showAndWait();
        }else{
            Object t =response.getData();
            List<MapSheet> pendingMaps=(List<MapSheet>)t;
            pendingList.getChildren().clear();
            for (MapSheet map : pendingMaps) {
                pendingList.getChildren().add(createMapRow(map));
            }

            System.out.println("loaded succsesfuly");
        }

    }

    public void onBackClicked(ActionEvent actionEvent) {
    }

    public void handleClose(ActionEvent actionEvent) {
    }

    public void onRefreshClicked(ActionEvent actionEvent) {
    }
}
