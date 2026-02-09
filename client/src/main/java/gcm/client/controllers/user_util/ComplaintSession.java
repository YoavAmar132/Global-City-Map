package gcm.client.controllers.user_util;

import common.model.Complaint;

public class ComplaintSession {
    private static ComplaintSession instance;

    // Session Data
    private int userID;        // <--- Added this
    private Complaint selectedComplaint;

    private ComplaintSession() {}

    public static synchronized ComplaintSession getInstance() {
        if (instance == null) {
            instance = new ComplaintSession();
        }
        return instance;
    }

    // Reset all data after purchase or cancellation
    public void clear() {
        this.selectedComplaint = null;
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

    public Complaint getSelectedComplaint() {
        return selectedComplaint;
    }

    public void setSelectedComplaint(Complaint selectedComplaint) {
        this.selectedComplaint = selectedComplaint;
    }

}