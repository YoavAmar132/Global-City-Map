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

public class ManagerMenuController {
    private GcmClient client;

    public void initialize() {
        client = ClientApp.getClient();

    }

    @FXML
    public void onPricingButton(ActionEvent actionEvent) {
        ClientApp.getNavigator().show(PendingCityPricesController.class);
    }
    @FXML
    public void onManageClientsButton(ActionEvent actionEvent) {
        SceneNavigator.LoadedView<ManageClientController> view =
                ClientApp.getNavigator().get(ManageClientController.class);
        view.controller.initialize(RequestType.LIST_ALL_USERS);
        ClientApp.getNavigator().showLoaded(view.root);

    }
    public void onManageWorkersButton(ActionEvent actionEvent) {
        SceneNavigator.LoadedView<ManageClientController> view =
                ClientApp.getNavigator().get(ManageClientController.class);
        view.controller.initialize(RequestType.LIST_ALL_WORKERS);
        ClientApp.getNavigator().showLoaded(view.root);
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
