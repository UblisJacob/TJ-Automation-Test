package TestModules;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.tommyjohn.automation.Components.GiftCardPageComponents;
import com.tommyjohn.automation.Pages.GiftCardPage;
import com.tommyjohn.automation.utils.CustomUtilities;

public class ValidateGiftCard {

	WebDriver driver;


	@BeforeMethod(alwaysRun = true)
	@Parameters("browser")
	public void setup(String browser) {
		try {
			driver = new CustomUtilities().launchtj(browser);

		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
	
	@Test
	public void TC_giftCardPageCheck() throws Exception
	{
		new GiftCardPage(driver).giftCardPageCheck(); 
	}
	@Test
	public void TC_verifyPGCard() throws Exception
	{
		new GiftCardPage(driver).verifyPGCard(); 
	}
	
	
	
//	@Test
//	public void TC_verifyfieldsforGCPage() throws Exception
//	{
//		new GiftCardPage(driver).verifyfieldsforGCPage(); 
//	}
//	@Test
//	public void TC_verifyerrormessagesongiftcard() throws Exception
//	{
//		new GiftCardPage(driver).verifyerrormessagesongiftcard(); 
//	}


	@AfterMethod(alwaysRun = true)
	void tearDown() {

		//this.driver.close();
		this.driver.quit();

	}
}

