package gcm.client.controllers.map;

import common.model.Poi;
import common.model.Route;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class MapOverlayLayerController {

    @FXML private AnchorPane overlayRoot;

    // We will put all POIs and Routes inside this pane.
    // We will set it to 'unmanaged' so it doesn't force the window to explode.
    private Pane contentPane;

    private java.util.function.IntSupplier zoomSupplier;
    private Map<Integer, PoiView> poiViews = new HashMap<>();
    private Map<Integer, RouteView> routeViews = new HashMap<>();
    private BiConsumer<Poi, Node> onPoiSelected;
    private BiConsumer<Double, Double> onEmptyMapClick;
    private java.util.function.BiConsumer<Double, Double> onEmptyPress;
    private java.util.function.BiConsumer<Double, Double> onEmptyDrag;
    private java.util.function.DoubleConsumer onScroll;
    private MapCoordinateMapper mapper;

    // Callbacks setters
    public void setOnEmptyPress(java.util.function.BiConsumer<Double, Double> h) { onEmptyPress = h; }
    public void setOnEmptyDrag(java.util.function.BiConsumer<Double, Double> h)  { onEmptyDrag = h; }
    public void setOnScroll(java.util.function.DoubleConsumer h)                 { onScroll = h; }
    public void setOnEmptyMapClick(BiConsumer<Double, Double> handler) { this.onEmptyMapClick = handler; }

    private Runnable onEmptyRightClick;
    public void setOnEmptyRightClick(Runnable r) { this.onEmptyRightClick = r; }
    public void setOnPoiSelected(BiConsumer<Poi, Node> handler) { this.onPoiSelected = handler; }
    public void setZoomSupplier(java.util.function.IntSupplier zoomSupplier) { this.zoomSupplier = zoomSupplier; }

    @FXML
    private void initialize() {
        // 1. Create the container for map objects
        contentPane = new Pane();
        // CRITICAL: This tells the layout engine to IGNORE the size of this pane.
        // This prevents the Canvas from trying to grow to 2.5 million pixels.
        contentPane.setManaged(false);
        contentPane.setPickOnBounds(false); // Let clicks pass through empty areas
        overlayRoot.getChildren().add(contentPane);

        // 2. Clip the overlayRoot just to be safe (visual clipping)
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(overlayRoot.widthProperty());
        clip.heightProperty().bind(overlayRoot.heightProperty());
        overlayRoot.setClip(clip);

        // --- Event Filters ---
        overlayRoot.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, e -> {
            if (isClickOnPoi(e.getTarget())) {
                ShowInfo(e.getTarget());
                return;
            }
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

        overlayRoot.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_CLICKED, e -> {
            if (e.isStillSincePress() && !isClickOnPoi(e.getTarget())) {
                if (onEmptyMapClick != null) {
                    onEmptyMapClick.accept(e.getSceneX(), e.getSceneY());
                }
            }
        });
    }

    public void setMapper(MapCoordinateMapper mapper) {
        this.mapper = mapper;
        rerender();
    }

    public void addPoi(Poi poi) {
        if (poi == null) return;
        PoiView view = new PoiView(poi);
        poiViews.put(poi.getId(), view);

        // Add to contentPane, NOT overlayRoot
        contentPane.getChildren().add(view);

        if (mapper != null && zoomSupplier != null) {
            int z = zoomSupplier.getAsInt();
            double wx = poi.getWorldX(z);
            double wy = poi.getWorldY(z);
            double[] xy = mapper.mapLonLatToView(wx, wy);

            if (xy != null && xy.length >= 2) {
                view.setLayoutX(xy[0]);
                view.setLayoutY(xy[1]);
                System.out.println("added poi"+ poi.getName());
            }
        }
    }

    public void addRoute(Route route) {
        if (route == null) return;
        RouteView rv = new RouteView(route);
        routeViews.put(route.getId(), rv);

        // Add to contentPane, NOT overlayRoot
        contentPane.getChildren().add(rv);

        if (mapper != null && zoomSupplier != null) {
            rv.rebuildGeometry(mapper, zoomSupplier.getAsInt());
        }
    }

    public void clearAll() {
        poiViews.clear();
        routeViews.clear();
        // Clear children from the contentPane
        contentPane.getChildren().clear();
    }

    public void rerender() {
        if (mapper == null || zoomSupplier == null) {
          ;return;}
        int z = zoomSupplier.getAsInt();

        for (PoiView view : poiViews.values()) {
            if (view == null) continue;
            Poi p = view.getPoi();
            if (p == null) continue;

            double wx = p.getWorldX(z);
            double wy = p.getWorldY(z);
            double[] xy = mapper.mapLonLatToView(wx, wy);
            System.out.println("drawn succcesfuly");
            view.setLayoutX(xy[0]);
            view.setLayoutY(xy[1]);
        }

        for (RouteView rv : routeViews.values()) {
            if (rv == null) continue;
            rv.rebuildGeometry(mapper, z);
        }
    }

    private boolean isClickOnPoi(Object target) {
        if (!(target instanceof javafx.scene.Node node)) return false;
        javafx.scene.Node cur = node;
        while (cur != null) {
            if (cur instanceof PoiView) return true;
            if (cur == contentPane || cur == overlayRoot) break;
            cur = cur.getParent();
        }
        return false;
    }

    private void ShowInfo(Object target) {
        if (!(target instanceof javafx.scene.Node node)) return;
        javafx.scene.Node cur = node;
        while (cur != null) {
            if (cur instanceof PoiView) {
                if (onPoiSelected != null) {
                    onPoiSelected.accept(((PoiView) cur).getPoi(), ((PoiView) cur));
                }
                return;
            }
            if (cur == contentPane || cur == overlayRoot) break;
            cur = cur.getParent();
        }
    }
}