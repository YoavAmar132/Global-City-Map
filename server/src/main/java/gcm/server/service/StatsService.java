package gcm.server.service;

import common.messages.Message;
import common.model.CityReportData;
import gcm.server.data.StatsRepo; // Import the new Repo

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StatsService {
    private final StatsRepo statsRepo;

    // Inject StatsRepo via constructor
    public StatsService(StatsRepo statsRepo) {
        this.statsRepo = statsRepo;
    }

    public List<CityReportData> generateReport(LocalDate from, LocalDate to, int cityId) {
        // Validation Logic
        if (from == null || to == null) {
            throw new IllegalArgumentException("Dates cannot be null.");
        }
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("Start date cannot be after end date.");
        }

        // Delegate to Repository
        return statsRepo.generateReport(from, to, cityId);
    }
    public Message getMessages(int userid) throws SQLException {

        if(statsRepo.expDate(userid))
        {
            Message m =new Message("Subscription Experation","Your subscription is about to expire use the cod '30OFF' to" +
                    "get 30% disscount");
            return m;
        }

         return null;
    }
    public ArrayList<String> getHistory(int userid) throws SQLException {

        return statsRepo.getHistory(userid);


    }
}