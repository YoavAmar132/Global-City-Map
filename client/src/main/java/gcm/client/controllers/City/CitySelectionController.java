package gcm.client.controllers.City;

import common.messages.EmptyPayload;
import common.messages.GcmRequest;
import common.messages.GcmResponse;
import common.messages.RequestType;
import common.model.CityCatalogItem;
import gcm.client.controllers.menu.ContentWorkerMenuController;
import gcm.client.controllers.menu.ManagerMenuController;
import gcm.client.network.GcmClient;
import gcm.client.utill.ClientApp;
import gcm.client.utill.SceneNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

import java.util.List;

public class CitySelectionController {

    private DeleteContentMenuController.DeleteTarget deleteTarget;
    private GcmClient client;

    @FXML
    private VBox cityList;

    public void setDeleteTarget(DeleteContentMenuController.DeleteTarget target) {
        this.deleteTarget = target;
        loadCities();
    }

    private void loadCities() {
        client = ClientApp.getClient();
        client.setResponseHandler(this::handleResponse);

        client.sendRequest(new GcmRequest(
                RequestType.GET_CITY_CATALOG,
                new EmptyPayload()
        ));
    }

    private void handleResponse(GcmResponse response) {
        if (!response.isSuccess()) {
            showError(response.getErrorMessage());
            return;
        }

        if (response.getData() instanceof List<?> list) {
            cityList.getChildren().clear();

            for (Object o : list) {
                if (!(o instanceof CityCatalogItem city)) continue;

                Button btn = new Button(city.getCityName());
                btn.setMaxWidth(Double.MAX_VALUE);
                btn.setOnAction(e -> openDeleteList(city));

                cityList.getChildren().add(btn);
            }
        }
    }

    private void openDeleteList(CityCatalogItem city) {
        switch (deleteTarget) {

            case POI -> {
                SceneNavigator.LoadedView<DeletePoiController> view =
                        ClientApp.getNavigator().get(DeletePoiController.class);

                view.controller.setCity(city);
                ClientApp.getNavigator().showLoaded(view.root);
            }

            case MAP -> {
                SceneNavigator.LoadedView<DeleteMapController> view =
                        ClientApp.getNavigator().get(DeleteMapController.class);

                view.controller.setCity(city);
                ClientApp.getNavigator().showLoaded(view.root);
            }

            case ROUTE -> {
                SceneNavigator.LoadedView<DeleteRouteController> view =
                        ClientApp.getNavigator().get(DeleteRouteController.class);

                view.controller.setCity(city);
                ClientApp.getNavigator().showLoaded(view.root);
            }
        }
    }


    @FXML
    private void onBack() {
        ClientApp.getNavigator().show(DeleteContentMenuController.class);
    }

    @FXML
    private void handleClose() {
        ClientApp.getNavigator().show(DeleteContentMenuController.class);
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg);
        a.showAndWait();
    }
}

