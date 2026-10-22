package com.mfi.tracker.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** A late or invalid installment shown on the alerts page. */
public class AlertRow {
    public final Long repaymentId;
    public final Long loanId;
    public final int installmentNo;
    public final String borrowerName;
    public final String village;
    public final LocalDate dueDate;
    public final BigDecimal amountDue;
    public final BigDecimal amountPaid;
    public final long daysOverdue;
    public final String reason;

    public AlertRow(Long repaymentId, Long loanId, int installmentNo, String borrowerName, String village,
                    LocalDate dueDate, BigDecimal amountDue, BigDecimal amountPaid, long daysOverdue, String reason) {
        this.repaymentId = repaymentId;
        this.loanId = loanId;
        this.installmentNo = installmentNo;
        this.borrowerName = borrowerName;
        this.village = village;
        this.dueDate = dueDate;
        this.amountDue = amountDue;
        this.amountPaid = amountPaid;
        this.daysOverdue = daysOverdue;
        this.reason = reason;
    }
}
