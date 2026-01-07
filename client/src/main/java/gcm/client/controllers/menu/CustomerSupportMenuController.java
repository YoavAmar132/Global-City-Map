package gcm.client.controllers.menu;

import gcm.client.controllers.WelcomeController;
import gcm.client.network.GcmClient ;
import gcm.client.utill.ClientApp;
import common.messages.*;
import javafx.event.ActionEvent;

public class CustomerSupportMenuController {
    private GcmClient client;

    public void initialize() {
        client = ClientApp.getClient();
    }

    public void handleClose(ActionEvent actionEvent) {
        ClientApp.logout();
    }

    public void onSupportButton(ActionEvent actionEvent) {

    }
}
