package hooks;

import driver.DriverFactory;
import io.appium.java_client.AppiumDriver;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import utils.ArtifactManager;

public class TestListener implements ITestListener {
    @Override
    public void onTestStart(ITestResult result) {
        try {
            String testName = result.getMethod().getMethodName();
            AppiumDriver d = DriverFactory.getDriver();
            ArtifactManager.initForTest(testName, d);
            ArtifactManager.startRecording();
        } catch (Exception ignored) {}
    }

    @Override
    public void onTestFailure(ITestResult result) {
        try { ArtifactManager.saveScreenshot("failure"); } catch (Exception ignored) {}
        ArtifactManager.finishTest(1, result.getThrowable() != null ? result.getThrowable().getMessage() : null);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ArtifactManager.finishTest(0, null);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ArtifactManager.finishTest(2, result.getThrowable() != null ? result.getThrowable().getMessage() : null);
    }

    @Override public void onStart(ITestContext context) {}
    @Override public void onFinish(ITestContext context) {}
    @Override public void onTestFailedButWithinSuccessPercentage(ITestResult result) {}
}
