package app2.service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class LocalizationReport {

    private final List<Path> createdFiles = new ArrayList<>();
    private final List<Path> skippedFiles = new ArrayList<>();
    private final List<String> failures = new ArrayList<>();

    public void markCreated(Path file) {
        createdFiles.add(file);
    }

    public void markSkipped(Path file) {
        skippedFiles.add(file);
    }

    public void markFailure(String message) {
        failures.add(message);
    }

    public List<Path> createdFiles() {
        return List.copyOf(createdFiles);
    }

    public List<Path> skippedFiles() {
        return List.copyOf(skippedFiles);
    }

    public List<String> failures() {
        return List.copyOf(failures);
    }

    public boolean hasFailures() {
        return !failures.isEmpty();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Localisations créées : ").append(createdFiles.size()).append('\n');
        createdFiles.forEach(path -> builder.append("  + ").append(path).append('\n'));
        builder.append("Fichiers ignorés (déjà présents) : ").append(skippedFiles.size()).append('\n');
        skippedFiles.forEach(path -> builder.append("  ~ ").append(path).append('\n'));
        if (hasFailures()) {
            builder.append("Échecs : ").append(failures.size()).append('\n');
            failures.forEach(message -> builder.append("  ! ").append(message).append('\n'));
        }
        return builder.toString();
    }
}
