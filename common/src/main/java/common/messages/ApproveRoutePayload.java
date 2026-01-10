package common.messages;

import java.io.Serializable;

public class ApproveRoutePayload implements Serializable {

    private final int routeId;
    private final Integer sourceRouteId; // null = new route

    public ApproveRoutePayload(int routeId, Integer sourceRouteId) {
        this.routeId = routeId;
        this.sourceRouteId = sourceRouteId;
    }

    public int getRouteId() {
        return routeId;
    }

    public Integer getSourceRouteId() {
        return sourceRouteId;
    }
}

