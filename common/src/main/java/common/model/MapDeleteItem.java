package common.model;

import java.io.Serializable;

public class MapDeleteItem implements Serializable {
    private final int mapId;
    private final String name;
    private final int version;

    public MapDeleteItem(int mapId, String name, int version) {
        this.mapId = mapId;
        this.name = name;
        this.version = version;
    }

    public int getMapId() { return mapId; }
    public String getName() { return name; }
    public int getVersion() { return version; }
}
