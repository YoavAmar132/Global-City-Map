package gcm.client.controllers.map;

import common.model.Poi;
import common.model.Route;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;

import java.util.List;

public class MapViewerController {

    @FXML
    private StackPane stackPane;

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
        int current = baseLayerController.getZoom();
        baseLayerController.setZoom(current + 1);
        overlayLayerController.rerender();
    }

    @FXML
    private void onZoomOut() {
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
}
