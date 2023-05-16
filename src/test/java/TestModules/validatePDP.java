package TestModules;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.tommyjohn.automation.Pages.ProductDetailsPage;
import com.tommyjohn.automation.utils.CustomUtilities;

public class validatePDP {
	
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
//	@Test
//	public void TC_ViewImageGallery() throws Exception {
//		new ProductDetailsPage(driver).viewImageGallery();
//		
//	}
//	
//	@Test
//	public void TC_verifyImageGallery() throws Exception {
//		new ProductDetailsPage(driver).verifyImageGallery();
//		
//	}
	
//	@Test
//	public void TC_verifyOnImageBadging() throws Exception {
//		new ProductDetailsPage(driver).verifyOnImageBadging();
//		
//	}
//	
//	@Test
//	public void TC_verifySecondaryBadging() throws Exception {
//		new ProductDetailsPage(driver).verifySecondaryBadging();
//		
//	}
	@Test
	public void TC_verifySelectedColorSwatch() throws Exception {
		new ProductDetailsPage(driver).verifySelectedColorSwatch();
		
	}
	
	@AfterMethod(alwaysRun = true)
	void teardown () {

		//driver.close();
		driver.quit();
	
	}
}
