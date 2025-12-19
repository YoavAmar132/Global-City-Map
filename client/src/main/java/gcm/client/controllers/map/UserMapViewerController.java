package gcm.client.controllers.map;

import common.messages.*;
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

public class UserMapViewerController {
    private int poid=0;
    private int routid=0;
    @FXML
    private StackPane stackPane;
    private List<Poi> pois = new ArrayList<>();
    private Route buildingRoute = null;
    private List<Route> routes = new ArrayList<>();
    private boolean buildingRouteWaitingFirstPoint = false;
    private int routeId = 0;
    private String path;
    private  MaPayload mapayload;
    private boolean initialized = false;
    private boolean hasVals = false;


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

        overlayLayerController.setOnPoiSelected((poi, node) -> {
            showPoiPopover(poi, node);
        });

        baseLayerController.recenterNow();


    }
    public void setVals(int version,String name,String path)
    {
        this.path=path;
        this.mapayload=new MaPayload(version,name);
        hasVals = true;
        System.out.println("path used:"+path);
        System.out.println("version,name used: "+version+name);
        baseLayerController.setTileRoot(path);
        client = ClientApp.getClient();
        client.setResponseHandler(this::handleResponse);
        GcmRequest request = new GcmRequest(RequestType.GET_MAP, mapayload);
        client.sendRequest(request);


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

    /**
     * Show a map with given POIs and routes.
     * Assumes POI coordinates are in the same "world" coordinate system
     * that the base layer uses (e.g. tile pixels at current zoom).
     */
    public void showMap(MapSheet map) {
        overlayLayerController.clearAll();
        List<Poi> pois=map.getPois();
         List<Route> routes=map.getRoutes();
        if (pois != null) {
            for (Poi p : pois) {
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

        MenuItem close = new MenuItem("Close");

        menu.getItems().addAll(title, desc, cat, new SeparatorMenuItem(), close);

        menu.show(anchor, Side.TOP, 0, -10);
    }


    private void handleResponse(GcmResponse response) {
        if (!response.isSuccess()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("map fetching Failed");
            alert.setContentText(response.getErrorMessage());
            alert.showAndWait();
        }else{
            Object t =response.getData();
            if(t instanceof MapSheet map) {
                showMap((MapSheet)t);
                System.out.println("loaded succsesfuly");
            }else {
                System.out.println("loading map failed");
            }

        }

    }

    public void handleClose(ActionEvent actionEvent) {
        ClientApp.getNavigator().show(PendingMapController.class);

    }
}
