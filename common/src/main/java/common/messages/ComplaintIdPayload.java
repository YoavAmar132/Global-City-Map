package common.messages;

import java.io.Serializable;

public class ComplaintIdPayload implements Serializable {
    private final int complaintId;

    public ComplaintIdPayload(int complaintId) {
        this.complaintId = complaintId;
    }

    public int getComplaintId() {
        return complaintId;
    }
}
