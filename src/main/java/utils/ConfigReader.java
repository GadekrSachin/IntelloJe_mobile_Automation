package utils;

import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {

    private static Properties props = new Properties();
    static {
        try (InputStream in = ConfigReader.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (in != null) props.load(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static String get(String key) {
        // First prefer system property overrides (e.g., -Dbs_user=...)
        try {
            String sys = System.getProperty(key);
            if (sys != null && !sys.isEmpty()) return sys;
        } catch (Exception ignored) {}

        return props.getProperty(key);
    }
}
