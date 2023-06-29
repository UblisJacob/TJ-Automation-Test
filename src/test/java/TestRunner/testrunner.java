package TestRunner;

import org.junit.runner.RunWith;

import io.cucumber.junit.CucumberOptions;
import io.cucumber.junit.Cucumber;


@RunWith(Cucumber.class)
@CucumberOptions(features="src/main/resources/Features",
			glue= {"StepDefinitions"}, 
			plugin = {"json:target/jsonReports/cucumber-report.json",
			"html:target/CucumberReport/cucumber-report.html",
			"pretty",
			"rerun:target/failedRerun.txt"},publish = true)
public class testrunner {

}
