package gcm.server.service;

import common.model.CityReportData;
import gcm.server.data.StatsRepo; // Import the new Repo
import java.time.LocalDate;
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
}