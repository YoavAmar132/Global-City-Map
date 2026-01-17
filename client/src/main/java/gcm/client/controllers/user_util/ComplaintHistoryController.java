package gcm.client.controllers.user_util;
import common.messages.*;

import common.model.Complaint;
import common.model.User;
import gcm.client.controllers.catalogPublic.GuestCatalogController;
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

    private HBox createMapRow(Complaint complaint) {

        // 2. תצוגת המחירים (רגיל + מנוי) - קטן יותר ובצבע בהיר אך שונה
        // אנו משתמשים ב-String.format כדי להציג 2 ספרות אחרי הנקודה



        //The complaint description
        String complaintText = complaint.getText();
        String complaintDescription = complaintText.length()<30 ? complaintText: complaintText.substring(0,50)+"...";
        Label description = new Label(complaintDescription);
        description.setStyle("-fx-text-fill: #dddddd; -fx-font-size: 12;");


        // 3. VBox שיחזיק את השם והמחיר אחד מתחת לשני
        VBox infoBox = new VBox(4, description);
        infoBox.setAlignment(Pos.CENTER_LEFT);

        // 4. כפתור הקנייה
        Button buyBtn = new Button("View complaint");
        buyBtn.setPrefSize(90, 34);
        buyBtn.setStyle(
                "-fx-background-color: linear-gradient(to right, #11998e, #38ef7d); " +
                        "-fx-text-fill: white; -fx-font-size: 13; -fx-font-weight: bold; " +
                        "-fx-background-radius: 8; -fx-cursor: hand;"
        );
        buyBtn.setOnAction(e -> navigateToViewComplaint(complaint));

        // 5. מרווח גמיש לדחיפת הכפתור שמאלה
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // 6. הרכבת השורה כולה
        HBox row = new HBox(12, infoBox, spacer, buyBtn);
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
        //ClientApp.getNavigator().show(ViewComplaintController.class);
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
                    // בדיקה שהרשימה לא ריקה ושהאיבר הראשון הוא אכן City
                    if (!list.isEmpty() && list.get(0) instanceof Complaint) {
                        complaintList.getChildren().clear();
                        for (Object o : list) {
                            complaintList.getChildren().add(createMapRow((Complaint) o));
                        }
                        statusLabel.setText("Select a complaint to view.");
                    } else if (list.isEmpty()) {
                        // טיפול במצב שאין פניות בכלל
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