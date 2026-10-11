package app2.properties;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

public final class PropertiesWriter {

    private PropertiesWriter() {
    }

    public static void write(Map<String, String> translatedProperties, Path outputFile, String targetLanguage)
            throws IOException {
        Path parentDirectory = outputFile.getParent();
        if (parentDirectory != null) {
            Files.createDirectories(parentDirectory);
        }

        Properties properties = new Properties();
        for (Map.Entry<String, String> entry : translatedProperties.entrySet()) {
            properties.setProperty(entry.getKey(), entry.getValue());
        }

        try (FileWriter writer = new FileWriter(outputFile.toFile(), StandardCharsets.UTF_8)) {
            properties.store(writer, "Messages traduits en " + targetLanguage);
        }
    }
}
