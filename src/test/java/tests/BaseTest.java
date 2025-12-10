package tests;

import driver.DriverFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;
import org.testng.ITestResult;
import org.testng.annotations.Listeners;

import utils.ArtifactManager;
import hooks.TestListener;

@Listeners(TestListener.class)
public class BaseTest {

    @Parameters({"platform"})
    @BeforeMethod(alwaysRun = true)
    public void setUp(String platform, ITestResult result) {
        DriverFactory.createDriver(platform);
    }



    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        try {
            ArtifactManager.stopRecording();
            int status = result == null ? 0 : (result.isSuccess() ? 0 : 1);
            String err = result != null && result.getThrowable() != null ? result.getThrowable().getMessage() : null;
            ArtifactManager.finishTest(status, err);
        } catch (Exception ignored) {}

        DriverFactory.quitDriver();

     }
}
