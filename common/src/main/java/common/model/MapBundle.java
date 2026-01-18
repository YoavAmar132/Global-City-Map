package common.model;
import java.io.Serializable;
import java.util.ArrayList;

public class MapBundle implements Serializable {
    private final MapSheet selected;
    private final ArrayList<MapSheet> allSheets;

    public MapBundle(MapSheet selected, ArrayList<MapSheet> allSheets) {
        this.selected = selected;
        this.allSheets = allSheets;
    }

    public MapSheet getSelected() { return selected; }
    public ArrayList<MapSheet> getAllSheets() { return allSheets; }
}
