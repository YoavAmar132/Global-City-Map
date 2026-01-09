package gcm.client.controllers.menu;

import gcm.client.controllers.WelcomeController;
import gcm.client.controllers.manager_util.ManageClientController;
import gcm.client.controllers.manager_util.ReportsInputController;
import gcm.client.network.GcmClient ;
import gcm.client.utill.ClientApp;
import common.messages.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class ManagerMenuController {
    private GcmClient client;

    public void initialize() {
        client = ClientApp.getClient();

    }

    @FXML
    public void onPricingButton(ActionEvent actionEvent) {
    }
    @FXML
    public void onManageClientsButton(ActionEvent actionEvent) {
        ClientApp.getNavigator().show(ManageClientController.class);

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
    public void onReportsButton(ActionEvent actionEvent) {
        ClientApp.getNavigator().show(ReportsInputController.class);
    }
    @FXML
    private void handleClose() {
        ClientApp.logout();
    }

}
