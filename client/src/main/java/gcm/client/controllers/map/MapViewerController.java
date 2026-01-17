package gcm.client.controllers.map;

import common.messages.*;
import common.model.*;
import gcm.client.controllers.menu.ContentWorkerMenuController;
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
    private Integer editedRouteId = null; // null = new route

    private enum Mode { VIEW, EDIT_MAP, EDIT_ROUTE, CREATE_MAP, ADD_POI, ADD_ROUTE }

    private enum EntryContext {
        CREATE_CONTENT,
        EDIT_MAP,
        EDIT_ROUTE
    }

    private EntryContext entryContext;
    private Mode mode = Mode.VIEW;

    private List<Poi> routePois = new ArrayList<>();

    @FXML private Button addPoiButton;
    @FXML private Button addRouteButton;
    @FXML private Button submitMapButton;
    @FXML private Button submitRouteButton;

    @FXML
    private MapBaseLayerController baseLayerController;

    @FXML
    private MapOverlayLayerController overlayLayerController;

    private GcmClient client;
    private boolean waitingForCityPois = false;

    private boolean editingExistingMap = false;

    public void setMap(MapSheet map) {
        this.map = map;
    }

    private boolean isMapMode() {
        return mode == Mode.CREATE_MAP
                || mode == Mode.EDIT_MAP
                || mode == Mode.ADD_POI;
    }

    private boolean isRouteMode() {
        return mode == Mode.ADD_ROUTE
                || mode == Mode.EDIT_ROUTE;
    }

    private boolean isEditingMapOnly() {
        return entryContext == EntryContext.EDIT_MAP;
    }

    private boolean isEditingRouteOnly() {
        return entryContext == EntryContext.EDIT_ROUTE;
    }

    private void configureToolbar() {

        addPoiButton.setVisible(false);
        submitMapButton.setVisible(false);
        addRouteButton.setVisible(false);
        submitRouteButton.setVisible(false);

        submitMapButton.setText("Submit Map");
        submitRouteButton.setText("Submit Route");

        if (entryContext == null) return;

        switch (entryContext) {

            case CREATE_CONTENT -> {

                if (mode == Mode.CREATE_MAP) {
                    addPoiButton.setVisible(true);
                    addRouteButton.setVisible(true);
                    return;
                }

                if (mode == Mode.ADD_POI) {
                    submitMapButton.setVisible(true);
                    submitMapButton.setText("Submit Map");
                    return;
                }

                if (mode == Mode.ADD_ROUTE) {
                    submitRouteButton.setVisible(true);
                    submitRouteButton.setText("Submit Route");
                    return;
                }
            }

            case EDIT_MAP -> {
                submitMapButton.setVisible(true);
                submitMapButton.setText("Submit Edited Map");
            }

            case EDIT_ROUTE -> {
                submitRouteButton.setVisible(true);
                submitRouteButton.setText("Submit Edited Route");
            }
        }
    }

    @FXML
    private void initialize() {

        overlayLayerController.setZoomSupplier(() -> baseLayerController.getZoom());
        overlayLayerController.setMapper((worldX, worldY) ->
                baseLayerController.mapToView(worldX, worldY)
        );

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

    private int safePoiCityId(common.model.Poi poi) {
        if (poi == null) return -1;

        try {
            var m = poi.getClass().getMethod("getCityID");
            Object v = m.invoke(poi);
            if (v instanceof Integer i) return i;
        } catch (Exception ignored) {}

        try {
            var m = poi.getClass().getMethod("getCityId");
            Object v = m.invoke(poi);
            if (v instanceof Integer i) return i;
        } catch (Exception ignored) {}

        return -1;
    }

    public void setValsForEdit(MapSheet approvedMap) {
        entryContext = EntryContext.EDIT_MAP;
        mode = Mode.ADD_POI;

        baseLayerController.setInteractionMode(
                MapBaseLayerController.InteractionMode.VIEW
        );

        configureToolbar();
        showDone = false;

        approvedMap.setEdit(true);

        this.path = approvedMap.getPath();
        setMap(approvedMap);
        hasVals = true;

        editingExistingMap = true;

        selectedPoiIds.clear();
        overlayLayerController.clearAll();

        if (approvedMap.getPois() != null) {
            for (Poi p : approvedMap.getPois()) {
                overlayLayerController.addPoi(p);
                selectedPoiIds.add(p.getId());
            }
        }

        overlayLayerController.rerender();

        if (approvedMap.getPois() != null) {
            for (Poi p : approvedMap.getPois()) {
                overlayLayerController.markPoiSelected(p);
            }
        }

        baseLayerController.setInteractionMode(
                MapBaseLayerController.InteractionMode.ADD_POI
        );

        baseLayerController.setOnPoiClick(world -> {

            int currentZoom = baseLayerController.getZoom();
            double scaleUp = Math.pow(2, Poi.BASE_ZOOM - currentZoom);

            double baseWorldX = world[0] * scaleUp;
            double baseWorldY = world[1] * scaleUp;

            String name = askPoiName();
            if (name == null) return;

            String description = askPoiDescription();
            if (description == null) return;

            POI_Category category = askPoiCategory();
            if (category == null) return;

            boolean isAccessible = askPoiAccessibility();
            int recommended_mins = askPoiRecommendedMinutes();

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
                    false,
                    recommended_mins
            );

            overlayLayerController.addPoi(poi);
            selectedPoiIds.add(poi.getId());
            overlayLayerController.markPoiSelected(poi);
        });

        client = ClientApp.getClient();
        client.setResponseHandler(this::handleResponse);

        waitingForCityPois = true;

        client.sendRequest(new GcmRequest(
                RequestType.LIST_POIS,
                new CityIdPayload(map.getCityID())
        ));

        baseLayerController.setTileRoot(path);
        tryShowMap();

        showInfo(
                "Edit Map mode: green POIs are in the map. " +
                        "Red POIs are approved for the city but not in the map."
        );
    }

    public void setValsForEditRoute(RouteSheet sheet) {
        entryContext = EntryContext.EDIT_ROUTE;
        mode = Mode.ADD_ROUTE;

        baseLayerController.setInteractionMode(
                MapBaseLayerController.InteractionMode.VIEW
        );

        configureToolbar();

        showDone = false;
        hasVals = true;

        editedRouteId = sheet.getRouteId();
        path = sheet.getCityTilePath();

        map = new MapSheet(
                1,
                sheet.getCityId(),
                sheet.getRoute().getName(),
                sheet.getRoute().getDescription(),
                path,
                new ArrayList<>()
        );

        selectedPoiIds.clear();
        routePois.clear();
        overlayLayerController.clearAll();

        baseLayerController.setTileRoot(path);

        client = ClientApp.getClient();
        client.setResponseHandler(this::handleResponse);
        waitingForCityPois = true;

        client.sendRequest(new GcmRequest(
                RequestType.LIST_POIS,
                new CityIdPayload(sheet.getCityId())
        ));

        if (sheet.getOrderedPois() != null) {
            for (Poi p : sheet.getOrderedPois()) {
                overlayLayerController.addPoi(p);
                selectedPoiIds.add(p.getId());
                overlayLayerController.markPoiSelected(p);
            }
        }

        overlayLayerController.updateRouteNumbers(selectedPoiIds);
        overlayLayerController.rerender();

        tryShowMap();

        showInfo(
                "Edit Route mode: numbered POIs are part of the route.\n" +
                        "Click a POI to remove it or add new ones at the end."
        );
    }

    public void setVals(MapSheet map) {
        showDone = false;

        this.path = map.getPath();
        setMap(map);
        hasVals = true;

        mode = Mode.VIEW;

        entryContext = null;
        configureToolbar();

        baseLayerController.setTileRoot(path);
        tryShowMap();
    }

    public void setValsForCreate(City city, String tilePath) {
        entryContext = EntryContext.CREATE_CONTENT;
        showDone = false;

        this.path = tilePath;
        this.map = new MapSheet(
                1,
                city.getId(),
                "",
                "",
                tilePath,
                new ArrayList<>()
        );

        hasVals = true;
        editingExistingMap = false;

        mode = Mode.CREATE_MAP;

        selectedPoiIds.clear();
        overlayLayerController.clearAll();

        configureToolbar();

        client = ClientApp.getClient();
        client.setResponseHandler(this::handleResponse);

        waitingForCityPois = true;

        client.sendRequest(new GcmRequest(
                RequestType.LIST_POIS,
                new CityIdPayload(map.getCityID())
        ));

        baseLayerController.setTileRoot(tilePath);
        tryShowMap();
    }

    private void tryShowMap() {
        if (showDone) return;
        if (!initialized) return;
        if (!hasVals) return;
        if (map == null) return;

        Scene scene = stackPane.getScene();
        if (scene == null) return;

        Platform.runLater(() -> {
            client = ClientApp.getClient();
            client.setResponseHandler(this::handleResponse);

            if (showDone) return;
            if (stackPane.getScene() == null) return;

            baseLayerController.setTileRoot(path);

            if (mode == Mode.VIEW) {
                showMap(map);
            } else {
                overlayLayerController.rerender();
            }
            showDone = true;
        });
    }

    @FXML
    private void onZoomIn() {
        baseLayerController.setInteractionMode(MapBaseLayerController.InteractionMode.VIEW);
        int current = baseLayerController.getZoom();
        baseLayerController.setZoom(current + 1);
        overlayLayerController.rerender();
    }

    @FXML
    private void onZoomOut() {
        baseLayerController.setInteractionMode(MapBaseLayerController.InteractionMode.VIEW);
        int current = baseLayerController.getZoom();
        baseLayerController.setZoom(current - 1);
        overlayLayerController.rerender();
    }

    @FXML
    private void onAddPoiMode() {

        if (isEditingRouteOnly()) {
            showInfo("You are editing a route.\nPOIs cannot be edited here.");
            return;
        }

        if (mode != Mode.CREATE_MAP && mode != Mode.EDIT_MAP && mode != Mode.ADD_POI) {
            showInfo("You must be creating or editing a map to add POIs.");
            return;
        }

        mode = Mode.ADD_POI;

        baseLayerController.setInteractionMode(
                MapBaseLayerController.InteractionMode.ADD_POI
        );

        baseLayerController.setOnPoiClick(world -> {

            int currentZoom = baseLayerController.getZoom();
            double scaleUp = Math.pow(2, Poi.BASE_ZOOM - currentZoom);

            double baseWorldX = world[0] * scaleUp;
            double baseWorldY = world[1] * scaleUp;

            String name = askPoiName();
            if (name == null) return;

            String description = askPoiDescription();
            if (description == null) return;

            POI_Category category = askPoiCategory(); // FIXED (no poi variable here)
            if (category == null) return;

            int recommended_mins = askPoiRecommendedMinutes();
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
                    false,
                    recommended_mins
            );

            overlayLayerController.addPoi(poi);
            selectedPoiIds.add(poi.getId());
            overlayLayerController.markPoiSelected(poi);

        });

        configureToolbar();
    }

    private void handlePoiSelection(Poi poi, Node node) {

        if (mode != Mode.ADD_POI && mode != Mode.ADD_ROUTE) {
            return;
        }

        int id = poi.getId();

        if (selectedPoiIds.contains(id)) {
            selectedPoiIds.remove((Integer) id);
            overlayLayerController.setPoiSelected(node, false);
        } else {
            selectedPoiIds.add(id);
            overlayLayerController.setPoiSelected(node, true);
        }

        if (mode == Mode.ADD_ROUTE) {
            overlayLayerController.updateRouteNumbers(selectedPoiIds);
        }
    }

    public void onSubmitPois(ActionEvent actionEvent) {

        if (!isMapMode()) {
            showInfo("You are not editing a map.");
            return;
        }

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
                (editingExistingMap ? map.getVersion() + 1 : 1),
                map.getCityID(),
                name,
                description,
                path,
                new ArrayList<>(selected)
        );

        payload.setEdit(map.isEdit());
        if (map.isEdit()) {
            payload.setSourceMapId(map.getSourceMapId());
        }

        client = ClientApp.getClient();
        client.setResponseHandler(this::handleResponse);
        client.sendRequest(new GcmRequest(RequestType.PEND_MAP, payload));
    }

    @FXML
    public void onSubmitRoute(ActionEvent actionEvent) {

        if (!isRouteMode()) {
            showInfo("You are not editing a route.");
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

        ArrayList<Integer> stops = new ArrayList<>(selectedPoiIds);

        PendingRoute payload;

        if (mode == Mode.EDIT_ROUTE) {
            payload = new PendingRoute(
                    0,
                    routeName,
                    routeDesc,
                    map.getCityID(),
                    stops,
                    editedRouteId
            );
            payload.setEdit(true);
        } else {
            payload = new PendingRoute(
                    0,
                    routeName,
                    routeDesc,
                    map.getCityID(),
                    stops
            );
        }

        client = ClientApp.getClient();
        client.setResponseHandler(this::handleResponse);
        client.sendRequest(new GcmRequest(RequestType.SUBMIT_ROUTE, payload));

        selectedPoiIds.clear();
        overlayLayerController.clearPoiSelections();
        overlayLayerController.updateRouteNumbers(selectedPoiIds);

        exitEditor();
    }

    private void exitEditor() {
        selectedPoiIds.clear();
        routePois.clear();

        overlayLayerController.clearAll();
        overlayLayerController.updateRouteNumbers(selectedPoiIds);

        mode = Mode.VIEW;
        editedRouteId = null;
        editingExistingMap = false;

        ClientApp.getNavigator().show(ContentWorkerMenuController.class);
    }

    private POI_Category askPoiCategory() {

        List<POI_Category> choices = Arrays.asList(POI_Category.values());

        ChoiceDialog<POI_Category> dialog = new ChoiceDialog<>(choices.get(0), choices);
        dialog.setTitle("POI Category");
        dialog.setHeaderText("Select the POI Category");
        dialog.setContentText("Category:");

        return dialog.showAndWait().orElse(null);
    }

    private POI_Category askPoiCategory(POI_Category current) {
        List<POI_Category> choices = Arrays.asList(POI_Category.values());

        ChoiceDialog<POI_Category> dialog =
                new ChoiceDialog<>(current, choices);

        dialog.setTitle("Edit POI Category");
        dialog.setHeaderText("Edit POI Category");
        dialog.setContentText("Category:");

        return dialog.showAndWait().orElse(null);
    }

    private void showPoiPopover(Poi poi, Node anchor) {

        ContextMenu menu = new ContextMenu();

        MenuItem title = new MenuItem("Name: " + poi.getName());
        title.setDisable(true);

        MenuItem desc = new MenuItem("Description: " + poi.getDescription());
        desc.setDisable(true);

        MenuItem cat = new MenuItem("Category: " + poi.getCategory());
        cat.setDisable(true);

        MenuItem time = new MenuItem("Recommended mins: " + poi.getRecommendedMinutes());
        time.setDisable(true);

        MenuItem accessibility = new MenuItem(
                "Accessible: " + (poi.isAccessible() ? "Yes" : "No")
        );
        accessibility.setDisable(true);

        menu.getItems().addAll(
                title, desc, cat, time, accessibility
        );

        if (entryContext == EntryContext.EDIT_MAP) {

            menu.getItems().add(new SeparatorMenuItem());

            MenuItem edit = new MenuItem("Edit POI");
            edit.setOnAction(e -> openEditPoiDialog(poi));


            menu.getItems().add(edit);
        }

        menu.show(anchor, Side.TOP, 0, -10);
    }


    private void openEditPoiDialog(Poi poi) {

        if (!selectedPoiIds.contains(poi.getId())) {
            showInfo("You can only edit POIs that are selected in the map.");
            return;
        }

        if (poi.getId() < 0) {
            showInfo("This POI is new and not saved yet.\nSubmit the map first.");
            return;
        }

        String name = askPoiName(poi.getName());
        if (name == null) return;

        String description = askPoiDescription(poi.getDescription());
        if (description == null) return;

        POI_Category category = askPoiCategory(poi.getCategory());
        if (category == null) return;

        boolean accessible = askPoiAccessibility(poi.isAccessible());
        int mins = askPoiRecommendedMinutes(poi.getRecommendedMinutes());

        Poi edited = new Poi(
                poi.getId(),
                name,
                description,
                poi.getNWorldX(),
                poi.getNWorldY(),
                category,
                accessible,
                poi.getCityID(),
                poi.isApproved(),
                mins
        );


        overlayLayerController.replacePoi(edited);
        overlayLayerController.rerender();
    }





    private void handleResponse(GcmResponse response) {

        if (!response.isSuccess()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Request Failed");
            alert.setContentText(response.getErrorMessage());
            alert.showAndWait();
            submitInProgress = false;
            return;
        }

        Object data = response.getData();

        if (waitingForCityPois && data instanceof ArrayList<?> list) {

            waitingForCityPois = false;

            int cityId = map.getCityID();

            for (Object o : list) {
                if (!(o instanceof Poi p)) continue;

                int poiCity = safePoiCityId(p);
                if (poiCity != -1 && poiCity != cityId) continue;

                if (overlayLayerController.getPoiView(p.getId()) != null) continue;

                overlayLayerController.addPoi(p);
            }

            overlayLayerController.rerender();
            submitInProgress = false;
            return;
        }

        if (!submitInProgress) {
            return;
        }

        submitInProgress = false;
        exitEditor();
    }

    private String askNonEmptyString(String title, String header, String label, String defaultValue) {
        while (true) {
            TextInputDialog dialog = new TextInputDialog(defaultValue);
            dialog.setTitle(title);
            dialog.setHeaderText(header);
            dialog.setContentText(label);

            Optional<String> result = dialog.showAndWait();

            if (result.isEmpty()) {
                return null;
            }

            String value = result.get().trim();
            if (!value.isEmpty()) {
                return value;
            }
        }
    }

    String askPoiName() {
        return askNonEmptyString("New POI", "Enter POI name", "Name:", "");
    }

    String askPoiDescription() {
        return askNonEmptyString("New POI", "Enter POI description", "Description:", "");
    }

    String askRouteName() {
        return askNonEmptyString("New Route", "Enter route name", "Name:", "");
    }

    String askMapName() {
        return askNonEmptyString("New Map", "Enter map name", "Name:", "");
    }

    String askRouteDescription() {
        return askNonEmptyString("New Route", "Enter route description", "Description:", "");
    }

    String askMapDescription() {
        return askNonEmptyString("New Map", "Enter map description", "Description:", "");
    }

    String askPoiName(String current) {
        return askNonEmptyString("Edit POI", "Edit POI name", "Name:", current);
    }

    String askPoiDescription(String current) {
        return askNonEmptyString("Edit POI", "Edit POI description", "Description:", current);
    }

    private int askPoiRecommendedMinutes() {
        return askPoiRecommendedMinutes(30);
    }

    private int askPoiRecommendedMinutes(int current) {
        while (true) {
            TextInputDialog dialog = new TextInputDialog(String.valueOf(current));
            dialog.setTitle("POI Recommended Time");
            dialog.setHeaderText("Recommended Visit Duration");
            dialog.setContentText("Enter recommended time in minutes (positive number):");

            Optional<String> result = dialog.showAndWait();
            if (result.isEmpty()) {
                return current;
            }

            try {
                int minutes = Integer.parseInt(result.get().trim());
                if (minutes <= 0) throw new NumberFormatException();
                return minutes;
            } catch (NumberFormatException e) {
                showInfo("Please enter a positive number.");
            }
        }
    }

    private boolean askPoiAccessibility() {
        return askPoiAccessibility(false);
    }

    private boolean askPoiAccessibility(boolean current) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("POI Accessibility");
        alert.setHeaderText("Accessibility");
        alert.setContentText("Is this POI accessible for people with special needs?");

        ButtonType yes = new ButtonType("Yes");
        ButtonType no = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(yes, no);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isEmpty()) return current;
        return result.get() == yes;
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    @FXML
    private void onAddRouteMode() {

        if (isEditingMapOnly()) {
            showInfo("You are editing a map.\nRoutes cannot be edited here.");
            return;
        }

        if (mode == Mode.EDIT_ROUTE) {
            showInfo("You are already editing this route.");
            return;
        }

        mode = Mode.ADD_ROUTE;

        selectedPoiIds.clear();
        overlayLayerController.clearPoiSelections();
        overlayLayerController.updateRouteNumbers(selectedPoiIds);

        showInfo("Route mode: select POIs in order, then Submit Route.");
        configureToolbar();
    }

    public void showMap(MapSheet map) {
        selectedPoiIds.clear();
        overlayLayerController.clearAll();

        if (map.getPois() == null) return;

        for (Poi p : map.getPois()) {
            if (p.isApproved()) {
                overlayLayerController.addPoi(p);
            }
        }

        overlayLayerController.rerender();
    }

    public void handleClose(ActionEvent actionEvent) {
        ClientApp.getNavigator().show(ContentWorkerMenuController.class);
    }
}
