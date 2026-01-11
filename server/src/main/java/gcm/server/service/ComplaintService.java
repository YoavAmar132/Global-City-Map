package gcm.server.service;

import common.model.Complaint;
import gcm.server.bot.*;
import gcm.server.data.ComplaintRepo;

import java.sql.SQLException;
import java.util.ArrayList;

public class ComplaintService {

    private final ComplaintRepo complaintRepo;

    public ComplaintService( ComplaintRepo complaintRepo) {
        this.complaintRepo = complaintRepo;
    }



    /*
     * when user open his ocmplaint Area he send:
     * complaintRepo.getAllPreviousComplaints(userId)
     *
     *
     *
     * complaintRepo.addComplaint(complaint)
     * if complaint.getPreviousComplaintId() == -1
     * complaintRepo.setComplaintWaitingForBot(long id)
     * return success
     *else
     * complaintRepo.getAnsweredBy(long id)
     * if bot:
     * complaintRepo.setComplaintWaitingForBot()
     * return success
     * else if customer_support
     * complaintRepo.setComplaintWaitingForCustomerSupport()
     * else throw exception
     *
     * (in botQueue,we get the first complaint that is waiting for bot ordered by time)
     *  using complaintRepo.getNextUnansweredByBot()
     *  then we use complaintRepo.answerByBot(string answer)
     *  in customer support
     *  we call: complaintRepo.getAllUnansweredByCustomerSupport(id)
     *  then we use complaintRepo.answerByCustomerSupport(id,string answer)
     *
     *
     * look at init in software file
     *
     * */

    public void submitComplaint(Complaint complaint) throws SQLException {

        int id = complaintRepo.createComplaint(complaint.getUserId(), complaint.getText(), complaint.getPreviousComplaintId());
        complaint.setId(id);
        if (complaint.getPreviousComplaintId() == 0) {
            complaintRepo.setWaitingForBot(complaint.getId());
        } else {
            //getting the previous complaint and deciding base on it's respondent to whom the complaint will wait
            Complaint previousComplaint = complaintRepo.getComplaint(complaint.getPreviousComplaintId());
            if (previousComplaint.getResponseBy().equals("Bot")) {
                complaintRepo.setWaitingForBot(complaint.getId());
            } else if (previousComplaint.getResponseBy().equals("Human")) {
                complaintRepo.setWaitingForHuman(complaint.getId());
            }
        }
    }

    //used for showing the customer support worker every complaint that they need to respond too
    public ArrayList<Complaint> getAllComplaintsForCustomerSupport() throws SQLException {
        return complaintRepo.getComplaintsWaitingForHuman();
    }
}