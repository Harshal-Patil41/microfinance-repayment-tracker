package com.mfi.tracker.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.mfi.tracker.model.Frequency;
import com.mfi.tracker.model.RepaymentStatus;

/** One dashboard row: a loan with its borrower, paid total and overall status. */
public class LoanRow {
    public final Long loanId;
    public final Long borrowerId;
    public final String borrowerName;
    public final String village;
    public final BigDecimal amount;
    public final BigDecimal paid;
    public final int installments;
    public final int scheduleRows;
    public final Frequency frequency;
    public final LocalDate startDate;
    public final RepaymentStatus status;

    public LoanRow(Long loanId, Long borrowerId, String borrowerName, String village, BigDecimal amount,
                   BigDecimal paid, int installments, int scheduleRows, Frequency frequency,
                   LocalDate startDate, RepaymentStatus status) {
        this.loanId = loanId;
        this.borrowerId = borrowerId;
        this.borrowerName = borrowerName;
        this.village = village;
        this.amount = amount;
        this.paid = paid;
        this.installments = installments;
        this.scheduleRows = scheduleRows;
        this.frequency = frequency;
        this.startDate = startDate;
        this.status = status;
    }
}
