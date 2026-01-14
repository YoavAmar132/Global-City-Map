package common.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PendingRoute implements Serializable {

    private int routeId;
    private String name;
    private String description;
    private int cityId;
    private Integer sourceRouteId; // null = new route

    // ORDER MATTERS – POI IDs ONLY
    private ArrayList<Integer> stops;
    private boolean isEdit;

    public PendingRoute(int routeId, String name, String description, int cityId, ArrayList<Integer> stops) {
        this.routeId = routeId;
        this.name = name;
        this.description = description;
        this.cityId = cityId;
        this.stops = stops;
        this.sourceRouteId = null;
        this.isEdit = false;
    }
    public PendingRoute(int routeId, String name, String description,
                        int cityId, ArrayList<Integer> stops,
                        Integer sourceRouteId) {
        this(routeId, name, description, cityId, stops);
        this.sourceRouteId = sourceRouteId;
    }

    public boolean isEdit() {
        return isEdit;
    }

    public void setEdit(boolean edit) {
        isEdit = edit;
    }

    public Integer getSourceRouteId() {
        return sourceRouteId;
    }

    public void setSourceRouteId(Integer sourceRouteId) {
        this.sourceRouteId = sourceRouteId;
    }


    public ArrayList<Integer> getStops() {
        return stops;
    }

    public int getCityId() {
        return cityId;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getRouteId() { return routeId; }

    public void setRouteId(int routeId) {
        this.routeId = routeId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public void setStops(ArrayList<Integer> stops) {
        this.stops = stops;
    }
}
