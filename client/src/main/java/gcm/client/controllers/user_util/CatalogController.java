package gcm.client.controllers.user_util;

import common.model.City;

import common.messages.*;
import gcm.client.network.GcmClient;
import gcm.client.utill.ClientApp;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CatalogController {

    private static final int MAPS_PER_PAGE = 40;
    private static final int MAPS_PER_ROW = 2;

    private GcmClient client;

    @FXML
    private Pagination pagination;

    @FXML
    private VBox catalogContainer;

    @FXML
    private TextField searchField;

    @FXML
    public void initialize() {

        client = ClientApp.getClient();
        client.setResponseHandler(this::handleResponse);

        pagination.setPageFactory(pageIndex -> {
            loadPage(pageIndex);
            return new VBox();
        });

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            pagination.setCurrentPageIndex(0);
            updatePagination();
        });

        updatePagination();
        loadPage(0);
    }

    private void updatePagination() {

        CitiesCountPayload payload = new CitiesCountPayload(searchField.getText());
        GcmRequest request = new GcmRequest(RequestType.GET_CITY_CATALOG, payload);
        client.sendRequest(request);

    }

    private void loadPage(int pageIndex) {
        int offset = pageIndex * MAPS_PER_PAGE;
        CityListPayload payload = new CityListPayload(offset,MAPS_PER_PAGE,searchField.getText());
        GcmRequest request = new GcmRequest(RequestType.LIST_CITIES, payload);
        client.sendRequest(request);

    }

    private Node createMapCard(City city) {
        try {
            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource("/gcm/client/user_util/city_card.fxml"));

            Node card = loader.load();
            CityCardController controller = loader.getController();
            controller.setCity(city);

            return card;
        } catch (IOException e) {
            e.printStackTrace();
            return new Label("Failed to load city");
        }
    }

    private void handleCitiesListResponse(GcmResponse response) {
        if (!response.isSuccess()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("loading all cities Failed");
            alert.setContentText(response.getErrorMessage());
            alert.showAndWait();
        }else {

            Object t = response.getData();
            if(t instanceof ArrayList<?>)
            {

                catalogContainer.getChildren().clear();

                List<City> citiesList = (ArrayList<City>) t;


                for (int i = 0; i < citiesList.size(); i += MAPS_PER_ROW) {
                    HBox row = new HBox(15);

                    for (int j = 0; j < MAPS_PER_ROW && i + j < citiesList.size(); j++) {
                        row.getChildren().add(createMapCard(citiesList.get(i + j)));
                    }

                    catalogContainer.getChildren().add(row);
                }

            }
        }
    }

    private void handleCitiesCountResponse(GcmResponse response) {
        if (!response.isSuccess()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("loading cities count Failed");
            alert.setContentText(response.getErrorMessage());
            alert.showAndWait();
        }else {

            Object t = response.getData();
            if(t instanceof Integer)
            {
               int  count = (Integer) t;

               int pages = count/MAPS_PER_PAGE;
               pagination.setPageCount(Math.max(pages, 1));

            }

        }
    }

    private void handleResponse(GcmResponse response) {
        Object t = response.getData();
        if(t instanceof ArrayList<?>){
            handleCitiesListResponse(response);
        }
        else{
            handleCitiesCountResponse(response);
        }
    }

    public void handleClose(ActionEvent actionEvent) {
        client.closeConnectionSafe();
        javafx.application.Platform.exit();
    }
}
