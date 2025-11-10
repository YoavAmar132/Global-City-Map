package Lab2;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import java.io.IOException;

public class WelcomeController {

    @FXML
    private Label welcomeLabel;

    /**
     * Called by LoginController after successful login
     */
    public void setUserEmail(String email) {
        //welcomeLabel.setText("Welcome, " + email + "!");
        welcomeLabel.setText("Welcome!");//ill it like this and still send email so i have it if i need it in the future
    }

    @FXML
    private void onLogoutClick() {
        try {
            // Load the login screen again
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
            stage.show();
        } catch (IOException e) {
            //e.printStackTrace(); //no need
        }
    }
}



