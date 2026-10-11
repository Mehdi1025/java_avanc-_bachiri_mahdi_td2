package app2.service;

import java.util.Locale;
import java.util.Map;

public final class LanguageCodes {

    private static final Map<String, String> LANGUAGE_ALIASES = Map.ofEntries(
            Map.entry("anglais", "en"),
            Map.entry("english", "en"),
            Map.entry("en", "en"),
            Map.entry("allemand", "de"),
            Map.entry("german", "de"),
            Map.entry("de", "de"),
            Map.entry("espagnol", "es"),
            Map.entry("spanish", "es"),
            Map.entry("es", "es"),
            Map.entry("francais", "fr"),
            Map.entry("français", "fr"),
            Map.entry("french", "fr"),
            Map.entry("fr", "fr")
    );

    private LanguageCodes() {
    }

    public static String toResourceSuffix(String targetLanguage) {
        String normalized = targetLanguage.trim().toLowerCase(Locale.ROOT);
        String code = LANGUAGE_ALIASES.get(normalized);
        if (code == null) {
            throw new IllegalArgumentException("Langue non reconnue : " + targetLanguage);
        }
        return code;
    }

    public static String toLocalizedFileName(String targetLanguage) {
        return "messages_" + toResourceSuffix(targetLanguage) + ".properties";
    }
}
