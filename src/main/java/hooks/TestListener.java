package hooks;

import driver.DriverFactory;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import utils.ArtifactManager;

public class TestListener implements ITestListener{

    @Override
    public void onTestStart(ITestResult result) {
        try {
            // set a nicer test name if possible
            String testName = result.getMethod().getMethodName();
            AppiumDriver d = DriverFactory.getDriver();
            ArtifactManager.initForTest(testName, d);
            ArtifactManager.startRecording();
        } catch (Exception ignored) {}
    }

    @Override
    public void onTestFailure(ITestResult result) {
        try {
            AppiumDriver d = DriverFactory.getDriver();
            ArtifactManager.saveScreenshot("failure");
        } catch (Exception e) {
            System.out.println("Screenshot capture failed: " + e.getMessage());
        }
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        // no-op
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        // no-op
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        // no-op
    }

    @Override
    public void onStart(ITestContext context) {
        // no-op
    }

    @Override
    public void onFinish(ITestContext context) {
        // no-op
    }
}
