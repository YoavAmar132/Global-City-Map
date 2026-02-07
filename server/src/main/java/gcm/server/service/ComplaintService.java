package gcm.server.service;

import common.messages.GcmResponse;
import common.messages.UpdateComplaint;
import common.model.Complaint;
import common.model.User;
import gcm.server.bot.*;
import gcm.server.data.ComplaintRepo;
import gcm.server.network.CurrentServer;
import gcm.server.network.GcmServer;
import ocsf.server.ConnectionToClient;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Optional;

public class ComplaintService {

    private final ComplaintRepo complaintRepo;

    public ComplaintService( ComplaintRepo complaintRepo) {
        this.complaintRepo = complaintRepo;
    }


    public GcmResponse submitComplaint(Complaint complaint) throws SQLException {
        System.out.println("adding complaint");
        try {
            int id = complaintRepo.createComplaint(complaint.getUserId(), complaint.getText());
            complaint.setId(id);
            BotConfig botConfig = BotConfig.getInstance();

            if (!botConfig.isEnabled()) {
                System.out.println("setting complaint to wait for human");
                setWaitingForHuman(complaint.getId());
                return GcmResponse.ok(complaint);
            }

            System.out.println("setting complaint to wait for bot");
            setWaitingForBot(complaint.getId());
            return GcmResponse.ok(complaint);

        } catch (Exception e) {
            System.out.println("server error");
            return GcmResponse.error("Server Error: " + e.getMessage());
        }
    }

    //used for showing the customer support worker every complaint that they need to respond too
    public ArrayList<Complaint> getAllComplaintsForCustomerSupport() throws SQLException {
        return complaintRepo.getComplaintsWaitingForHuman();

    }

    public boolean hasInProgressComplaint() throws SQLException{
        return complaintRepo.hasInProgressComplaint();
    }
    public boolean claimNextComplaint() throws SQLException {
        return complaintRepo.claimNextComplaint();
    }


    public void setWaitingForBot(int ticketId) throws SQLException, IOException {
        complaintRepo.setWaitingForBot(ticketId);
        Complaint complaint = complaintRepo.getComplaint(ticketId);
        ConnectionToClient connection = getConnectionToId(complaint.getUserId());
        if(connection != null){
            System.out.println("sending to client: waiting for bot");
            connection.sendToClient(GcmResponse.ok(new UpdateComplaint(complaint)));
        }
    }

    public void setWaitingForHuman(int ticketId) throws SQLException, IOException {
        complaintRepo.setWaitingForHuman(ticketId);
        Complaint complaint = complaintRepo.getComplaint(ticketId);
        ConnectionToClient connection = getConnectionToId(complaint.getUserId());
        if(connection != null){
            System.out.println("sending to client: waiting for human");
            connection.sendToClient(GcmResponse.ok(new UpdateComplaint(complaint)));
        }
    }

    public boolean closeWithHumanAnswer(int ticketId, String response) throws SQLException, IOException {
        boolean isComplaintAlreadyClosed = complaintRepo.closeWithHumanAnswer(ticketId,response);
        if(isComplaintAlreadyClosed){
            System.out.println("complaint isn't closed");

            Complaint complaint = complaintRepo.getComplaint(ticketId);
            ConnectionToClient connection = getConnectionToId(complaint.getUserId());
            if(connection != null){
                System.out.println("sending to client: closed with human");

                connection.sendToClient(GcmResponse.ok(new UpdateComplaint(complaint)));
            }
        }
        return isComplaintAlreadyClosed;

    }


    public void closeWithBotAnswer(int ticketId, String response) throws SQLException, IOException {
        complaintRepo.closeWithBotAnswer(ticketId,response);
        Complaint complaint = complaintRepo.getComplaint(ticketId);
        ConnectionToClient connection = getConnectionToId(complaint.getUserId());
        if(connection != null){
            System.out.println("sending to client: closed with bot");

            connection.sendToClient(GcmResponse.ok(new UpdateComplaint(complaint)));
        }
    }


    public boolean isComplaintClosed(int ticketId) throws SQLException{
        Complaint complaint = complaintRepo.getComplaint(ticketId);
        return complaint.getStatus()==Complaint.Status.CLOSED;
    }

    public Optional<Complaint> getNextInProgressComplaint() throws SQLException{
        return complaintRepo.getNextInProgressComplaint();
    }



    public ArrayList<Complaint> getUserComplaints(int id) throws SQLException{
        return complaintRepo.getUserComplaints(id);
    }

    public Complaint getComplaint(int complaintId) throws SQLException{
        return complaintRepo.getComplaint(complaintId);
    }

    public void reassignHangingComplaintsForCustomerSupport() throws SQLException {
        complaintRepo.reassignHangingComplaintsForCustomerSupport();
    }

    private ConnectionToClient getConnectionToId(int id) throws SQLException {
        Thread[] clientThreadList = CurrentServer.getInstance().getServer().getClientConnections();
        for (int i=0; i<clientThreadList.length; i++)
        {
            try
            {
                ConnectionToClient connection = (ConnectionToClient) clientThreadList[i];
                User user = (User)(connection.getInfo("user"));
                if(user.getId()==id)
                    return connection;
            }
            // Ignore all exceptions when closing clients.
            catch(Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
        return null;
    }
}