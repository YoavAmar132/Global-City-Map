package common.messages;

import java.io.Serializable;

public class ApproveRoutePayload implements Serializable {

    private int routeId;

    public ApproveRoutePayload(int routeId) {
        this.routeId = routeId;
    }

    public int getRouteId() {
        return routeId;
    }
}
