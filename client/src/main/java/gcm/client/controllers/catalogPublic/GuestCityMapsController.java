package gcm.client.controllers.catalogPublic;

import common.model.CityCatalogItem;
import common.model.MapCatalogItem;
import gcm.client.utill.ClientApp;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.List;

public class GuestCityMapsController {

    @FXML
    private Label titleLabel;

    @FXML
    private VBox mapList;

    @FXML
    private TextField searchField;


    private CityCatalogItem city;
    private List<MapCatalogItem> allMaps;

    public void setCity(CityCatalogItem city) {
        this.city = city;
        titleLabel.setText(city.getCityName() + " Maps");
    }

    public void setMaps(List<MapCatalogItem> maps) {
        this.allMaps = maps;
        renderMaps(maps);
    }

    private void renderMaps(List<MapCatalogItem> maps) {
        mapList.getChildren().clear();
        for (MapCatalogItem map : maps) {
            mapList.getChildren().add(createMapRow(map));
        }
    }

    @FXML
    private void handleSearch() {
        if (allMaps == null) return;

        String query = searchField.getText();
        if (query == null || query.isBlank()) {
            renderMaps(allMaps);
            return;
        }

        String q = query.toLowerCase();

        List<MapCatalogItem> filtered = allMaps.stream()
                .filter(m ->
                        (m.getMapName() != null && m.getMapName().toLowerCase().contains(q)) ||
                                (m.getDescription() != null && m.getDescription().toLowerCase().contains(q))
                )
                .toList();

        renderMaps(filtered);
    }



    private VBox createMapRow(MapCatalogItem map) {

        Label name = new Label(map.getMapName());
        name.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #1F2937;");

        Label desc = new Label(map.getDescription());
        desc.setStyle("-fx-text-fill: #4B5563;");


        VBox box = new VBox(6, name, desc);
        box.setStyle(
                "-fx-background-color: #F9FAFB;" +
                        "-fx-padding: 14;" +
                        "-fx-background-radius: 12;"
        );

        return box;
    }

    @FXML
    private void handleClose() {
        ClientApp.getNavigator().show(GuestCatalogController.class);
    }


}
