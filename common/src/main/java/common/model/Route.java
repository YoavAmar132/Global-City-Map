package common.model;


import java.util.List;

public class Route {
    private final int id;
    private final String name;
    private final List<Poi> poisInOrder;

    public Route(int id, String name, List<Poi> poisInOrder) {
        this.id = id;
        this.name = name;
        this.poisInOrder = poisInOrder;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public List<Poi> getPoisInOrder() { return poisInOrder; }
}
