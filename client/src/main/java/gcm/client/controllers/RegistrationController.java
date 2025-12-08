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

public class RegistrationController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    private String Errormsg;
    private boolean filledform=false;
    private GcmClient client;
    //network init
    @FXML
    public void initialize() {
        client = ClientApp.getClient();

        // for now: handle all responses here (only LOGIN exists)
        client.setResponseHandler(this::handleResponse);
    }



    public void onFillEForm() {
    }


    public void onRegisterClicked() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        if((password.equals(confirmPassword))) {

            LoginPayload payload = new LoginPayload(username, password);
            GcmRequest request = new GcmRequest(RequestType.REGISTER, payload);

            client.sendRequest(request);
              return;
        }
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error:");
        alert.setContentText("passwords don't match");
        alert.showAndWait();
    }

    private void handleResponse(GcmResponse response) {
        if (!response.isSuccess()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("registration Failed");
            alert.setContentText(response.getErrorMessage());
            alert.showAndWait();
        }else {

            User user = (User) response.getData();
            System.out.println("registered as: " + user.getUsername() + " (" + user.getRole() + ")");
            ClientApp.getNavigator().show(LoginController.class);
        }

    }


    public void handleClose() {
        client.closeConnectionSafe();
        javafx.application.Platform.exit();
    }
}
