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

    public Polyline getPolyline() {
        return polyline;
    }

    public void rebuildGeometry(MapCoordinateMapper mapper, int zoom) {
        polyline.getPoints().clear();

        double scale = Math.pow(2, Poi.BASE_ZOOM - zoom); // same logic as Poi

        for (double[] basePt : route.getBasePoints()) {
            double worldX = basePt[0] / scale;
            double worldY = basePt[1] / scale;

            double[] screen = mapper.mapLonLatToView(worldX, worldY);
            polyline.getPoints().addAll(screen[0], screen[1]);
        }
    }

}
