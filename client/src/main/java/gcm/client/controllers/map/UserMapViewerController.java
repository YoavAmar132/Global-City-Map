package gcm.client.controllers.map;

import common.messages.*;
import common.model.*;
import gcm.client.controllers.menu.ContentWorkerMenuController;
import gcm.client.controllers.menu.CustomerSupportMenuController;
import gcm.client.controllers.menu.ManagerMenuController;
import gcm.client.controllers.menu.UserMenuController;
import gcm.client.network.GcmClient;
import gcm.client.utill.ClientApp;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

import java.util.Arrays;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;

public class UserMapViewerController {
    private int poid=0;
    @FXML
    private StackPane stackPane;
    private String path;
    private MapSheet map;
    private boolean initialized = false;
    private boolean hasVals = false;
    private boolean showDone = false;


    public int getPoid()
    {
        return this.poid;
    }
    public void setPoid(int id)
    {
        this.poid=id;
    }
    public void setMap(MapSheet map)
    {
        this.map=map;
    }
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

        overlayLayerController.setOnPoiSelected((poi, node) -> {
            showPoiPopover(poi, node);
        });
        initialized = true;
        baseLayerController.recenterNow();
        stackPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                tryShowMap();
            }
        });
        if (stackPane.getScene() != null) {
            tryShowMap();
        }


    }
    public void setVals(MapSheet map)
    {
        this.path=map.getPath();
        setMap(map);
        hasVals = true;
        baseLayerController.setTileRoot(path);
        tryShowMap();
        // ask server for all approved routes of this city
        client.sendRequest(
                new GcmRequest(
                        RequestType.GET_APPROVED_ROUTES_FOR_CITY,
                        new CityIdPayload(map.getCityID())   // use your actual getter name
                )
        );

    }

    private void handleResponse(GcmResponse response) {
        Platform.runLater(() -> {

            if (!response.isSuccess()) {
                //System.out.println("ERROR: " + response.getErrorMessage());
                return;
            }
            Object data = response.getData();
            //System.out.println("HANDLE RESPONSE DATA = " + data);

            if (data instanceof List<?> list &&
                    !list.isEmpty() &&
                    list.get(0) instanceof RouteSheet) {

                //System.out.println("ROUTES RECEIVED: " + list.size());
                @SuppressWarnings("unchecked")
                List<RouteSheet> routes = (List<RouteSheet>) list;
                drawRouteConnections(routes);
            }
        });
    }

    private void drawRouteConnections(List<RouteSheet> approvedRoutes) {

        if (map == null) {
            System.out.println("MAP IS NULL");
            return;
        }
        if (approvedRoutes == null) return;

        System.out.println("=== DRAW ROUTE CONNECTIONS ===");
        System.out.println("MAP POIS:");
        for (Poi p : map.getPois()) {
            System.out.println("  MAP POI id=" + p.getId() + " name=" + p.getName());
        }

        if (approvedRoutes == null) return;

        // Set of POI ids that exist in the opened map
        java.util.Set<Integer> mapPoiIds = new java.util.HashSet<>();
        if (map.getPois() != null) {
            for (Poi p : map.getPois()) mapPoiIds.add(p.getId());
        }

        // clear previous lines (we will add this method next)
        overlayLayerController.clearRouteLines();

        for (RouteSheet rs : approvedRoutes) {
            List<Poi> stops = rs.getOrderedPois();
            if (stops == null || stops.size() < 2) continue;

            for (int i = 0; i < stops.size() - 1; i++) {
                Poi a = stops.get(i);
                Poi b = stops.get(i + 1);

                if (mapPoiIds.contains(a.getId()) && mapPoiIds.contains(b.getId())) {
                    overlayLayerController.addRouteLine(a, b);
                }
            }
        }

        overlayLayerController.rerender();
    }


    private void tryShowMap() {
        if (showDone) return;
        if (!initialized) return;
        if (!hasVals) return;
        if (map == null) return;

        Scene scene = stackPane.getScene();
        if (scene == null) return; // still not attached -> too early

        // Defer to next FX pulse (prevents “too early” crashes)
        Platform.runLater(() -> {
            if (showDone) return;
            if (stackPane.getScene() == null) return;

            baseLayerController.setTileRoot(path);
            showMap(map);
            System.out.println("stackPane size=" + stackPane.getWidth() + "x" + stackPane.getHeight());
            showDone = true;
        });
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




    public void setRouteVals(RouteSheet sheet) {

        // Load base map tiles
        baseLayerController.setTileRoot(sheet.getCityTilePath());

        // Clear overlay
        overlayLayerController.clearAll();

        // Defer POI rendering until JavaFX layout is ready
        Platform.runLater(() -> {

            List<Integer> orderedIds = new ArrayList<>();

            for (Poi p : sheet.getOrderedPois()) {
                overlayLayerController.addPoi(p);
                orderedIds.add(p.getId());
            }

            // Apply numbering (1..N)
            overlayLayerController.updateRouteNumbers(orderedIds);

            // Force render
            overlayLayerController.rerender();

            // Ensure everything is in view
            baseLayerController.recenterNow();
        });
        overlayLayerController.rerender();

    }








    /**
     * Show a map with given POIs and routes.
     * Assumes POI coordinates are in the same "world" coordinate system
     * that the base layer uses (e.g. tile pixels at current zoom).
     */
    public void showMap(MapSheet map) {
        overlayLayerController.clearAll();
        List<Poi> pois=map.getPois();
        if (pois != null) {
            for (Poi p : pois) {
                System.out.println(p.getName()+" x:"+p.getWorldX(13));
                overlayLayerController.addPoi(p);
            }
        }

        // Once all objects are added, ensure positions are correct
        overlayLayerController.rerender();
    }

    @FXML


    private void showPoiPopover(Poi poi, Node anchor) {
        System.out.println("should pop");
        ContextMenu menu = new ContextMenu();

        MenuItem title = new MenuItem("Name: "+poi.getName());
        title.setDisable(true);

        MenuItem desc = new MenuItem("Description : "+poi.getDescription());
        desc.setDisable(true);

        MenuItem cat = new MenuItem("Category: " + poi.getCategory());
        cat.setDisable(true);

        String is_accessible = poi.isAccessible() ? "Yes" : "No";
        MenuItem accessibility = new MenuItem("Accessible: " + is_accessible);
        accessibility.setDisable(true);

        MenuItem close = new MenuItem("Close");

        menu.getItems().addAll(title, desc, cat, accessibility, new SeparatorMenuItem(), close);

        menu.show(anchor, Side.TOP, 0, -10);
    }



    public void handleClose(ActionEvent actionEvent) {
        User current=ClientApp.getCurrentUser();
        switch (current.getRole())
        {
            case "Customer":
                ClientApp.getNavigator().show(UserMenuController.class);
                break;
            case "ContentManager":
            case "Worker":
            case "ContentEmployee":
                ClientApp.getNavigator().show(ContentWorkerMenuController.class);
                break;

            case "CustomerSupport":
                ClientApp.getNavigator().show(CustomerSupportMenuController.class);
                break;


            case "CompanyManager":
                ClientApp.getNavigator().show(ManagerMenuController.class);
                break;
        }


    }
}
