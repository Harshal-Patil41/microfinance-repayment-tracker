package com.mfi.tracker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.mfi.tracker.dto.AlertReport;
import com.mfi.tracker.service.AlertService;

/** US7 / FR10 / FR11 / BR7. */
@SpringBootTest
@Transactional
class AlertRulesTest {

    @Autowired
    private AlertService alertService;

    @Test
    void lateAlertsOnlyIncludeRepaymentsOverdueByMoreThanSevenDays() {
        AlertReport r = alertService.report();
        assertEquals(6, r.late.size());                       // Suresh x3, Ravi x2, Farah x1
        assertEquals("Suresh Rao", r.late.get(0).borrowerName);   // most overdue first (46 days)
        assertTrue(r.late.stream().allMatch(a -> a.daysOverdue > 7));
        // Ravi's installment due exactly 7 days ago and Meena's part-paid one are NOT alerts
        assertEquals(2, r.late.stream().filter(a -> a.borrowerName.equals("Ravi Kumar")).count());
        assertEquals(0, r.late.stream().filter(a -> a.borrowerName.equals("Meena Joshi")).count());
    }

    @Test
    void invalidPaymentsAreFlagged() {
        AlertReport r = alertService.report();
        assertEquals(2, r.invalid.size());
        assertTrue(r.invalid.stream().allMatch(a -> a.borrowerName.equals("Farah Khan")));
    }

    @Test
    void loanWithMissingScheduleIsFlagged() {
        AlertReport r = alertService.report();
        assertEquals(1, r.missing.size());
        assertEquals("Lakshmi Nair", r.missing.get(0).borrowerName);
        assertEquals(4, r.missing.get(0).expected);
        assertEquals(2, r.missing.get(0).found);
    }
}
