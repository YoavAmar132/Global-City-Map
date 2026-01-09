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
    @FXML
    private StackPane stackPane;
    private boolean submitInProgress = false;
    private String path;
    private MapSheet map;
    private boolean initialized = false;
    private boolean hasVals = false;
    private boolean showDone = false;
    private final List<Integer> selectedPoiIds = new ArrayList<>();
    private int tempPoiId = -1;


    private enum Mode { VIEW, ADD_POI, ADD_ROUTE }
    private Mode mode = Mode.VIEW;







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
        overlayLayerController.setZoomSupplier(() -> baseLayerController.getZoom());


        overlayLayerController.setMapper((worldX, worldY) ->
                baseLayerController.mapToView(worldX, worldY)
        );

        // 2. Whenever the base layer view changes (pan / zoom), rerender overlay.
        //    → Requires a small setter in MapBaseLayerController (see below).
        baseLayerController.setOnViewChanged(() -> overlayLayerController.rerender());

        baseLayerController.setInteractionMode(MapBaseLayerController.InteractionMode.VIEW);
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

        overlayLayerController.setOnPoiSelected(this::handlePoiSelection);
        overlayLayerController.setOnPoiInfo(this::showPoiPopover);


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

    public void setVals(MapSheet map) {
        this.path = map.getPath();
        setMap(map);
        hasVals = true;

        mode = Mode.VIEW;

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
        mode = Mode.ADD_POI;
        System.out.println("ADD_POI mode ON");

        baseLayerController.setInteractionMode(
                MapBaseLayerController.InteractionMode.ADD_POI
        );

        baseLayerController.setOnPoiClick(world -> {

            int currentZoom = baseLayerController.getZoom();

            double worldX = world[0];
            double worldY = world[1];

            double scaleUp = Math.pow(2, Poi.BASE_ZOOM - currentZoom);
            double baseWorldX = worldX * scaleUp;
            double baseWorldY = worldY * scaleUp;

            String name = askPoiName();
            if (name == null) return;

            String description = askPoiDescription();
            if (description == null) return;

            POI_Category category = askPoiCategory();
            if (category == null) return;

            boolean isAccessible = askPoiAccessibility();

            tempPoiId--;
            Poi poi = new Poi(
                    tempPoiId,
                    name,
                    description,
                    baseWorldX,
                    baseWorldY,
                    category,
                    isAccessible,
                    map.getCityID(),
                    false
            );

            overlayLayerController.addPoi(poi);
            selectedPoiIds.add(poi.getId());
            overlayLayerController.markPoiSelected(poi);
        });
    }



    private void handlePoiSelection(Poi poi, Node node) {

        // ADD POI MODE: select only once, no toggle
        if (mode == Mode.ADD_POI) {
            int id = poi.getId();
            if (!selectedPoiIds.contains(id)) {
                selectedPoiIds.add(id);
                overlayLayerController.setPoiSelected(node, true);
            }
            return;
        }

        // ROUTE MODE: toggle + numbering
        if (mode == Mode.ADD_ROUTE) {
            int id = poi.getId();

            if (selectedPoiIds.contains(id)) {
                selectedPoiIds.remove((Integer) id);
                overlayLayerController.setPoiSelected(node, false);
                overlayLayerController.updateRouteNumbers(selectedPoiIds);
            } else {
                selectedPoiIds.add(id);
                overlayLayerController.setPoiSelected(node, true);
                overlayLayerController.updateRouteNumbers(selectedPoiIds);
            }
        }
    }






    @FXML
    public void onSubmitPois(ActionEvent actionEvent) {

        if (submitInProgress) return;
        submitInProgress = true;

        List<Poi> selected = overlayLayerController.getAllPois()
                .stream()
                .filter(p -> selectedPoiIds.contains(p.getId()))
                .toList();

        if (selected.isEmpty()) {
            showInfo("No POIs selected.");
            submitInProgress = false;
            return;
        }

        int version = askVersionNum();
        if (version < 0) {
            submitInProgress = false;
            return;
        }

        String name = askMapName();
        if (name == null) {
            submitInProgress = false;
            return;
        }

        String description = askMapDescription();
        if (description == null) {
            submitInProgress = false;
            return;
        }

        MapSheet payload = new MapSheet(
                version,
                map.getCityID(),
                name,
                description,
                path,
                new ArrayList<>(selected)
        );

        client = ClientApp.getClient();
        client.setResponseHandler(this::handleResponse);
        client.sendRequest(new GcmRequest(RequestType.PEND_MAP, payload));
    }



    @FXML
    public void onSubmitRoute(ActionEvent actionEvent) {

        if (mode != Mode.ADD_ROUTE) {
            showInfo("Press Add Route first.");
            return;
        }

        if (selectedPoiIds.size() < 2) {
            showInfo("Select at least 2 POIs.");
            return;
        }

        String routeName = askRouteName();
        if (routeName == null) return;

        String routeDesc = askRouteDescription();
        if (routeDesc == null) return;

        List<RouteStop> stops = new ArrayList<>();

        for (int i = 0; i < selectedPoiIds.size(); i++) {
            int poiId = selectedPoiIds.get(i);

            // for now hardcode minutes, later ask user
            stops.add(new RouteStop(poiId, i, 10));
        }

        PendingRoute payload = new PendingRoute(
                map.getCityID(),
                routeName,
                routeDesc,
                ClientApp.getCurrentUser().getId(),
                stops
        );

        client = ClientApp.getClient();
        client.setResponseHandler(this::handleResponse);
        client.sendRequest(new GcmRequest(RequestType.SUBMIT_ROUTE, payload));

        selectedPoiIds.clear();
        overlayLayerController.clearPoiSelections();
        mode = Mode.VIEW;
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


    private void handleResponse(GcmResponse response) {
        if (!response.isSuccess()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Pending Failed");
            alert.setContentText(response.getErrorMessage());
            alert.showAndWait();
            return;
        }

        // success – nothing else to do here for now
        System.out.println("Map submitted successfully");
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

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }



    @FXML
    private void onAddRouteMode() {
        mode = Mode.ADD_ROUTE;

        showInfo("Route mode: click POIs in order, then Submit Route.");
    }




    private boolean askPoiAccessibility() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("POI Accessibility");
        alert.setHeaderText("Accessibility");
        alert.setContentText("Is this POI accessible for people with special needs?");

        ButtonType yes = new ButtonType("Yes");
        ButtonType no = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(yes, no);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == yes;
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


    public void showMap(MapSheet map) {
        selectedPoiIds.clear();
        overlayLayerController.clearAll();

        if (map.getPois() == null) return;

        for (Poi p : map.getPois()) {
            if (p.isApproved()) {
                overlayLayerController.addPoi(p); // red by default
            }
        }

        overlayLayerController.rerender();
    }



    public void handleClose(ActionEvent actionEvent) {
                ClientApp.getNavigator().show(ContentWorkerMenuController.class);
    }


}