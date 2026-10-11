package app2.ai;

import java.util.Map;

public interface TranslationClient {

    Map<String, String> translateProperties(Map<String, String> sourceProps, String targetLanguage);
}
