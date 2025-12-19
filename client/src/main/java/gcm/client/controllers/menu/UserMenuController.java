package gcm.client.controllers.menu;
import gcm.client.controllers.map.PendingMapController;
import gcm.client.controllers.map.UserMapViewerController;
import gcm.client.network.GcmClient ;
import gcm.client.utill.ClientApp;
import common.messages.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;


public class UserMenuController {
    private GcmClient client;

    public void initialize() {
        client = ClientApp.getClient();

        // for now: handle all responses here (only LOGIN exists)
        client.setResponseHandler(this::handleResponse);
    }
    private void handleResponse(GcmResponse response) {}

    @FXML
    private void handleClose(ActionEvent event) {

    }

    @FXML
    private void onViewCatalogClicked(ActionEvent event) {
        System.out.println("View Catalog clicked");
        // TODO: SceneNavigator to catalog
    }

    @FXML
    private void onBuyMapClicked(ActionEvent event) {
        System.out.println("Buy a Map clicked");
        // TODO: open purchase flow
    }

    @FXML
    private void onMyMapsClicked(ActionEvent event) {
        System.out.println("My Maps clicked");
        ClientApp.getNavigator().show(PendingMapController.class);
    }
}
