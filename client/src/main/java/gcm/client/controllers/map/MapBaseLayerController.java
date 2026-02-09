package gcm.client.controllers.map;

import gcm.client.utill.ClientApp;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;


import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

public class MapBaseLayerController {
    public enum InteractionMode {
        VIEW,
        ADD_POI,
        ADD_ROUTE
    }

    private URI tileRootUri;


    private InteractionMode mode = InteractionMode.VIEW;
    // make tiles cache to load smoother
    private final java.util.Map<String, Image> tileCache = new java.util.HashMap<>();

    public void setInteractionMode(InteractionMode mode) {
        System.out.println("BaseLayer mode set to: " + mode);
        this.mode = mode;
    }
    public InteractionMode getInteractionMode() {
        return this.mode;
    }

    // callback that you will set from MapViewerController
    private Consumer<double[]> onPoiClick;

    private Runnable onRightClick;

    public void setOnRightClick(Runnable r) {
        this.onRightClick = r;
    }


    public void setOnPoiClick(Consumer<double[]> handler) {
        System.out.println("BaseLayer: onPoiClick handler set = " + (handler != null));
        this.onPoiClick = handler;
    }
    private static final int TILE_SIZE = 256;

    // folder with zoom/x/y.jpg inside
    private String tileRoot = "Tiels";   // can be overridden with setTileRoot()

    // zoom levels we actually have
    private int zoom = 13;
    private static final int MIN_ZOOM = 13;
    private static final int MAX_ZOOM = 16;

    // pan offset in WORLD pixels
    private double offsetX = 0;
    private double offsetY = 0;

    private double dragStartX, dragStartY;

    @FXML
    private Canvas mapCanvas;

    private GraphicsContext gc;

    // callback so overlay can rerender
    private Runnable onViewChanged;

    public void setOnViewChanged(Runnable onViewChanged) {
        this.onViewChanged = onViewChanged;
    }

    /** Optional: set absolute or relative tiles folder from MapViewerController */
    public void setTileRoot(String path) {
        this.tileRoot = path;
        centerOnAvailableTiles();
        redraw();
    }


    @FXML
    private void initialize() {
        gc = mapCanvas.getGraphicsContext2D();


        tileRootUri = Paths.get(
                "C:/Users/ADAM/Desktop/DONT YOU DARE/Labs/Project/Global-City-Map/client/src/main/resources/gcm/client/map/Haifa/13/4891/3303.jpg"
        ).toUri();
        File testTile = new File(tileRootUri);
        System.out.println("DEBUG testTile: " + testTile.getAbsolutePath()
                + " exists=" + testTile.exists());


        AnchorPane parent = (AnchorPane) mapCanvas.getParent();

        parent.widthProperty().addListener((obs, o, n) -> {
            mapCanvas.setWidth(n.doubleValue());
            centerOnAvailableTiles();
            redraw();
        });
        parent.heightProperty().addListener((obs, o, n) -> {
            mapCanvas.setHeight(n.doubleValue());
            centerOnAvailableTiles();
            redraw();
        });


        mapCanvas.setWidth(parent.getWidth());
        mapCanvas.setHeight(parent.getHeight());

        initMouseHandlers();
        Platform.runLater(() -> {
            centerOnAvailableTiles();
            redraw();
        });

    }

    private void initMouseHandlers() {

        mapCanvas.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            System.out.println("MOUSE_PRESSED mode=" + mode);

            if (e.getButton() == MouseButton.SECONDARY) {
                if (onRightClick != null) onRightClick.run();
                return;
            }

            if (mode == InteractionMode.VIEW) {
                // start pan
                dragStartX = e.getX();
                dragStartY = e.getY();

            } else if (mode == InteractionMode.ADD_POI) {
                // click => world coords => callback
                double[] world = mapViewToWorld(e.getX(), e.getY());
                if (onPoiClick != null) {
                    onPoiClick.accept(world);
                }
            }
        });

        mapCanvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
            if (mode != InteractionMode.VIEW) return;

            double dx = e.getX() - dragStartX;
            double dy = e.getY() - dragStartY;
            offsetX += dx;
            offsetY += dy;
            dragStartX = e.getX();
            dragStartY = e.getY();
            redraw();
        });

        mapCanvas.addEventHandler(ScrollEvent.SCROLL, e -> {
            if (mode != InteractionMode.VIEW) return;  // no zoom in ADD_POI
            if (e.getDeltaY() > 0 && zoom < MAX_ZOOM) setZoom(zoom + 1);
            else if (e.getDeltaY() < 0 && zoom > MIN_ZOOM) setZoom(zoom - 1);
        });
    }



    /* ========= public API used by MapViewerController ========= */

    public void setZoom(int newZoom) {
        int clamped = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, newZoom));
        if (clamped == this.zoom) return;

        // zoom around screen center by default
        double anchorX = mapCanvas.getWidth() / 2.0;
        double anchorY = mapCanvas.getHeight() / 2.0;

        setZoomAt(clamped, anchorX, anchorY);
    }

    public void setZoomAt(int newZoom, double anchorScreenX, double anchorScreenY) {
        int clamped = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, newZoom));
        if (clamped == this.zoom) return;

        int oldZoom = this.zoom;
        double scale = Math.pow(2, clamped - oldZoom);

        // world point currently under the anchor
        double anchorWorldX = anchorScreenX - offsetX;
        double anchorWorldY = anchorScreenY - offsetY;

        // after zoom, world pixels scale
        double anchorWorldXNew = anchorWorldX * scale;
        double anchorWorldYNew = anchorWorldY * scale;

        // adjust offsets so anchor stays fixed on screen
        offsetX = anchorScreenX - anchorWorldXNew;
        offsetY = anchorScreenY - anchorWorldYNew;

        this.zoom = clamped;

        redraw();
    }


    public int getZoom() {
        return zoom;
    }

    /** world→screen, used by overlay mapper */
    public double[] mapToView(double worldX, double worldY) {
        return new double[]{worldX + offsetX, worldY + offsetY};
    }

    /** screen→world, if you need it later */
    public double[] mapViewToWorld(double screenX, double screenY) {
        return new double[]{screenX - offsetX, screenY - offsetY};
    }

    /* ================= drawing ================= */

    private void redraw() {
        if (gc == null)
        {
            System.out.println("gc was null");
            return;
        }

        gc.setFill(Color.LIGHTGRAY);
        gc.fillRect(0, 0, mapCanvas.getWidth(), mapCanvas.getHeight());

        drawTiles(gc, mapCanvas);

        if (onViewChanged != null) {
            onViewChanged.run();
        }
    }


    private void drawTiles(GraphicsContext gc, Canvas canvas) {
        int z = zoom;
        int tilesCount = 1 << z;

        double leftWorld   = -offsetX;
        double topWorld    = -offsetY;
        double rightWorld  = leftWorld + canvas.getWidth();
        double bottomWorld = topWorld + canvas.getHeight();

        int minTileX = (int) Math.floor(leftWorld  / TILE_SIZE);
        int maxTileX = (int) Math.floor(rightWorld / TILE_SIZE);
        int minTileY = (int) Math.floor(topWorld   / TILE_SIZE);
        int maxTileY = (int) Math.floor(bottomWorld/ TILE_SIZE);

        // ✅ CLAMP to legal tile indices to avoid huge loops
        int clampedMinX = Math.max(0, Math.min(minTileX, tilesCount - 1));
        int clampedMaxX = Math.max(0, Math.min(maxTileX, tilesCount - 1));
        int clampedMinY = Math.max(0, Math.min(minTileY, tilesCount - 1));
        int clampedMaxY = Math.max(0, Math.min(maxTileY, tilesCount - 1));

        // ✅ if view is outside the world (or overflow happened), nothing to draw
        if (clampedMaxX < clampedMinX || clampedMaxY < clampedMinY) {
            return;
        }

        int drawn = 0;

        for (int tx = clampedMinX; tx <= clampedMaxX; tx++) {
            for (int ty = clampedMinY; ty <= clampedMaxY; ty++) {
                File tileFile = new File(
                        tileRoot + File.separator + z + File.separator +
                                tx + File.separator + ty + ".jpg"
                );
                if (!tileFile.exists()) continue;

                String key = z + "/" + tx + "/" + ty;
                Image img = tileCache.get(key);
                if (img == null) {
                    img = new Image(tileFile.toURI().toString(), false); // background loading
                    tileCache.put(key, img);
                }

                double screenX = tx * TILE_SIZE + offsetX;
                double screenY = ty * TILE_SIZE + offsetY;

                if (!img.isError()) {
                    gc.drawImage(img, screenX, screenY, TILE_SIZE, TILE_SIZE);
                    drawn++;
                }
            }
        }

    }



    /** copied from your test app, adapted to use tileRoot + mapCanvas */
    private void centerOnAvailableTiles() {
        if (!(this.mapCanvas.getWidth() <= (double)0.0F) && !(this.mapCanvas.getHeight() <= (double)0.0F)) {
            File zoomDir;
            if (isRunningFromJar()) {
                System.out.println("ran from jar");
                String city = (new File(this.tileRoot)).getName();
                Path jarDir = this.getJarDir();
                Path tilesDir = jarDir.resolve("maps").resolve(city);
                this.tileRoot = tilesDir.toString();
                zoomDir = new File(this.tileRoot + File.separator + this.zoom);
            } else {
                System.out.println("ran from local");
                File test = new File(this.tileRoot);
                String pathh = test.getAbsolutePath();
                zoomDir = new File(pathh + File.separator + this.zoom);
            }

            File[] xDirs = zoomDir.listFiles(File::isDirectory);
            if (xDirs != null && xDirs.length != 0) {
                int minX = Integer.MAX_VALUE;
                int maxX = Integer.MIN_VALUE;
                int minY = Integer.MAX_VALUE;
                int maxY = Integer.MIN_VALUE;

                for(File xDir : xDirs) {
                    int tx;
                    try {
                        tx = Integer.parseInt(xDir.getName());
                    } catch (NumberFormatException var21) {
                        continue;
                    }

                    minX = Math.min(minX, tx);
                    maxX = Math.max(maxX, tx);
                    File[] yFiles = xDir.listFiles(File::isFile);
                    if (yFiles != null) {
                        for(File f : yFiles) {
                            String name = f.getName();
                            int dot = name.indexOf(46);
                            if (dot > 0) {
                                try {
                                    int ty = Integer.parseInt(name.substring(0, dot));
                                    minY = Math.min(minY, ty);
                                    maxY = Math.max(maxY, ty);
                                } catch (NumberFormatException var20) {
                                }
                            }
                        }
                    }
                }

                if (minX != Integer.MAX_VALUE && minY != Integer.MAX_VALUE) {
                    double centerTileX = (double)(minX + maxX) / (double)2.0F;
                    double centerTileY = (double)(minY + maxY) / (double)2.0F;
                    double centerWorldX = centerTileX * (double)256.0F;
                    double centerWorldY = centerTileY * (double)256.0F;
                    this.offsetX = this.mapCanvas.getWidth() / (double)2.0F - centerWorldX;
                    this.offsetY = this.mapCanvas.getHeight() / (double)2.0F - centerWorldY;
                }
            }
        }
    }

    public void handleExternalClick(double sceneX, double sceneY) {
        javafx.geometry.Point2D local = mapCanvas.sceneToLocal(sceneX, sceneY);

        if (mode == InteractionMode.ADD_POI && onPoiClick != null) {
            double[] world = mapViewToWorld(local.getX(), local.getY());
            onPoiClick.accept(world);
        }
    }
    public void handleExternalPress(double sceneX, double sceneY, boolean primaryDown) {
        if (!primaryDown) return;

        javafx.geometry.Point2D local = mapCanvas.sceneToLocal(sceneX, sceneY);

        if (mode == InteractionMode.VIEW) {
            dragStartX = local.getX();
            dragStartY = local.getY();
        } else if (mode == InteractionMode.ADD_POI && onPoiClick != null) {
            double[] world = mapViewToWorld(local.getX(), local.getY());
            onPoiClick.accept(world);
        }
    }

    public void handleExternalDrag(double sceneX, double sceneY, boolean primaryDown) {
        if (!primaryDown) return;
        if (mode != InteractionMode.VIEW) return;

        javafx.geometry.Point2D local = mapCanvas.sceneToLocal(sceneX, sceneY);

        double dx = local.getX() - dragStartX;
        double dy = local.getY() - dragStartY;

        offsetX += dx;
        offsetY += dy;

        dragStartX = local.getX();
        dragStartY = local.getY();

        redraw();
    }


    public void handleExternalScroll(double deltaY) {
        if (deltaY > 0 && zoom < MAX_ZOOM) setZoom(zoom + 1);
        else if (deltaY < 0 && zoom > MIN_ZOOM) setZoom(zoom - 1);
    }
    public void recenterNow() {
        centerOnAvailableTiles();
        redraw();
    }

    public Path getJarDir() {
        try {
            return Paths.get(this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isRunningFromJar() {
        try {
            String path = ClientApp.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            return path.endsWith(".jar");
        } catch (Exception var1) {
            return false;
        }
    }

}
