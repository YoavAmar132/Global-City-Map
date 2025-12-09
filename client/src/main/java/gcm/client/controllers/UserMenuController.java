package gcm.client.controllers;
import gcm.client.network.GcmClient ;
import gcm.client.utill.ClientApp;
import gcm.client.network.GcmClient;
import common.messages.*;
import common.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import javax.xml.validation.Validator;


public class UserMenuController {
    private GcmClient client;

    public void initialize() {
        client = ClientApp.getClient();

        // for now: handle all responses here (only LOGIN exists)
        client.setResponseHandler(this::handleResponse);
    }
    private void handleResponse(GcmResponse response) {}

    @FXML
    private void handleClose(ActionEvent event) {

    }

    @FXML
    private void onViewCatalogClicked(ActionEvent event) {
        System.out.println("View Catalog clicked");
        // TODO: SceneNavigator to catalog
    }

    @FXML
    private void onBuyMapClicked(ActionEvent event) {
        System.out.println("Buy a Map clicked");
        // TODO: open purchase flow
    }

    @FXML
    private void onMyMapsClicked(ActionEvent event) {
        System.out.println("My Maps clicked");
        // TODO: show user's maps
    }
}
