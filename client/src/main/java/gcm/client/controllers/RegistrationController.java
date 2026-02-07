package gcm.client.controllers;

import gcm.client.network.GcmClient;
import gcm.client.utill.ClientApp;
import common.messages.*;
import common.model.User;
import gcm.client.utill.SceneNavigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;


public class RegistrationController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    private RegisterPayload payload;
    private boolean fillForm=false;
    public boolean getformstatus()
    {
        return this.fillForm;
    }
    public void setFillForm(boolean fillform)
    {
        this.fillForm=fillform;
    }

    public void setPayload(RegisterPayload payload) {
        this.payload = payload;
    }

    private GcmClient client;

    //network init
    @FXML
    public void initialize(RegisterPayload payload) {
        client = ClientApp.getClient();
        client.setResponseHandler(this::handleResponse);
        if(payload !=null)
        {
            setPayload(payload);
            setFillForm(true);
            usernameField.setText(payload.getUsername());
            passwordField.setText(payload.getPassword());
            confirmPasswordField.setText(payload.getPassword());
        }
    }



    public void onRegisterClicked() {
        if(!fillForm)
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(" Failed");
            alert.setContentText("please fill form first");
            alert.showAndWait();
            return;
        }

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
        payload.setUsername(username);
           payload.setPassword(password);

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

    public void onFillFormClicked(ActionEvent actionEvent) {
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
        SceneNavigator.LoadedView<FillFormController> view =
                ClientApp.getNavigator().get(FillFormController.class);
                   view.controller.setUsername(username);
                   view.controller.setPassword(password);
        ClientApp.getNavigator().showLoaded(view.root);


    }
}
