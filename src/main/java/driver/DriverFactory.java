package driver;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.remote.RemoteWebElement;

import java.net.MalformedURLException;
import java.net.URL;

import utils.ArtifactManager;
import utils.ConfigReader;

public class DriverFactory {


    private static final ThreadLocal<AppiumDriver> driver = new ThreadLocal<>();

    public static void createDriver(String platform) {
        try {
            var caps = CapabilitiesManager.getCapabilities(platform);

            String runMode = ConfigReader.get("runMode");
            runMode = runMode != null ? runMode : "local";

            String serverUrl = ConfigReader.get("serverUrl");
            if ("browserstack".equalsIgnoreCase(runMode)) {
                String bsUser = ConfigReader.get("bs_user");
                String bsKey = ConfigReader.get("bs_key");
                if (bsUser != null && bsKey != null && !bsUser.isEmpty() && !bsKey.isEmpty()) {
                    serverUrl = "https://" + bsUser + ":" + bsKey + "@hub-cloud.browserstack.com/wd/hub";
                } else {
                    throw new RuntimeException("BrowserStack runMode requires bs_user and bs_key to be set (system props or config.properties)");
                }
            }

            if (serverUrl == null || serverUrl.isEmpty()) {
                throw new RuntimeException("serverUrl is not configured. Please set 'serverUrl' in config.properties or pass -DserverUrl=...");
            }

            URL url;
            try {
                url = new URL(serverUrl);
            } catch (MalformedURLException e) {
                throw new RuntimeException("serverUrl is not a valid URL: " + serverUrl, e);
            }

            // Debug output to help diagnose failures
            System.out.println("[DriverFactory] runMode=" + runMode + " serverUrl=" + serverUrl + " platform=" + platform);
            try {
                System.out.println("[DriverFactory] Capabilities: " + caps.toString());
            } catch (Exception ignored) {}

            AppiumDriver appiumDriver;
            String resolvedPlatform = platform != null ? platform : ConfigReader.get("platformName");
            if (resolvedPlatform == null) resolvedPlatform = "android";

            if (resolvedPlatform.equalsIgnoreCase("android")) {
                appiumDriver = new AndroidDriver(url, caps);
            } else if (resolvedPlatform.equalsIgnoreCase("ios")) {
                appiumDriver = new IOSDriver(url, caps);
            } else {
                throw new RuntimeException("Unsupported platform for driver creation: " + resolvedPlatform);
            }

            driver.set(appiumDriver);

            // initialize artifact manager for this thread/test - test name unknown here, will be set from BaseTest or listener
            ArtifactManager.initForTest(Thread.currentThread().getName(), appiumDriver);

        } catch (RuntimeException re) {
            // pass through known runtime exceptions so stack contains cause
            throw re;
        } catch (Exception e) {
            // include the original exception so the root cause is visible in logs
            throw new RuntimeException("Failed to create driver: " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
    }

    public static AppiumDriver getDriver() {
        return driver.get();
    }

    public static void quitDriver() {
        if (driver.get() != null) {
            try {
                driver.get().quit();
            } catch (Exception ignored) {
            }
            driver.remove();
        }
    }
}
