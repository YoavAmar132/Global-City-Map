package gcm.client.controllers.menu;
import common.model.User;
import gcm.client.controllers.City.DeleteContentMenuController;
import gcm.client.controllers.City.PendingCityPricesController;
import gcm.client.controllers.WelcomeController;
import gcm.client.controllers.catalog.ContentCatalogController;
import gcm.client.controllers.map.*;
import gcm.client.network.GcmClient ;
import gcm.client.utill.ClientApp;
import common.messages.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;

import java.util.Optional;


public class ContentWorkerMenuController {
    private GcmClient client;

    public void initialize() {
        client = ClientApp.getClient();

    }
    @FXML
    public void handleClose(ActionEvent actionEvent) {
        ClientApp.logout();
    }

    @FXML
    public void onViewCatalogClicked(ActionEvent actionEvent) {

        var view = ClientApp.getNavigator()
                .get(ContentCatalogController.class);

        view.controller.setOpenIntent(
                ContentCatalogController.OpenIntent.VIEW
        );

        ClientApp.getNavigator().showLoaded(view.root);
    }

    @FXML
    public void onEditExistingMap(ActionEvent actionEvent) {

        var view = ClientApp.getNavigator()
                .get(ContentCatalogController.class);

        view.controller.setOpenIntent(
                ContentCatalogController.OpenIntent.EDIT_MAP
        );

        ClientApp.getNavigator().showLoaded(view.root);
    }

    @FXML
    public void onEditExistingRoute(ActionEvent actionEvent) {

        var view = ClientApp.getNavigator()
                .get(ContentCatalogController.class);

        view.controller.setOpenIntent(
                ContentCatalogController.OpenIntent.EDIT_ROUTE
        );

        ClientApp.getNavigator().showLoaded(view.root);
    }



    @FXML
    public void onCreateButton(ActionEvent actionEvent) {
        ClientApp.getNavigator().show(BaseMapSelectorController.class);
    }

    @FXML
    public void onDeleteButton(ActionEvent actionEvent) {
        ClientApp.getNavigator().show(DeleteContentMenuController.class);
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
    public void onRouteApproveButton(ActionEvent actionEvent) {
        User current = ClientApp.getCurrentUser();

        if (current.getRole().equals("ContentManager") ||
                current.getRole().equals("CompanyManager")) {

            ClientApp.getNavigator().show(PendingRouteController.class);
            return;
        }

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Restriction Error");
        alert.setContentText("This Feature is not Accessible for your account");
        alert.showAndWait();
    }

    @FXML
    public void onPriceApproveButton(ActionEvent actionEvent) {
        ClientApp.getNavigator().show(PendingCityPricesController.class);
    }



    @FXML
    public void onEditPricesButton(ActionEvent actionEvent) {
        ClientApp.getNavigator().show(EditPricesController.class);
    }

    /* lazy af sorry */
    @FXML
    public void onCreateCityButton(ActionEvent actionEvent) {
        String cityName = askNonEmptyString(
                "Add City",
                "Create New City",
                "City name:"
        );

        if (cityName == null) return;

        String baseMapPath = askNonEmptyString(
                "Add City",
                "Create New City",
                "Base map path:"
        );

        if (baseMapPath == null) return;

        String cityDescription = askNonEmptyString(
                "Add City",
                "Create New City",
                "City description:"
        );

        if (cityDescription == null) return;

        CreateCityPayload payload =
                new CreateCityPayload(cityName, baseMapPath,cityDescription);

        GcmClient client = ClientApp.getClient();
        client.setResponseHandler(this::handleCreateCityResponse);

        client.sendRequest(
                new GcmRequest(RequestType.CREATE_CITY, payload)
        );
    }
    private void handleCreateCityResponse(GcmResponse response) {

        Platform.runLater(() -> {

            if (!response.isSuccess()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Create City Failed");
                alert.setHeaderText(null);
                alert.setContentText(response.getErrorMessage());
                alert.showAndWait();
                return;
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("City Created");
            alert.setHeaderText(null);
            alert.setContentText("City was created successfully.");
            alert.showAndWait();
        });
    }

    private String askNonEmptyString(String title, String header, String label) {

        while (true) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle(title);
            dialog.setHeaderText(header);
            dialog.setContentText(label);

            Optional<String> result = dialog.showAndWait();

            if (result.isEmpty()) {
                return null; // cancel
            }

            String value = result.get().trim();
            if (!value.isEmpty()) {
                return value;
            }
        }
    }



}
