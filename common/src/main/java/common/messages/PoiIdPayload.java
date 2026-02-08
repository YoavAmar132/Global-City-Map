package common.messages;

import java.io.Serializable;

public class PoiIdPayload implements Serializable {

    private final int poiId;

    public PoiIdPayload(int poiId) {
        this.poiId = poiId;
    }

    public int getPoiId() {
        return poiId;
    }
}
