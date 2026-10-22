package com.mfi.tracker.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

/** J2 (US2): add a loan and see its generated schedule. */
class J2_AddLoanUiTest extends BaseUiTest {

    @Test
    void loanOfFourWeeklyInstallmentsGeneratesFourRows() {
        open("/loans/new");
        new Select(driver.findElement(By.id("borrowerId"))).selectByVisibleText("Asha Patil · Rampur");
        driver.findElement(By.id("amount")).sendKeys("1200");
        driver.findElement(By.id("installments")).clear();
        driver.findElement(By.id("installments")).sendKeys("4");
        new Select(driver.findElement(By.id("frequency"))).selectByValue("WEEKLY");
        driver.findElement(By.id("save-loan")).click();

        assertTrue(visible(testId("flash-success")).getText().contains("4 installments"));
        wait.until(ExpectedConditions.numberOfElementsToBe(testId("installment-row"), 4));
        assertEquals("₹300.00", driver.findElements(testId("amount-due")).get(0).getText());
    }

    @Test
    void zeroAmountIsRejected() {
        open("/loans/new");
        new Select(driver.findElement(By.id("borrowerId"))).selectByVisibleText("Asha Patil · Rampur");
        driver.findElement(By.id("amount")).sendKeys("0");
        driver.findElement(By.id("save-loan")).click();

        assertEquals("Loan amount must be greater than 0", visible(testId("error-amount")).getText());
    }
}
