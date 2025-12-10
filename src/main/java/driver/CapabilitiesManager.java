package driver;

import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.options.XCUITestOptions;
import org.openqa.selenium.MutableCapabilities;
import utils.ConfigReader;

public class CapabilitiesManager {
    public static MutableCapabilities getCapabilities(String platformName) {
        platformName = platformName != null ? platformName : ConfigReader.get("platformName");

        String runMode = ConfigReader.get("runMode");
        runMode = runMode != null ? runMode : "local";

        if ("browserstack".equalsIgnoreCase(runMode)) {
            return getBrowserStackCapabilities(platformName);
        }

        if (platformName.equalsIgnoreCase("android")) {
            return getAndroidOptions();
        } else if (platformName.equalsIgnoreCase("ios")) {
            return getIOSOptions();
        } else {
            throw new RuntimeException("Unsupported platform: " + platformName);
        }
    }

    // =================== ANDROID =====================
    private static UiAutomator2Options getAndroidOptions() {
        UiAutomator2Options options = new UiAutomator2Options();

        options.setPlatformName( ConfigReader.get ("platformName")  );
        options.setAutomationName(ConfigReader.get ("automationName") );
        options.setDeviceName(ConfigReader.get("deviceName"));
        options.setPlatformVersion(ConfigReader.get("platformVersion"));

        String appPath = ConfigReader.get("appPath");
        if (appPath != null && !appPath.isEmpty()) {
            options.setApp(appPath);
        }

        // If your app is installed by package/activity
//        if (ConfigReader.get("appPackage") != null) {
//            options.setAppPackage(ConfigReader.get("appPackage"));
//            options.setAppActivity(ConfigReader.get("appActivity"));
//        }

        return options;
    }

    // =================== IOS ==========================
    private static XCUITestOptions getIOSOptions() {
        XCUITestOptions options = new XCUITestOptions();

        options.setPlatformName("iOS");
        options.setDeviceName(ConfigReader.get("deviceName"));
        options.setPlatformVersion(ConfigReader.get("platformVersion"));
        options.setAutomationName("XCUITest");

        if (ConfigReader.get("udid") != null) {
            options.setUdid(ConfigReader.get("udid"));
        }

        // IPA or app file
        String appPath = ConfigReader.get("appPath");
        if (appPath != null && !appPath.isEmpty()) {
            options.setApp(appPath);
        }

        // Installed app
        if (ConfigReader.get("bundleId") != null) {
            options.setBundleId(ConfigReader.get("bundleId"));
        }

        return options;
    }

    // =================== BROWSERSTACK =================
    private static MutableCapabilities getBrowserStackCapabilities(String platformName) {
        MutableCapabilities caps = new MutableCapabilities();

        String bsUser = ConfigReader.get("bs_user");
        String bsKey = ConfigReader.get("bs_key");
        String bsApp = ConfigReader.get("bs_app");
        String bsDevice = ConfigReader.get("bs_device");
        String bsOsVersion = ConfigReader.get("bs_os_version");

        // bstack:options
        MutableCapabilities bstackOptions = new MutableCapabilities();
        if (bsUser != null) bstackOptions.setCapability("userName", bsUser);
        if (bsKey != null) bstackOptions.setCapability("accessKey", bsKey);
        bstackOptions.setCapability("project", "Mobile_Automation_Practice");
        bstackOptions.setCapability("build", System.getProperty("BUILD_NAME", "local-build"));
        bstackOptions.setCapability("sessionName", "Automation Test");

        if (platformName.equalsIgnoreCase("android")) {
            caps.setCapability("platformName", "Android");
            String device = bsDevice != null && !bsDevice.isEmpty() ? bsDevice : ConfigReader.get("deviceName");
            if (device != null) caps.setCapability("deviceName", device);
            String osv = bsOsVersion != null && !bsOsVersion.isEmpty() ? bsOsVersion : ConfigReader.get("platformVersion");
            if (osv != null) caps.setCapability("platformVersion", osv);
            if (bsApp != null && !bsApp.isEmpty()) caps.setCapability("app", bsApp);
            caps.setCapability("automationName", ConfigReader.get("automationName"));

        } else if (platformName.equalsIgnoreCase("ios")) {
            caps.setCapability("platformName", "iOS");
            String device = bsDevice != null && !bsDevice.isEmpty() ? bsDevice : ConfigReader.get("deviceName");
            if (device != null) caps.setCapability("deviceName", device);
            String osv = bsOsVersion != null && !bsOsVersion.isEmpty() ? bsOsVersion : ConfigReader.get("platformVersion");
            if (osv != null) caps.setCapability("platformVersion", osv);
            if (bsApp != null && !bsApp.isEmpty()) caps.setCapability("app", bsApp);
            caps.setCapability("automationName", "XCUITest");
            if (ConfigReader.get("bundleId") != null) caps.setCapability("bundleId", ConfigReader.get("bundleId"));
        }

        caps.setCapability("bstack:options", bstackOptions);

        return caps;
    }
}
