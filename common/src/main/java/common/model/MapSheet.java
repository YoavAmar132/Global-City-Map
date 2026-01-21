package common.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MapSheet implements Serializable {
    private  int version;
    private  int cityID;
    private  int x;
    private  int y;
    private  String name;
    private  String description;
    private  String path;
    private ArrayList<Poi> pois = new ArrayList<>();
    private int FirstPoi=0;
    private int LastPoi=0;
    private boolean edit = false;   // true = edit existing map, false = new map
    private Integer sourceMapId; // null = new map

    public MapSheet(
            int version,
            int cityID,
            String name,
            String description,
            String path,
            ArrayList<Poi> pois
    ) {
        this.version = version;
        this.cityID = cityID;
        this.name = name;
        this.description = description;
        this.path = path;
        this.pois = (pois != null) ? pois : new ArrayList<>();

        if (!this.pois.isEmpty()) {
            this.FirstPoi = this.pois.get(0).getId();
            this.LastPoi  = this.pois.get(this.pois.size() - 1).getId();
        } else {
            this.FirstPoi = -1;
            this.LastPoi  = -1;
        }
    }

    public Integer getSourceMapId() {
        return sourceMapId;
    }

    public void setSourceMapId(Integer sourceMapId) {
        this.sourceMapId = sourceMapId;
    }

    public boolean isEdit() {
        return edit;
    }

    public void setEdit(boolean edit) {
        this.edit = edit;
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

    public void setVersion(int version) {
        this.version = version;
    }

    public void setCityID(int cityID) {
        this.cityID = cityID;
    }



    //getters

    public int getFirstPoi() {
        return FirstPoi;
    }

    public int getLastPoi() {
        return LastPoi;
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

    public int getCityID() {return cityID;}
    public ArrayList<Poi> getPois() {
        return pois;
    }

}
