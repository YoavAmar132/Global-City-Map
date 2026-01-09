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
    public MapSheet(int version, String name, String description, String path,
                    ArrayList<Route> routes, ArrayList<Poi> pois) {

        this.version = version;
        this.name = name;
        this.description = description;
        this.path = path;

        this.routes = (routes != null) ? routes : new ArrayList<>();
        this.pois   = (pois != null)   ? pois   : new ArrayList<>();

        // POI bounds
        if (!this.pois.isEmpty()) {
            Poi firstp = this.pois.get(0);
            Poi lastp  = this.pois.get(this.pois.size() - 1);
            this.FirstPoi = firstp.getId();
            this.LastPoi  = lastp.getId();
        } else {
            this.FirstPoi = -1;
            this.LastPoi  = -1;
        }

        // Route bounds
        if (!this.routes.isEmpty()) {
            Route firstr = this.routes.get(0);
            Route lastr  = this.routes.get(this.routes.size() - 1);
            this.FirstRoute = firstr.getId();
            this.LastRoute  = lastr.getId();
        } else {
            this.FirstRoute = -1;
            this.LastRoute  = -1;
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
