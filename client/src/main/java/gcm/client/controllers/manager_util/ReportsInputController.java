package gcm.client.controllers.manager_util;

import common.messages.*;
import common.model.CityCatalogItem;
import common.model.CityReportData; // Make sure you have this from the previous step
import gcm.client.controllers.menu.ManagerMenuController;
import gcm.client.network.GcmClient;
import gcm.client.utill.ClientApp;
import gcm.client.utill.SceneNavigator; // Assuming you have this for navigation
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.util.List;

public class ReportsInputController {
    @FXML private DatePicker dateFrom;
    @FXML private DatePicker dateTo;
    @FXML private ComboBox<CityCatalogItem> cityCombo;

    private GcmClient client;

    @FXML
    void initialize() {
        this.client = ClientApp.getClient();
        client.setResponseHandler(this::handleResponse);

        // 1. Configure ComboBox to display City Names properly
        cityCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(CityCatalogItem item) {
                return item == null ? "" : item.getCityName();
            }

            @Override
            public CityCatalogItem fromString(String string) {
                return null; // Not needed for selection
            }
        });

        // 2. Request the list of cities from the server to populate the combo box
        client.sendRequest(new GcmRequest(RequestType.GET_CITY_CATALOG, new EmptyPayload()));
    }

    @FXML
    void onMakeReport() {
        LocalDate from = dateFrom.getValue();
        LocalDate to = dateTo.getValue();
        CityCatalogItem selectedCity = cityCombo.getValue();

        // Validation
        if(from == null || to == null || selectedCity == null) {
            showError("Please select a date range and a city.");
            return;
        }

        if(from.isAfter(to)) {
            showError("Start date cannot be after end date.");
            return;
        }

        // 3. Send the Report Request
        // Note: selectedCity.getCityId() will be 0 if "All Cities" is selected
        ReportPayload requestPayload = new ReportPayload(
                from,
                to,
                selectedCity.getCityId()
        );

        client.sendRequest(new GcmRequest(RequestType.GET_REPORT, requestPayload));
    }

    private void handleResponse(GcmResponse response) {
        Platform.runLater(() -> {
            if (!response.isSuccess()) {
                showError(response.getErrorMessage());
                return;
            }

            Object data = response.getData();

            // Scenario A: Server returned the City Catalog (for the ComboBox)
            // We check if the first item is a CityCatalogItem
            if (data instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof CityCatalogItem) {
                populateCityCombo((List<CityCatalogItem>) data);
            }

            // Scenario B: Server returned the Report Data (Result of "Make Report")
            // We check if the first item is a CityReportData
            else if (data instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof CityReportData) {
                openReportView((List<CityReportData>) data);
            }

            // Scenario C: Empty list (could be empty catalog or empty report)
            else if (data instanceof List<?> list && list.isEmpty()) {
                // Check if we were waiting for the combo box or the report
                // Logic here depends on state, but usually safe to ignore or show "No Data"
                if(cityCombo.getItems().isEmpty()) {
                    // It was likely the catalog request returning empty
                    showError("No cities found in database.");
                } else {
                    showError("No data found for this report configuration.");
                }
            }
        });
    }

    private void populateCityCombo(List<CityCatalogItem> cities) {
        cityCombo.getItems().clear();

        // Create a dummy "All Cities" item with ID 0
        // Adjust the constructor arguments (0, 0.0, etc) based on your CityCatalogItem constructor
        CityCatalogItem allCities = new CityCatalogItem(
                0, "All Cities", 0, 0, 0, 0
        );

        cityCombo.getItems().add(allCities);
        cityCombo.getItems().addAll(cities);

        // Select "All Cities" by default
        cityCombo.getSelectionModel().selectFirst();
    }

    private void openReportView(List<CityReportData> reportData) {
        // Navigate to the results screen and pass the data
        // You need to create 'ReportResultController' as discussed previously

        SceneNavigator.LoadedView<ReportResultController> view =
                ClientApp.getNavigator().get(ReportResultController.class);

        view.controller.setReportData(reportData); // Pass data to the chart
        ClientApp.getNavigator().showLoaded(view.root);
    }

    @FXML
    private void handleBack() {
        ClientApp.getNavigator().show(ManagerMenuController.class);
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

}