package app2.service;

import app2.properties.PropertiesWriter;
import app2.support.FakeTranslationClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalizationServiceTest {

    @Test
    void createsMultipleLocalizationsInOneRun(@TempDir Path tempDir) throws Exception {
        Map<String, String> source = new LinkedHashMap<>();
        source.put("msg_1", "Bonjour");
        source.put("msg_2", "Bienvenue {0}");

        Path sourceFile = tempDir.resolve("messages.properties");
        PropertiesWriter.write(source, sourceFile, "français");

        LocalizationService service = new LocalizationService(FakeTranslationClient.prefixValuesWithLanguageCode());
        LocalizationReport report = service.addLocalizations(
                sourceFile,
                tempDir,
                List.of("anglais", "allemand"),
                false
        );

        assertEquals(2, report.createdFiles().size());
        assertTrue(Files.exists(tempDir.resolve("messages_en.properties")));
        assertTrue(Files.exists(tempDir.resolve("messages_de.properties")));
    }

    @Test
    void skipsExistingTranslationsUnlessForced(@TempDir Path tempDir) throws Exception {
        Map<String, String> source = Map.of("msg_1", "Bonjour");
        Path sourceFile = tempDir.resolve("messages.properties");
        PropertiesWriter.write(source, sourceFile, "français");

        Path existingEnglishFile = tempDir.resolve("messages_en.properties");
        PropertiesWriter.write(Map.of("msg_1", "Existing"), existingEnglishFile, "anglais");

        LocalizationService service = new LocalizationService(FakeTranslationClient.prefixValuesWithLanguageCode());
        LocalizationReport skippedReport = service.addLocalizations(
                sourceFile,
                tempDir,
                List.of("anglais"),
                false
        );

        assertEquals(1, skippedReport.skippedFiles().size());
        assertTrue(Files.readString(existingEnglishFile).contains("Existing"));

        LocalizationReport forcedReport = service.addLocalizations(
                sourceFile,
                tempDir,
                List.of("anglais"),
                true
        );

        assertEquals(1, forcedReport.createdFiles().size());
        assertTrue(Files.readString(existingEnglishFile).contains("[anglais]"));
    }

    @Test
    void invalidAiResponseDoesNotCorruptExistingResources(@TempDir Path tempDir) throws Exception {
        Map<String, String> source = Map.of("msg_1", "Bonjour");
        Path sourceFile = tempDir.resolve("messages.properties");
        PropertiesWriter.write(source, sourceFile, "français");

        LocalizationService service = new LocalizationService(FakeTranslationClient.alwaysFails());
        LocalizationReport report = service.addLocalizations(
                sourceFile,
                tempDir,
                List.of("espagnol"),
                false
        );

        assertFalse(Files.exists(tempDir.resolve("messages_es.properties")));
        assertTrue(report.hasFailures());
        assertTrue(Files.readString(sourceFile).contains("Bonjour"));
    }
}
