package com.mfi.tracker.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

/** J1 (US1): add a borrower; an empty name is rejected. */
class J1_AddBorrowerUiTest extends BaseUiTest {

    @Test
    void validBorrowerAppearsInTheList() {
        open("/borrowers/new");
        driver.findElement(By.id("name")).sendKeys("Selenium Tester");
        driver.findElement(By.id("phone")).sendKeys("9123456789");
        driver.findElement(By.id("village")).sendKeys("Testpur");
        driver.findElement(By.id("save-borrower")).click();

        assertTrue(visible(testId("flash-success")).getText().contains("Selenium Tester"));
        assertTrue(visible(testId("borrower-table")).getText().contains("Selenium Tester"));
    }

    @Test
    void emptyNameShowsErrorAndNothingIsSaved() {
        open("/borrowers/new");
        driver.findElement(By.id("phone")).sendKeys("9123456789");
        driver.findElement(By.id("village")).sendKeys("Testpur");
        driver.findElement(By.id("save-borrower")).click();

        assertEquals("Name is required", visible(testId("error-name")).getText());
        assertTrue(driver.getCurrentUrl().endsWith("/borrowers"));   // form re-displayed, POST /borrowers
        assertEquals(0, driver.findElements(testId("flash-success")).size());
    }
}
