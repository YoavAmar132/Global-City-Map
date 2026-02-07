package gcm.client.controllers.customer_support_worker_util;

import common.messages.*;
import common.model.Complaint;
import gcm.client.controllers.user_util.ComplaintSession;
import gcm.client.network.GcmClient;
import gcm.client.utill.ClientApp;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;


public class ReviewComplaintController {
    private GcmClient client;
    private Complaint complaint;
    @FXML private Label complaintDescription;
    @FXML private TextArea responseText;

    public void initialize() {
        client = ClientApp.getClient();
        client.setResponseHandler(this::handleResponse);
        complaint = ComplaintSession.getInstance().getSelectedComplaint();
        complaintDescription.setText(complaint.getText());
    }


    @FXML
    private void handleClose() {
        ClientApp.getNavigator().show(CustomerSupportComplaintsController.class);
    }

    @FXML
    private void onSubmitResponseClicked(ActionEvent event) {
        System.out.println("sent response");
        complaint = ComplaintSession.getInstance().getSelectedComplaint();
        complaint.setResponse(responseText.getText());


        SubmitComplaintPayload payload = new SubmitComplaintPayload(complaint);
        GcmRequest request = new GcmRequest(RequestType.CLOSE_COMPLAINT_WITH_HUMAN_ANSWER, payload);
        client.sendRequest(request);

        ClientApp.getNavigator().show(CustomerSupportComplaintsController.class);
    }

    private void handleResponse(GcmResponse response) {
        Platform.runLater(() -> {
            if (!response.isSuccess()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setContentText(response.getErrorMessage());
                alert.showAndWait();
            } else {
                Object t = response.getData();
                if (t instanceof Popup) {
                    if((((Popup) t).isInList(ClientApp.getCurrentUser().getId())))
                    {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("");
                        alert.setContentText("you have a new message");
                        alert.showAndWait();
                    }
                }
                else if(t instanceof Message message) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle(message.getTitle());
                    alert.setContentText(message.getMessage());
                    alert.showAndWait();
                }
                else if (t instanceof Reload) {
                    Platform.runLater(() -> ClientApp.getNavigator().show(ReviewComplaintController.class));
                }
            }
        });
    }
}
