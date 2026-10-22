package com.mfi.tracker.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

/** J3 (US4): search the dashboard by borrower name or village. */
class J3_SearchUiTest extends BaseUiTest {

    private void search(String text) {
        open("/dashboard");
        driver.findElement(By.id("search-box")).sendKeys(text);
        driver.findElement(By.id("search-btn")).click();
        wait.until(ExpectedConditions.urlContains("q="));
    }

    @Test
    void searchByNamePartialAndCaseInsensitive() {
        search("rav");
        List<String> names = texts("loan-borrower");
        assertFalse(names.isEmpty());
        assertTrue(names.stream().allMatch(n -> n.equals("Ravi Kumar")));
    }

    @Test
    void searchByVillage() {
        search("Rampur");
        List<String> names = texts("loan-borrower");
        assertFalse(names.isEmpty());
        assertTrue(names.stream().allMatch(n -> n.equals("Asha Patil")));
    }

    @Test
    void unknownTextShowsNoRecordsMessage() {
        search("zzz-nobody");
        assertEquals("No records found.", visibleId("no-records").getText());
    }
}
