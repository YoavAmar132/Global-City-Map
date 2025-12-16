package gcm.server.service;

import common.model.MapSheet;
import common.model.Poi;
import common.model.User;
import gcm.server.data.MapRepo;
import gcm.server.data.PoiRepo;
import gcm.server.data.RouteRepo;
import gcm.server.data.UserRepo;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class MapService {
    private final MapRepo mapRepository;

    public MapSheet map;

    public MapService(MapRepo mapRepository) {
        this.mapRepository = mapRepository;
    }

  //store
    public boolean PendMap(MapSheet map)
    {
        System.out.println("map service created");
    return mapRepository.insertPendingMap(map);
    }
    //load map
    public MapSheet PullMap(int version)
    {
        return  mapRepository.loadPendingMap(version);
    }
}
