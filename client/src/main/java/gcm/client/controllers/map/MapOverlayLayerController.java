package gcm.client.controllers.map;


import common.model.Poi;
import common.model.Route;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.scene.layout.Region;
import java.util.HashMap;
import java.util.Map;

public class MapOverlayLayerController {

    @FXML
    private Pane routeLayer;

    @FXML
    private Pane poiLayer;

    private Map<Integer, PoiView> poiViews = new HashMap<>();
    private Map<Integer, RouteView> routeViews = new HashMap<>();

    private MapCoordinateMapper mapper;
    @FXML
    private void initialize() {

        // --- CRITICAL FIX ---
        // Overlay layers must NOT affect layout bounds
        poiLayer.setManaged(false);
        routeLayer.setManaged(false);

        // Overlay should not block mouse events to base map
        routeLayer.setMouseTransparent(true);
        routeLayer.setPickOnBounds(false);

        poiLayer.setPickOnBounds(false);

        // --- CLIP OVERLAY TO VIEWPORT ---
        // Prevent huge POI coordinates from expanding visual bounds
        Rectangle poiClip = new Rectangle();
        Rectangle routeClip = new Rectangle();

        // Parent is the container holding both layers (StackPane / AnchorPane)
        Region parent = (Region) poiLayer.getParent();

        poiClip.widthProperty().bind(parent.widthProperty());
        poiClip.heightProperty().bind(parent.heightProperty());

        routeClip.widthProperty().bind(parent.widthProperty());
        routeClip.heightProperty().bind(parent.heightProperty());

        poiLayer.setClip(poiClip);
        routeLayer.setClip(routeClip);
    }


    public void setMapper(MapCoordinateMapper mapper) {
        this.mapper = mapper;
        // when mapper changes (zoom/pan), rebuild geometry
        rerender();
    }

    public void addPoi(Poi poi) {
        PoiView view = new PoiView(poi);
        poiViews.put(poi.getId(), view);
        poiLayer.getChildren().add(view);

        if (mapper != null) {
            double[] xy = mapper.mapLonLatToView(poi.getWorldX(), poi.getWorldy());
            view.setLayoutX(xy[0]);
            view.setLayoutY(xy[1]);
        }
    }


    public void addRoute(Route route) {
        RouteView rv = new RouteView(route);
        routeViews.put(route.getId(), rv);
        routeLayer.getChildren().add(rv);
        if (mapper != null) {
            rv.rebuildGeometry(mapper);
        }
    }

    public void clearAll() {
        poiViews.clear();
        routeViews.clear();
        poiLayer.getChildren().clear();
        routeLayer.getChildren().clear();
    }

    public void rerender() {
        if (mapper == null) return;
        if (poiViews == null) return;

        poiViews.values().forEach(view -> {
            if (view == null) return;
            Poi p = view.getPoi();
            if (p == null) return;

            double wx = p.getWorldX();
            double wy = p.getWorldy();
            double[] xy = mapper.mapLonLatToView(wx, wy);
            if (xy == null || xy.length < 2) return;

            view.setLayoutX(xy[0]);
            view.setLayoutY(xy[1]);
        });


        System.out.println("rerender: end");
    }



}
