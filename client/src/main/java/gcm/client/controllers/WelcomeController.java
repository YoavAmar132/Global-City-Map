package gcm.client.controllers;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class WelcomeController {
    @FXML
    private Label welcomeText;

    @FXML
    private void handleClose() {
        javafx.application.Platform.exit();
    }

}
