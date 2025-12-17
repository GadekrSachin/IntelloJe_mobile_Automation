package hooks;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import utils.ConfigReader;

public class RetryAnalyzer implements IRetryAnalyzer {
    private int retryCount = 0;
    private final int maxRetry;

    public RetryAnalyzer() {
        int defaultRetries = 1; // default: retry once
        String v = ConfigReader.get("retryCount");
        int parsed = defaultRetries;
        try {
            if (v != null && !v.isEmpty()) parsed = Integer.parseInt(v);
        } catch (NumberFormatException ignored) {}
        // ensure non-negative
        this.maxRetry = Math.max(0, parsed);
    }

    @Override
    public boolean retry(ITestResult result) {
        if (retryCount < maxRetry) {
            retryCount++;
            System.out.println("[RetryAnalyzer] Retrying " + result.getMethod().getMethodName() + " - retry " + retryCount + " of " + maxRetry);
            return true;
        }
        return false;
    }
}

