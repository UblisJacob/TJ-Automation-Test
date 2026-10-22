package com.tommyjohn.automation.Components;

import com.base.ExtentReport;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
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



    public void ValidateClickingTheUGCModule() throws InterruptedException {

        SoftAssert softAssert = new SoftAssert();
        Actions action = new Actions(driver);
        JavascriptExecutor js = (JavascriptExecutor) driver;

        driver.get("https://www.tommyjohn.com/?toggle_content=men");

        try {

            // Scroll to the UGC module
            js.executeScript("window.scrollBy(0,15000)");

            wait.until(
                    ExpectedConditions.visibilityOfElementLocated(UGC_MODUL_1)
            );

            WebElement ugcModule = driver.findElement(UGC_MODUL_1);

            softAssert.assertTrue(
                    ugcModule.isDisplayed(),
                    "UGC module is not available"
            );

            System.out.println("UGC module is available");
            Reporter.log("UGC module is available");
            ExtentReport.getExtentTest().info("UGC module is available");

            // Locate the first visible UGC post
            WebElement ugcPost = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(
                            FIRST_UGC_MODUL_1
                    )
            );

            js.executeScript(
                    "arguments[0].scrollIntoView({block:'center'});",
                    ugcPost
            );

            // Hover over the UGC post
            action.moveToElement(ugcPost).perform();

            // Validate See Details CTA
            WebElement seeDetails = ugcPost.findElement(
                    SEE_DETAILS_CTA
            );

            String ctaText = (String) js.executeScript(
                    "return window.getComputedStyle(arguments[0], '::after').content;",
                    seeDetails
            );

            softAssert.assertTrue(
                    ctaText != null && ctaText.contains("See Details"),
                    "See Details CTA is not available on hover"
            );

            System.out.println("See Details CTA is available on hover");
            Reporter.log("See Details CTA is available on hover");
            ExtentReport.getExtentTest()
                    .pass("See Details CTA is available on hover");

        } catch (Exception e) {

            String errorMessage =
                    "UGC module validation failed: " + e.getMessage();

            System.out.println(errorMessage);
            Reporter.log(errorMessage);
            ExtentReport.getExtentTest().fail(errorMessage);

            softAssert.fail(errorMessage);

        } finally {
            softAssert.assertAll();
        }
    }



    public void VerifyUpsellModulePDP() throws Exception {

        SoftAssert softAssert = new SoftAssert();
        JavascriptExecutor js1 = (JavascriptExecutor) driver;
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));

        try {

            // Navigate to the PDP using your existing framework
            new HomePageComponents(driver).navigateToMensAll();
            new CollectionPageComponent(driver).navigateToProductDetailsPage();

            // Scroll down to the Upsell module
            WebElement upsellModule = wait.until(
                    ExpectedConditions.presenceOfElementLocated(UPSELL_MODULE)
            );

            js1.executeScript(
                    "arguments[0].scrollIntoView({block:'center'});",
                    upsellModule
            );

            wait.until(ExpectedConditions.visibilityOf(upsellModule));

            // Validate Upsell module availability
            softAssert.assertTrue(
                    upsellModule.isDisplayed(),
                    "Upsell module is not available on PDP"
            );

            System.out.println("Upsell module is available on PDP");
            Reporter.log("Upsell module is available on PDP");
            ExtentReport.getExtentTest()
                    .pass("Upsell module is available on PDP");

            // Validate Shop Now CTA availability
            WebElement shopNowCTA = upsellModule.findElement(
                    By.xpath(".//a[normalize-space()='Shop Now']")
            );

            softAssert.assertTrue(
                    shopNowCTA.isDisplayed(),
                    "Shop Now CTA is not displayed in the Upsell module"
            );

            softAssert.assertTrue(
                    shopNowCTA.isEnabled(),
                    "Shop Now CTA is not enabled"
            );

            String actualProductUrl = shopNowCTA.getAttribute("href");

            String expectedProductUrl =
                    "https://www.tommyjohn.com/products/"
                            + "the-best-of-tj-trunk-4-3-pack"
                            + "?variant=42739639320621";

            softAssert.assertNotNull(
                    actualProductUrl,
                    "Shop Now CTA URL is missing"
            );

            if (actualProductUrl != null) {
                softAssert.assertEquals(
                        actualProductUrl,
                        expectedProductUrl,
                        "Shop Now CTA URL does not match the expected product URL"
                );
            }

            System.out.println("Shop Now CTA is available");
            Reporter.log("Shop Now CTA is available");
            ExtentReport.getExtentTest()
                    .pass("Shop Now CTA is available");

            // Click CTA only when it has a valid destination
            if (actualProductUrl != null
                    && actualProductUrl.contains(
                    "/products/the-best-of-tj-trunk-4-3-pack")) {

                shopNowCTA.click();

                // Validate navigation to the correct product page
                wait.until(ExpectedConditions.urlContains(
                        "/products/the-best-of-tj-trunk-4-3-pack"
                ));

                WebElement productTitle = wait.until(
                        ExpectedConditions.visibilityOfElementLocated(
                                UPSELL_PRODUCT_TITLE
                        )
                );

                softAssert.assertTrue(
                        productTitle.isDisplayed(),
                        "Expected Upsell product title is not displayed"
                );

                softAssert.assertEquals(
                        productTitle.getText().trim(),
                        "The Best of TJ Trunk 4\" (3-Pack)",
                        "Navigated to an unexpected product page"
                );

                System.out.println(
                        "Successfully navigated to: "
                                + productTitle.getText().trim()
                );

                Reporter.log(
                        "Successfully navigated to: "
                                + productTitle.getText().trim()
                );

                ExtentReport.getExtentTest()
                        .pass("Shop Now CTA navigated to the correct product page");

            } else {
                softAssert.fail(
                        "Shop Now CTA has an invalid product destination: "
                                + actualProductUrl
                );
            }

        } catch (Exception e) {

            String errorMessage =
                    "Upsell module validation failed: " + e.getMessage();

            System.out.println(errorMessage);
            Reporter.log(errorMessage);

            ExtentReport.getExtentTest().fail(errorMessage);

            softAssert.fail(errorMessage);

        } finally {
            softAssert.assertAll();
        }
    }


    public void ValidateActivitiesInspirationSection() {

        SoftAssert softAssert = new SoftAssert();
        JavascriptExecutor js = (JavascriptExecutor) driver;

        try {
            driver.get(
                    "https://www.tommyjohn.com/?_ab=0&_sc=1&toggle_content=men"
            );

            wait.until(
                    ExpectedConditions.visibilityOfElementLocated(HERO_BANNER_IMAGE)
            );

            WebElement activitiesSection = wait.until(
                    ExpectedConditions.presenceOfElementLocated(
                            ACTIVITIES_INSPIRATION_SECTION
                    )
            );

            js.executeScript(
                    "arguments[0].scrollIntoView({block:'center'});",
                    activitiesSection
            );

            wait.until(
                    ExpectedConditions.visibilityOf(activitiesSection)
            );

            softAssert.assertTrue(
                    activitiesSection.isDisplayed(),
                    "Activities & Inspiration section is not visible"
            );

            System.out.println("========================================");
            System.out.println("Activities & Inspiration Section");
            System.out.println("Section is available");
            System.out.println("========================================");

            Reporter.log("Activities & Inspiration section is available");
            ExtentReport.getExtentTest()
                    .info("Activities & Inspiration section is available");

            // Extract and print the initially visible section text
            List<WebElement> sectionTexts =
                    activitiesSection.findElements(ACTIVITIES_INSPIRATION_TEXT);

            for (WebElement element : sectionTexts) {

                if (element.isDisplayed()) {
                    String text = element.getText().trim();

                    if (!text.isEmpty()) {
                        System.out.println("Section Text: " + text);
                        Reporter.log("Section Text: " + text);
                        ExtentReport.getExtentTest().info(
                                "Section Text: " + text
                        );
                    }
                }
            }

            // Validate both activities and their corresponding images
            String[][] activities = {
                    {"Test Activity 1", "Doorway_1_19.jpg"},
                    {"Test Activity 2", "Doorway_1_18.jpg"}
            };

            for (String[] activity : activities) {

                String activityName = activity[0];
                String expectedImage = activity[1];

                System.out.println("----------------------------------------");
                System.out.println("Validating: " + activityName);

                try {
                    By activityButtonLocator = By.xpath(
                            ".//button[normalize-space()='"
                                    + activityName + "']"
                    );

                    WebElement activityButton = activitiesSection.findElement(
                            activityButtonLocator
                    );

                    js.executeScript(
                            "arguments[0].scrollIntoView({block:'center'});",
                            activityButton
                    );

                    wait.until(
                            ExpectedConditions.elementToBeClickable(activityButton)
                    );

                    activityButton.click();

                    // Validate that the selected activity is active
                    wait.until(driver ->
                            "true".equals(
                                    activityButton.getAttribute("aria-current")
                            )
                    );

                    softAssert.assertEquals(
                            activityButton.getAttribute("aria-current"),
                            "true",
                            activityName + " was not selected successfully"
                    );

                    // Find the panel controlled by the selected activity
                    String panelId = activityButton.getAttribute("aria-controls");

                    softAssert.assertNotNull(
                            panelId,
                            "Panel ID is missing for " + activityName
                    );

                    if (panelId == null || panelId.trim().isEmpty()) {
                        continue;
                    }

                    WebElement activityPanel = wait.until(
                            ExpectedConditions.visibilityOfElementLocated(
                                    By.id(panelId)
                            )
                    );

                    softAssert.assertTrue(
                            activityPanel.isDisplayed(),
                            activityName + " description panel is not visible"
                    );

                    // Extract the selected activity's description
                    List<WebElement> descriptionElements =
                            activityPanel.findElements(By.xpath(".//p"));

                    boolean descriptionFound = false;

                    for (WebElement description : descriptionElements) {

                        String descriptionText = description.getText().trim();

                        if (!descriptionText.isEmpty()) {
                            descriptionFound = true;

                            System.out.println(
                                    activityName + " Description: "
                                            + descriptionText
                            );

                            Reporter.log(
                                    activityName + " Description: "
                                            + descriptionText
                            );

                            ExtentReport.getExtentTest().info(
                                    activityName + " Description: "
                                            + descriptionText
                            );
                        }
                    }

                    softAssert.assertTrue(
                            descriptionFound,
                            "Description is missing for " + activityName
                    );

                    // Locate the image in the same activity container
                    WebElement activityImage = activityPanel.findElement(
                            By.xpath(
                                    "ancestor::div[contains(@class,'basis-1/2')][1]//img"
                            )
                    );

                    wait.until(
                            ExpectedConditions.visibilityOf(activityImage)
                    );

                    String imageSrc = activityImage.getAttribute("src");

                    softAssert.assertTrue(
                            imageSrc != null && !imageSrc.trim().isEmpty(),
                            "Image src is missing for " + activityName
                    );

                    if (imageSrc != null && !imageSrc.trim().isEmpty()) {

                        System.out.println(
                                activityName + " Image URL: " + imageSrc
                        );

                        Reporter.log(
                                activityName + " Image URL: " + imageSrc
                        );

                        ExtentReport.getExtentTest().info(
                                activityName + " Image URL: " + imageSrc
                        );

                        // Validate that the image belongs to the selected activity
                        softAssert.assertTrue(
                                imageSrc.contains(expectedImage),
                                "Incorrect image for " + activityName
                                        + ". Expected: " + expectedImage
                                        + ", Actual: " + imageSrc
                        );
                    }

                    // Confirm the browser successfully loaded the image
                    boolean imageLoaded = Boolean.TRUE.equals(
                            wait.until(driver ->
                                    Boolean.TRUE.equals(
                                            ((JavascriptExecutor) driver)
                                                    .executeScript(
                                                            "return arguments[0].complete"
                                                                    + " && arguments[0].naturalWidth > 0;",
                                                            activityImage
                                                    )
                                    )
                            )
                    );

                    softAssert.assertTrue(
                            imageLoaded,
                            "Image failed to load for " + activityName
                    );

                    if (imageLoaded) {
                        System.out.println(
                                activityName + " image loaded successfully"
                        );

                        Reporter.log(
                                activityName + " image loaded successfully"
                        );

                        ExtentReport.getExtentTest().pass(
                                activityName
                                        + " description and image validated"
                        );
                    }

                } catch (Exception e) {

                    String errorMessage = activityName
                            + " validation failed: " + e.getMessage();

                    System.out.println(errorMessage);
                    Reporter.log(errorMessage);

                    ExtentReport.getExtentTest().fail(errorMessage);

                    // Collect the failure and continue with the next activity
                    softAssert.fail(errorMessage);
                }
            }

        } catch (Exception e) {

            String errorMessage =
                    "Activities & Inspiration section validation failed: "
                            + e.getMessage();

            System.out.println(errorMessage);
            Reporter.log(errorMessage);

            ExtentReport.getExtentTest().fail(errorMessage);

            softAssert.fail(errorMessage);

        } finally {
            // Fail the test after all possible validations are completed
            softAssert.assertAll();
        }
    }



    public void ValidateFiftyFiftyFeaturedCollection() throws InterruptedException {

        SoftAssert softAssert = new SoftAssert();
        JavascriptExecutor js = (JavascriptExecutor) driver;

        try {
            driver.get("https://www.tommyjohn.com/?_ab=0&_sc=1&toggle_content=men");

            wait.until(ExpectedConditions.visibilityOfElementLocated(HERO_BANNER_IMAGE));

            WebElement featuredCollection = wait.until(
                    ExpectedConditions.presenceOfElementLocated(
                            FIFTY_FIFTY_FEATURED_COLLECTION
                    )
            );

            js.executeScript(
                    "arguments[0].scrollIntoView({block:'center'});",
                    featuredCollection
            );

            wait.until(ExpectedConditions.visibilityOf(featuredCollection));

            // Validate collection availability
            softAssert.assertTrue(
                    featuredCollection.isDisplayed(),
                    "50/50 Featured Collection is not available"
            );

            System.out.println("========================================");
            System.out.println("50/50 Featured Collection is available");
            System.out.println("========================================");

            Reporter.log("50/50 Featured Collection is available");
            ExtentReport.getExtentTest()
                    .info("50/50 Featured Collection is available");

            // Validate product cards
            List<WebElement> productCards = featuredCollection.findElements(
                    By.xpath(".//article[@data-product-id]")
            );

            softAssert.assertFalse(
                    productCards.isEmpty(),
                    "No products are available in the 50/50 Featured Collection"
            );

            System.out.println("Total Product Cards: " + productCards.size());

            // Validate and print product IDs
            for (WebElement productCard : productCards) {

                String productId = productCard.getAttribute("data-product-id");

                softAssert.assertNotNull(
                        productId,
                        "Product ID is missing from a product card"
                );

                if (productId != null) {
                    softAssert.assertFalse(
                            productId.trim().isEmpty(),
                            "Product ID is empty"
                    );

                    System.out.println("Product ID: " + productId);
                    Reporter.log("Product ID: " + productId);
                    ExtentReport.getExtentTest()
                            .info("Product ID: " + productId);
                }
            }

            // Validate product links
            List<WebElement> productLinks = featuredCollection.findElements(
                    By.xpath(
                            ".//article[@data-product-id]" +
                                    "//a[@data-cy='productTileLink' and @href]"
                    )
            );

            softAssert.assertFalse(
                    productLinks.isEmpty(),
                    "No product links are available in the Featured Collection"
            );

            // Navigate to the first product if a valid link exists
            if (!productLinks.isEmpty()) {

                String productUrl = productLinks.get(0).getAttribute("href");

                softAssert.assertNotNull(
                        productUrl,
                        "Product URL is missing"
                );

                if (productUrl != null && productUrl.contains("/products/")) {

                    System.out.println("Navigating to Product: " + productUrl);
                    Reporter.log("Navigating to Product: " + productUrl);

                    driver.get(productUrl);

                    // Validate product detail page
                    softAssert.assertTrue(
                            wait.until(ExpectedConditions.urlContains("/products/")),
                            "Product Detail Page URL is invalid"
                    );

                    WebElement productTitle = wait.until(
                            ExpectedConditions.visibilityOfElementLocated(
                                    By.xpath("//h1")
                            )
                    );

                    softAssert.assertTrue(
                            productTitle.isDisplayed(),
                            "Product Detail Page did not load successfully"
                    );

                    System.out.println(
                            "Successfully Navigated to Product: "
                                    + productTitle.getText().trim()
                    );

                    Reporter.log(
                            "Successfully Navigated to Product: "
                                    + productTitle.getText().trim()
                    );

                    ExtentReport.getExtentTest()
                            .info("Product Detail Page loaded successfully");

                } else {
                    softAssert.fail("Invalid product URL: " + productUrl);
                }
            }

        } catch (Exception e) {

            System.out.println(
                    "50/50 Featured Collection validation error: " + e.getMessage()
            );

            Reporter.log(
                    "50/50 Featured Collection validation error: " + e.getMessage()
            );

            ExtentReport.getExtentTest()
                    .fail("50/50 Featured Collection validation error: "
                            + e.getMessage());

            softAssert.fail(
                    "50/50 Featured Collection validation failed: "
                            + e.getMessage()
            );

        } finally {
            // Report all collected assertion failures
            softAssert.assertAll();
        }
    }


    public void ValidateAmbassadorSpotlight() throws InterruptedException {

        SoftAssert softAssert = new SoftAssert();
        JavascriptExecutor js = (JavascriptExecutor) driver;

        driver.get("https://www.tommyjohn.com/?_ab=0&_sc=1&toggle_content=men");

        wait.until(ExpectedConditions.visibilityOfElementLocated(HERO_BANNER_IMAGE));
        Thread.sleep(2000);

        // Scroll down to the Ambassador Spotlight section
        js.executeScript("window.scrollBy(0, 800)");

        try {
            WebElement spotlight = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(
                            AMBASSADOR_SPOTLIGHT
                    )
            );

            softAssert.assertTrue(
                    spotlight.isDisplayed(),
                    "Ambassador Spotlight Section Is Not Available"
            );

            System.out.println("========================================");
            System.out.println("Ambassador Spotlight Section Is Available");
            System.out.println("========================================");

            Reporter.log("Ambassador Spotlight Section Is Available");
            ExtentReport.getExtentTest()
                    .info("Ambassador Spotlight Section Is Available");

            // Extract and print the available text
            List<WebElement> textElements =
                    spotlight.findElements(AMBASSADOR_SPOTLIGHT_TEXT);

            for (WebElement element : textElements) {
                String text = element.getText().trim();

                if (element.isDisplayed() && !text.isEmpty()) {
                    System.out.println("Spotlight Text: " + text);

                    Reporter.log("Spotlight Text: " + text);
                    ExtentReport.getExtentTest()
                            .info("Spotlight Text: " + text);
                }
            }

        } catch (TimeoutException e) {
            softAssert.fail("Ambassador Spotlight Section Is Not Available");
        }

        softAssert.assertAll();
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

    public void ValidateLifeStyleDoorway() throws InterruptedException {

        SoftAssert softAssert = new SoftAssert();
        JavascriptExecutor js = (JavascriptExecutor) driver;
        driver.get("https://www.tommyjohn.com/?_ab=0&_sc=1&toggle_content=men");
        wait.until(ExpectedConditions.visibilityOfElementLocated(HERO_BANNER_IMAGE));
        Thread.sleep(2000);
        js.executeScript("window.scrollBy(0,800)");
        softAssert.assertTrue(driver.findElement(LIFESTYLE_DOORWAY).isDisplayed(),"Lifestyle Doorway Is Not Available");
        Reporter.log("Lifestyle Doorway Is Available");
        ExtentReport.getExtentTest().info("Lifestyle Doorway Is Available");
        WebElement element1 = driver.findElement(LIFESTYLE_DOORWAY_CTA);
        js.executeScript("arguments[0].click();", element1);
        //driver.findElement(MENS_TOGGLE).click();
        //js.executeScript("window.scrollBy(0,500)");
        wait.until(ExpectedConditions.visibilityOfElementLocated(FIRST_PRODUCT_IMAGE));
        softAssert.assertTrue(driver.findElement(FIRST_PRODUCT_IMAGE).isDisplayed(),"CTA in the Lifestyle Doorway Slice Is not functional");
        Reporter.log("CTA in the Lifestyle Doorway Is functional");
        ExtentReport.getExtentTest().info("CTA in the Lifestyle Doorway Is  functional");
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



