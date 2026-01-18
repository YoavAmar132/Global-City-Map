package common.messages;

import java.io.Serializable;

public class BuyMapPayload implements Serializable {
    private int userId;
    private String cityName;
    private double price;
    private boolean isSubscription;
    private int version; // <--- NEW FIELD
    private String credit;

    // Updated Constructor
    public BuyMapPayload(int userId, String cityName, double price, boolean isSubscription, int version) {
        this.userId = userId;
        this.cityName = cityName;
        this.price = price;
        this.isSubscription = isSubscription;
        this.version = version;
    }

    public String getCredit() {
        return credit;
    }

    public void setCredit(String credit) {
        this.credit = credit;
    }

    public int getUserId() { return userId; }
    public String getCityName() { return cityName; }
    public double getPrice() { return price; }
    public boolean isSubscription() { return isSubscription; }
    public int getVersion() { return version; } // <--- NEW GETTER
}