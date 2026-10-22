package TestModules;

import com.base.ExtentReport;
import com.tommyjohn.automation.Components.HomePageComponents;
import com.tommyjohn.automation.Components.ShopifyTestCasesComponents;
import com.tommyjohn.automation.utils.CustomUtilities;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public class ValidateShopifyTests  {

    public WebDriver driver;

    @BeforeMethod(alwaysRun = true)
    @Parameters("browser")
    public void setup(String browser) {
        try {
            driver = new CustomUtilities().launchk(browser);

        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    @Test
    public void ValidateClickingTheUGCModule() throws Exception {
        ShopifyTestCasesComponents shopifytests = new ShopifyTestCasesComponents(driver);
        shopifytests.ValidateClickingTheUGCModule();
    }

    @Test
    public void ValidateActivitiesInspirationSection() throws Exception {
        ShopifyTestCasesComponents shopifytests = new ShopifyTestCasesComponents(driver);
        shopifytests.ValidateActivitiesInspirationSection();
    }


    @Test
    public void ValidateFiftyFiftyFeaturedCollection() throws Exception {
        ShopifyTestCasesComponents shopifytests = new ShopifyTestCasesComponents(driver);
        shopifytests.ValidateFiftyFiftyFeaturedCollection();
    }


    @Test
    public void ValidateAmbassadorSpotlight() throws Exception {
        ShopifyTestCasesComponents shopifytests = new ShopifyTestCasesComponents(driver);
        shopifytests.ValidateAmbassadorSpotlight();
    }

    @Test
    public void ValidateContentBannerSlice() throws Exception {
        ShopifyTestCasesComponents shopifytests = new ShopifyTestCasesComponents(driver);
        shopifytests.ValidateContentBannerSlice();
    }

    @Test
    public void ValidateBrandTickerNeonColors() throws Exception {
        ShopifyTestCasesComponents shopifytests = new ShopifyTestCasesComponents(driver);
        shopifytests.ValidateBrandTickerNeonColors();
    }

    @Test
    public void ValidateLifestyleDoorways() throws Exception {
        ShopifyTestCasesComponents shopifytests = new ShopifyTestCasesComponents(driver);
        shopifytests.ValidateLifeStyleDoorway();
    }



    @AfterMethod(alwaysRun = true)
    void teardown () {

//		driver.close();
//		driver.quit();
        if(driver != null)
        {
            driver.quit();
        }
    }


}
