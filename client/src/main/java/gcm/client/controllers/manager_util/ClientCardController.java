package gcm.client.controllers.manager_util;

import common.messages.GcmRequest;
import common.messages.GcmResponse;
import common.messages.RegisterPayload;
import common.messages.RequestType;
import gcm.client.controllers.menu.MessagesController;
import gcm.client.controllers.menu.UserMenuController;
import gcm.client.network.GcmClient;
import gcm.client.utill.ClientApp;
import gcm.client.utill.SceneNavigator;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;

import java.util.ArrayList;

public class ClientCardController {

    /* ===== Client info ===== */
    @FXML private Label lblUsername;
    @FXML private Label lblFirstName;
    @FXML private Label lblSurname;
    @FXML private Label lblemail;
    @FXML private Label lblPhone;
    @FXML private Label lblRole;
          private RequestType type;
          private RegisterPayload payload;
    private RegisterPayload changed;
          private ArrayList<String> rows;
    private GcmClient client;


    /* ===== Purchase history ===== */
    @FXML private TableView<String> tblPurchases;
    @FXML private TableColumn<String, String> colPurchase;

    @FXML private Label statusLabel;

    /* ===== DATA SOURCE (ArrayList ONLY) ===== */
    private final ArrayList<String> purchaseHistory = new ArrayList<>();

    @FXML
    private void initialize() {
        // Display the string itself in each row
        colPurchase.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue())
        );

        tblPurchases.setPlaceholder(new Label("No purchases yet."));
        statusLabel.setText("");
    }
    private void handleResponse(GcmResponse response) {
        if (!response.isSuccess()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(" Failed to change info");
            alert.setContentText(response.getErrorMessage());
            alert.showAndWait();
            return;
        }else{
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("successes");
                alert.setContentText("Users information has changed");
                alert.showAndWait();
               setClientInfo(changed);
                payload=changed;
            }
        }



    public void setClientInfo(RegisterPayload p) {
        this.payload=p;
        lblUsername.setText(p.getUsername());
        lblFirstName.setText(p.getFirstname());
        lblSurname.setText(p.getLastname());
        lblPhone.setText("0"+p.getPhonenum());
        lblRole.setText(p.getRole());
        lblemail.setText(p.getEmail());
    }
    public void setType(RequestType type)
    {
        this.type=type;
    }

    /** Replace entire purchase history */
    public void setPurchaseHistory(ArrayList<String> rows) {
        this.rows=rows;
        purchaseHistory.clear();

        if (rows != null) {
            for (String r : rows) {
                if (r != null && !r.isBlank()) {
                    System.out.println(r);
                    purchaseHistory.add(r);
                }
            }
        }
        refreshTable();
    }



    /* ===================== UI Actions ===================== */
    private void refreshTable() {
        tblPurchases.setItems(
                FXCollections.observableArrayList(purchaseHistory)
        );
    }


    @FXML
    public void onBackClicked(ActionEvent actionEvent) {
        if(type==RequestType.GET_USER_BY_ID)
        {
            System.out.println("user type");
            Platform.runLater(() ->  ClientApp.getNavigator().show(UserMenuController.class));
            return;

        }

        SceneNavigator.LoadedView<ManageClientController> view =
                ClientApp.getNavigator().get(ManageClientController.class);
        view.controller.initialize(type);

        Platform.runLater(() ->  ClientApp.getNavigator().showLoaded(view.root));

    }

    public void handleClose(ActionEvent actionEvent) {
        onBackClicked(null);
    }

    public void onRefreshClicked(ActionEvent actionEvent) {
        SceneNavigator.LoadedView<ClientCardController> view =
                ClientApp.getNavigator().get(ClientCardController.class);
        view.controller.setType(this.type);
        view.controller.setPurchaseHistory(this.rows);
        view.controller.setClientInfo(payload);

        Platform.runLater(() ->  ClientApp.getNavigator().showLoaded(view.root));

    }


    public void onChangeInfo(ActionEvent actionEvent) {
setInfo(payload);

    }
    // lazy change info popup screen
    public void setInfo(RegisterPayload p) {

        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle("User Information");

        // --- Text fields ---
        TextField nameField = new TextField(p.getFirstname());
        TextField surnameField = new TextField(p.getLastname());
        TextField phoneField = new TextField(p.getPhonenum());
        TextField emailField = new TextField(p.getEmail());

        nameField.setPromptText("Name");
        surnameField.setPromptText("Surname");
        phoneField.setPromptText("Phone");
        emailField.setPromptText("Email");

        // Common style for text fields
        String textFieldStyle =
                "-fx-background-color: #1e293b;" +
                        "-fx-text-fill: white;" +
                        "-fx-prompt-text-fill: #94a3b8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-radius: 8;" +
                        "-fx-border-color: #334155;" +
                        "-fx-padding: 8;";

        nameField.setStyle(textFieldStyle);
        surnameField.setStyle(textFieldStyle);
        phoneField.setStyle(textFieldStyle);
        emailField.setStyle(textFieldStyle);

        // --- Labels ---
        Label nameLbl = new Label("Name:");
        Label surnameLbl = new Label("Surname:");
        Label phoneLbl = new Label("Phone:");
        Label emailLbl = new Label("Email:");

        List<Label> labels = List.of(nameLbl, surnameLbl, phoneLbl, emailLbl);
        labels.forEach(l ->
                l.setStyle("-fx-text-fill: #e5e7eb; -fx-font-size: 13px;")
        );

        // --- Submit button ---
        Button submitBtn = new Button("Submit");
        submitBtn.setStyle(
                "-fx-background-color: linear-gradient(#38bdf8, #2563eb);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 8 20 8 20;"
        );

        submitBtn.setOnAction(e -> {
            client = ClientApp.getClient();
            client.setResponseHandler(this::handleResponse);
            this.changed=new RegisterPayload(p.getUsername(),p.getPassword(),nameField.getText(),surnameField.getText(),phoneField.getText(),p.getCredit(),p.getPin());
            changed.setEmail(emailField.getText());
            GcmRequest request=new GcmRequest(RequestType.CHANGE_INFO,changed);
            popupStage.close();
            client.sendRequest(request);
        });

        // --- Layout ---
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setVgap(12);
        grid.setHgap(10);

        grid.add(nameLbl, 0, 0);
        grid.add(nameField, 1, 0);

        grid.add(surnameLbl, 0, 1);
        grid.add(surnameField, 1, 1);

        grid.add(phoneLbl, 0, 2);
        grid.add(phoneField, 1, 2);

        grid.add(emailLbl, 0, 3);
        grid.add(emailField, 1, 3);

        grid.add(submitBtn, 1, 4);

        // --- Root background (same vibe as your FXMLs) ---
        StackPane root = new StackPane(grid);
        root.setStyle(
                "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #0f172a, #1e293b);"
        );

        Scene scene = new Scene(root, 380, 280);
        popupStage.setScene(scene);
        popupStage.showAndWait();
    }


}
