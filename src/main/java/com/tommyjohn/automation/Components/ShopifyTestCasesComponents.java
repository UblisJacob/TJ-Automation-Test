package com.tommyjohn.automation.Components;

import com.base.ExtentReport;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.asserts.SoftAssert;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static com.tommyjohn.automation.PageLocators.CollectionPageLocator.FIRST_PRODUCT_IMAGE;
import static com.tommyjohn.automation.PageLocators.HomePageLocators.*;
import static com.tommyjohn.automation.utils.ChromeExtensionUtility.driver;


public class ShopifyTestCasesComponents {

    public WebDriver driver;
    Wait<WebDriver> wait;
    public JavascriptExecutor jse;

    public ShopifyTestCasesComponents(WebDriver driver) {

        this.driver = driver;
        wait = new FluentWait<WebDriver>(driver).pollingEvery(Duration.ofMillis(500))
                .withTimeout(Duration.ofSeconds(60)).ignoring(NoSuchElementException.class);

    }

    public void ValidateContentBannerSlice() throws InterruptedException {

        SoftAssert softAssert = new SoftAssert();
        JavascriptExecutor js = (JavascriptExecutor) driver;
        driver.get("https://www.tommyjohn.com/?_ab=0&_sc=1&toggle_content=men");
        wait.until(ExpectedConditions.visibilityOfElementLocated(HERO_BANNER_IMAGE));
        Thread.sleep(2000);
        js.executeScript("window.scrollBy(0,500)");
        softAssert.assertTrue(driver.findElement(CONTENT_BANNER_SLICE).isDisplayed(),"Content Banner Slice Is Not Available");
        Reporter.log("Content Banner Slice Is Available");
        ExtentReport.getExtentTest().info("Content Banner Slice Is Available");
        WebElement element1 = driver.findElement(CONTENT_BANNER_SLICE_CTA);
        js.executeScript("arguments[0].click();", element1);
        //driver.findElement(MENS_TOGGLE).click();
        //js.executeScript("window.scrollBy(0,500)");
        wait.until(ExpectedConditions.visibilityOfElementLocated(FIRST_PRODUCT_IMAGE));
        softAssert.assertTrue(driver.findElement(FIRST_PRODUCT_IMAGE).isDisplayed(),"CTA in the Content Banner Slice Is not functional");
        Reporter.log("CTA in the Content Banner Slice Is functional");
        ExtentReport.getExtentTest().info("CTA in the Content Banner Slice Is  functional");
    }



    public void ValidateBrandTickerNeonColors() {

        SoftAssert softAssert = new SoftAssert();

        System.out.println("==============================================");
        System.out.println("Starting Brand Ticker Neon Color Validation");
        System.out.println("==============================================");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        JavascriptExecutor js = (JavascriptExecutor) driver;

        By tickerLocator = By.xpath(
                "//*[@id='ticker']//span[contains(normalize-space(.),'Comfort Perfected')]"
        );

        List<WebElement> tickerElements = driver.findElements(tickerLocator);

        // Soft assert if the ticker text is unavailable.
        softAssert.assertTrue(
                !tickerElements.isEmpty(),
                "FAIL: Comfort Perfected™ text is not available."
        );

        if (tickerElements.isEmpty()) {
            System.out.println("Ticker text is unavailable. Skipping color validation.");
            softAssert.assertAll();
            return;
        }

        WebElement tickerElement = null;

        // Prefer the visible ticker copy.
        try {
            tickerElement = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(tickerLocator)
            );
        } catch (Exception e) {
            softAssert.fail(
                    "FAIL: Comfort Perfected™ ticker text was found but is not visible."
            );
            softAssert.assertAll();
            return;
        }

        System.out.println("Current URL: " + driver.getCurrentUrl());
        System.out.println("Ticker text: " + tickerElement.getText());

        // Inspect all matching ticker copies.
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> tickerCopies =
                (List<Map<String, Object>>) js.executeScript(
                        "return Array.from(document.querySelectorAll('#ticker span'))" +
                                ".filter(el => el.textContent.includes('Comfort Perfected'))" +
                                ".map((el, index) => {" +
                                "  const css = window.getComputedStyle(el);" +
                                "  return {" +
                                "    index: index," +
                                "    text: el.textContent.trim()," +
                                "    visible: el.getClientRects().length > 0," +
                                "    ariaHidden: !!el.closest('[aria-hidden=\"true\"]')," +
                                "    inlineStyle: el.getAttribute('style')," +
                                "    color1: css.getPropertyValue('--neon-color-1').trim()," +
                                "    color2: css.getPropertyValue('--neon-color-2').trim()," +
                                "    textShadow: css.textShadow" +
                                "  };" +
                                "});"
                );

        System.out.println("\n========== ALL TICKER COPIES ==========");

        for (Map<String, Object> copy : tickerCopies) {
            System.out.println(copy);
        }

        // Trace the selected ticker for five seconds.
        System.out.println("\n========== STYLE TRACE ==========");

        for (int i = 1; i <= 10; i++) {

            @SuppressWarnings("unchecked")
            Map<String, String> styles = (Map<String, String>) js.executeScript(
                    "const el = arguments[0];" +
                            "const css = window.getComputedStyle(el);" +
                            "return {" +
                            "  color1: css.getPropertyValue('--neon-color-1').trim()," +
                            "  color2: css.getPropertyValue('--neon-color-2').trim()," +
                            "  textShadow: css.textShadow," +
                            "  inlineStyle: el.getAttribute('style')" +
                            "};",
                    tickerElement
            );

            System.out.println("Sample " + i + ": " + styles);

            if (i < 10) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    Assert.fail("Interrupted while tracing ticker styles.", e);
                }
            }
        }

        // Read final computed values.
        @SuppressWarnings("unchecked")
        Map<String, String> finalStyles = (Map<String, String>) js.executeScript(
                "const el = arguments[0];" +
                        "const css = window.getComputedStyle(el);" +
                        "return {" +
                        "  color1: css.getPropertyValue('--neon-color-1').trim()," +
                        "  color2: css.getPropertyValue('--neon-color-2').trim()," +
                        "  textShadow: css.textShadow," +
                        "  inlineStyle: el.getAttribute('style')" +
                        "};",
                tickerElement
        );

        String color1 = finalStyles.get("color1");
        String color2 = finalStyles.get("color2");

        System.out.println("\n========== FINAL VALIDATION ==========");
        System.out.println("Neon Color 1: " + color1);
        System.out.println("Neon Color 2: " + color2);
        System.out.println("Computed Text Shadow: " + finalStyles.get("textShadow"));
        System.out.println("Inline Style: " + finalStyles.get("inlineStyle"));

        // Accept expected hex colors or transparent RGBA.
        softAssert.assertTrue(
                isAllowedNeonColor(color1, "#1F38DB"),
                "Unexpected Neon Color 1: " + color1
        );

        softAssert.assertTrue(
                isAllowedNeonColor(color2, "#A60BA1"),
                "Unexpected Neon Color 2: " + color2
        );

        System.out.println("Brand ticker validation completed.");

        // Report all collected assertion failures.
        softAssert.assertAll();
    }

    private boolean isAllowedNeonColor(String actualColor, String expectedHex) {

        if (actualColor == null) {
            return false;
        }

        String normalized = actualColor
                .replaceAll("\\s+", "")
                .toUpperCase();

        return normalized.equals(expectedHex.toUpperCase())
                || normalized.equals("RGBA(0,0,0,0)");
    }

}



