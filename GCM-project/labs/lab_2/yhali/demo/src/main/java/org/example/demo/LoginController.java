package org.example.demo;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private TextField passwordField;

    //a list of allowed
    private ArrayList<User> users = new ArrayList<>();

    @FXML
    public void initialize() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("usersOutput.txt"));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] user_args = line.split(" ");
                users.add(new User(user_args[0], user_args[1]));
            }
            reader.close();
        } catch (Exception e) {

        }
    }

    //get username and password from user and check if allowed
    @FXML
    public void handleLogin(){
        String user = usernameField.getText();
        String pass = passwordField.getText();

        boolean found = false;
        for(User u : users) {
            if(u.getUsername().equals(user) && u.getPassword().equals(pass)) {
                found = true;
                break;
            }
        }
        if (found){
            Stage stage = (Stage) usernameField.getScene().getWindow();
            try {
                FXMLSwitcher.switchScene("loginSuccessful.fxml");
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Error loading loginSuccessful.fxml");
                alert.show();
            }
        }
        else{
            Alert alert = new Alert(Alert.AlertType.ERROR, "Wrong username or password");
            alert.show();
        }


    }

    //close application (exit button)
    @FXML
    private void handleExit() {
        javafx.application.Platform.exit(); // closes JavaFX runtime
        System.exit(0);                      // ensures full JVM exit
    }
}
