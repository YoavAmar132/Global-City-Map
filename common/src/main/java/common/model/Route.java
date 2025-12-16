package common.model;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
public class Route implements Serializable {
    public static final int BASE_ZOOM = Poi.BASE_ZOOM; // keep one base for whole app

    private final int id;
    private final String name;
    private final String description;
    private final POI_Category category;

    // each point: [baseWorldX, baseWorldY]
    private final List<double[]> basePoints = new ArrayList<>();

    public Route(int id, String name, String description,
                 POI_Category category, List<double[]> basePoints) {
        this.id = id;
        this.name = name;
        this.description=description;
        this.category=category;
        if (basePoints != null) this.basePoints.addAll(basePoints);
    }

    public int getId() { return id; }
    public String getName() { return name; }

    public String getDescription() {
        return description;
    }

    public POI_Category getCategory() {
        return category;
    }

    public void addBasePoint(double baseWorldX, double baseWorldY) {
        basePoints.add(new double[]{baseWorldX, baseWorldY});
    }

    public List<double[]> getBasePoints() {
        return basePoints;
    }

    // Convert BASE_ZOOM coords -> current zoom coords
    public List<double[]> getPointsAtZoom(int zoomLevel) {
        double scale = Math.pow(2, BASE_ZOOM - zoomLevel);
        List<double[]> out = new ArrayList<>(basePoints.size());
        for (double[] p : basePoints) {
            out.add(new double[]{ p[0] / scale, p[1] / scale });
        }
        return out;
    }
}
