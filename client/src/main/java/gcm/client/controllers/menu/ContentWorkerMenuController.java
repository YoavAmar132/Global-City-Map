package gcm.client.controllers.menu;
import common.model.User;
import gcm.client.controllers.catalog.ContentCatalogController;
import gcm.client.controllers.map.BaseMapSelectorController;
import gcm.client.controllers.map.MapViewerController;
import gcm.client.controllers.map.PendingMapController;
import gcm.client.controllers.map.UserMapViewerController;
import gcm.client.network.GcmClient ;
import gcm.client.utill.ClientApp;
import common.messages.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;

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
        client.closeConnectionSafe();
        javafx.application.Platform.exit();
    }
    @FXML
    public void onViewCatalogClicked(ActionEvent actionEvent) {
        ClientApp.getNavigator().show(ContentCatalogController.class);
    }
    @FXML
    public void onEditButton(ActionEvent actionEvent) {
        ClientApp.getNavigator().show(BaseMapSelectorController.class);
    }
    @FXML
    public void onMapApproveButton(ActionEvent actionEvent) {
        User current=ClientApp.getCurrentUser();
        System.out.println(current.getRole());
        if (current.getRole().equals("ContentManager")|| current.getRole().equals("CompanyManager")) {
            ClientApp.getNavigator().show(PendingMapController.class);
            return;

        }else{
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Restriction Error");
            alert.setContentText("This Feature is not Accessible for your account");
            alert.showAndWait();

        }


    }
    @FXML
    public void onPricingButton(ActionEvent actionEvent) {
    }
}
