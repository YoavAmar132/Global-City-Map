package gcm.client.controllers.map;

import common.messages.*;
import common.model.City;
import common.model.MapSheet;
import gcm.client.controllers.catalog.ContentCatalogController;
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

public class MapLoaderController {
    private GcmClient client;
    @FXML
    private VBox MapList;
    private boolean editMode = false;


    @FXML
    private void initialize() {

    }
    public void setVals(String Cityname)
    {
        client = ClientApp.getClient();
        client.setResponseHandler(this::handleResponse);
        MaPayload maPayload=new MaPayload(0,Cityname);
        GcmRequest request = new GcmRequest(RequestType.LIST_MAPS_FOR_CITY, maPayload);
        client.sendRequest(request);
    }

    public void setVals(String Cityname, boolean editMode) {
        this.editMode = editMode;
        setVals(Cityname); // reuse existing logic
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
        open.setOnAction(e -> openApprovedMap(map));

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        // ✅ add BOTH buttons
        HBox row = new HBox(12, name, spacer, open);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: rgba(255,255,255,0.14); -fx-background-radius: 12;");
        row.setPadding(new javafx.geometry.Insets(10, 12, 10, 12));

        return row;
    }


    public void openApprovedMap(MapSheet map) {

        if (!editMode) {
            // VIEW (your existing behavior)
            SceneNavigator.LoadedView<UserMapViewerController> view =
                    ClientApp.getNavigator().get(UserMapViewerController.class);

            view.controller.setVals(map);
            ClientApp.getNavigator().showLoaded(view.root);

            System.out.println("opened (VIEW): " + map.getName());
            return;
        }

        // EDIT (new behavior)
        SceneNavigator.LoadedView<MapViewerController> view =
                ClientApp.getNavigator().get(MapViewerController.class);

        view.controller.setValsForEdit(map);
        ClientApp.getNavigator().showLoaded(view.root);

        System.out.println("opened (EDIT): " + map.getName());
    }



    public void openPendingMap(MapSheet map) {
        SceneNavigator.LoadedView<UserMapViewerController> view =
                ClientApp.getNavigator().get(UserMapViewerController.class);

        // set values BEFORE showing
        view.controller.setVals(map);

        // now show
        ClientApp.getNavigator().showLoaded(view.root);

        System.out.println("opened: " + map.getName());
    }


    private void handleResponse(GcmResponse response) {
        if (!response.isSuccess()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("loading all maps for that City Failed");
            alert.setContentText(response.getErrorMessage());
            alert.showAndWait();
        }else {

            Object t = response.getData();
             if(t instanceof ArrayList<?>)
            {
                ArrayList<?> list = (ArrayList<?>) t;

                if (!list.isEmpty() && list.get(0) instanceof MapSheet) {
                    @SuppressWarnings("unchecked")
                    List<MapSheet> pendingMaps = (List<MapSheet>) t;
                    MapList.getChildren().clear();
                    for (MapSheet map : pendingMaps) {
                        MapList.getChildren().add(createMapRow(map));
                    }
                    System.out.println("loaded succsesfuly");

                }

            }


        }

    }

    public void onBackClicked(ActionEvent actionEvent) {
        ClientApp.getNavigator().show(ContentCatalogController.class);
    }

    public void handleClose(ActionEvent actionEvent) {
        GcmRequest request = new GcmRequest(RequestType.LOGOUT,ClientApp.getCurrentUser() );
        client.sendRequest(request);
    }

    public void onRefreshClicked(ActionEvent actionEvent) {
        ClientApp.getNavigator().show(MapLoaderController.class);
    }
}
