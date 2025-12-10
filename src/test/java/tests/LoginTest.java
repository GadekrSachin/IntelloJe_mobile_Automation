package tests;

import driver.DriverFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.Login;

public class LoginTest extends BaseTest {

    private Login lp;

    @BeforeMethod(alwaysRun = true)
    public void initPage() {
        lp = new Login(DriverFactory.getDriver());
    }

    @Test
    public void sampleLoginTest() {
        lp.CLickONView();
        lp.enterPassword();
        lp.validation();
    }
}
