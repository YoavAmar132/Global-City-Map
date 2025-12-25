package gcm.client.controllers.user_util;

import common.model.City;

public class PurchaseSession {
    private static PurchaseSession instance;

    // Session Data
    private int userID;        // <--- Added this
    private City selectedCity;
    private PurchaseType purchaseType;
    private double price;

    public enum PurchaseType {
        ONE_TIME_PURCHASE,
        SUBSCRIPTION
    }

    private PurchaseSession() {}

    public static synchronized PurchaseSession getInstance() {
        if (instance == null) {
            instance = new PurchaseSession();
        }
        return instance;
    }

    // Reset all data after purchase or cancellation
    public void clear() {
        this.selectedCity = null;
        this.purchaseType = null;
        this.price = 0.0;
        // Note: We typically DON'T clear userID here if the user stays logged in,
        // but if you want a clean slate for the transaction object:
        // this.userID = 0;
    }

    // --- Getters and Setters ---

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public City getSelectedCity() {
        return selectedCity;
    }

    public void setSelectedCity(City selectedCity) {
        this.selectedCity = selectedCity;
    }

    public PurchaseType getPurchaseType() {
        return purchaseType;
    }

    public void setPurchaseType(PurchaseType purchaseType) {
        this.purchaseType = purchaseType;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}