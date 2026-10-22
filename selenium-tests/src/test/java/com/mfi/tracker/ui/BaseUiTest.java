package com.mfi.tracker.ui;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Shared browser setup. The tests run against an instance started with the "selenium" profile
 * (in-memory H2, port 8083), so they never touch the developer's data. Explicit waits only.
 */
public abstract class BaseUiTest {

    protected static final String BASE_URL = System.getProperty("baseUrl", "http://localhost:8083");
    protected static WebDriver driver;
    protected static WebDriverWait wait;

    @RegisterExtension
    ScreenshotOnFailure screenshotOnFailure = new ScreenshotOnFailure();

    @BeforeAll
    static void startBrowser() {
        ChromeOptions options = new ChromeOptions();
        if (!"false".equalsIgnoreCase(System.getProperty("headless", "true"))) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--no-sandbox", "--disable-dev-shm-usage", "--window-size=1400,1000");
        String chromeBin = System.getenv("CHROME_BIN");
        if (chromeBin != null && !chromeBin.isBlank()) {
            options.setBinary(chromeBin);
        }
        driver = new ChromeDriver(options);   // Selenium Manager fetches a matching chromedriver
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    static void stopBrowser() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

    protected void open(String path) {
        driver.get(BASE_URL + path);
    }

    protected static By testId(String id) {
        return By.cssSelector("[data-testid='" + id + "']");
    }

    protected WebElement visible(By by) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    protected WebElement visibleId(String id) {
        return visible(By.id(id));
    }

    protected List<String> texts(String testId) {
        return driver.findElements(testId(testId)).stream().map(WebElement::getText).collect(Collectors.toList());
    }

    /** "₹16,650.00" -> 16650.00 */
    protected static BigDecimal money(String text) {
        return new BigDecimal(text.replaceAll("[^0-9.\\-]", ""));
    }
}
