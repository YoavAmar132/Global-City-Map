package org.example.lab_2.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.example.lab_2.UserApp;
import org.example.lab_2.WindowApplication;

public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;

    @FXML
    private void handleLogin() {
        System.out.println("accepted");
        String username = usernameField.getText();
        String password = passwordField.getText();
        boolean state= UserApp.ConfirmUser(username, password);
        System.out.println(state);
       if(state==true)
       {
           WindowApplication.getNavigator().show(WelcomeController.class);
       }
       else {
           Alert alert = new Alert(Alert.AlertType.ERROR);
           alert.setTitle("Login Failed");
           alert.setHeaderText("Invalid username or password");
           alert.setContentText("Please check your credentials and try again.");
           alert.showAndWait();
       }


    }
    @FXML
    private void handleClose() {
        javafx.application.Platform.exit();
    }

}


