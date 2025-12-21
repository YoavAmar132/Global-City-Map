package gcm.client.controllers.menu;

import common.model.User;
import gcm.client.controllers.WelcomeController;
import gcm.client.utill.ClientApp;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class CatalogController {

    @FXML
    private Label modeLabel;

    @FXML
    public void initialize() {
        User user = ClientApp.getCurrentUser();
        if (user == null) {
            modeLabel.setText("Guest Mode");
        } else {
            modeLabel.setText("Logged in as: " + user.getUsername());
        }
    }

    @FXML
    private void handleClose() {
        ClientApp.getNavigator().show(WelcomeController.class);
    }
}
