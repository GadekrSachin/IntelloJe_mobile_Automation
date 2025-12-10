package utils;

import io.appium.java_client.AppiumDriver;

public class StepCapture {

    public static String captureStep(String stepName) {
        return ArtifactManager.saveScreenshot(stepName != null ? stepName : "step");
    }

    public static String captureStep(AppiumDriver driver, String stepName) {
        // driver parameter is kept for API compatibility; ArtifactManager will pick ThreadLocal driver
        return ArtifactManager.saveScreenshot(stepName != null ? stepName : "step");
    }
}
