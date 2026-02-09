package common.messages;

import java.io.Serializable;

public class MapIdPayload implements Serializable {

    private final int mapId;

    public MapIdPayload(int mapId) {
        this.mapId = mapId;
    }

    public int getMapId() {
        return mapId;
    }
}
