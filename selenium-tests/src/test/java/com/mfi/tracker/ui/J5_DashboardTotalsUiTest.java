package com.mfi.tracker.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/** J5 (US3 + US5): dashboard totals are correct and change when a payment is recorded. */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class J5_DashboardTotalsUiTest extends BaseUiTest {

    private BigDecimal collected() {
        open("/dashboard");
        return money(visibleId("total-collected").getText());
    }

    private WebElement firstInstallmentOfImran() {
        open("/dashboard");
        driver.findElement(By.id("search-box")).sendKeys("Imran");
        driver.findElement(By.id("search-btn")).click();
        visible(testId("loan-link")).click();
        return driver.findElements(testId("installment-row")).get(0);
    }

    private void pay(String amount) {
        WebElement row = firstInstallmentOfImran();
        row.findElement(By.name("amountPaid")).sendKeys(amount);
        row.findElement(By.cssSelector("button[type='submit']")).click();
    }

    @Test
    @Order(1)
    void totalDisbursedEqualsTheSumOfTheLoanTable() {
        open("/dashboard");
        BigDecimal shown = money(visibleId("total-disbursed").getText());
        BigDecimal sum = texts("loan-amount").stream().map(BaseUiTest::money).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(0, shown.compareTo(sum), "Dashboard total must equal the sum of the loans listed");
    }

    @Test
    @Order(2)
    void recordingAPaymentIncreasesTotalCollected() {
        BigDecimal before = collected();
        pay("500");
        assertTrue(visible(testId("flash-success")).getText().contains("Payment recorded"));
        BigDecimal after = collected();
        assertEquals(0, before.add(new BigDecimal("500")).compareTo(after));
    }

    @Test
    @Order(3)
    void paymentAboveAmountDueIsRejected() {
        BigDecimal before = collected();
        pay("999999");
        assertTrue(visible(testId("flash-error")).getText().contains("exceeds the amount still due"));
        assertEquals(0, before.compareTo(collected()));
    }
}
