package app2.properties;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PropertiesIOTest {

    @Test
    void readAndWriteRoundTrip(@TempDir Path tempDir) throws Exception {
        Map<String, String> source = new LinkedHashMap<>();
        source.put("msg_1", "Bonjour");
        source.put("msg_2", "Bienvenue {0}");

        Path outputFile = tempDir.resolve("messages_en.properties");
        PropertiesWriter.write(source, outputFile, "anglais");

        Map<String, String> loaded = PropertiesReader.read(outputFile);
        assertEquals(source, loaded);
    }
}
