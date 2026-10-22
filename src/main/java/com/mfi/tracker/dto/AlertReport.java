package com.mfi.tracker.dto;

import java.util.List;

public class AlertReport {
    public final List<AlertRow> late;
    public final List<AlertRow> invalid;
    public final List<MissingSchedule> missing;

    public AlertReport(List<AlertRow> late, List<AlertRow> invalid, List<MissingSchedule> missing) {
        this.late = late;
        this.invalid = invalid;
        this.missing = missing;
    }
}
