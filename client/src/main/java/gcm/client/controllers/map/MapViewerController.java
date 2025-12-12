package gcm.client.controllers.map;

import common.model.POI_Category;
import common.model.Poi;
import common.model.Route;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.layout.StackPane;
import javafx.scene.control.TextInputDialog;

import java.util.Arrays;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;

public class MapViewerController {
    static int id=0;
    @FXML
    private StackPane stackPane;
    private List<Poi> pois = new ArrayList<>();

    // These are injected because of fx:id="baseLayer" / "overlayLayer" on the fx:include tags
    @FXML
    private MapBaseLayerController baseLayerController;

    @FXML
    private MapOverlayLayerController overlayLayerController;

    @FXML
    private void initialize() {
        baseLayerController.setTileRoot("C:/Users/Ayoav/IdeaProjects/Global_City_Map/Global_City_Map/client/src/main/resources/gcm/client/map/Tiels");


        overlayLayerController.setMapper((worldX, worldY) ->
                baseLayerController.mapToView(worldX, worldY)
        );

        // 2. Whenever the base layer view changes (pan / zoom), rerender overlay.
        //    → Requires a small setter in MapBaseLayerController (see below).
        baseLayerController.setOnViewChanged(() -> overlayLayerController.rerender());
    }

    /* ===========================
       Toolbar zoom button handlers
       =========================== */

    @FXML
    private void onZoomIn() {

        baseLayerController.setInteractionMode(MapBaseLayerController.InteractionMode.VIEW); // optional safety
        int current = baseLayerController.getZoom();
        baseLayerController.setZoom(current + 1);
        overlayLayerController.rerender();
    }

    @FXML
    private void onZoomOut() {
        baseLayerController.setInteractionMode(MapBaseLayerController.InteractionMode.VIEW); // optional safety
        int current = baseLayerController.getZoom();
        baseLayerController.setZoom(current - 1);
        overlayLayerController.rerender();
    }

    /* ===========================
       Public API for the rest of app
       Called after you get data from server
       =========================== */

    /**
     * Show a map with given POIs and routes.
     * Assumes POI coordinates are in the same "world" coordinate system
     * that the base layer uses (e.g. tile pixels at current zoom).
     */
    public void showCityMap(List<Poi> pois, List<Route> routes) {
        overlayLayerController.clearAll();

        if (pois != null) {
            for (Poi p : pois) {
                overlayLayerController.addPoi(p);
            }
        }

        if (routes != null) {
            for (Route r : routes) {
                overlayLayerController.addRoute(r);
            }
        }

        // Once all objects are added, ensure positions are correct
        overlayLayerController.rerender();
    }

    @FXML
    private void onAddPoiMode() {
        System.out.println("added poi");


          baseLayerController.setInteractionMode(MapBaseLayerController.InteractionMode.ADD_POI);

        baseLayerController.setOnPoiClick(world -> {

            double worldX = world[0];
            double worldY = world[1];
            System.out.printf("worldx:"+worldX+",worldy:"+worldY);
            // ask name
            try {
                String name = askPoiName();
                if (name == null) {
                    // user cancelled or closed
                    baseLayerController.setInteractionMode(MapBaseLayerController.InteractionMode.VIEW);
                    return;
                }
                //ask description
                String description = askPoiDescription();
                if (description == null) {
                    // user cancelled or closed
                    baseLayerController.setInteractionMode(MapBaseLayerController.InteractionMode.VIEW);
                    return;
                }
                //ask category
                POI_Category category = askPoiCategory();
                if (category == null) return;
                Poi poi = new Poi(id, name, description, worldX, worldY, category);
                id++;
                overlayLayerController.addPoi(poi);
                pois.add(poi);
                System.out.println("Created POI " + name + " at " + worldX + ", " + worldY);

            }finally {
                baseLayerController.setInteractionMode(MapBaseLayerController.InteractionMode.VIEW);
                baseLayerController.setOnPoiClick(null);
            }



        });
    }

    String askPoiName() {
        while (true) {

            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("New POI");
            dialog.setHeaderText("Enter POI name");
            dialog.setContentText("Name:");

            Optional<String> result = dialog.showAndWait();

            // User clicked CANCEL
            if (result.isEmpty()) {
                return null; // or return "" if you prefer
            }

            String name = result.get().trim();

            // If empty → ask again
            if (!name.isEmpty()) {
                return name; // valid! break the loop
            }

            // else loop again automatically
        }
    }
    String askPoiDescription() {
        while (true) {

            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("New POI");
            dialog.setHeaderText("Enter POI Description");
            dialog.setContentText("Description:");

            Optional<String> result = dialog.showAndWait();

            // User clicked CANCEL
            if (result.isEmpty()) {
                return null; // or return "" if you prefer
            }

            String Description = result.get().trim();

            // If empty → ask again
            if (!Description.isEmpty()) {
                return Description; // valid! break the loop
            }

            // else loop again automatically
        }
    }
    private POI_Category askPoiCategory() {

        List<POI_Category> choices = Arrays.asList(POI_Category.values());

        ChoiceDialog<POI_Category> dialog = new ChoiceDialog<>(choices.get(0), choices);
        dialog.setTitle("POI Category");
        dialog.setHeaderText("Select the POI Category");
        dialog.setContentText("Category:");

        Optional<POI_Category> result = dialog.showAndWait();

        return result.orElse(null);
    }


    public void onAddRouteMode(ActionEvent actionEvent) {
    }

    public void onViewMode(ActionEvent actionEvent) {
    }
}
