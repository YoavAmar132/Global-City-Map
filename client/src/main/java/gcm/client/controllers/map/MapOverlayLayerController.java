package gcm.client.controllers.map;

import common.model.Poi;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

import java.util.*;
import java.util.function.BiConsumer;

public class MapOverlayLayerController {

    @FXML
    private AnchorPane overlayRoot; // Container for everything in overlay

    // Separate panes so lines are always behind POIs
    private Pane contentPane;
    private Pane linesPane;
    private Pane poiPane;

    private java.util.function.IntSupplier zoomSupplier;

    private final Map<Integer, PoiView> poiViews = new HashMap<>();

    private BiConsumer<Poi, Node> onPoiSelected;
    private BiConsumer<Poi, Node> onPoiInfo;
    private BiConsumer<Double, Double> onEmptyMapClick;
    private java.util.function.BiConsumer<Double, Double> onEmptyPress;
    private java.util.function.BiConsumer<Double, Double> onEmptyDrag;
    private java.util.function.DoubleConsumer onScroll;

    private MapCoordinateMapper mapper;
    private Runnable onEmptyRightClick;

    /* ====================== Route line support ====================== */

    private final List<RouteLine> routeLines = new ArrayList<>();

    private static class RouteLine {
        final Poi a;
        final Poi b;
        final Line line;

        RouteLine(Poi a, Poi b, Line line) {
            this.a = a;
            this.b = b;
            this.line = line;
        }
    }

    /* ====================== Callbacks setters ====================== */

    public void setOnEmptyPress(java.util.function.BiConsumer<Double, Double> h) {
        onEmptyPress = h;
    }

    public void setOnEmptyDrag(java.util.function.BiConsumer<Double, Double> h) {
        onEmptyDrag = h;
    }

    public void setOnScroll(java.util.function.DoubleConsumer h) {
        onScroll = h;
    }

    public void setOnEmptyMapClick(BiConsumer<Double, Double> handler) {
        this.onEmptyMapClick = handler;
    }

    public void setOnEmptyRightClick(Runnable r) {
        this.onEmptyRightClick = r;
    }

    public void setOnPoiSelected(BiConsumer<Poi, Node> handler) {
        this.onPoiSelected = handler;
    }

    public void setOnPoiInfo(BiConsumer<Poi, Node> handler) {
        this.onPoiInfo = handler;
    }

    public void setZoomSupplier(java.util.function.IntSupplier zoomSupplier) {
        this.zoomSupplier = zoomSupplier;
    }

    public void setMapper(MapCoordinateMapper mapper) {
        this.mapper = mapper;
        rerender();
    }

    @FXML
    private void initialize() {
        // create contentPane ONLY ONCE
        contentPane = new Pane();
        contentPane.setManaged(false);
        contentPane.setPickOnBounds(false);

        linesPane = new Pane();
        linesPane.setManaged(false);
        linesPane.setPickOnBounds(false);

        poiPane = new Pane();
        poiPane.setManaged(false);
        poiPane.setPickOnBounds(false);

        // IMPORTANT: lines behind POIs
        contentPane.getChildren().addAll(linesPane, poiPane);
        overlayRoot.getChildren().add(contentPane);

        // Clip overlayRoot
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
                    showInfo(e.getTarget());
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

            // LEFT PRESS ON EMPTY MAP
            if (onEmptyPress != null)
                onEmptyPress.accept(e.getSceneX(), e.getSceneY());

            e.consume();
        });

        overlayRoot.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_DRAGGED, e -> {
            if (isClickOnPoi(e.getTarget())) return;

            if (onEmptyDrag != null)
                onEmptyDrag.accept(e.getSceneX(), e.getSceneY());

            e.consume();
        });

        overlayRoot.addEventFilter(javafx.scene.input.ScrollEvent.SCROLL, e -> {
            if (onScroll != null)
                onScroll.accept(e.getDeltaY());

            e.consume();
        });

        overlayRoot.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_CLICKED, e -> {
            if (e.isStillSincePress() && !isClickOnPoi(e.getTarget())) {
                if (onEmptyMapClick != null)
                    onEmptyMapClick.accept(e.getSceneX(), e.getSceneY());
            }
        });
    }

    /**
     * MAIN HELPER FOR ROUTE PREVIEW:
     * - clears overlay
     * - adds POIs in route order
     * - numbers them 1..n
     */
    public void showRouteStopsNumbered(List<Poi> orderedStops) {
        clearAll();

        if (orderedStops == null || orderedStops.isEmpty()) return;

        for (Poi p : orderedStops)
            addPoi(p);

        // numbering 1..n
        for (int i = 0; i < orderedStops.size(); i++) {
            Poi p = orderedStops.get(i);
            PoiView view = poiViews.get(p.getId());
            if (view != null)
                view.setOrderIndex(i + 1);
        }

        rerender();
    }

    private void triggerSelection(Object target) {
        if (!(target instanceof Node node)) return;

        Node cur = node;
        while (cur != null) {
            if (cur instanceof PoiView pv) {
                if (onPoiSelected != null)
                    onPoiSelected.accept(pv.getPoi(), pv);
                return;
            }
            if (cur == poiPane || cur == contentPane || cur == overlayRoot)
                break;
            cur = cur.getParent();
        }
    }

    public void setPoiSelected(Node node, boolean selected) {
        if (node instanceof PoiView poiView)
            poiView.setSelected(selected);
    }

    public void markPoiSelected(Poi poi) {
        PoiView view = poiViews.get(poi.getId());
        if (view != null) view.setSelected(true);
    }

    public PoiView getPoiView(int poiId) {
        return poiViews.get(poiId);
    }

    public boolean isClickOnExistingPoi(double sceneX, double sceneY) {
        for (PoiView view : poiViews.values()) {
            if (view == null) continue;
            double dx = Math.abs(view.getLayoutX() - sceneX);
            double dy = Math.abs(view.getLayoutY() - sceneY);
            if (dx < 20 && dy < 20) return true;
        }
        return false;
    }

    public void addPoi(Poi poi) {
        if (poi == null) return;

        PoiView view = new PoiView(poi);
        poiViews.put(poi.getId(), view);

        // add into poiPane (not contentPane)
        poiPane.getChildren().add(view);

        if (mapper != null && zoomSupplier != null) {
            int z = zoomSupplier.getAsInt();
            double wx = poi.getWorldX(z);
            double wy = poi.getWorldY(z);
            double[] xy = mapper.mapLonLatToView(wx, wy);
            if (xy != null && xy.length >= 2) {
                view.setLayoutX(xy[0]);
                view.setLayoutY(xy[1]);
            }
        }
    }

    /* ====================== Route lines API ====================== */

    public void clearRouteLines() {
        routeLines.clear();
        if (linesPane != null)
            linesPane.getChildren().clear();
    }

    public void addRouteLine(Poi a, Poi b) {
        if (a == null || b == null || linesPane == null) return;

        Line line = new Line();
        line.setStroke(Color.ORANGE);
        line.setStrokeWidth(3);
        line.setMouseTransparent(true);

        linesPane.getChildren().add(line);
        routeLines.add(new RouteLine(a, b, line));
    }

    public void clearPoiSelections() {
        for (PoiView view : poiViews.values()) {
            if (view != null) view.setSelected(false);
        }
    }

    public List<Poi> getAllPois() {
        List<Poi> res = new ArrayList<>();
        for (PoiView view : poiViews.values()) {
            if (view != null) res.add(view.getPoi());
        }
        return res;
    }

    public void clearAll() {
        poiViews.clear();
        clearRouteLines();
        if (poiPane != null)
            poiPane.getChildren().clear();
    }

    public void rerender() {
        if (mapper == null || zoomSupplier == null) return;

        int z = zoomSupplier.getAsInt();

        // update POIs
        for (PoiView view : poiViews.values()) {
            if (view == null) continue;

            Poi p = view.getPoi();
            if (p == null) continue;

            double[] xy = mapper.mapLonLatToView(
                    p.getWorldX(z),
                    p.getWorldY(z)
            );

            if (xy != null && xy.length >= 2) {
                double w = view.getWidth() > 0 ? view.getWidth() : 24;
                double h = view.getHeight() > 0 ? view.getHeight() : 24;
                view.setLayoutX(xy[0] - w / 2.0);
                view.setLayoutY(xy[1] - h / 2.0);
            }
        }

        // update lines
        for (RouteLine rl : routeLines) {
            double[] p1 = mapper.mapLonLatToView(
                    rl.a.getWorldX(z),
                    rl.a.getWorldY(z)
            );
            double[] p2 = mapper.mapLonLatToView(
                    rl.b.getWorldX(z),
                    rl.b.getWorldY(z)
            );

            if (p1 != null && p2 != null) {
                rl.line.setStartX(p1[0]);
                rl.line.setStartY(p1[1]);
                rl.line.setEndX(p2[0]);
                rl.line.setEndY(p2[1]);
            }
        }
    }

    private boolean isClickOnPoi(Object target) {
        if (!(target instanceof Node node)) return false;

        Node cur = node;
        while (cur != null) {
            if (cur instanceof PoiView) return true;
            if (cur == poiPane || cur == contentPane || cur == overlayRoot)
                break;
            cur = cur.getParent();
        }
        return false;
    }

    private void showInfo(Object target) {
        if (!(target instanceof Node node)) return;

        Node cur = node;
        while (cur != null) {
            if (cur instanceof PoiView pv) {
                if (onPoiInfo != null)
                    onPoiInfo.accept(pv.getPoi(), pv);
                return;
            }
            if (cur == poiPane || cur == contentPane || cur == overlayRoot)
                break;
            cur = cur.getParent();
        }
    }

    public void updateRouteNumbers(List<Integer> orderedPoiIds) {
        for (PoiView view : poiViews.values())
            view.clearOrderIndex();

        for (int i = 0; i < orderedPoiIds.size(); i++) {
            PoiView view = poiViews.get(orderedPoiIds.get(i));
            if (view != null)
                view.setOrderIndex(i); // keeping your existing behavior
        }
    }
}
