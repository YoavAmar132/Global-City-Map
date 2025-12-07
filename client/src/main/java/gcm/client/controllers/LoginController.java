package gcm.client.controllers;
import gcm.client.network.GcmClient ;
import gcm.client.utill.ClientApp;
import gcm.client.network.GcmClient;
import common.messages.*;
import common.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private GcmClient client;

    @FXML
    public void initialize() {
        client = ClientApp.getClient();

        // for now: handle all responses here (only LOGIN exists)
        client.setResponseHandler(this::handleResponse);
    }

    @FXML
    private void onLoginClicked() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        LoginPayload payload = new LoginPayload(username, password);
        GcmRequest request = new GcmRequest(RequestType.LOGIN, payload);

        client.sendRequest(request);
    }

    private void handleResponse(GcmResponse response) {
        if (!response.isSuccess()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Login Failed");
            alert.showAndWait();
        }else {

            User user = (User) response.getData();
            System.out.println("Logged in as: " + user.getUsername() + " (" + user.getRole() + ")");
            ClientApp.getNavigator().show(WelcomeController.class);
        }

    }
    @FXML
    private void handleClose() {
        client.closeConnectionSafe();
        javafx.application.Platform.exit();
    }
}
