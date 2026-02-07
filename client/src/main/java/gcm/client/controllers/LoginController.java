package gcm.client.controllers;
import gcm.client.controllers.menu.*;
import gcm.client.network.GcmClient ;
import gcm.client.utill.ClientApp;
import common.messages.*;
import common.model.User;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import javax.lang.model.type.NullType;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;


    private GcmClient client;

    @FXML
    public void initialize() {
        client = ClientApp.getClient();

        // for now: handle all responses here (only LOGIN exists)
        client.setResponseHandler(this::handleResponse);
    }
    public void onRegisterClicked(ActionEvent actionEvent) {
        ClientApp.getNavigator().show(RegistrationController.class);
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
            alert.setContentText(response.getErrorMessage());
            alert.showAndWait();
            return;
        }
        Object data = response.getData();

        if(data instanceof User) {
            User user = (User) response.getData();
            ClientApp.setCurrentUser(user);          // store the logged-in user globally
            System.out.println("Logged in as: " + user.getUsername() + " (" + user.getRole() + ")");
            int x;
            try {
                x = Integer.parseInt(response.getErrorMessage());
            } catch (NumberFormatException e) {
                x = 0; // or handle error
            }
            Alert alert;
            switch (user.getRole()) {

                case "Customer":
                    if(x!=0)
                    {
                         alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Welcome "+user.getUsername());
                        alert.setContentText("You have "+x+" new messages");
                        alert.showAndWait();
                    }

                    ClientApp.getNavigator().show(UserMenuController.class);
                    break;
                case "ContentManager":
                    ClientApp.getNavigator().show(ContentManagerController.class);
                    break;
                case "Worker":
                    ClientApp.getNavigator().show(WorkerMenuController.class);
                    break;
                case "ContentEmployee":
                    ClientApp.getNavigator().show(ContentWorkerMenuController.class);
                    break;

                case "CustomerSupport":
                    ClientApp.getNavigator().show(CustomerSupportMenuController.class);
                    break;


                case "CompanyManager":
                    ClientApp.getNavigator().show(ManagerMenuController.class);
                    break;

                default:
                     alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Login Failed");
                    alert.setContentText("Unknown role: " + user.getRole());
                    alert.showAndWait();
            }
        }
        else
        {
            ClientApp.getNavigator().show(WelcomeController.class);
        }
    }

    @FXML
    private void handleClose() {
        GcmRequest request = new GcmRequest(RequestType.LOGOUT,ClientApp.getCurrentUser() );
        client.sendRequest(request);

    }



}
