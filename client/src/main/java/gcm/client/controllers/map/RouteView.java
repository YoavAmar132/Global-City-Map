// client/src/main/java/gcm/client/map/RouteView.java
package gcm.client.controllers.map;

import common.model.Route;
import common.model.Poi;
import javafx.scene.Group;
import javafx.scene.shape.Polyline;

public class RouteView extends Group {

    private final Route route;
    private final Polyline polyline;

    public RouteView(Route route) {
        this.route = route;
        this.polyline = new Polyline();
        getChildren().add(polyline);
        getStyleClass().add("route-line");
    }

    public Route getRoute() {
        return route;
    }

    public Polyline getPolyline() {
        return polyline;
    }

    /** Call this when map transform / zoom changes */
    public void rebuildGeometry(MapCoordinateMapper mapper) {
        polyline.getPoints().clear();
        for (Poi p : route.getPoisInOrder()) {
            double[] xy = mapper.mapLonLatToView(p.getWorldX(), p.getWorldy());
            polyline.getPoints().addAll(xy[0], xy[1]);
        }
    }
}
