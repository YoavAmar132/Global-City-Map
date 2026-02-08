package common.messages;

import common.model.City;

import java.io.Serializable;

public class Download implements Serializable {
    private int id;
    private int cityid;
    public Download(int id,int cityid)
    {
        this.id=id;
        this.cityid=cityid;
    }

    public int getId() {
        return id;
    }

    public int getCityid() {
        return cityid;
    }
}
