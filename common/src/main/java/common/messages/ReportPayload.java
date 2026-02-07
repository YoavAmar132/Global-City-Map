package common.messages;

import java.io.Serializable;
import java.time.LocalDate;

public class ReportPayload implements Serializable {
    // strict requirement for serialization (optional but recommended to avoid version errors)
    private static final long serialVersionUID = 1L;

    private LocalDate fromDate;
    private LocalDate toDate;
    private int cityId;

    public ReportPayload(LocalDate fromDate, LocalDate toDate, int cityId) {
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.cityId = cityId;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public int getCityId() {
        return cityId;
    }
}