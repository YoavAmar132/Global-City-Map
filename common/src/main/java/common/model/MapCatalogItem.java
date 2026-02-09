package common.model;

import java.io.Serializable;

public class MapCatalogItem implements Serializable {

    private final int mapId;
    private final String mapName;
    private final String description;
    private final double price;

    public MapCatalogItem(int mapId, String mapName,
                           String description, double price) {
        this.mapId = mapId;
        this.mapName = mapName;
        this.description = description;
        this.price = price;
    }

    public int getMapId() { return mapId; }
    public String getMapName() { return mapName; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
}
