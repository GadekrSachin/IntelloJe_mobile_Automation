package utils;

import driver.DriverFactory;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.OutputType;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility to manage per-test artifacts: screenshots, recordings and manifest.
 * Thread-safe via ThreadLocal TestContext.
 */
public class ArtifactManager {

    private static final ThreadLocal<TestContext> context = new ThreadLocal<>();

    private static final String baseDir = ConfigReader.get("artifactDir") != null ? ConfigReader.get("artifactDir") : "target/test-artifacts";
    private static final boolean enableScreenshots = Boolean.parseBoolean(ConfigReader.get("enableScreenshots") != null ? ConfigReader.get("enableScreenshots") : "true");
    private static final boolean enableRecording = Boolean.parseBoolean(ConfigReader.get("enableRecording") != null ? ConfigReader.get("enableRecording") : "true");
    private static final int recordingTimeLimitSecs = Integer.parseInt(ConfigReader.get("recordingTimeLimitSecs") != null ? ConfigReader.get("recordingTimeLimitSecs") : "120");

    private static final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss").withZone(ZoneId.systemDefault());

    public static void initForTest(String testName, AppiumDriver driver) {
        String ts = fmt.format(Instant.now());
        long tid = Thread.currentThread().getId();
        String dirName = sanitizeFileName(testName + "_t" + tid + "_" + ts);
        Path testDir = Paths.get(baseDir, dirName);
        try {
            Files.createDirectories(testDir);
            Files.createDirectories(testDir.resolve("screenshots"));
            Files.createDirectories(testDir.resolve("videos"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        TestContext tc = new TestContext();
        tc.testName = testName;
        tc.testDir = testDir.toFile();
        tc.startTime = Instant.now().toString();
        tc.driver = driver;
        try {
            if (driver != null && driver.getSessionId() != null) tc.sessionId = driver.getSessionId().toString();
        } catch (Exception ignored) {}

        context.set(tc);
    }

    public static String saveScreenshot(String stepName) {
        if (!enableScreenshots) return null;
        TestContext tc = context.get();
        if (tc == null) return null;
        AppiumDriver d = tc.driver != null ? tc.driver : DriverFactory.getDriver();
        if (d == null) return null;
        try {
            byte[] src = d.getScreenshotAs(OutputType.BYTES);
            String ts = fmt.format(Instant.now());
            int idx = tc.screenshots.size() + 1;
            String fileName = String.format("%02d_%s_%s.png", idx, ts, sanitizeFileName(stepName));
            Path p = Paths.get(tc.testDir.getAbsolutePath(), "screenshots", fileName);
            try (FileOutputStream fos = new FileOutputStream(p.toFile())) {
                fos.write(src);
            }
            Map<String, Object> m = new HashMap<>();
            m.put("type", "screenshot");
            m.put("step", stepName);
            m.put("file", p.toString());
            m.put("timestamp", Instant.now().toString());
            tc.screenshots.add(m);
            return p.toString();
        } catch (Exception e) {
            System.out.println("Failed to capture screenshot: " + e.getMessage());
            return null;
        }
    }

    public static String startRecording() {
        if (!enableRecording) return null;
        TestContext tc = context.get();
        if (tc == null) return null;
        AppiumDriver d = tc.driver != null ? tc.driver : DriverFactory.getDriver();
        if (d == null) return null;

        Map<String, Object> opts = new HashMap<>();
        opts.put("timeLimit", recordingTimeLimitSecs + "s");

        // Try methods in preferred order
        String[] methods = new String[]{"mobile: startMediaProjectionRecording", "mobile: startRecordingScreen", "mobile: startScreenStreaming"};
        Exception lastEx = null;
        for (String method : methods) {
            try {
                d.executeScript(method, opts);
                tc.recordingStarted = true;
                tc.recordingMethod = method;
                System.out.println("[ArtifactManager] recording started using: " + method);
                return method;
            } catch (Exception e) {
                lastEx = e;
                // try next
            }
        }
        System.out.println("[ArtifactManager] Failed to start recording. last error: " + (lastEx != null ? lastEx.getMessage() : "none"));
        return null;
    }

    public static String stopRecording() {
        if (!enableRecording) return null;
        TestContext tc = context.get();
        if (tc == null) return null;
        AppiumDriver d = tc.driver != null ? tc.driver : DriverFactory.getDriver();
        if (d == null) return null;

        String[] stopMethods = new String[]{};
        if (tc.recordingMethod != null) {
            if (tc.recordingMethod.contains("startMediaProjectionRecording")) stopMethods = new String[]{"mobile: stopMediaProjectionRecording"};
            else if (tc.recordingMethod.contains("startRecordingScreen")) stopMethods = new String[]{"mobile: stopRecordingScreen"};
            else if (tc.recordingMethod.contains("startScreenStreaming")) stopMethods = new String[]{"mobile: stopScreenStreaming"};
        } else {
            // fallback order
            stopMethods = new String[]{"mobile: stopMediaProjectionRecording", "mobile: stopRecordingScreen", "mobile: stopScreenStreaming"};
        }

        Exception lastEx = null;
        Object res = null;
        for (String stop : stopMethods) {
            try {
                res = d.executeScript(stop);
                System.out.println("[ArtifactManager] recording stopped using: " + stop);
                break;
            } catch (Exception e) {
                lastEx = e;
            }
        }

        if (res == null) {
            System.out.println("Failed to stop recording. last error: " + (lastEx != null ? lastEx.getMessage() : "no response"));
            return null;
        }

        try {
            String asString = res.toString();
            // try decode as base64 -> mp4
            try {
                byte[] data = Base64.getDecoder().decode(asString);
                String ts = fmt.format(Instant.now());
                String fileName = String.format("recording_%s.mp4", ts);
                Path p = Paths.get(tc.testDir.getAbsolutePath(), "videos", fileName);
                try (FileOutputStream fos = new FileOutputStream(p.toFile())) {
                    fos.write(data);
                }
                Map<String, Object> m = new HashMap<>();
                m.put("type", "video");
                m.put("file", p.toString());
                m.put("timestamp", Instant.now().toString());
                tc.videos.add(m);
                return p.toString();
            } catch (IllegalArgumentException decodeEx) {
                // not base64 - maybe a URL or some other info; save as text
                String ts = fmt.format(Instant.now());
                String fileName = String.format("recording_info_%s.txt", ts);
                Path p = Paths.get(tc.testDir.getAbsolutePath(), "videos", fileName);
                try {
                    Files.write(p, asString.getBytes());
                } catch (IOException ioe) {
                    System.out.println("Failed to write recording info: " + ioe.getMessage());
                    return null;
                }
                Map<String, Object> m = new HashMap<>();
                m.put("type", "video_info");
                m.put("file", p.toString());
                m.put("timestamp", Instant.now().toString());
                tc.videos.add(m);
                return p.toString();
            }
        } catch (Exception e) {
            System.out.println("Failed to process recording result: " + e.getMessage());
            return null;
        }
    }

    public static void finishTest(int status, String errorMessage) {
        TestContext tc = context.get();
        if (tc == null) return;
        tc.endTime = Instant.now().toString();
        tc.status = status;
        tc.errorMessage = errorMessage;
        // write manifest
        Path manifest = Paths.get(tc.testDir.getAbsolutePath(), "manifest.txt");
        List<String> lines = new ArrayList<>();
        lines.add("testName=" + tc.testName);
        lines.add("startTime=" + tc.startTime);
        lines.add("endTime=" + tc.endTime);
        lines.add("status=" + status);
        if (tc.sessionId != null) lines.add("sessionId=" + tc.sessionId);
        if (tc.recordingMethod != null) lines.add("recordingMethod=" + tc.recordingMethod);
        if (tc.errorMessage != null) lines.add("errorMessage=" + tc.errorMessage);
        try {
            Files.write(manifest, lines);
        } catch (IOException e) {
            e.printStackTrace();
        }

        context.remove();
    }

    private static String sanitizeFileName(String in) {
        if (in == null) return "x";
        return in.replaceAll("[^a-zA-Z0-9_\\-\\. ]", "_").replaceAll("\\s+", "_");
    }

    private static class TestContext {
        String testName;
        java.io.File testDir;
        String startTime;
        String endTime;
        int status;
        String errorMessage;
        AppiumDriver driver;
        String sessionId;
        boolean recordingStarted = false;
        String recordingMethod;
        String recordingName;
        List<Map<String, Object>> screenshots = new ArrayList<>();
        List<Map<String, Object>> videos = new ArrayList<>();
    }
}
