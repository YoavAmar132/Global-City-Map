package gcm.client.controllers.menu;

import gcm.client.controllers.City.PendingCityPricesController;
import gcm.client.controllers.WelcomeController;
import gcm.client.controllers.manager_util.ClientCardController;
import gcm.client.controllers.manager_util.ManageClientController;
import gcm.client.controllers.manager_util.ReportsInputController;
import gcm.client.controllers.map.EditPricesController;
import gcm.client.network.GcmClient ;
import gcm.client.utill.ClientApp;
import common.messages.*;
import gcm.client.utill.SceneNavigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
//TODO:make a class,similarly to purchaseSession called browsingMenuSession which will save the menu we need to go back to when pressing x or return
public class ManagerMenuController {
    private GcmClient client;

    public void initialize() {
        client = ClientApp.getClient();

    }

    @FXML
    public void onContentManagerMenuButton(ActionEvent actionEvent) {
        ClientApp.getNavigator().show(ContentManagerController.class);
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
    public void onPricingButton(ActionEvent actionEvent) {
        ClientApp.getNavigator().show(PendingCityPricesController.class);
    }
    @FXML
    private void handleClose() {
        ClientApp.logout();
    }


}
