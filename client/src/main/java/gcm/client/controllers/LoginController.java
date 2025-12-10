package gcm.client.controllers;
import gcm.client.controllers.menu.ContentWorkerMenuController;
import gcm.client.controllers.menu.CustomerSupportMenuController;
import gcm.client.controllers.menu.ManagerMenuController;
import gcm.client.controllers.menu.UserMenuController;
import gcm.client.network.GcmClient ;
import gcm.client.utill.ClientApp;
import common.messages.*;
import common.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

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
        }else {

            User user = (User) response.getData();
            System.out.println("Logged in as: " + user.getUsername() + " (" + user.getRole() + ")");
            switch (user.getRole()) {
                case "user":
                    ClientApp.getNavigator().show(UserMenuController.class);
                    break;

                case "content_worker":
                    ClientApp.getNavigator().show(ContentWorkerMenuController.class);
                    break;

                    case "content_manager":
                        ClientApp.getNavigator().show(ContentWorkerMenuController.class);
                        break;

                        case "manager":
                            ClientApp.getNavigator().show(ManagerMenuController.class);
                            break;

                            case "customer_support":
                                ClientApp.getNavigator().show(CustomerSupportMenuController.class);
                                break;


            }

        }

    }
    @FXML
    private void handleClose() {
        client.closeConnectionSafe();
        javafx.application.Platform.exit();
    }


}
