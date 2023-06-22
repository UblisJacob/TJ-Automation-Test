package com.tommyjohn.automation.PageLocators;

import org.openqa.selenium.By;

public class SanityModulelocators {

	public static By HERO_IMAGE_1 = By.cssSelector("body > main:nth-child(27) > div:nth-child(3) > div:nth-child(1) > div:nth-child(5) > div:nth-child(1) > div:nth-child(1) > div:nth-child(1)");
	public static By HERO_IMAGE_TEXT = By.cssSelector("body > main:nth-child(27) > div:nth-child(3) > div:nth-child(1) > div:nth-child(5) > div:nth-child(1) > div:nth-child(1) > div:nth-child(2)>div>div>div>p");
	
	public static By FIXED_BANNER_IMG = By.cssSelector("body > main:nth-child(27) > div:nth-child(3) > div:nth-child(1) > div:nth-child(6) > div:nth-child(1)");
	
	public static By THREE_TILE_MODULE = By.cssSelector(".three-tile-module.vue-module-container");
	public static By THREE_TILE_IMG_1 = By.cssSelector(".three-tile-module.vue-module-container>div>div:nth-child(1)");
	public static By THREE_TILE_IMG_2 = By.cssSelector(".three-tile-module.vue-module-container>div>div:nth-child(2)");
	public static By THREE_TILE_IMG_3 = By.cssSelector(".three-tile-module.vue-module-container>div>div:nth-child(3)");
	
	
	
	
}
