package common.messages;

import java.io.Serializable;
import java.util.List;

public class BuyMapPayload implements Serializable {
    private int userId;
    private String cityName;
    private List<String> mapsList; // <--- Added as requested
    private double price;
    private String paymentDetails;

    public BuyMapPayload(int userId, String cityName, List<String> mapsList, double price, String paymentDetails) {
        this.userId = userId;
        this.cityName = cityName;
        this.mapsList = mapsList;
        this.price = price;
        this.paymentDetails = paymentDetails;
    }

    public int getUserId() { return userId; }
    public String getCityName() { return cityName; }
    public List<String> getMapsList() { return mapsList; } // Getter
    public double getPrice() { return price; }
    public String getPaymentDetails() { return paymentDetails; }
}