package gcm.client.controllers.user_util;

import java.util.List;

public class PurchaseSession {
    private static PurchaseSession instance;

    // Data we need to keep alive across screens
    private int userId;
    private String cityName;
    private List<String> mapsList;
    private double price;
    private boolean isSubscription; // Helpful to track which radio was picked

    private PurchaseSession() {}

    public static PurchaseSession getInstance() {
        if (instance == null) {
            instance = new PurchaseSession();
        }
        return instance;
    }

    // Call this when the user clicks "Buy" in the Catalog (Step 3)
    public void startPurchase(int userId, String cityName, List<String> mapsList) {
        this.userId = userId;
        this.cityName = cityName;
        this.mapsList = mapsList;
        // Reset other fields
        this.price = 0;
        this.isSubscription = false;
    }

    // Call this when user clicks "Next" in BuyMapScreen (Step 5)
    public void updatePrice(double price, boolean isSubscription) {
        this.price = price;
        this.isSubscription = isSubscription;
    }

    // Getters for PaymentScreen (Step 6)
    public int getUserId() { return userId; }
    public String getCityName() { return cityName; }
    public List<String> getMapsList() { return mapsList; }
    public double getPrice() { return price; }
    public boolean isSubscription() { return isSubscription; }

    // Clear data when done
    public void clear() {
        instance = null;
    }
}