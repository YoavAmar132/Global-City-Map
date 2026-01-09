package gcm.client.controllers.manager_util;


import common.model.*;
import gcm.client.controllers.catalogPublic.GuestCatalogController;
import gcm.client.controllers.map.MapLoaderController;
import gcm.client.controllers.map.MapViewerController;
import gcm.client.controllers.map.UserMapViewerController;
import gcm.client.controllers.menu.ManagerMenuController;
import gcm.client.controllers.menu.UserMenuController;
import javafx.application.Platform;
import javafx.event.ActionEvent;

import common.messages.*;
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

public class ManageClientController {
    private GcmClient client;
    @FXML
    private VBox Baselist;

    private ArrayList<RegisterPayload> users; // loaded earlier
    public static ArrayList<String> history;
    private static  GcmRequest request;

    @FXML
    private void initialize() {
        client = ClientApp.getClient();
        client.setResponseHandler(this::handleResponse);


         request = new GcmRequest(RequestType.LIST_ALL_USERS,null);
        client.sendRequest(request);
    }


    private HBox createMapRow(RegisterPayload user) {
        Label name = new Label(user.getUsername());
        name.setStyle("-fx-text-fill: white; -fx-font-size: 14;");

        Button open = new Button("Open");
        open.setPrefSize(90, 30);
        open.setStyle("-fx-background-color: linear-gradient(to right, #00c6ff, #0072ff); -fx-text-fill: white; -fx-font-size: 13; -fx-background-radius: 8; -fx-cursor: hand;");
        open.setOnAction(e -> openCard(user));


        HBox spacer = new HBox();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        HBox row = new HBox(12, name, spacer, open);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: rgba(255,255,255,0.14); -fx-background-radius: 12;");
        row.setPadding(new javafx.geometry.Insets(10, 12, 10, 12));

        return row;
    }



    public void openCard(RegisterPayload user) {

         request = new GcmRequest(RequestType.LIST_USER_PURCHASES_HISTORY,user);
        client.sendRequest(request);


    }



    private void handleResponse(GcmResponse response) {
        if (!response.isSuccess()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("loading all Messages Failed");
            alert.setContentText(response.getErrorMessage());
            alert.showAndWait();
        }else {

            Object t = response.getData();
            if(t instanceof ArrayList<?>)
            {
                ArrayList<?> list = (ArrayList<?>) t;

                if (!list.isEmpty() && list.get(0) instanceof RegisterPayload) {
                    users = (ArrayList<RegisterPayload>) list;
                    Baselist.getChildren().clear();
                    for (RegisterPayload u  : users) {
                        Baselist.getChildren().add(createMapRow(u));
                    }
                    System.out.println("user list success");
                }
                if (!list.isEmpty() && list.get(0) instanceof String) {
                    this.history = (ArrayList<String>) list;
                    SceneNavigator.LoadedView<ClientCardController> view =
                            ClientApp.getNavigator().get(ClientCardController.class);

                    // set values BEFORE showing
                    Object s =request.getPayload();
                    if(s instanceof RegisterPayload)
                    {
                        RegisterPayload payload=(RegisterPayload)s;
                        view.controller.setClientInfo(payload);
                    }

                    view.controller.setPurchaseHistory(history);

                    // now show
                    ClientApp.getNavigator().showLoaded(view.root);
                }


            }

        }


    }


    public void onBackClicked(ActionEvent actionEvent) {
        ClientApp.getNavigator().show(ManagerMenuController.class);
    }

    public void handleClose(ActionEvent actionEvent) {
        ClientApp.getNavigator().show(ManagerMenuController.class);
    }

    public void onRefreshClicked(ActionEvent actionEvent) {
        Platform.runLater(() -> ClientApp.getNavigator().show(ManageClientController.class));
    }
}
