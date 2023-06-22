package TestModules;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.tommyjohn.automation.Pages.SizeGuidePage;
import com.tommyjohn.automation.utils.CustomUtilities;



public class ValidateSizeGuides
{
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

	@Test(priority=0)
	public void TC_verifySizeChartsPage() throws Exception {	
		new SizeGuidePage(driver).verifySizeCharts();
	}
	@Test(priority=1)
	public void TC_verifySizeChartTextBralette() throws Exception {	
		new SizeGuidePage(driver).verifySizeChartTextBralette();
	}
	@Test(priority=2)
	public void TC_verifySizeChartTextBras() throws Exception {	
		new SizeGuidePage(driver).verifySizeChartTextBras();
	}
	

	@AfterMethod(alwaysRun = true)
	void teardown () {

	//	driver.close();
		driver.quit();
	}
}

