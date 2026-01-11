package gcm.client.controllers;

import gcm.client.controllers.catalogPublic.GuestCatalogController;
import gcm.client.controllers.user_util.CatalogController;
import gcm.client.utill.ClientApp;
import javafx.fxml.FXML;

public class WelcomeController {

    @FXML
    private void onViewCatalog() {
        ClientApp.getNavigator().show(GuestCatalogController.class);
    }

    @FXML
    private void onLogin() {
        ClientApp.getNavigator().show(LoginController.class);
    }

    @FXML
    private void handleClose() {
        System.exit(0);
    }


}
