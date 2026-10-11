package app2.ai;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public final class AlbertApiKeyLoader {

    private static final String KEY_NAME = "ALBERT_API_KEY";

    private AlbertApiKeyLoader() {
    }

    public static String load() {
        return loadOptional()
                .orElseThrow(() -> new TranslationException(
                        "Clé API absente : définissez " + KEY_NAME
                                + " ou créez un fichier .env à la racine du projet."));
    }

    static Optional<String> loadOptional() {
        return loadOptionalFrom(Path.of("").toAbsolutePath());
    }

    static Optional<String> loadOptionalFrom(Path startDirectory) {
        String fromEnvironment = System.getenv(KEY_NAME);
        if (fromEnvironment != null && !fromEnvironment.isBlank()) {
            return Optional.of(fromEnvironment.trim());
        }
        return findEnvFile(startDirectory)
                .flatMap(AlbertApiKeyLoader::readKeyFromEnvFile);
    }

    private static Optional<Path> findEnvFile(Path startDirectory) {
        Path current = startDirectory.toAbsolutePath().normalize();
        while (current != null) {
            Path envFile = current.resolve(".env");
            if (Files.isRegularFile(envFile)) {
                return Optional.of(envFile);
            }
            current = current.getParent();
        }
        return Optional.empty();
    }

    private static Optional<String> readKeyFromEnvFile(Path envFile) {
        try {
            List<String> lines = Files.readAllLines(envFile);
            return lines.stream()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                    .filter(line -> line.startsWith(KEY_NAME + "="))
                    .map(line -> line.substring(KEY_NAME.length() + 1).trim())
                    .filter(value -> !value.isBlank())
                    .findFirst();
        } catch (IOException exception) {
            throw new TranslationException("Impossible de lire le fichier " + envFile, exception);
        }
    }
}
