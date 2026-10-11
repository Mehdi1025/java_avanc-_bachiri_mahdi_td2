package app2.ai;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TranslationValidatorTest {

    @Test
    void extractsPlaceholders() {
        Set<String> placeholders = TranslationValidator.extractPlaceholders("Bonjour {0}, vous avez {1} messages.");
        assertEquals(Set.of("{0}", "{1}"), placeholders);
    }

    @Test
    void rejectsMissingPlaceholderInTranslation() {
        Map<String, String> source = Map.of("msg_1", "Bienvenue {0}");
        Map<String, String> translated = Map.of("msg_1", "Welcome");

        TranslationException exception = assertThrows(
                TranslationException.class,
                () -> TranslationValidator.validate(source, translated)
        );

        assertTrue(exception.getMessage().contains("msg_1"));
    }

    @Test
    void acceptsValidTranslation() {
        Map<String, String> source = Map.of("msg_1", "Bienvenue {0}");
        Map<String, String> translated = Map.of("msg_1", "Welcome {0}");

        TranslationValidator.validate(source, translated);
    }
}
