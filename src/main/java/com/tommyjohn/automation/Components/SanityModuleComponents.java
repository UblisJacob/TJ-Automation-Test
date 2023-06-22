package com.tommyjohn.automation.Components;

import java.util.List;
import java.util.Properties;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Reporter;
import org.testng.asserts.SoftAssert;

import com.tommyjohn.automation.PageLocators.SanityModulelocators;
import com.tommyjohn.automation.utils.CustomUtilities;

public class SanityModuleComponents extends SanityModulelocators{

	
	public WebDriver driver;
	public Actions action;
	WebElement element;
	WebElement element1;
	Properties properties = CustomUtilities.properties;
	WebDriverWait wait;
	int elementsCount;
	public JavascriptExecutor jse;
	SoftAssert softassert = new SoftAssert();
	WebElement Rec_mail;
	String GC_Price_PDP;
	String Expected_text;
	String Delivery_date;
	String newDelivery_Date;
	String invalidemail = "test";
		
	String text;
	List<WebElement> allElements;

	
	public SanityModuleComponents(WebDriver driver) {
		this.driver = driver;
	}
	
	public void verifyHeroModule() throws Exception {
		
		// call method to navigate to collection page
		new HomePageComponents(driver).navigateToCareersPage();
		JavascriptExecutor jse = (JavascriptExecutor)driver;
		jse.executeScript("window.scrollBy(0,250)");
		if(driver.findElement(HERO_IMAGE_1).isDisplayed()) 
			{
				Thread.sleep(3000);
				WebElement heroimg = driver.findElement(HERO_IMAGE_1); 
				org.openqa.selenium.Dimension dim = heroimg.getSize();
				
				System.out.println(dim);
				int width=	dim.width;
				
				System.out.println(width);
				
				//margin validations
				if(width>=1349)  {
					Reporter.log("Hero Module annotations are correct");
					System.out.println("Hero Module Tile annotations are correct");
				}
				else {
					System.out.println("Hero Module annotations are not correct");
					throw new Exception("Hero Module annotations are not correct");
				}
			}
				
		//Text Validations
		if(driver.findElement(HERO_IMAGE_TEXT).isDisplayed())
			{
				Thread.sleep(3000);
				WebElement herotext = driver.findElement(HERO_IMAGE_TEXT); 
				String fontsize = herotext.getCssValue("font-size");
				System.out.println(fontsize);
				
				if (!fontsize.contentEquals("40px")) {
					System.out.println("Hero Module Text annotations are correct");
				}
				
			}
				
	}
	
	public void verifyFixedBannerModule() throws Exception {
		// call method to navigate to collection page
		new HomePageComponents(driver).navigateToCareersPage();
		JavascriptExecutor jse = (JavascriptExecutor)driver;
		jse.executeScript("window.scrollBy(0,1000)");
		if(driver.findElement(FIXED_BANNER_IMG).isDisplayed()) 
			{
				Thread.sleep(3000);
				WebElement fxdimg = driver.findElement(FIXED_BANNER_IMG); 
				org.openqa.selenium.Dimension dim = fxdimg.getSize();
				
				System.out.println(dim);
				int width=	dim.width;
				
				System.out.println(width);
				
				//margin validations
				if(width>=1184)  {
					Reporter.log("Fixed Banner Module annotations are correct");
					System.out.println("Fixed Banner Module annotations are correct");
				}
				else {
					System.out.println("Fixed Banner Module annotations are not correct");
					throw new Exception("Fixed Banner Module annotations are not correct");
				}
			}
		
	}
	
	public void verifyThreeTileModule() throws Exception {
		
		// call method to navigate to collection page
				new HomePageComponents(driver).navigateToCareersPage();
				JavascriptExecutor jse = (JavascriptExecutor)driver;
				jse.executeScript("window.scrollBy(0,1000)");
				if(driver.findElement(THREE_TILE_MODULE).isDisplayed()) 
					{
						Thread.sleep(3000);
						//calculating total marging of module
						WebElement tilesimg = driver.findElement(THREE_TILE_MODULE); 
						org.openqa.selenium.Dimension dim = tilesimg.getSize();
						System.out.println(dim);
						int width=	dim.width;
						System.out.println(width);
						
						Thread.sleep(3000);
						WebElement module = driver.findElement(THREE_TILE_MODULE); 
						WebElement tileimg1 = driver.findElement(THREE_TILE_IMG_1); 
						WebElement tileimg2 = driver.findElement(THREE_TILE_IMG_2); 
						softassert.assertEquals(module.getCssValue("margin-right"), "24px" , "Right margin is not correct");
						softassert.assertEquals(module.getCssValue("margin-left"), "24px" , "Left margin is not correct");
						
						//checking individual tiles are present
						softassert.assertTrue(driver.findElement(THREE_TILE_IMG_1).isDisplayed(), "Tile 1 is displayed");
						softassert.assertTrue(driver.findElement(THREE_TILE_IMG_2).isDisplayed(), "Tile 2 is displayed");
						softassert.assertTrue(driver.findElement(THREE_TILE_IMG_3).isDisplayed(), "Tile 3 is displayed");
						Thread.sleep(3000);
						
						//checking individual margins of tiles
						softassert.assertEquals(tileimg1.getCssValue("margin-right"), "24px" , "Left margin is not correct");
						softassert.assertEquals(tileimg2.getCssValue("margin-right"), "24px" , "Left margin is not correct");
					}
				
	}	
	
}
