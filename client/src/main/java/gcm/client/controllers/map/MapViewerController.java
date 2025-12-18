package gcm.client.controllers.map;

import common.messages.GcmRequest;
import common.messages.GcmResponse;
import common.messages.RequestType;
import common.model.MapSheet;
import common.model.POI_Category;
import common.model.Poi;
import common.model.Route;
import gcm.client.network.GcmClient;
import gcm.client.utill.ClientApp;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

import java.util.Arrays;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;

public class MapViewerController {
    static int id=0;
    @FXML
    private StackPane stackPane;
    private List<Poi> pois = new ArrayList<>();
    private Route buildingRoute = null;
    private List<Route> routes = new ArrayList<>();
    private boolean buildingRouteWaitingFirstPoint = false;
    private int routeId = 0;
private String path="/Users/yahlio/IdeaProjects/Global-City-Map/client/src/main/resources/gcm/client/map/Tiels";


    // These are injected because of fx:id="baseLayer" / "overlayLayer" on the fx:include tags
    @FXML
    private MapBaseLayerController baseLayerController;

    @FXML
    private MapOverlayLayerController overlayLayerController;
    private GcmClient client;
    @FXML
    private void initialize() {
        client = ClientApp.getClient();
        client.setResponseHandler(this::handleResponse);
        baseLayerController.setTileRoot(path);
        overlayLayerController.setZoomSupplier(() -> baseLayerController.getZoom());


        overlayLayerController.setMapper((worldX, worldY) ->
                baseLayerController.mapToView(worldX, worldY)
        );

        // 2. Whenever the base layer view changes (pan / zoom), rerender overlay.
        //    → Requires a small setter in MapBaseLayerController (see below).
        baseLayerController.setOnViewChanged(() -> overlayLayerController.rerender());

        baseLayerController.setInteractionMode(MapBaseLayerController.InteractionMode.VIEW); // optional safety
        baseLayerController.setZoom(baseLayerController.getZoom());
        overlayLayerController.setOnEmptyMapClick((sx, sy) -> {
            baseLayerController.handleExternalClick(sx, sy);
        });
        overlayLayerController.setOnEmptyPress((sx, sy) ->
                baseLayerController.handleExternalPress(sx, sy, true)
        );

        overlayLayerController.setOnEmptyDrag((sx, sy) ->
                baseLayerController.handleExternalDrag(sx, sy, true)
        );

        overlayLayerController.setOnScroll(dy ->
                baseLayerController.handleExternalScroll(dy)
        );
        overlayLayerController.setOnEmptyRightClick(this::finishRouteMode);

        overlayLayerController.setOnPoiSelected((poi, node) -> {
            showPoiPopover(poi, node);
        });



        baseLayerController.recenterNow();


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
              //  overlayLayerController.addRoute(r);
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
            int currentZoom = baseLayerController.getZoom();

            double worldX = world[0];
            double worldY = world[1];

// convert current zoom coords -> BASE_ZOOM coords
            double scaleUp = Math.pow(2, Poi.BASE_ZOOM - currentZoom);
            double baseWorldX = worldX * scaleUp;
            double baseWorldY = worldY * scaleUp;
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

                Poi poi = new Poi(id, name, description, baseWorldX, baseWorldY, category);  id++;
                overlayLayerController.addPoi(poi);
                pois.add(poi);
                System.out.println("Created POI " + name + " at " + worldX + ", " + worldY);

            }finally {
                baseLayerController.setInteractionMode(MapBaseLayerController.InteractionMode.VIEW);
              //  baseLayerController.setOnPoiClick(null);
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
    int askVersionnum() {
        while (true) {

            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("New Map Version");
            dialog.setHeaderText("Enter Map Version");
            dialog.setContentText("Version:");

            Optional<String> result = dialog.showAndWait();

            // User clicked CANCEL
            if (result.isEmpty()) {
                return -1;
            }

            String text = result.get().trim();

            try {
                int version = Integer.parseInt(text);

                // optional validation
                if (version >= 0) {
                    return version;
                }

            } catch (NumberFormatException e) {
                // not a valid integer → loop again
            }

            // If we reach here, input was invalid → show dialog again
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


    @FXML
    public void onAddRouteMode(ActionEvent actionEvent) {
        System.out.println("Add Route mode");

        // start a new route
        buildingRoute = null;
        buildingRouteWaitingFirstPoint = true;

        baseLayerController.setInteractionMode(MapBaseLayerController.InteractionMode.ADD_POI);

        baseLayerController.setOnPoiClick(world -> {
            double worldX = world[0];
            double worldY = world[1];

            int currentZoom = baseLayerController.getZoom();
            double scaleUp = Math.pow(2, Poi.BASE_ZOOM - currentZoom);

            double baseWorldX = worldX * scaleUp;
            double baseWorldY = worldY * scaleUp;

            try {
                // FIRST point: ask metadata once
                if (buildingRouteWaitingFirstPoint) {
                    String name = askPoiName();
                    if (name == null) return;

                    String description = askPoiDescription();
                    if (description == null) return;

                    POI_Category category = askPoiCategory();
                    if (category == null) return;

                    // Create route object (adjust ctor to your Route model)
                    buildingRoute = new Route(routeId++, name, description, category,null);
                    buildingRouteWaitingFirstPoint = false;

                    // Add first point
                    buildingRoute.addBasePoint(baseWorldX, baseWorldY);
                     Poi start=new Poi(routeId,name,description,baseWorldX,baseWorldY,POI_Category.OTHER);
                    overlayLayerController.addPoi(start);

                    // Add to overlay immediately so user sees it grow
                    overlayLayerController.addRoute(buildingRoute);
                    routes.add(buildingRoute);
                    overlayLayerController.rerender();

                    System.out.println("Started route: " + name);
                    return;
                }

                // NEXT points: only coords
                if (buildingRoute != null) {
                    buildingRoute.addBasePoint(baseWorldX, baseWorldY);
                    overlayLayerController.rerender();
                    System.out.println("Added route point: " + baseWorldX + "," + baseWorldY);
                }

            } finally {
                // We do NOT exit route mode after each click.
                // We stay in ADD_POI mode so user can keep clicking points.
                // So: no reset here.
            }
        });

        // Optional: add a right-click to finish route
        baseLayerController.setOnRightClick(() -> finishRouteMode());
    }
    private void finishRouteMode() {
        System.out.println("Finish Route mode");

        buildingRoute = null;
        buildingRouteWaitingFirstPoint = false;

        baseLayerController.setInteractionMode(MapBaseLayerController.InteractionMode.VIEW);
        baseLayerController.setOnPoiClick(null);

        // If you added a right-click hook:
        baseLayerController.setOnRightClick(null);
    }
    private void showPoiPopover(Poi poi, Node anchor) {
        System.out.println("should pop");
        ContextMenu menu = new ContextMenu();

        MenuItem title = new MenuItem("Name: "+poi.getName());
        title.setDisable(true);

        MenuItem desc = new MenuItem("Description : "+poi.getDescription());
        desc.setDisable(true);

        MenuItem cat = new MenuItem("Category: " + poi.getCategory());
        cat.setDisable(true);

        MenuItem close = new MenuItem("Close");

        menu.getItems().addAll(title, desc, cat, new SeparatorMenuItem(), close);

        menu.show(anchor, Side.TOP, 0, -10);
    }


    private void handleResponse(GcmResponse response) {
        if (!response.isSuccess()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("pending Failed");
            alert.setContentText(response.getErrorMessage());
            alert.showAndWait();
        }else{
            System.out.println("sent succsesfuly");
        }

    }

    public void onSubmitMap(ActionEvent actionEvent) {
    int version=askVersionnum();
    String name = askPoiName();
    String description=askPoiDescription();


        MapSheet map =new MapSheet(version,name,description,path, (ArrayList) routes, (ArrayList) pois);
        GcmRequest request = new GcmRequest(RequestType.PEND_MAP, map);
        client.sendRequest(request);

    }
}
