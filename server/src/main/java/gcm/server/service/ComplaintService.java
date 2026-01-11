package gcm.server.service;

import common.model.Complaint;
import gcm.server.bot.*;
import gcm.server.data.ComplaintRepo;

public class ComplaintService {

    private final BotAgent botAgent;
    private final ComplaintRepo complaintRepo;

    public ComplaintService(BotAgent botAgent, ComplaintRepo complaintRepo) {
        this.botAgent = botAgent;
        this.complaintRepo = complaintRepo;
    }



    /*
    * complaintRepo.addComplaint()
    * if previousComplaintId == null
    * complaintRepo.setComplaintWaitingForBot()
    * return success
    *else
    * complaintRepo.getAnsweredBy()
    * if bot or null:
    * complaintRepo.setComplaintWaitingForBot()
    * return success
    * else
    * (in botQueue,we get the first complaint that is waiting for bot ordered by time)
    *
    * look at init in software file
    *
    * */

    public boolean submitComplaint(Complaint complaint) {


        botAgent.handleComplaint(complaint.getText());
        return false;
    }
}
