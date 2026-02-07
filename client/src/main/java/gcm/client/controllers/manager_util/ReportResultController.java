package gcm.client.controllers.manager_util;

import common.messages.*;
import common.model.CityCatalogItem;
import common.model.CityReportData; // Make sure you have this from the previous step
import gcm.client.network.GcmClient;
import gcm.client.utill.ClientApp;
import gcm.client.utill.SceneNavigator; // Assuming you have this for navigation
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.util.List;


public class ReportResultController {

    @FXML
    private BarChart<String, Number> reportChart;

    @FXML
    private CategoryAxis xAxis;

    @FXML
    private NumberAxis yAxis;

    /**
     * This method is called by the previous screen (ReportsInputController)
     * to pass the data received from the server.
     */
    public void setReportData(List<CityReportData> reportDataList) {
        // 1. Clear any existing data in the chart
        reportChart.getData().clear();

        // 2. Loop through each city report and create a "Series" for it
        for (CityReportData data : reportDataList) {

            // Create a new series (a set of bars with the same color/legend entry)
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(data.getCityName()); // This will appear in the legend

            // 3. Add the data points (Bars)
            // Format: new XYChart.Data<>("Category Name", Value)

            series.getData().add(new XYChart.Data<>("Maps", data.getNumMaps()));
            series.getData().add(new XYChart.Data<>("Purchases", data.getNumPurchases()));
            series.getData().add(new XYChart.Data<>("Subscribers", data.getNumSubscriptions()));
            //series.getData().add(new XYChart.Data<>("Renewals", data.getNumRenewals())); // Optional if you added this field
            series.getData().add(new XYChart.Data<>("Views", data.getNumViews()));
            series.getData().add(new XYChart.Data<>("Downloads", data.getNumDownloads())); // Optional if you added this field

            // 4. Add the series to the chart
            reportChart.getData().add(series);
        }
    }

    @FXML
    private void handleBack() {
        // Return to the previous screen (ReportsInputController)
        ClientApp.getNavigator().show(ReportsInputController.class);
    }
}
