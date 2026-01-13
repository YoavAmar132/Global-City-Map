package gcm.client.controllers.user_util;

import common.model.Complaint;
import gcm.client.controllers.WelcomeController;
import gcm.client.controllers.catalog.BuyMapCatalogController;
import gcm.client.controllers.catalogPublic.GuestCatalogController;
import gcm.client.controllers.menu.MessagesController;
import gcm.client.controllers.menu.UserMenuController;
import gcm.client.controllers.user_util.MyMapsController;
import gcm.client.network.GcmClient ;
import gcm.client.utill.ClientApp;
import common.messages.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;


public class CreateComplaintController {

    public TextArea complaintText;

    public Button submitComplaintButton;

    private GcmClient client;

    public void initialize() {
        client = ClientApp.getClient();
        client.setResponseHandler(this::handleResponse);
    }

    private void handleResponse(GcmResponse response) {
        System.out.println("called");
        Object t = response.getData();
        if (t instanceof Popup) {
            System.out.println("is popup");
            if((((Popup) t).isInList(ClientApp.getCurrentUser().getId())))
            {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("");
                alert.setContentText("you have a new message");
                alert.showAndWait();
            }
        }


    }


    @FXML
    public void handleClose(ActionEvent actionEvent) {
        ClientApp.getNavigator().show(UserMenuController.class);
    }


    @FXML
    private void onSubmitComplaintClicked(ActionEvent event) {
        System.out.println("sent complaint");
        Complaint complaint = new Complaint(
                ClientApp.getCurrentUser().getId(),
                complaintText.getText()
                );
        SubmitComplaintPayload payload = new SubmitComplaintPayload(complaint);
        GcmRequest request = new GcmRequest(RequestType.SUBMIT_COMPLAINT, payload);
        client.sendRequest(request);
    }

}