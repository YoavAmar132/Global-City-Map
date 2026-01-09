package gcm.client.controllers.manager_util;

import common.messages.RegisterPayload;
import common.messages.RequestType;
import gcm.client.utill.ClientApp;
import gcm.client.utill.SceneNavigator;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.ArrayList;

public class ClientCardController {

    /* ===== Client info ===== */
    @FXML private Label lblUsername;
    @FXML private Label lblFirstName;
    @FXML private Label lblSurname;
    @FXML private Label lblPhone;
    @FXML private Label lblRole;
          private RequestType type;


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


    public void setClientInfo(RegisterPayload p) {
        lblUsername.setText(p.getUsername());
        lblFirstName.setText(p.getFirstname());
        lblSurname.setText(p.getLastname());
        lblPhone.setText("0"+p.getPhonenum());
        lblRole.setText(p.getRole());
    }
    public void setType(RequestType type)
    {
        this.type=type;
    }

    /** Replace entire purchase history */
    public void setPurchaseHistory(ArrayList<String> rows) {
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
        SceneNavigator.LoadedView<ManageClientController> view =
                ClientApp.getNavigator().get(ManageClientController.class);
        view.controller.initialize(type);

        Platform.runLater(() ->  ClientApp.getNavigator().showLoaded(view.root));

    }

    public void handleClose(ActionEvent actionEvent) {
        onBackClicked(null);
    }

    public void onRefreshClicked(ActionEvent actionEvent) {

    }


}
