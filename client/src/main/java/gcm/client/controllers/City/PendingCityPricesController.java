package gcm.client.controllers.City;

import common.messages.*;
import common.model.PendingCityPriceItem;
import gcm.client.controllers.menu.ManagerMenuController;
import gcm.client.network.GcmClient;
import gcm.client.utill.ClientApp;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.TableCell;

import javafx.scene.control.Button;
import javafx.scene.layout.HBox;


import java.util.List;

public class PendingCityPricesController {

    @FXML private TableView<PendingCityPriceItem> table;
    @FXML private TableColumn<PendingCityPriceItem, String> cityCol;
    @FXML private TableColumn<PendingCityPriceItem, Double> cityPriceCol;
    @FXML private TableColumn<PendingCityPriceItem, Double> subPriceCol;
    @FXML private TableColumn<PendingCityPriceItem, Void> actionCol;

    private GcmClient client;

    @FXML
    private void initialize() {

        cityCol.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getCityName())
        );

        cityPriceCol.setCellValueFactory(c ->
                new SimpleDoubleProperty(c.getValue().getCityPrice()).asObject());

        subPriceCol.setCellValueFactory(c ->
                new SimpleDoubleProperty(c.getValue().getSubPrice()).asObject());

        actionCol.setCellFactory(
                (TableColumn<PendingCityPriceItem, Void> col) ->
                        new TableCell<PendingCityPriceItem, Void>() {

                            private final Button approve = new Button("Approve");
                            private final Button reject = new Button("Reject");

                            {
                                approve.setOnAction(e -> {
                                    PendingCityPriceItem item =
                                            getTableView().getItems().get(getIndex());
                                    send(RequestType.APPROVE_CITY_PRICE, item.getCityId());
                                });

                                reject.setOnAction(e -> {
                                    PendingCityPriceItem item =
                                            getTableView().getItems().get(getIndex());
                                    send(RequestType.REJECT_CITY_PRICE, item.getCityId());
                                });
                            }

                            @Override
                            protected void updateItem(Void item, boolean empty) {
                                super.updateItem(item, empty);
                                if (empty) {
                                    setGraphic(null);
                                } else {
                                    setGraphic(new HBox(10, approve, reject));
                                }
                            }
                        }
        );


        client = ClientApp.getClient();
        client.setResponseHandler(this::handleResponse);
        refresh();
    }

    private void refresh() {
        client.sendRequest(
                new GcmRequest(
                        RequestType.GET_PENDING_CITY_PRICES,
                        new EmptyPayload()
                )
        );
    }

    private void send(RequestType type, int cityId) {
        client.sendRequest(
                new GcmRequest(type, new CityIdPayload(cityId))
        );
    }

    private void handleResponse(GcmResponse response) {
        if (!response.isSuccess()) return;

        if (response.getData() instanceof List<?> list) {
            Platform.runLater(() ->
                    table.getItems().setAll(
                            (List<PendingCityPriceItem>) list
                    )
            );
        } else {
            refresh();
        }
    }

    @FXML
    private void handleClose() {
        ClientApp.getNavigator().show(ManagerMenuController.class);
    }

}
