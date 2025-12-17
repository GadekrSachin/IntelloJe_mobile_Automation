package hooks;

import driver.DriverFactory;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import utils.ConfigReader;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import utils.ArtifactManager;
import io.appium.java_client.AppiumDriver;

public class CucumberHooks {

    @Before
    public void beforeScenario(Scenario scenario) {
        // start driver using platform from system property or config
        String platform = System.getProperty("platform");
        if (platform == null || platform.isEmpty()) {
            platform = ConfigReader.get("platformName");
        }
        if (DriverFactory.getDriver() == null) {
            DriverFactory.createDriver(platform);
        }

        AppiumDriver driver = DriverFactory.getDriver();
        // initialize artifact manager with scenario name
        ArtifactManager.initForTest(scenario.getName(), driver);
        // start recording if enabled
        ArtifactManager.startRecording();
    }

    @After
    public void afterScenario(Scenario scenario) {
        int status = scenario.isFailed() ? 1 : 0;
        String err = null;
        if (scenario.isFailed()) {
            try {
                Object drv = DriverFactory.getDriver();
                if (drv instanceof TakesScreenshot) {
                    byte[] bytes = ((TakesScreenshot) drv).getScreenshotAs(OutputType.BYTES);
                    scenario.attach(bytes, "image/png", "screenshot");
                }
                // save screenshot via ArtifactManager as well
                ArtifactManager.saveScreenshot("failure");
            } catch (Exception ignored) {}
            try {
                err = scenario.getStatus().name();
            } catch (Exception ignored) {}
        }

        try {
            ArtifactManager.stopRecording();
        } catch (Exception ignored) {}

        ArtifactManager.finishTest(status, err);

        // always quit driver at the end of scenario
        DriverFactory.quitDriver();
    }
}

