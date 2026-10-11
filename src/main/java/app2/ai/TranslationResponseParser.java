package app2.ai;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class TranslationResponseParser {

    private static final Pattern JSON_CODE_BLOCK = Pattern.compile("```(?:json)?\\s*(\\{.*?\\})\\s*```", Pattern.DOTALL);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private TranslationResponseParser() {
    }

    static Map<String, String> parse(String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank()) {
            throw new TranslationException("Réponse IA vide.");
        }

        String jsonPayload = extractJsonPayload(rawResponse.trim());

        try {
            Map<String, String> parsed = OBJECT_MAPPER.readValue(jsonPayload, new TypeReference<>() {
            });
            return new LinkedHashMap<>(parsed);
        } catch (Exception exception) {
            throw new TranslationException("Réponse IA invalide : le JSON retourné n'a pas pu être parsé.", exception);
        }
    }

    private static String extractJsonPayload(String response) {
        Matcher matcher = JSON_CODE_BLOCK.matcher(response);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        int start = response.indexOf('{');
        int end = response.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return response.substring(start, end + 1);
        }

        throw new TranslationException("Réponse IA invalide : aucun objet JSON détecté.");
    }
}
