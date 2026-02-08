package gcm.client.controllers.user_util;
import common.messages.*;

import common.model.Complaint;
import gcm.client.controllers.menu.UserMenuController;
import gcm.client.network.GcmClient;
import gcm.client.utill.ClientApp;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;

public class ComplaintHistoryController {
    private GcmClient client;
    private static final int DESCRIPTION_LENGTH = 50;
    @FXML
    private VBox complaintList;
    @FXML private Label statusLabel;

    @FXML
    private void initialize() {
        client = ClientApp.getClient();
        client.setResponseHandler(this::handleResponse);
        loadComplaints();
    }

    private void loadComplaints() {
        GcmRequest request = new GcmRequest(RequestType.LIST_USER_COMPLAINTS, ClientApp.getCurrentUser());
        client.sendRequest(request);
    }

    private HBox createComplaintRow(Complaint complaint) {

        //The complaint description
        String complaintText = complaint.getText();
        String complaintDescription = complaintText.length()< DESCRIPTION_LENGTH ? complaintText: complaintText.substring(0,DESCRIPTION_LENGTH)+"...";
        Label description = new Label(complaintDescription);
        description.setStyle("-fx-text-fill: #dddddd; -fx-font-size: 12;");


        // VBox to hold the description
        VBox infoBox = new VBox(4, description);
        infoBox.setAlignment(Pos.CENTER_LEFT);

        // Button to view complaint
        Button viewBtn = new Button("View complaint");
        viewBtn.setPrefSize(120, 34);
        viewBtn.setStyle(
                "-fx-background-color: linear-gradient(to right, #11998e, #38ef7d); " +
                        "-fx-text-fill: white; -fx-font-size: 13; -fx-font-weight: bold; " +
                        "-fx-background-radius: 8; -fx-cursor: hand;"
        );
        viewBtn.setOnAction(e -> navigateToViewComplaint(complaint));

        //adding space for the button
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        //assembling the entire row
        HBox row = new HBox(12, infoBox, spacer, viewBtn);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: rgba(255,255,255,0.14); -fx-background-radius: 12;");
        row.setPadding(new javafx.geometry.Insets(10, 15, 10, 15));

        return row;
    }

    private void navigateToViewComplaint(Complaint complaint) {
        // 1. Clear old session data
        ComplaintSession.getInstance().clear();

        // 2. Set the new city in the singleton
        ComplaintSession.getInstance().setSelectedComplaint(complaint);
        ComplaintSession.getInstance().setUserID(ClientApp.getCurrentUser().getId());

        // 3. Navigate
        System.out.println("should show complaint");
        ClientApp.getNavigator().show(ViewComplaintController.class);
    }

    private void handleResponse(GcmResponse response) {
        Platform.runLater(() -> {
            if (!response.isSuccess()) {
                statusLabel.setText("Error loading complaints.");
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
                if (t instanceof ArrayList<?>) {
                    ArrayList<?> list = (ArrayList<?>) t;
                    if (!list.isEmpty() && list.get(0) instanceof Complaint) {
                        //list is a list of complaints
                        complaintList.getChildren().clear();
                        for (Object o : list) {
                            complaintList.getChildren().add(0,createComplaintRow((Complaint) o));
                        }
                        statusLabel.setText("Select a complaint to view.");
                    } else if (list.isEmpty()) {
                        // dealing with cases where there are no complaints
                        complaintList.getChildren().clear();
                        statusLabel.setText("No complaints available.");
                    }
                }

                else if (t instanceof Reload) {
                    Platform.runLater(() -> ClientApp.getNavigator().show(ComplaintHistoryController.class));
                }
            }
        });
    }

    public void onCreateComplaintClicked(ActionEvent actionEvent) {
        ClientApp.getNavigator().show(CreateComplaintController.class);
    }


    public void handleClose(ActionEvent actionEvent) {
        ClientApp.getNavigator().show(UserMenuController.class);
    }
}