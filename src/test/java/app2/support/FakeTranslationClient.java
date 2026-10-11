package app2.support;

import app2.ai.TranslationClient;
import app2.ai.TranslationException;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public final class FakeTranslationClient implements TranslationClient {

    private final BiFunction<Map<String, String>, String, Map<String, String>> translator;

    public FakeTranslationClient(BiFunction<Map<String, String>, String, Map<String, String>> translator) {
        this.translator = translator;
    }

    @Override
    public Map<String, String> translateProperties(Map<String, String> sourceProps, String targetLanguage) {
        return translator.apply(sourceProps, targetLanguage);
    }

    public static FakeTranslationClient prefixValuesWithLanguageCode() {
        return new FakeTranslationClient((source, language) -> {
            Map<String, String> translated = new HashMap<>();
            source.forEach((key, value) -> translated.put(key, "[" + language + "] " + value));
            return translated;
        });
    }

    public static FakeTranslationClient alwaysFails() {
        return new FakeTranslationClient((source, language) -> {
            throw new TranslationException("Réponse simulée invalide");
        });
    }
}
