package gcm.client.controllers.menu;

import gcm.client.controllers.catalog.ContentCatalogController;
import gcm.client.controllers.manager_util.ReportsInputController;
import gcm.client.network.GcmClient;
import gcm.client.utill.ClientApp;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class WorkerMenuController {
    private GcmClient client;

    public void initialize() {
        client = ClientApp.getClient();

    }

    @FXML
    public void onViewCatalogClicked(ActionEvent actionEvent) {

        var view = ClientApp.getNavigator()
                .get(ContentCatalogController.class);

        view.controller.setOpenIntent(
                ContentCatalogController.OpenIntent.VIEW
        );

        ClientApp.getNavigator().showLoaded(view.root);
    }

    @FXML
    private void handleClose() {
        ClientApp.logout();
    }
}
