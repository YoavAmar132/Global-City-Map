package gcm.client.controllers;

import gcm.client.network.GcmClient;
import gcm.client.utill.ClientApp;
import common.messages.*;
import common.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * overall very good just removed onFillEForm idk what that is xD
 */
public class RegistrationController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;


    private GcmClient client;

    //network init
    @FXML
    public void initialize() {
        client = ClientApp.getClient();
        client.setResponseHandler(this::handleResponse);
    }



    public void onRegisterClicked() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (!password.equals(confirmPassword)) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Registration Failed");
            alert.setContentText("Passwords do not match");
            alert.showAndWait();
            return;
        }

        LoginPayload payload = new LoginPayload(username, password);
        GcmRequest request = new GcmRequest(RequestType.REGISTER, payload);

        client.sendRequest(request);
    }

    private void handleResponse(GcmResponse response) {
        if (!response.isSuccess()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("registration Failed");
            alert.setContentText(response.getErrorMessage());
            alert.showAndWait();
            return;
        }
        User user = (User) response.getData();
        System.out.println("registered as: " + user.getUsername() + " (" + user.getRole() + ")");

        ClientApp.getNavigator().show(LoginController.class);

    }

    @FXML
    private void handleClose() {
        ClientApp.getNavigator().show(WelcomeController.class);
    }
}
