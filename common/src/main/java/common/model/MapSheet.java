package common.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MapSheet implements Serializable {
    private  int version;
    private  String name;
    private  String description;
    private  String path;
    private ArrayList<Route> routes = new ArrayList<>();
    private ArrayList<Poi> pois = new ArrayList<>();
    private int FirstPoi=0;
    private int LastPoi=0;
    private int FirstRoute=0;
    private int LastRoute=0;
public MapSheet(int version, String name, String description,String path,
                ArrayList routes,ArrayList pois) {
    this.version = version;
    this.name = name;
    this.description = description;
    this.path = path;
    this.routes = routes;
    this.pois = pois;
    if (this.pois != null && this.routes != null) {
        Poi firstp = (Poi) pois.get(0);
        Poi lastp = (Poi) pois.get(pois.size() - 1);
        this.FirstPoi = firstp.getId();
        this.LastPoi = lastp.getId();
        Route firstr = (Route) routes.get(0);
        Route lastr = (Route) routes.get(routes.size() - 1);
        this.FirstRoute = firstr.getId();
        this.LastRoute = lastr.getId();

    }
}

    public void setDescription(String description) {
        this.description = description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setPois(ArrayList<Poi> pois) {
        this.pois = pois;
    }

    public void setRoutes(ArrayList<Route> routes) {
        this.routes = routes;
    }

    public void setVersion(int version) {
        this.version = version;
    }


    //getters

    public int getFirstPoi() {
        return FirstPoi;
    }

    public int getFirstRoute() {
        return FirstRoute;
    }

    public int getLastPoi() {
        return LastPoi;
    }

    public int getLastRoute() {
        return LastRoute;
    }

    public String getPath() {
        return path;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public int getVersion() {
        return version;
    }

    public ArrayList<Poi> getPois() {
        return pois;
    }

    public ArrayList<Route> getRoutes() {
        return routes;
    }
}
