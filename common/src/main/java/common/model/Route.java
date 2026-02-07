package common.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Route implements Serializable {

    private String name;
    private String description;

    // ORDER MATTERS
    private ArrayList<Integer> poiOrder;

    public Route(String name, String description, ArrayList<Integer> poiOrder) {
        this.name = name;
        this.description = description;
        this.poiOrder = poiOrder;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public ArrayList<Integer> getPoiOrder() { return poiOrder; }
}
