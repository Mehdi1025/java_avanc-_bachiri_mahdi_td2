package app2.ai;

import java.util.Map;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.stream.Collectors;

public final class TranslationValidator {

    private static final java.util.regex.Pattern PLACEHOLDER_PATTERN = java.util.regex.Pattern.compile("\\{\\d+}");

    private TranslationValidator() {
    }

    public static void validate(Map<String, String> sourceProps, Map<String, String> translatedProps) {
        if (!sourceProps.keySet().equals(translatedProps.keySet())) {
            throw new TranslationException(
                    "Réponse IA invalide : les clés traduites ne correspondent pas au dictionnaire source.");
        }

        for (Map.Entry<String, String> entry : translatedProps.entrySet()) {
            String key = entry.getKey();
            String translatedValue = entry.getValue();

            if (translatedValue == null || translatedValue.isBlank()) {
                throw new TranslationException(
                        "Réponse IA invalide : la valeur traduite pour la clé '" + key + "' est vide.");
            }

            validatePlaceholders(sourceProps.get(key), translatedValue, key);
        }
    }

    public static void validatePlaceholders(String sourceValue, String translatedValue, String key) {
        Set<String> sourcePlaceholders = extractPlaceholders(sourceValue);
        Set<String> translatedPlaceholders = extractPlaceholders(translatedValue);

        if (!sourcePlaceholders.equals(translatedPlaceholders)) {
            throw new TranslationException(
                    "Réponse IA invalide : les paramètres de '" + key
                            + "' ne sont pas conservés (attendu " + sourcePlaceholders
                            + ", obtenu " + translatedPlaceholders + ").");
        }
    }

    public static Set<String> extractPlaceholders(String value) {
        return PLACEHOLDER_PATTERN.matcher(value)
                .results()
                .map(MatchResult::group)
                .collect(Collectors.toSet());
    }
}
