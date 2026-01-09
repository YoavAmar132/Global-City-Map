package common.model;

import java.io.Serializable;
import java.util.List;

public class PendingRoute implements Serializable {


    private int routeId;          // optional, usually 0 before DB insert
    private int cityId;
    private String name;
    private String description;
    private int createdBy;        // userID
    private List<RouteStop> stops;

    public PendingRoute(int cityId,
                        String name,
                        String description,
                        int createdBy,
                        List<RouteStop> stops) {
        this.cityId = cityId;
        this.name = name;
        this.description = description;
        this.createdBy = createdBy;
        this.stops = stops;
    }


    public int getRouteId() {
        return routeId;
    }

    public int getCityId() {
        return cityId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public List<RouteStop> getStops() {
        return stops;
    }


    public void setRouteId(int routeId) {
        this.routeId = routeId;
    }

}
