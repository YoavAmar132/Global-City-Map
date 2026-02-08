package common.messages;

import common.model.User;

import java.io.Serializable;

public class CityMapsRequestPayload implements Serializable {
    private int cityId;
    private int userId;
    private User user; // for adding to view logs if "Customer" or "Guest"

    public CityMapsRequestPayload(int cityId, int userId, User user) {

        this.cityId = cityId;
        this.userId = userId;
        this.user = user;
    }

    public int getCityId() {
        return cityId;
    }
    public int getUserId() { return userId; }
    public User getUser() { return user; }
    public String getUserRole() {
        if (user == null) return "Guest";
        else return user.getRole();
    }
}
