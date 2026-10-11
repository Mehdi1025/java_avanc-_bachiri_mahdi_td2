package app1.extractor;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public final class ProjectScanner {

    private static final Set<String> EXCLUDED_DIRECTORIES = Set.of(
            "target", ".git", ".mvn", "node_modules", "build", "out", ".idea"
    );

    private ProjectScanner() {
    }

    public static List<Path> scanJavaFiles(Path root) {
        try (Stream<Path> paths = Files.walk(root)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".java"))
                    .filter(path -> !isInsideExcludedDirectory(root, path))
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException("Impossible de parcourir le répertoire : " + root, e);
        }
    }

    private static boolean isInsideExcludedDirectory(Path root, Path file) {
        Path relativePath = root.relativize(file);
        for (int index = 0; index < relativePath.getNameCount(); index++) {
            if (EXCLUDED_DIRECTORIES.contains(relativePath.getName(index).toString())) {
                return true;
            }
        }
        return false;
    }
}
