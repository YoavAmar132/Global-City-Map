package common.model;

import java.io.Serializable;

public class Complaint implements Serializable {

    public enum Status {
        OPEN,
        WAITING_FOR_BOT,
        WAITING_FOR_HUMAN,
        IN_PROGRESS,
        CLOSED
    }

    private int complaintId = 0;
    private int userId;
    private String text;
    private Status status = Status.OPEN;
    private String response = null;
    private String responseBy = null;
    private int previousComplaintId = 0; //if the complaint isn't a response to a previous complaint then previousComplaintId = 0


    public Complaint(int userId, String text) {
        this.userId = userId;
        this.text = text;
    }

    public int getId() {
        return complaintId;
    }
    public void setId(int complaintId) {
        this.complaintId = complaintId;
    }
    public int getUserId() {
        return userId;
    }
    public String getText() {
        return text;
    }
    public int getPreviousComplaintId() {
        return previousComplaintId;
    }
    public void setPreviousComplaintId(int previousComplaintId) {
        this.previousComplaintId = previousComplaintId;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
    public Status getStatus() {return status;}

    public void setResponse(String response) {this.response = response;}
    public String getResponse() {return response;}
    public void setResponseBy(String responseBy) {this.responseBy = responseBy;}
    public String getResponseBy() {return responseBy;}
}
