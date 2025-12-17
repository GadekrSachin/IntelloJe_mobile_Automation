package steps;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import driver.DriverFactory;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.testng.Assert;
import pages.Login;
import utils.ConfigReader;

public class LoginSteps {
    private Login lp;
    private AppiumDriver driver;

    @Before
    public void initPage() {
        // Ensure a driver exists; CucumberHooks also creates one, but be defensive
        if (DriverFactory.getDriver() == null) {
            String platform = System.getProperty("platform");
            if (platform == null || platform.isEmpty()) {
                platform = ConfigReader.get("platformName");
            }
            DriverFactory.createDriver(platform);
        }
        driver = (AppiumDriver) DriverFactory.getDriver();
        lp = new Login(driver);
    }

    @Given("the app is launched")
    public void the_app_is_launched() {
        lp.CLickONView();
        lp.enterPassword();
        lp.validation();
    }

    @When("I enter username {string} and password {string}")
    public void i_enter_username_and_password(String username, String password) {
        System.out.println("as");
    }

    @When("I tap the login button")
    public void i_tap_the_login_button() {
        System.out.println("as");
    }

    @Then("I should see the home screen")
    public void i_should_see_the_home_screen() {
        System.out.println("as");
    }
}

