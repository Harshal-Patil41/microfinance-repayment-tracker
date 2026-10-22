package com.mfi.tracker;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.mfi.tracker.dto.Indicators;
import com.mfi.tracker.model.RepaymentStatus;
import com.mfi.tracker.service.DashboardService;
import com.mfi.tracker.service.LoanService;
import com.mfi.tracker.service.RepaymentService;

/** US4-US6 / FR6-FR9 / BR8-BR9, checked against the hand-calculated seed data. */
@SpringBootTest
@Transactional
class DashboardAndStatusTest {

    @Autowired
    private DashboardService dashboardService;
    @Autowired
    private LoanService loanService;
    @Autowired
    private RepaymentService repaymentService;

    @Test
    void indicatorsMatchSeedData() {
        Indicators i = dashboardService.indicators();
        assertEquals(8, i.totalBorrowers);
        assertEquals(11, i.totalLoans);
        assertEquals(new BigDecimal("55500.00"), i.totalDisbursed);
        assertEquals(new BigDecimal("16650.00"), i.totalCollected);
        assertEquals(8, i.overdueCount);
        assertEquals(15, i.paidCount);
        assertEquals(19, i.pendingCount);
        assertEquals(new BigDecimal("55.5"), i.collectionRate);   // 16,650 / 30,000 due up to today
    }

    @Test
    void statusListsMatchSeedData() {
        assertEquals(8, repaymentService.byStatus(RepaymentStatus.OVERDUE).size());
        assertEquals(15, repaymentService.byStatus(RepaymentStatus.PAID).size());
        assertEquals(19, repaymentService.byStatus(RepaymentStatus.PENDING).size());
        assertEquals(42, repaymentService.byStatus(null).size());
    }

    @Test
    void searchMatchesNameOrVillagePartiallyAndIgnoringCase() {
        assertEquals(1, loanService.rows("imran").size());
        assertEquals(2, loanService.rows("RAMP").size());     // Rampur: Asha's two loans
        assertEquals(11, loanService.rows("").size());
        assertEquals(0, loanService.rows("zzz-nobody").size());
    }
}
