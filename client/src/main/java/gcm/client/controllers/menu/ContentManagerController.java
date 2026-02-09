package gcm.client.controllers.menu;

import gcm.client.controllers.City.PendingCityPricesController;
import gcm.client.controllers.WelcomeController;
import gcm.client.controllers.manager_util.ClientCardController;
import gcm.client.controllers.manager_util.ManageClientController;
import gcm.client.controllers.manager_util.ReportsInputController;
import gcm.client.controllers.map.EditPricesController;
import gcm.client.controllers.map.PendingMapController;
import gcm.client.controllers.map.PendingRouteController;
import gcm.client.network.GcmClient ;
import gcm.client.utill.ClientApp;
import common.messages.*;
import gcm.client.utill.SceneNavigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class ContentManagerController {
    private GcmClient client;

    public void initialize() {
        client = ClientApp.getClient();

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
    public void onReportsButton(ActionEvent actionEvent) {
        ClientApp.getNavigator().show(ReportsInputController.class);
    }

    @FXML
    public void onApprovePendingMap(ActionEvent actionEvent) {
        ClientApp.getNavigator().show(PendingMapController.class);
    }
    @FXML
    public void onApprovePendingRoute(ActionEvent actionEvent) {
        ClientApp.getNavigator().show(PendingRouteController.class);
    }

    @FXML
    public void onEditPrice(ActionEvent actionEvent) {
        ClientApp.getNavigator().show(EditPricesController.class);
    }

    @FXML
    public void onContentWorkerMenu(ActionEvent actionEvent) {
        ClientApp.getNavigator().show(ContentWorkerMenuController.class);
    }

    @FXML
    public void handleClose(ActionEvent actionEvent) {
        String role = ClientApp.getCurrentUser().getRole();
        if(role.equals("CompanyManager")) {
            ClientApp.getNavigator().show(ManagerMenuController.class);
        }
        else ClientApp.logout();
    }


    public void onContentMenuButton(ActionEvent actionEvent) {
    }
}
