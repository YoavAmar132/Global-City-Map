package common.messages;

import java.io.Serializable;
import java.util.List;

public class BuySubscriptionPayload implements Serializable {
    private int userId;
    private List<String> cityNames;
    private double price;
    private String paymentDetails;

    public BuySubscriptionPayload(int userId, List<String> cityNames, double price, String paymentDetails) {
        this.userId = userId;
        this.cityNames = cityNames;
        this.price = price;
        this.paymentDetails = paymentDetails;
    }

    public int getUserId() { return userId; }
    public List<String> getCityNames() { return cityNames; }
    public double getPrice() { return price; }
    public String getPaymentDetails() { return paymentDetails; }
}