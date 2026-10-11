package app1.extractor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProjectScannerTest {

    @Test
    void excludesGeneratedAndVersionControlDirectories(@TempDir Path root) throws Exception {
        Path sourceFile = root.resolve("src/main/java/com/demo/App.java");
        Files.createDirectories(sourceFile.getParent());
        Files.writeString(sourceFile, "public class App {}");

        Path ignoredFile = root.resolve("target/generated/App.java");
        Files.createDirectories(ignoredFile.getParent());
        Files.writeString(ignoredFile, "public class App {}");

        List<Path> javaFiles = ProjectScanner.scanJavaFiles(root);

        assertEquals(1, javaFiles.size());
        assertTrue(javaFiles.getFirst().endsWith("src/main/java/com/demo/App.java"));
    }
}
