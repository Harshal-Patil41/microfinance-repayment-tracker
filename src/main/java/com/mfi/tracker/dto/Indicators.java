package com.mfi.tracker.dto;

import java.math.BigDecimal;

public class Indicators {
    public final long totalBorrowers;
    public final long totalLoans;
    public final BigDecimal totalDisbursed;
    public final BigDecimal totalCollected;
    public final long overdueCount;
    public final long paidCount;
    public final long pendingCount;
    public final BigDecimal collectionRate;

    public Indicators(long totalBorrowers, long totalLoans, BigDecimal totalDisbursed, BigDecimal totalCollected,
                      long overdueCount, long paidCount, long pendingCount, BigDecimal collectionRate) {
        this.totalBorrowers = totalBorrowers;
        this.totalLoans = totalLoans;
        this.totalDisbursed = totalDisbursed;
        this.totalCollected = totalCollected;
        this.overdueCount = overdueCount;
        this.paidCount = paidCount;
        this.pendingCount = pendingCount;
        this.collectionRate = collectionRate;
    }
}
