package gcm.client.controllers.map;


import common.model.Poi;
import common.model.Route;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;

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
        // nothing yet
    }

    public void setMapper(MapCoordinateMapper mapper) {
        this.mapper = mapper;
        // when mapper changes (zoom/pan), rebuild geometry
        rerender();
    }

    public void addPoi(Poi poi) {
        PoiView view = new PoiView(poi);
        poiViews.put(poi.getId(), view);

        if (mapper != null) {
            double[] xy = mapper.mapLonLatToView(poi.getLon(), poi.getLat());
            view.setLayoutX(xy[0]);
            view.setLayoutY(xy[1]);
        }

        poiLayer.getChildren().add(view);
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

        poiViews.values().forEach(view -> {
            Poi p = view.getPoi();
            double[] xy = mapper.mapLonLatToView(p.getLon(), p.getLat());
            view.setLayoutX(xy[0]);
            view.setLayoutY(xy[1]);
        });

        routeViews.values().forEach(routeView -> routeView.rebuildGeometry(mapper));
    }
}
