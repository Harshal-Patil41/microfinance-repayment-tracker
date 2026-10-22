package com.mfi.tracker.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/** Extra journey (US7): the alerts page. */
class J6_AlertsUiTest extends BaseUiTest {

    @Test
    void lateInvalidAndMissingSectionsShowTheExpectedRecords() {
        open("/alerts");
        assertEquals("6", visibleId("late-count").getText());
        assertEquals("2", visibleId("invalid-count").getText());
        assertEquals("1", visibleId("missing-count").getText());

        List<WebElement> late = driver.findElements(By.cssSelector("[data-testid='alerts-late'] [data-testid='alert-row']"));
        assertEquals(6, late.size());
        assertEquals("Suresh Rao", late.get(0).getAttribute("data-borrower"));   // most overdue first

        String missing = driver.findElement(testId("alerts-missing")).getText();
        assertTrue(missing.contains("Lakshmi Nair"));
        String invalid = driver.findElement(testId("alerts-invalid")).getText();
        assertTrue(invalid.contains("Farah Khan"));
    }
}
