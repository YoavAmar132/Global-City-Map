package gcm.client.controllers.map;

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

public class MapBaseLayerController {

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

        File testTile = new File(
                "C:/Users/Ayoav/IdeaProjects/Global_City_Map/Global_City_Map/client/src/main/resources/gcm/client/map/Tiels/13/4891/3303.jpg"
        );
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
        centerOnAvailableTiles();
        redraw();
    }

    private void initMouseHandlers() {

        mapCanvas.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            dragStartX = e.getX();
            dragStartY = e.getY();
        });

        mapCanvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
            // IMPORTANT: do NOT check e.getButton()
            double dx = e.getX() - dragStartX;
            double dy = e.getY() - dragStartY;

            offsetX += dx;
            offsetY += dy;

            dragStartX = e.getX();
            dragStartY = e.getY();

            redraw();
        });

        mapCanvas.addEventHandler(ScrollEvent.SCROLL, e -> {
            if (e.getDeltaY() > 0 && zoom < MAX_ZOOM) {
                setZoom(zoom + 1);
            } else if (e.getDeltaY() < 0 && zoom > MIN_ZOOM) {
                setZoom(zoom - 1);
            }
        });
    }


    /* ========= public API used by MapViewerController ========= */

    public void setZoom(int newZoom) {
        int clamped = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, newZoom));
        if (clamped == this.zoom) return;
        this.zoom = clamped;
        centerOnAvailableTiles();
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
        if (gc == null) return;

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

        int drawn = 0;

        for (int tx = minTileX; tx <= maxTileX; tx++) {
            for (int ty = minTileY; ty <= maxTileY; ty++) {
                if (tx < 0 || ty < 0 || tx >= tilesCount || ty >= tilesCount) continue;

                File tileFile = new File(
                        tileRoot + File.separator + z + File.separator +
                                tx + File.separator + ty + ".jpg"
                );
                if (!tileFile.exists()) continue;

                Image img = new Image(tileFile.toURI().toString(), false);

                double worldX = tx * TILE_SIZE;
                double worldY = ty * TILE_SIZE;
                double screenX = worldX + offsetX;
                double screenY = worldY + offsetY;

                gc.drawImage(img, screenX, screenY, TILE_SIZE, TILE_SIZE);
                drawn++;
            }
        }
        System.out.println("drawTiles: zoom=" + z + " tilesDrawn=" + drawn);
    }

    /** copied from your test app, adapted to use tileRoot + mapCanvas */
    private void centerOnAvailableTiles() {
        if (mapCanvas.getWidth() <= 0 || mapCanvas.getHeight() <= 0) return;

        File zoomDir = new File(tileRoot + File.separator + zoom);
        File[] xDirs = zoomDir.listFiles(File::isDirectory);
        if (xDirs == null || xDirs.length == 0) return;

        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;

        for (File xDir : xDirs) {
            int tx;
            try {
                tx = Integer.parseInt(xDir.getName());
            } catch (NumberFormatException e) {
                continue;
            }
            minX = Math.min(minX, tx);
            maxX = Math.max(maxX, tx);

            File[] yFiles = xDir.listFiles(File::isFile);
            if (yFiles == null) continue;
            for (File f : yFiles) {
                String name = f.getName(); // e.g. "3303.jpg"
                int dot = name.indexOf('.');
                if (dot <= 0) continue;
                try {
                    int ty = Integer.parseInt(name.substring(0, dot));
                    minY = Math.min(minY, ty);
                    maxY = Math.max(maxY, ty);
                } catch (NumberFormatException ignored) {}
            }
        }

        if (minX == Integer.MAX_VALUE || minY == Integer.MAX_VALUE) return;

        double centerTileX = (minX + maxX) / 2.0;
        double centerTileY = (minY + maxY) / 2.0;

        double centerWorldX = centerTileX * TILE_SIZE;
        double centerWorldY = centerTileY * TILE_SIZE;

        offsetX = mapCanvas.getWidth()  / 2.0 - centerWorldX;
        offsetY = mapCanvas.getHeight() / 2.0 - centerWorldY;
    }
}
