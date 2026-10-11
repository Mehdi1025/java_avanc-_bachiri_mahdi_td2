package app1.extractor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StringExtractorTest {

    @Test
    void filtersIrrelevantStringsAndIgnoresComments(@TempDir Path tempDir) throws Exception {
        Path javaFile = tempDir.resolve("Sample.java");
        Files.writeString(javaFile, """
                public class Sample {
                    public void demo() {
                        // "Texte dans commentaire"
                        String message = "Bonjour utilisateur";
                        String ignored = "x";
                        String special = "!!!";
                        System.out.println("Bienvenue {0}");
                    }
                }
                """);

        List<String> extracted = StringExtractor.extractFromFile(javaFile);

        assertEquals(2, extracted.size());
        assertTrue(extracted.contains("Bonjour utilisateur"));
        assertTrue(extracted.contains("Bienvenue {0}"));
        assertFalse(extracted.contains("x"));
        assertFalse(extracted.contains("!!!"));
    }

    @Test
    void deduplicatesValuesAcrossFiles(@TempDir Path tempDir) throws Exception {
        Path firstFile = tempDir.resolve("First.java");
        Path secondFile = tempDir.resolve("Second.java");
        Files.writeString(firstFile, "public class First { String s = \"Bonjour\"; }");
        Files.writeString(secondFile, "public class Second { String s = \"Bonjour\"; }");

        Map<String, String> messages = StringExtractor.extractFromFiles(List.of(firstFile, secondFile));

        assertEquals(1, messages.size());
        assertEquals("Bonjour", messages.get("msg_1"));
    }
}
