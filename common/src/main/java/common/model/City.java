package common.model;

import java.io.Serializable;

/**
 * nthg special here
 */
public class City implements Serializable {

    private final int id;
    private final String name;

    public City(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
