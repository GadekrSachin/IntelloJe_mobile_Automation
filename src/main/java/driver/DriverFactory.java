package driver;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import org.openqa.selenium.remote.RemoteWebElement;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import utils.ArtifactManager;
import utils.ConfigReader;

public class DriverFactory {


    private static final ThreadLocal<AppiumDriver> driver = new ThreadLocal<>();
    // ThreadLocal to hold per-thread local Appium service
    private static final ThreadLocal<AppiumDriverLocalService> localService = new ThreadLocal<>();

    public static void createDriver(String platform) {
        try {
            var caps = CapabilitiesManager.getCapabilities(platform);

            String runMode = ConfigReader.get("runMode");
            runMode = runMode != null ? runMode : "local";

            String serverUrl = ConfigReader.get("serverUrl");

            // If running on BrowserStack, construct remote URL with bs credentials
            if ("browserstack".equalsIgnoreCase(runMode)) {
                String bsUser = ConfigReader.get("bs_user");
                String bsKey = ConfigReader.get("bs_key");
                if (bsUser != null && bsKey != null && !bsUser.isEmpty() && !bsKey.isEmpty()) {
                    serverUrl = "https://" + bsUser + ":" + bsKey + "@hub-cloud.browserstack.com/wd/hub";
                } else {
                    throw new RuntimeException("BrowserStack runMode requires bs_user and bs_key to be set (system props or config.properties)");
                }
            }

            boolean startLocal = Boolean.parseBoolean(ConfigReader.get("startLocalAppium") != null ? ConfigReader.get("startLocalAppium") : "true");

            // If local run and user requested starting Appium locally, start AppiumDriverLocalService
            if ("local".equalsIgnoreCase(runMode) && startLocal) {
                try {
                    // Only start service if not already started for this thread
                    if (localService.get() == null) {
                        AppiumServiceBuilder builder = new AppiumServiceBuilder();

                        String host = ConfigReader.get("appiumHost") != null ? ConfigReader.get("appiumHost") : "127.0.0.1";
                        String portProp = ConfigReader.get("appiumPort");

                        builder.withIPAddress(host);

                        if (portProp != null && !portProp.isEmpty()) {
                            try {
                                int port = Integer.parseInt(portProp);
                                builder.usingPort(port);
                            } catch (NumberFormatException ignored) {}
                        } else {
                            builder.usingAnyFreePort();
                        }

                        // optional node/appium paths
                        String nodePath = ConfigReader.get("appiumNodePath");
                        String appiumJSPath = ConfigReader.get("appiumJSPath");
                        if (nodePath != null && !nodePath.isEmpty()) {
                            builder.usingDriverExecutable(new File(nodePath));
                        }
                        if (appiumJSPath != null && !appiumJSPath.isEmpty()) {
                            builder.withAppiumJS(new File(appiumJSPath));
                        }

                        AppiumDriverLocalService service = AppiumDriverLocalService.buildService(builder);
                        service.start();
                        if (service.isRunning()) {
                            localService.set(service);
                            serverUrl = service.getUrl().toString();
                            System.out.println("[DriverFactory] Started local Appium server at: " + serverUrl);
                        }
                    } else {
                        // service already started for this thread
                        serverUrl = localService.get().getUrl().toString();
                    }
                } catch (Exception e) {
                    // If starting local Appium fails, log and fall back to provided serverUrl
                    System.out.println("[DriverFactory] Failed to start local Appium service: " + e.getMessage());
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

        // stop local Appium service if started for this thread
        AppiumDriverLocalService svc = localService.get();
        if (svc != null) {
            try {
                if (svc.isRunning()) svc.stop();
            } catch (Exception ignored) {}
            localService.remove();
        }
    }
}
