// client/src/main/java/gcm/client/map/RouteView.java
package gcm.client.controllers.map;

import common.model.Route;
import common.model.Poi;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polyline;
public class RouteView extends Pane {
    private final Route route;
    private final Polyline polyline = new Polyline();

    public RouteView(Route route) {
        this.route = route;
        polyline.setStrokeWidth(3);
        setMouseTransparent(true);
        polyline.setStroke(Color.BLUE);   // ✅ ROUTE COLOR HERE
        polyline.setFill(null);
        getChildren().add(polyline);
    }

    public Route getRoute() {
        return route;
    }

    public void rebuildGeometry(MapCoordinateMapper mapper, int zoomLevel) {
        polyline.getPoints().clear();

        for (double[] p : route.getPointsAtZoom(zoomLevel)) {
            double[] xy = mapper.mapLonLatToView(p[0], p[1]); // world -> view
            polyline.getPoints().addAll(xy[0], xy[1]);
        }
    }
}
