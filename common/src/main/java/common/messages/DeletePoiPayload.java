package common.messages;

import java.io.Serializable;

public class DeletePoiPayload implements Serializable {

    private final int poiId;
    private final Integer sourceMapId; // null = global delete

    public DeletePoiPayload(int poiId, Integer sourceMapId) {
        this.poiId = poiId;
        this.sourceMapId = sourceMapId;
    }

    public int getPoiId() {
        return poiId;
    }

    public Integer getSourceMapId() {
        return sourceMapId;
    }
}
