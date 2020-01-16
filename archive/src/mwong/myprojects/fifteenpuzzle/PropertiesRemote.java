package mwong.myprojects.fifteenpuzzle;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;

/**
 * PropertiesCache load all custom setting from resources/config.properties file.
 *
 * @author Meisze Wong
 *         www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public class PropertiesRemote {
    private final String filepath = "resources/remote.properties";
    private final Properties prop = new Properties();

    private PropertiesRemote() {
        try (FileInputStream in = new FileInputStream(filepath)) {
            prop.load(in);
        } catch (IOException ex) {
            System.err.println("Unable to locate configuration files, restore to"
                    + " system default settings");
        }
    }

    private static class Singleton {
        private static final PropertiesRemote INSTANCE = new PropertiesRemote();
    }

    public static PropertiesRemote getInstance() {
        return Singleton.INSTANCE;
    }

    public String getProperty(String key) {
        return prop.getProperty(key);
    }

    public Set<String> getAllPropertyNames() {
        return prop.stringPropertyNames();
    }

    public boolean containsKey(String key) {
        return prop.containsKey(key);
    }
}