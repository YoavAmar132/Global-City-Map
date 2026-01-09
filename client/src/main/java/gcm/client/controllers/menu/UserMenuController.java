package gcm.client.controllers.menu;

import gcm.client.controllers.WelcomeController;
import gcm.client.controllers.catalog.BuyMapCatalogController;
import gcm.client.controllers.catalogPublic.GuestCatalogController;
import gcm.client.controllers.user_util.MyMapsController;
import gcm.client.network.GcmClient ;
import gcm.client.utill.ClientApp;
import common.messages.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert;
import java.util.ArrayList;


public class UserMenuController {
    private GcmClient client;

    public void initialize() {
        client = ClientApp.getClient();
        client.setResponseHandler(this::handleResponse);
    }

    private void handleResponse(GcmResponse response) {
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


    }


    @FXML
    private void handleClose(ActionEvent event) {
        ClientApp.logout();
    }


    @FXML
    private void onViewCatalogClicked(ActionEvent event) {
        ClientApp.getNavigator().show(GuestCatalogController.class);

    }

    @FXML
    private void onBuyMapClicked(ActionEvent event) {
        System.out.println("Buy a Map clicked");
        ClientApp.getNavigator().show(BuyMapCatalogController.class);
        // TODO: open purchase flow
    }

    @FXML
    private void onMyMapsClicked(ActionEvent event) {
        System.out.println("My Maps clicked");
        ClientApp.getNavigator().show(MyMapsController.class);
    }

    public void onMyMessagesClicked(ActionEvent actionEvent) {
        ClientApp.getNavigator().show(MessagesController.class);
    }
}