package gcm.client.controllers.map;

import common.messages.*;
import common.model.MapSheet;
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
        open.setStyle(
                "-fx-background-color: linear-gradient(to right, #00c6ff, #0072ff); " +
                        "-fx-text-fill: white; -fx-font-size: 13; -fx-background-radius: 8; -fx-cursor: hand;"
        );
        open.setOnAction(e -> openPendingMap(map));

        Button approve = new Button("Approve");
        approve.setPrefSize(90, 30);
        approve.setStyle(
                "-fx-background-color: rgba(255,255,255,0.20); " +   // different look (optional)
                        "-fx-text-fill: white; -fx-font-size: 13; -fx-background-radius: 8; -fx-cursor: hand;"
        );
        approve.setOnAction(e -> ApproveMap(map)); // make sure method name matches exactly

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        // ✅ add BOTH buttons
        HBox row = new HBox(12, name, spacer, open, approve);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: rgba(255,255,255,0.14); -fx-background-radius: 12;");
        row.setPadding(new javafx.geometry.Insets(10, 12, 10, 12));

        return row;
    }

    private void ApproveMap(MapSheet map) {
        System.out.println("approved");
    }

    public void openPendingMap(MapSheet map) {
        SceneNavigator.LoadedView<UserMapViewerController> view =
                ClientApp.getNavigator().get(UserMapViewerController.class);

        // set values BEFORE showing
        view.controller.setVals(map.getVersion(), map.getName(), map.getPath());

        // now show
        ClientApp.getNavigator().showLoaded(view.root);

        System.out.println("opened: " + map.getName());
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
