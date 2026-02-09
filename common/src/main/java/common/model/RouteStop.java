package common.model;

import java.io.Serializable;

public class RouteStop implements Serializable {

    private int poiId;
    private int order;
    private int recommendedMinutes;

    public RouteStop(int poiId, int order, int recommendedMinutes) {
        this.poiId = poiId;
        this.order = order;
        this.recommendedMinutes = recommendedMinutes;
    }


    public int getPoiId() {
        return poiId;
    }

    public int getOrder() {
        return order;
    }

    public int getRecommendedMinutes() {
        return recommendedMinutes;
    }

}
