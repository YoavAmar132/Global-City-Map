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

public class ManagerMenuController {
    private GcmClient client;

    public void initialize() {
        client = ClientApp.getClient();

        // for now: handle all responses here (only LOGIN exists)
        client.setResponseHandler(this::handleResponse);
    }
    private void handleResponse(GcmResponse response) {}

    @FXML
    public void onPricingButton(ActionEvent actionEvent) {
    }
    @FXML
    public void onManageClientsButton(ActionEvent actionEvent) {
    }
    @FXML
    public void onContentMenuButton(ActionEvent actionEvent) {
        ClientApp.getNavigator().show(ContentWorkerMenuController.class);
    }
    @FXML
    public void onCustomerSupportMenuButton(ActionEvent actionEvent) {
        ClientApp.getNavigator().show(CustomerSupportMenuController.class);
    }
    @FXML
    private void handleClose() {
        client.closeConnectionSafe();
        javafx.application.Platform.exit();
    }
}
