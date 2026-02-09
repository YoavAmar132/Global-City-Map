

package common.messages;

import common.model.Complaint;

public class SubmitComplaintPayload implements java.io.Serializable {
    private Complaint complaint;

    public SubmitComplaintPayload(Complaint complaint) {
        this.complaint = complaint;
    }

    public Complaint getComplaint() {
        return complaint;
    }
}
