package common.messages;

import common.model.Complaint;

import java.io.Serializable;

public class UpdateComplaint implements Serializable {
    Complaint complaint;
    public UpdateComplaint(Complaint complaint) {
        this.complaint = complaint;
    }
    public Complaint getComplaint() {
        return complaint;
    }
    public void setComplaint(Complaint complaint) {
        this.complaint = complaint;
    }
}
