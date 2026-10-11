package app2.properties;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public final class PropertiesReader {

    private PropertiesReader() {
    }

    public static Map<String, String> read(Path propertiesFile) throws IOException {
        Properties properties = new Properties();

        try (FileReader reader = new FileReader(propertiesFile.toFile(), StandardCharsets.UTF_8)) {
            properties.load(reader);
        }

        Map<String, String> result = new LinkedHashMap<>();
        for (String key : properties.stringPropertyNames()) {
            result.put(key, properties.getProperty(key));
        }
        return result;
    }
}
