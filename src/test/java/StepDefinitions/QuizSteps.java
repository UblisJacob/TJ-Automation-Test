package StepDefinitions;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.Parameters;

import com.tommyjohn.automation.Pages.QuizPage;
import com.tommyjohn.automation.utils.CustomUtilities;


import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;



public class QuizSteps {
	WebDriver driver=null;


	public void setup(String browser) throws Exception {
		
			driver = new CustomUtilities().launchtj(browser);

	
	}
	
	@Given("User navigates to Quiz page")
	public void user_navigates_to_quiz_page() throws Exception {
		setup("chrome");
	    // Write code here that turns the phrase above into concrete actions
		new QuizPage(driver).VerifyMensQuizFlow();
	    System.out.println("User is navigated to Quiz Page");
	}

	@When("User clicks get started cta on mens underwear quiz")
	public void user_clicks_get_started_cta_on_mens_underwear_quiz() {
	    // Write code here that turns the phrase above into concrete actions
		System.out.println("User clicks get started cta on mens underwear quiz");
	}

	@When("Answer quiz question")
	public void answer_quiz_question() {
	    // Write code here that turns the phrase above into concrete actions
		System.out.println("Answer quiz question");
	}

	@When("click next")
	public void click_next() {
	    // Write code here that turns the phrase above into concrete actions
		System.out.println("click next and answer all questions");
	}

	@Then("Quiz results are displayed")
	public void quiz_results_are_displayed() throws Exception {
		setup("chrome");
		new QuizPage(driver).MensResultPage();
		System.out.println("Quiz results are displayed");
	}

	@When("User clicks get started cta on womens underwear quiz")
	public void user_clicks_get_started_cta_on_womens_underwear_quiz() {
	    // Write code here that turns the phrase above into concrete actions
		System.out.println("User clicks get started cta on womens underwear quiz");
	}

	
	@When("User clicks get started cta on bras quiz")
	public void user_clicks_get_started_cta_on_bras_quiz() {
	    // Write code here that turns the phrase above into concrete actions
		System.out.println("User clicks get started cta on bras quiz");
	}

}
