package gcm.client.controllers.map;


public interface MapCoordinateMapper {
    double[] mapLonLatToView(double lon, double lat);
}
