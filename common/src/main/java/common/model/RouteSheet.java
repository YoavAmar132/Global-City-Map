package common.model;

import java.io.Serializable;
import java.util.List;

public class RouteSheet implements Serializable {

    private final int routeId;
    private final int cityId;
    private final String cityTilePath;

    private final Route route;
    private final List<Poi> orderedPois;

    public RouteSheet(
            int routeId,
            int cityId,
            String cityTilePath,
            Route route,
            List<Poi> orderedPois
    ) {
        this.routeId = routeId;
        this.cityId = cityId;
        this.cityTilePath = cityTilePath;
        this.route = route;
        this.orderedPois = orderedPois;
    }

    public int getRouteId() { return routeId; }
    public int getCityId() { return cityId; }
    public String getCityTilePath() { return cityTilePath; }

    public Route getRoute() { return route; }
    public List<Poi> getOrderedPois() { return orderedPois; }
}
