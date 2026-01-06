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

public class MapViewerController {
    private int poid=0;
    private int routid=0;
    @FXML
    private StackPane stackPane;
    private boolean submitInProgress = false;
    private List<Poi> pois = new ArrayList<>();
    private Route buildingRoute = null;
    private List<Route> routes = new ArrayList<>();
    private boolean buildingRouteWaitingFirstPoint = false;
    private int routeId = 0;
    private String path;
    private MapSheet map;
    private boolean initialized = false;
    private boolean hasVals = false;
    private boolean showDone = false;

    public int getPoid()
    {
        return this.poid;
    }
    public int getRoutid()
    {
        return this.routeId;
    }
    public void setPoid(int id)
    {
        this.poid=id;
    }
    public void setRoutid(int id)
    {
        this.routeId=id;
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
            if (!pois.contains(poi)) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Add");
                alert.setContentText("selected poi");
                alert.showAndWait();
                pois.add(poi);
            }else if(pois.contains(poi))
            {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("remove");
                alert.setContentText("deselected poi");
                alert.showAndWait();
                pois.remove(poi);
            }
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
        System.out.println("should show map");
        showMap(map);
        overlayLayerController.rerender();
        showDone = true;

        System.out.println("stackPane size=" + stackPane.getWidth() + "x" + stackPane.getHeight());

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

    /* ===========================
       Public API for the rest of app
       Called after you get data from server
       =========================== */

    /**
     * Show a map with given POIs and routes.
     * Assumes POI coordinates are in the same "world" coordinate system
     * that the base layer uses (e.g. tile pixels at current zoom).
     */


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

                Poi poi = new Poi(poid++, name, description, baseWorldX, baseWorldY, category);
                overlayLayerController.addPoi(poi);
                pois.add(poi);
                System.out.println("Created POI " + name + " at " + worldX + ", " + worldY);

            }finally {
                baseLayerController.setInteractionMode(MapBaseLayerController.InteractionMode.VIEW);
                //  baseLayerController.setOnPoiClick(null);
            }



        });
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
                    String name = askRouteName();
                    if (name == null) return;

                    String description = askRouteDescription();
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
            Object t =response.getData();
            if(t instanceof RouteIndexPayload indexPayload)
            {
                setRoutid(((RouteIndexPayload) t).getIndex()+1);
            }else if(t instanceof IndexPayload indexPayload)
            {
                setPoid(((IndexPayload)t).getIndex()+1);
            }
            System.out.println("sent succsesfuly");
        }

    }

    public void onSubmitMap(ActionEvent actionEvent) {
        if (submitInProgress) {
            System.out.println("tried to dupe");
            return;
        }
        submitInProgress = true;

        int version = askVersionNum();
        if (version < 0) { submitInProgress = false; return; }

        double price = askPrice();
        if (price < 0) { submitInProgress = false; return; }

        String name = askMapName();
        if (name == null) { submitInProgress = false; return; }

        String description = askMapDescription();
        if (description == null) { submitInProgress = false; return; }

        MapSheet map = new MapSheet(version, price, name, description, path,
                (ArrayList) routes, (ArrayList) pois);
 client=ClientApp.getClient();
        GcmRequest request = new GcmRequest(RequestType.PEND_MAP, map);
        client.sendRequest(request);
    }








    private int askNonNegativeInt(String title, String header, String label) {
        while (true) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle(title);
            dialog.setHeaderText(header);
            dialog.setContentText(label);

            Optional<String> result = dialog.showAndWait();

            if (result.isEmpty()) {
                return -1; // CANCEL
            }

            try {
                int value = Integer.parseInt(result.get().trim());
                if (value >= 0) {
                    return value;
                }
            } catch (NumberFormatException ignored) {}
        }
    }

    private double askNonNegativeDouble(String title, String header, String label) {
        while (true) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle(title);
            dialog.setHeaderText(header);
            dialog.setContentText(label);

            Optional<String> result = dialog.showAndWait();

            if (result.isEmpty()) {
                return -1; // CANCEL
            }

            try {
                double value = Double.parseDouble(result.get().trim());
                if (value >= 0) {
                    return value;
                }
            } catch (NumberFormatException ignored) {}
        }
    }


    private String askNonEmptyString(String title, String header, String label) {
        while (true) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle(title);
            dialog.setHeaderText(header);
            dialog.setContentText(label);

            Optional<String> result = dialog.showAndWait();

            if (result.isEmpty()) {
                return null; // CANCEL
            }

            String value = result.get().trim();
            if (!value.isEmpty()) {
                return value;
            }
        }
    }

    String askPoiName() {
        return askNonEmptyString("New POI", "Enter POI name", "Name:");
    }

    String askRouteName() {
        return askNonEmptyString("New Route", "Enter route name", "Name:");
    }

    String askMapName() {
        return askNonEmptyString("New Map", "Enter map name", "Name:");
    }

    String askPoiDescription() {
        return askNonEmptyString("New POI", "Enter POI description", "Description:");
    }

    String askRouteDescription() {
        return askNonEmptyString("New Route", "Enter route description", "Description:");
    }

    String askMapDescription() {
        return askNonEmptyString("New Map", "Enter map description", "Description:");
    }

    int askVersionNum() {
        return askNonNegativeInt("New Map Version", "Enter Map Version", "Version:");
    }


    double askPrice() {
        return askNonNegativeDouble("New Map Price", "Enter Map Price", "Price:");
    }

    public void showMap(MapSheet map) {
        overlayLayerController.clearAll();
        List<Poi> pois=map.getPois();
        List<Route> routes=map.getRoutes();
        if (pois != null) {
            for (Poi p : pois) {
                System.out.println(p.getName()+" x:"+p.getWorldX(13));
                overlayLayerController.addPoi(p);
            }
        }

        if (routes != null) {
            for (Route r : routes) {
                System.out.println("route id :"+r.getBasePoints());
                List<double[]> pts = r.getBasePoints();

                if (pts != null && !pts.isEmpty() && pts.get(0).length >= 2) {
                    double firstX = pts.get(0)[0];
                    double firstY = pts.get(0)[1];

                    Poi head = new Poi(r.getId(), r.getName(), r.getDescription(), firstX, firstY, r.getCategory());
                    //  overlayLayerController.addPoi(head);
                }
                overlayLayerController.addRoute(r);

                overlayLayerController.rerender();
            }
        }

        // Once all objects are added, ensure positions are correct
        overlayLayerController.rerender();
        System.out.println("rerenderddd");
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
                ClientApp.getNavigator().show(BaseMapSelectorController.class);
                break;


            case "CompanyManager":
                ClientApp.getNavigator().show(BaseMapSelectorController.class);
                break;
        }
    }


}