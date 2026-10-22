package com.mfi.tracker.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

/** J4 (US6): open the overdue list from the dashboard with one click. */
class J4_OverdueListUiTest extends BaseUiTest {

    @Test
    void overdueLinkListsOnlyOverdueRepayments() {
        open("/dashboard");
        int overdue = Integer.parseInt(visibleId("overdue-count").getText().trim());
        assertTrue(overdue > 0);

        driver.findElement(By.id("overdue-link")).click();      // 1 click (success criterion: 2 or fewer)
        wait.until(ExpectedConditions.urlContains("status=OVERDUE"));

        wait.until(ExpectedConditions.numberOfElementsToBe(testId("repayment-row"), overdue));
        List<String> statuses = texts("repayment-status");
        assertTrue(statuses.stream().allMatch(s -> s.equals("OVERDUE")));

        String table = driver.findElement(testId("repayment-table")).getText();
        assertTrue(table.contains("Ravi Kumar"));
        assertTrue(table.contains("Suresh Rao"));
        assertEquals(overdue, statuses.size());
    }

    @Test
    void paidFilterShowsOnlyPaidRows() {
        open("/dashboard");
        visibleId("paid-link").click();
        wait.until(ExpectedConditions.urlContains("status=PAID"));
        List<String> statuses = texts("repayment-status");
        assertTrue(!statuses.isEmpty() && statuses.stream().allMatch(s -> s.equals("PAID")));
    }
}
