package gcm.client.controllers.menu;

import gcm.client.network.GcmClient ;
import gcm.client.utill.ClientApp;
import common.messages.*;
import javafx.event.ActionEvent;

public class CustomerSupportMenuController {
    private GcmClient client;

    public void initialize() {
        client = ClientApp.getClient();

        // for now: handle all responses here (only LOGIN exists)
        client.setResponseHandler(this::handleResponse);
    }
    private void handleResponse(GcmResponse response) {}
    public void handleClose(ActionEvent actionEvent) {
    }

    public void onSupportButton(ActionEvent actionEvent) {

    }
}
