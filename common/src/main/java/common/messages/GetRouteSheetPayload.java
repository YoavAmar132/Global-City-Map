package common.messages;

import java.io.Serializable;

public class GetRouteSheetPayload implements Serializable {
    private final int routeId;

    public GetRouteSheetPayload(int routeId) {
        this.routeId = routeId;
    }

    public int getRouteId() {
        return routeId;
    }
}

