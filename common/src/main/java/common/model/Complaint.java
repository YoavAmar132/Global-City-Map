package common.model;

import java.io.Serializable;

public class Complaint implements Serializable {
    private long id;
    private long userId;
    private String text;
    private long previousComplaintId;

    // getters + setters
    Complaint(long id, long userId, String text, long previousComplaintId) {
        this.id = id;
        this.userId = userId;
        this.text = text;
        this.previousComplaintId = previousComplaintId;
    }
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
    public long getUserId() {
        return userId;
    }
    public void setUserId(long userId) {
        this.userId = userId;
    }
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }
    public long getPreviousComplaintId() {
        return previousComplaintId;
    }
    public void setPreviousComplaintId(long previousComplaintId) {
        this.previousComplaintId = previousComplaintId;
    }
}
