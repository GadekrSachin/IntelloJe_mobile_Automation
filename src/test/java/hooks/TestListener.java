package hooks;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import utils.ArtifactManager;

public class TestListener implements ITestListener {
    @Override
    public void onTestStart(ITestResult result) {
        // initialize artifacts for TestNG tests (name is test method)
        try {
            ArtifactManager.initForTest(result.getMethod().getMethodName(), null);
        } catch (Exception ignored) {}
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ArtifactManager.finishTest(0, null);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        try {
            ArtifactManager.saveScreenshot("failure");
        } catch (Exception ignored) {}
        ArtifactManager.finishTest(1, result.getThrowable() != null ? result.getThrowable().getMessage() : null);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ArtifactManager.finishTest(2, result.getThrowable() != null ? result.getThrowable().getMessage() : null);
    }

    // unused methods
    @Override public void onStart(ITestContext context) {}
    @Override public void onFinish(ITestContext context) {}
    @Override public void onTestFailedButWithinSuccessPercentage(ITestResult result) {}
}

