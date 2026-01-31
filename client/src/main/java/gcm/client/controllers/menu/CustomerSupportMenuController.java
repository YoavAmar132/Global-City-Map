package gcm.client.controllers.menu;

import gcm.client.controllers.WelcomeController;
import gcm.client.controllers.customer_support_worker_util.CustomerSupportComplaintsController;
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
        String role = ClientApp.getCurrentUser().getRole();
        if(role.equals("CompanyManager")) {
            ClientApp.getNavigator().show(ManagerMenuController.class);
        }
        else if(role.equals("ContentManager")) {
            ClientApp.getNavigator().show(ContentManagerController.class);
        }
        else ClientApp.logout();
    }

    public void onSupportButton(ActionEvent actionEvent) {
        ClientApp.getNavigator().show(CustomerSupportComplaintsController.class);
    }
}
