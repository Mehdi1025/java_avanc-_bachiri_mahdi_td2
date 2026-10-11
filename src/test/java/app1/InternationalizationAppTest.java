package app1;

import app1.extractor.ProjectScanner;
import app1.extractor.StringExtractor;
import app1.modifier.CodeModifier;
import app1.modifier.PropertiesGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InternationalizationAppTest {

    @Test
    void internationalizesDemoLikeProjectWithoutTouchingTarget(@TempDir Path projectRoot) throws Exception {
        Path sourceDir = projectRoot.resolve("src/main/java/com/demo");
        Files.createDirectories(sourceDir);
        Files.createDirectories(projectRoot.resolve("src/main/resources"));
        Files.createDirectories(projectRoot.resolve("target/classes"));

        Files.writeString(sourceDir.resolve("App.java"), """
                package com.demo;
                public class App {
                    public String hello() {
                        return "Bonjour le monde";
                    }
                }
                """);
        Files.writeString(projectRoot.resolve("target/classes/App.java"), "public class App {}");

        List<Path> javaFiles = ProjectScanner.scanJavaFiles(projectRoot);
        Map<String, String> messages = StringExtractor.extractFromFiles(javaFiles);

        PropertiesGenerator.generate(messages, projectRoot.resolve("src/main/resources"));
        CodeModifier.modify(javaFiles, messages);

        String transformedSource = Files.readString(sourceDir.resolve("App.java"));
        assertTrue(transformedSource.contains("ResourceBundle.getBundle(\"messages\").getString(\"msg_1\")"));
        assertTrue(Files.exists(projectRoot.resolve("src/main/resources/messages.properties")));
        assertFalse(Files.readString(projectRoot.resolve("target/classes/App.java")).contains("ResourceBundle"));
    }

    @Test
    void secondRunDoesNotBreakAlreadyInternationalizedCode(@TempDir Path projectRoot) throws Exception {
        Path sourceDir = projectRoot.resolve("src/main/java/com/demo");
        Files.createDirectories(sourceDir);
        Files.createDirectories(projectRoot.resolve("src/main/resources"));

        Files.writeString(sourceDir.resolve("App.java"), """
                package com.demo;
                import java.util.ResourceBundle;
                public class App {
                    public String hello() {
                        return ResourceBundle.getBundle("messages").getString("msg_1");
                    }
                }
                """);

        List<Path> javaFiles = ProjectScanner.scanJavaFiles(projectRoot);
        Map<String, String> messages = StringExtractor.extractFromFiles(javaFiles);

        assertTrue(messages.isEmpty());
        CodeModifier.modify(javaFiles, messages);

        String source = Files.readString(sourceDir.resolve("App.java"));
        assertTrue(source.contains("getString(\"msg_1\")"));
        assertFalse(source.contains("getString(\"msg_2\")"));
    }
}
