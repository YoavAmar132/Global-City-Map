package gcm.client.controllers.user_util;
import common.messages.*;
import common.model.Complaint;

import gcm.client.network.GcmClient;
import gcm.client.utill.ClientApp;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;


public class ViewComplaintController {
    private GcmClient client;
    private Complaint complaint;
    @FXML private Label complaintDescription;
    @FXML private VBox responseBox;

    public void initialize() {
        client = ClientApp.getClient();
        client.setResponseHandler(this::handleResponse);

        complaint = ComplaintSession.getInstance().getSelectedComplaint();
        complaintDescription.setText(complaint.getText());
        GcmRequest request = new GcmRequest(RequestType.GET_COMPLAINT, new ComplaintIdPayload(complaint.getId()));
        client.sendRequest(request);

        updateUI(complaint);
    }


    public void onComplaintUpdated(Complaint updatedComplaint) {
        if (updatedComplaint.getId() != complaint.getId()) return;

        complaint.setStatus(updatedComplaint.getStatus());
        complaint.setResponse(updatedComplaint.getResponse());
        complaint.setResponseBy(updatedComplaint.getResponseBy());
        updateUI(complaint);
    }

    private void updateUI(Complaint complaint) {
        if (complaint == null) return;

        responseBox.getChildren().clear();

        Label title = new Label();
        title.setStyle("-fx-font-size: 13; -fx-text-fill: white; -fx-font-weight: bold;");

        Label content = new Label();
        content.setWrapText(true);
        content.setStyle("-fx-font-size: 12; -fx-text-fill: white;");

        switch (complaint.getStatus()) {

            case OPEN -> {
                title.setText("Status:");
                content.setText("The complaint has just been sent.");
                responseBox.getChildren().addAll(title, content);
            }

            case WAITING_FOR_BOT,IN_PROGRESS -> {
                title.setText("Status:");
                content.setText("Waiting for the bot response...");
                responseBox.getChildren().addAll(title, content);
            }

            case WAITING_FOR_HUMAN -> {
                title.setText("Status:");
                content.setText(
                        "Waiting for a response from customer service.\n" +
                                "Please be patient, response might take up to a couple of days."
                );
                responseBox.getChildren().addAll(title, content);
            }

            case CLOSED -> {
                title.setText("Response:");
                content.setText(
                        complaint.getResponse() != null
                                ? complaint.getResponse()
                                : "No response was provided."
                );
                responseBox.getChildren().addAll(title, content);
            }
        }
    }

    public void handleClose() {
        ClientApp.getNavigator().show(ComplaintHistoryController.class);
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
                else if (t instanceof UpdateComplaint updatedComplaint) {
                    Complaint complaint = updatedComplaint.getComplaint();
                    onComplaintUpdated(complaint);

                }
                else if(t instanceof Complaint updatedComplaint) {
                    complaint = updatedComplaint;
                    ComplaintSession.getInstance().setSelectedComplaint(updatedComplaint);
                    onComplaintUpdated(updatedComplaint);
                }

                else if (t instanceof Reload) {
                    Platform.runLater(() -> ClientApp.getNavigator().show(ViewComplaintController.class));
                }
            }
        });
    }
}
