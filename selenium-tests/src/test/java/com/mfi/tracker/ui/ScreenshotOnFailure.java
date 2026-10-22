package com.mfi.tracker.ui;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

/** Saves target/screenshots/<Class>_<method>.png when a UI test fails. */
public class ScreenshotOnFailure implements TestWatcher {

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        if (BaseUiTest.driver == null) {
            return;
        }
        try {
            Path dir = Path.of("target", "screenshots");
            Files.createDirectories(dir);
            String name = context.getRequiredTestClass().getSimpleName() + "_" + context.getRequiredTestMethod().getName() + ".png";
            Path file = ((TakesScreenshot) BaseUiTest.driver).getScreenshotAs(OutputType.FILE).toPath();
            Files.copy(file, dir.resolve(name), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Saved failure screenshot: " + dir.resolve(name).toAbsolutePath());
        } catch (Exception e) {
            System.out.println("Could not save screenshot: " + e.getMessage());
        }
    }
}
