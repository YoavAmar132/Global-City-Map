
package main.java.com.LoginApp.controllers;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;

public class LoginController {

    @FXML
    private Text errorMessage;

    @FXML
    private AnchorPane loginScreenAnchorPane;

    @FXML
    private PasswordField password;

    @FXML
    private TextField username;

    @FXML
    void onLoginButtonClick(ActionEvent event) {

    }

}