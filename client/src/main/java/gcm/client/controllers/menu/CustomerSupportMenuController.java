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

        // for now: handle all responses here (only LOGIN exists)
        client.setResponseHandler(this::handleResponse);
    }
    private void handleResponse(GcmResponse response) {
        ClientApp.getNavigator().show(WelcomeController.class);
    }

    public void handleClose(ActionEvent actionEvent) {
        GcmRequest request = new GcmRequest(RequestType.LOGOUT,ClientApp.getCurrentUser() );
        client.sendRequest(request);
    }

    public void onSupportButton(ActionEvent actionEvent) {

    }
}
