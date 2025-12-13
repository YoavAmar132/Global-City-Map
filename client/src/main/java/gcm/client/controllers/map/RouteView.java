// client/src/main/java/gcm/client/map/RouteView.java
package gcm.client.controllers.map;

import common.model.Route;
import common.model.Poi;
import javafx.scene.Group;
import javafx.scene.shape.Polyline;
public class RouteView extends javafx.scene.shape.Polyline {
    private final Route route;

    public RouteView(Route route) {
        this.route = route;
        setStrokeWidth(3);
        setMouseTransparent(true);
    }

    public Route getRoute() {
        return route;
    }

    public void rebuildGeometry(MapCoordinateMapper mapper, int zoomLevel) {
        getPoints().clear();

        for (double[] p : route.getPointsAtZoom(zoomLevel)) {
            double[] xy = mapper.mapLonLatToView(p[0], p[1]); // world -> view
            getPoints().addAll(xy[0], xy[1]);
        }
    }
}
