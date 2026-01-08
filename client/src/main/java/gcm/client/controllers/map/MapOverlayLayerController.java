package gcm.client.controllers.map;

import common.model.Poi;
import common.model.Route;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;


public class MapOverlayLayerController {

    @FXML private AnchorPane overlayRoot;

    // We will put all POIs and Routes inside this pane.
    // We will set it to 'unmanaged' so it doesn't force the window to explode.
    private Pane contentPane;

    private java.util.function.IntSupplier zoomSupplier;
    private Map<Integer, PoiView> poiViews = new HashMap<>();
    private BiConsumer<Poi, Node> onPoiSelected;
    private BiConsumer<Poi, Node> onPoiInfo;
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

            // CLICK ON POI
            if (isClickOnPoi(e.getTarget())) {

                // RIGHT CLICK → INFO
                if (e.getButton() == javafx.scene.input.MouseButton.SECONDARY) {
                    ShowInfo(e.getTarget());
                    e.consume();
                    return;
                }

                // LEFT CLICK → SELECTION
                if (e.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
                    triggerSelection(e.getTarget());
                    e.consume();
                    return;
                }
            }


            // RIGHT CLICK ON EMPTY MAP
            if (e.getButton() == javafx.scene.input.MouseButton.SECONDARY) {
                if (onEmptyRightClick != null) onEmptyRightClick.run();
                e.consume();
                return;
            }

            // LEFT CLICK ON EMPTY MAP
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


    public void updateRouteNumbers(List<Integer> orderedPoiIds) {

        // Clear all numbers first
        for (PoiView view : poiViews.values()) {
            view.clearOrderIndex();
        }

        // Assign numbers strictly by CLICK ORDER
        for (int i = 0; i < orderedPoiIds.size(); i++) {
            int poiId = orderedPoiIds.get(i);
            PoiView view = poiViews.get(poiId);

            if (view != null) {
                view.setOrderIndex(i);
            }
        }
    }


    private void triggerSelection(Object target) {
        if (!(target instanceof Node node)) return;

        Node cur = node;
        while (cur != null) {
            if (cur instanceof PoiView pv) {
                if (onPoiSelected != null) {
                    onPoiSelected.accept(pv.getPoi(), pv);
                }
                return;
            }
            if (cur == contentPane || cur == overlayRoot) break;
            cur = cur.getParent();
        }
    }

    public void setOnPoiInfo(BiConsumer<Poi, Node> handler) {
        this.onPoiInfo = handler;
    }

    public void setMapper(MapCoordinateMapper mapper) {
        this.mapper = mapper;
        rerender();
    }

    public void setPoiSelected(Node node, boolean selected) {
        if (node instanceof PoiView poiView) {
            poiView.setSelected(selected);
        }
    }

    public void markPoiSelected(Poi poi) {
        PoiView view = poiViews.get(poi.getId());
        if (view != null) {
            view.setSelected(true);
        }
    }

    public PoiView getPoiView(int poiId) {
        return poiViews.get(poiId);
    }


    public boolean isClickOnExistingPoi(double sceneX, double sceneY) {
        for (PoiView view : poiViews.values()) {
            if (view == null) continue;

            double dx = Math.abs(view.getLayoutX() - sceneX);
            double dy = Math.abs(view.getLayoutY() - sceneY);

            if (dx < 20 && dy < 20) {
                return true;
            }
        }
        return false;
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

    public void clearPoiSelections() {
        for (PoiView view : poiViews.values()) {
            if (view != null) {
                view.setSelected(false);
            }
        }
    }


    public java.util.List<Poi> getAllPois() {
        java.util.List<Poi> res = new java.util.ArrayList<>();
        for (PoiView view : poiViews.values()) {
            res.add(view.getPoi());
        }
        return res;
    }


    public void clearAll() {
        poiViews.clear();
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
            view.setLayoutX(xy[0]);
            view.setLayoutY(xy[1]);
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
        if (!(target instanceof Node node)) return;

        Node cur = node;
        while (cur != null) {
            if (cur instanceof PoiView pv) {
                if (onPoiInfo != null) {
                    onPoiInfo.accept(pv.getPoi(), pv);
                }
                return;
            }
            if (cur == contentPane || cur == overlayRoot) break;
            cur = cur.getParent();
        }
    }


}