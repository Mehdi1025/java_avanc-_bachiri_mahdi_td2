package app2.service;

import app2.ai.TranslationClient;
import app2.ai.TranslationException;
import app2.properties.PropertiesReader;
import app2.properties.PropertiesWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public final class LocalizationService {

    private final TranslationClient translationClient;

    public LocalizationService(TranslationClient translationClient) {
        this.translationClient = translationClient;
    }

    public LocalizationReport addLocalizations(
            Path sourcePropertiesFile,
            Path outputDirectory,
            List<String> targetLanguages,
            boolean overwriteExisting
    ) throws IOException {
        Map<String, String> sourceProperties = PropertiesReader.read(sourcePropertiesFile);
        LocalizationReport report = new LocalizationReport();

        for (String targetLanguage : targetLanguages) {
            Path outputFile = outputDirectory.resolve(LanguageCodes.toLocalizedFileName(targetLanguage));

            if (Files.exists(outputFile) && !overwriteExisting) {
                report.markSkipped(outputFile);
                continue;
            }

            try {
                Map<String, String> translatedProperties =
                        translationClient.translateProperties(sourceProperties, targetLanguage);
                PropertiesWriter.write(translatedProperties, outputFile, targetLanguage);
                report.markCreated(outputFile);
            } catch (TranslationException exception) {
                report.markFailure(targetLanguage + " : " + exception.getMessage());
            }
        }

        return report;
    }
}
