package gcm.client.controllers.menu;
import gcm.client.controllers.map.MapViewerController;
import gcm.client.network.GcmClient ;
import gcm.client.utill.ClientApp;
import common.messages.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class ContentWorkerMenuController {
    private GcmClient client;

    public void initialize() {
        client = ClientApp.getClient();

        // for now: handle all responses here (only LOGIN exists)
        client.setResponseHandler(this::handleResponse);
    }
    private void handleResponse(GcmResponse response) {}
    @FXML
    public void handleClose(ActionEvent actionEvent) {
    }
    @FXML
    public void onViewCatalogClicked(ActionEvent actionEvent) {
    }
    @FXML
    public void onEditButton(ActionEvent actionEvent) {
        ClientApp.getNavigator().show(MapViewerController.class);
    }
    @FXML
    public void onReportsButton(ActionEvent actionEvent) {
    }
    @FXML
    public void onPricingButton(ActionEvent actionEvent) {
    }
}
