package pages;

import driver.DriverFactory;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import org.openqa.selenium.By;
import org.openqa.selenium.support.PageFactory;
import org.testng.Assert;

public class Login {


    public Login(AppiumDriver driver) {
        PageFactory.initElements(new AppiumFieldDecorator(driver), this);
    }
//    private AppiumDriver<WebElement> driver = DriverFactory.getDriver();


    private By View_button = AppiumBy.androidUIAutomator("new UiSelector().text(\"Views\")");
    private By Animation_button = AppiumBy.androidUIAutomator("new UiSelector().text(\"Animation\")");
    private By Validation = AppiumBy.androidUIAutomator("\n" +
            "    new UiSelector().text(\"3D Transition\")");


//    @FindBy(id = "com.example.myapp:id/username")
//    private WebElement username;


    public void CLickONView() {
        DriverFactory.getDriver().findElement(View_button).click();
    }

    public void enterPassword() {
        DriverFactory.getDriver().findElement(Animation_button).click();
    }

    public void validation() {
        Assert.assertTrue(DriverFactory.getDriver().findElement(Validation).getText().equals("3D Transition"));
    }


}
