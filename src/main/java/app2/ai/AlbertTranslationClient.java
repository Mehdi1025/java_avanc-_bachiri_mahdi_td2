package app2.ai;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AlbertTranslationClient {

    private static final String ALBERT_BASE_URL = "https://albert.api.etalab.gouv.fr/v1";
    private static final String API_KEY_ENV = "ALBERT_API_KEY";
    private static final Pattern JSON_CODE_BLOCK = Pattern.compile("```(?:json)?\\s*(\\{.*?\\})\\s*```", Pattern.DOTALL);

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;

    public AlbertTranslationClient(String modelName) {
        String apiKey = System.getenv(API_KEY_ENV);
        if (apiKey == null || apiKey.isBlank()) {
            throw new TranslationException("La variable d'environnement " + API_KEY_ENV + " est absente ou vide.");
        }
        if (modelName == null || modelName.isBlank()) {
            throw new TranslationException("Le nom du modèle est obligatoire.");
        }

        this.chatModel = OpenAiChatModel.builder()
                .baseUrl(ALBERT_BASE_URL)
                .apiKey(apiKey)
                .modelName(modelName)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public Map<String, String> translateProperties(Map<String, String> sourceProps, String targetLanguage) {
        if (sourceProps == null || sourceProps.isEmpty()) {
            return Map.of();
        }
        if (targetLanguage == null || targetLanguage.isBlank()) {
            throw new TranslationException("La langue cible est obligatoire.");
        }

        String prompt = buildPrompt(sourceProps, targetLanguage);

        try {
            String rawResponse = chatModel.chat(prompt);
            Map<String, String> translatedProps = parseTranslatedProperties(rawResponse);
            validateTranslatedProperties(sourceProps, translatedProps);
            return translatedProps;
        } catch (TranslationException e) {
            throw e;
        } catch (Exception e) {
            throw new TranslationException("Erreur lors de l'appel à l'API Albert : " + e.getMessage(), e);
        }
    }

    private String buildPrompt(Map<String, String> sourceProps, String targetLanguage) throws TranslationException {
        try {
            String sourceJson = objectMapper.writeValueAsString(sourceProps);
            return """
                    Tu es un traducteur expert. Traduis les valeurs de ce dictionnaire JSON en %s. \
                    Tu dois IMPÉRATIVEMENT conserver les marqueurs de paramètres comme {0} ou {1}. \
                    Renvoie UNIQUEMENT un objet JSON valide où les clés sont identiques et les valeurs sont traduites, sans aucun autre texte.

                    %s
                    """.formatted(targetLanguage, sourceJson);
        } catch (Exception e) {
            throw new TranslationException("Impossible de sérialiser les propriétés source en JSON.", e);
        }
    }

    private Map<String, String> parseTranslatedProperties(String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank()) {
            throw new TranslationException("Réponse IA vide.");
        }

        String jsonPayload = extractJsonPayload(rawResponse.trim());

        try {
            Map<String, String> parsed = objectMapper.readValue(jsonPayload, new TypeReference<>() {
            });
            return new LinkedHashMap<>(parsed);
        } catch (Exception e) {
            throw new TranslationException("Réponse IA invalide : le JSON retourné n'a pas pu être parsé.", e);
        }
    }

    private String extractJsonPayload(String response) {
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

    private void validateTranslatedProperties(Map<String, String> sourceProps, Map<String, String> translatedProps) {
        if (!sourceProps.keySet().equals(translatedProps.keySet())) {
            throw new TranslationException("Réponse IA invalide : les clés traduites ne correspondent pas au dictionnaire source.");
        }

        for (Map.Entry<String, String> entry : translatedProps.entrySet()) {
            if (entry.getValue() == null || entry.getValue().isBlank()) {
                throw new TranslationException("Réponse IA invalide : la valeur traduite pour la clé '" + entry.getKey() + "' est vide.");
            }
        }
    }
}
