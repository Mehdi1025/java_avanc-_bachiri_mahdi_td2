package app2.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

import java.util.Map;

public final class AlbertTranslationClient implements TranslationClient {

    private static final String ALBERT_BASE_URL = "https://albert.api.etalab.gouv.fr/v1";
    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;

    public AlbertTranslationClient(String modelName) {
        String apiKey = AlbertApiKeyLoader.load();
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

    @Override
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
            Map<String, String> translatedProps = TranslationResponseParser.parse(rawResponse);
            TranslationValidator.validate(sourceProps, translatedProps);
            return translatedProps;
        } catch (TranslationException e) {
            throw e;
        } catch (Exception e) {
            throw new TranslationException("Erreur lors de l'appel à l'API Albert : " + e.getMessage(), e);
        }
    }

    private String buildPrompt(Map<String, String> sourceProps, String targetLanguage) {
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

}
