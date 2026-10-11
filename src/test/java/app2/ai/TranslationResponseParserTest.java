package app2.ai;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TranslationResponseParserTest {

    @Test
    void parsesJsonWrappedInMarkdownCodeBlock() {
        Map<String, String> parsed = TranslationResponseParser.parse("""
                ```json
                {"msg_1":"Hello","msg_2":"Welcome {0}"}
                ```
                """);

        assertEquals("Hello", parsed.get("msg_1"));
        assertEquals("Welcome {0}", parsed.get("msg_2"));
    }

    @Test
    void rejectsInvalidJsonResponse() {
        assertThrows(TranslationException.class, () -> TranslationResponseParser.parse("pas du json"));
    }
}
