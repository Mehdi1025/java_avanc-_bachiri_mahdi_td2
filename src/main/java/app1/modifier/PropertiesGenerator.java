package app1.modifier;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

public final class PropertiesGenerator {

    private static final String PROPERTIES_FILE_NAME = "messages.properties";

    private PropertiesGenerator() {
    }

    public static void generate(Map<String, String> extractedStrings, Path destinationDirectory) throws IOException {
        Files.createDirectories(destinationDirectory);

        Properties properties = new Properties();
        for (Map.Entry<String, String> entry : extractedStrings.entrySet()) {
            properties.setProperty(entry.getKey(), entry.getValue());
        }

        Path propertiesFile = destinationDirectory.resolve(PROPERTIES_FILE_NAME);
        try (FileWriter writer = new FileWriter(propertiesFile.toFile(), StandardCharsets.UTF_8)) {
            properties.store(writer, "Messages extraits pour l'internationalisation");
        }
    }
}
