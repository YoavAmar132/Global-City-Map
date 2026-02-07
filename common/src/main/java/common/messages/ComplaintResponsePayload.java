package common.messages;


import common.model.Complaint;

public class ComplaintResponsePayload implements java.io.Serializable {
    private int complaintId;
    private String complaintResponse;

    public ComplaintResponsePayload(int complaintId,String complaintResponse) {
        this.complaintId = complaintId;
        this.complaintResponse = complaintResponse;
    }

    public ComplaintResponsePayload(Complaint complaint) {
        this.complaintId = complaint.getId();
        this.complaintResponse = complaint.getResponse();
    }

    public int getComplaintId() {
        return complaintId;
    }
    public String getComplaintResponse() {
        return complaintResponse;
    }
}
