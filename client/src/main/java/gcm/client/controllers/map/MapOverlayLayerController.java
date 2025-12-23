package gcm.client.controllers.map;


import common.model.Poi;
import common.model.Route;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.scene.layout.Region;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class MapOverlayLayerController {

    @FXML
    private Pane routeLayer;
    @FXML private AnchorPane overlayRoot;
    private java.util.function.IntSupplier zoomSupplier;
    private Map<Integer, PoiView> poiViews = new HashMap<>();
    private Map<Integer, RouteView> routeViews = new HashMap<>();
    private BiConsumer<Poi, Node> onPoiSelected;
    private BiConsumer<Double, Double> onEmptyMapClick;
    private java.util.function.BiConsumer<Double, Double> onEmptyPress;
    private java.util.function.BiConsumer<Double, Double> onEmptyDrag;
    private java.util.function.DoubleConsumer onScroll;

    public void setOnEmptyPress(java.util.function.BiConsumer<Double, Double> h) { onEmptyPress = h; }
    public void setOnEmptyDrag(java.util.function.BiConsumer<Double, Double> h)  { onEmptyDrag = h; }
    public void setOnScroll(java.util.function.DoubleConsumer h)                 { onScroll = h; }


    public void setOnEmptyMapClick(BiConsumer<Double, Double> handler) {
        this.onEmptyMapClick = handler;
    }
    private Runnable onEmptyRightClick;

    public void setOnEmptyRightClick(Runnable r) {
        this.onEmptyRightClick = r;
    }



    public void setOnPoiSelected(BiConsumer<Poi, Node> handler) {
        this.onPoiSelected = handler;
    }


    public void setZoomSupplier(java.util.function.IntSupplier zoomSupplier) {
        this.zoomSupplier = zoomSupplier;
    }
    private MapCoordinateMapper mapper;
    @FXML
    private void initialize() {

        // --- CRITICAL FIX ---
        // Overlay layers must NOT affect layout bounds

        routeLayer.setManaged(false);

        // Overlay should not block mouse events to base map
        routeLayer.setMouseTransparent(true);
        routeLayer.setPickOnBounds(false);

        // --- CLIP OVERLAY TO VIEWPORT ---
        overlayRoot.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, e -> {
            // If the click was on a PoiView, let the PoiView handle it
            if (isClickOnPoi(e.getTarget()))
            {
                System.out.println(isClickOnPoi(e.getTarget()));
                ShowInfo(e.getTarget());
                return;
            }

            // Otherwise forward to base-layer logic
            if (onEmptyMapClick != null) {
                onEmptyMapClick.accept(e.getSceneX(), e.getSceneY());
                // consume so it doesn't accidentally trigger something else on the overlay
                e.consume();
            }
        });
        overlayRoot.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, e -> {
            if (isClickOnPoi(e.getTarget())) return;
            if (e.getButton() == javafx.scene.input.MouseButton.SECONDARY) {
                if (onEmptyRightClick != null) onEmptyRightClick.run();
                e.consume();
                return;
            }
            if (onEmptyPress != null) onEmptyPress.accept(e.getSceneX(), e.getSceneY());
            e.consume();
        });

        overlayRoot.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_DRAGGED, e -> {
            if (isClickOnPoi(e.getTarget())) return;

            if (onEmptyDrag != null) onEmptyDrag.accept(e.getSceneX(), e.getSceneY());
            e.consume();
        });

        overlayRoot.addEventFilter(javafx.scene.input.ScrollEvent.SCROLL, e -> {
            if (onScroll != null) onScroll.accept(e.getDeltaY());
            e.consume();
        });

    }



    public void setMapper(MapCoordinateMapper mapper) {
        this.mapper = mapper;
        // when mapper changes (zoom/pan), rebuild geometry
        rerender();
    }

    public void addPoi(Poi poi) {
        if (poi == null) return;

        // Create the clickable widget
        PoiView view = new PoiView(poi);

        // Put in map + scene graph
        poiViews.put(poi.getId(), view);
        overlayRoot.getChildren().add(view);

        // Position it immediately (so it appears without needing a pan/zoom)
        if (mapper != null && zoomSupplier != null) {
            int z = zoomSupplier.getAsInt();

            double wx = poi.getWorldX(z);
            double wy = poi.getWorldY(z);

            double[] xy = mapper.mapLonLatToView(wx, wy);

            if (xy != null && xy.length >= 2) {
                // If you want the marker centered on the point, offset by half width/height:
                // Note: width/height may be 0 until CSS/layout runs, so keep it simple for now
                view.setLayoutX(xy[0]);
                view.setLayoutY(xy[1]);
            }
        }
    }

    public void addRoute(Route route) {
        if (route == null) return;

        RouteView rv = new RouteView(route);
        routeViews.put(route.getId(), rv);

        // IMPORTANT: overlayRoot is visible
        overlayRoot.getChildren().add(rv);

        if (mapper != null && zoomSupplier != null) {
            rv.rebuildGeometry(mapper, zoomSupplier.getAsInt());
        }
        System.out.println("overlayRoot children = " + overlayRoot.getChildren().size());
        System.out.println("routeLayer children  = " + routeLayer.getChildren().size());
        System.out.println("routeLayer parent    = " + routeLayer.getParent());

    }




    public void clearAll() {
        poiViews.clear();
        //routeViews.clear();
        overlayRoot.getChildren().clear();
        routeLayer.getChildren().clear();
    }

    public void rerender() {
        if (mapper == null || zoomSupplier == null) return;

        int z = zoomSupplier.getAsInt();

        // POIs
        for (PoiView view : poiViews.values()) {
            if (view == null) continue;
            Poi p = view.getPoi();
            if (p == null) continue;

            double wx = p.getWorldX(z);
            double wy = p.getWorldY(z);
            double[] xy = mapper.mapLonLatToView(wx, wy);

            view.setLayoutX(xy[0]);
            view.setLayoutY(xy[1]);
        }

        // Routes
        for (RouteView rv : routeViews.values()) {
            if (rv == null) continue;
            rv.rebuildGeometry(mapper, z);
        }
    }


    private boolean isClickOnPoi(Object target) {
        if (!(target instanceof javafx.scene.Node node)) return false;
        javafx.scene.Node cur = node;
        while (cur != null) {
            if (cur instanceof PoiView)
            {
                if (onPoiSelected != null) return true;
            }
            cur = cur.getParent();
        }
        return false;
    }
    private void ShowInfo(Object target) {
        if (!(target instanceof javafx.scene.Node node)) return ;
        javafx.scene.Node cur = node;
        while (cur != null) {
            if (cur instanceof PoiView)
            {
                if (onPoiSelected != null) {
                    onPoiSelected.accept(((PoiView) cur).getPoi(), ((PoiView) cur));
                    System.out.println("should lunch");
                }
                return ;
            }
            cur = cur.getParent();
        }
    }
    private void forwardClickToBaseLayer(javafx.scene.input.MouseEvent e) {
        if (onEmptyMapClick == null) return;

        // Screen → scene → map-local coords
        double sceneX = e.getSceneX();
        double sceneY = e.getSceneY();

        onEmptyMapClick.accept(sceneX, sceneY);
    }


}
