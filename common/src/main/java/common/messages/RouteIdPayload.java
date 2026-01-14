package common.messages;

import java.io.Serializable;

public class RouteIdPayload implements Serializable {

    private int routeId;

    public RouteIdPayload(int routeId) {
        this.routeId = routeId;
    }

    public int getRouteId() {
        return routeId;
    }
}
