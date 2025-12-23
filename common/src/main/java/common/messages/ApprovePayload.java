package common.messages;

import common.model.MapSheet;

import java.io.Serializable;
import java.io.Serializable;
import java.rmi.MarshalledObject;

public class ApprovePayload implements Serializable  {

        private final MapSheet map;
        private final String CityName;
        public ApprovePayload(MapSheet map,String name)
        {
            this.map=map;
            this.CityName=name;
        }

    public MapSheet getMap() {
        return map;
    }

    public String getCityName() {
        return CityName;
    }
}
