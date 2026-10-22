package com.mfi.tracker.dto;

/** A loan whose repayment schedule has fewer rows than its installment count. */
public class MissingSchedule {
    public final Long loanId;
    public final String borrowerName;
    public final String village;
    public final int expected;
    public final int found;

    public MissingSchedule(Long loanId, String borrowerName, String village, int expected, int found) {
        this.loanId = loanId;
        this.borrowerName = borrowerName;
        this.village = village;
        this.expected = expected;
        this.found = found;
    }
}
