package gcm.client.controllers.menu;

import gcm.client.controllers.WelcomeController;
// Import the new Reports Controller
import gcm.client.controllers.manager_util.ReportsInputController;
import gcm.client.network.GcmClient;
import gcm.client.utill.ClientApp;
import common.messages.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class ManagerMenuController {
    private GcmClient client;

    public void initialize() {
        client = ClientApp.getClient();
        client.setResponseHandler(this::handleResponse);
    }

    private void handleResponse(GcmResponse response) {
        // After logout, go back to welcome screen
        ClientApp.getNavigator().show(WelcomeController.class);
    }

    @FXML
    public void onPricingButton(ActionEvent actionEvent) {
        // TODO: Navigate to pricing approval screen if needed
    }

    @FXML
    public void onManageClientsButton(ActionEvent actionEvent) {
        // TODO: Navigate to client management screen if needed
    }

    /**
     * NEW: Button to open the Reports Generation Screen
     */
    @FXML
    public void onReportsButton(ActionEvent actionEvent) {
        ClientApp.getNavigator().show(ReportsInputController.class);
    }

    @FXML
    public void onContentMenuButton(ActionEvent actionEvent) {
        ClientApp.getNavigator().show(ContentWorkerMenuController.class);
    }

    @FXML
    public void onCustomerSupportMenuButton(ActionEvent actionEvent) {
        ClientApp.getNavigator().show(CustomerSupportMenuController.class);
    }

    @FXML
    private void handleClose() {
        GcmRequest request = new GcmRequest(RequestType.LOGOUT, ClientApp.getCurrentUser());
        client.sendRequest(request);
    }
}