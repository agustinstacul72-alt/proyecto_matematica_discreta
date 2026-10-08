package com.medset.medsetai.util;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Reads AI provider settings from a local {@code .env} file and supplies
 * defaults for optional settings.
 */
public final class AppConfig {

    private static final Dotenv DOTENV = Dotenv.configure()
            .ignoreIfMissing()
            .load();

    private AppConfig() {
    }

    /**
     * Gets the required Gemini API key.
     *
     * @return configured Gemini API key
     * @throws IllegalStateException if {@code GEMINI_API_KEY} is absent or blank
     */
    public static String getGeminiApiKey() {

        String value = DOTENV.get("GEMINI_API_KEY");

        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "GEMINI_API_KEY no está configurada en .env"
            );
        }

        return value;
    }

    /**
     * Gets the Gemini model identifier.
     *
     * @return configured model, or {@code gemini-3.8-flash}
     */
    public static String getGeminiModel() {

        return getOrDefault(
                "GEMINI_MODEL",
                "gemini-3.8-flash"
        );
    }

    /**
     * Gets the Ollama server base URL.
     *
     * @return configured URL, or {@code http://localhost:11434}
     */
    public static String getOllamaUrl() {

        return getOrDefault(
                "OLLAMA_URL",
                "http://localhost:11434"
        );
    }

    /**
     * Gets the Ollama model identifier.
     *
     * @return configured model, or {@code gemma3:4b}
     */
    public static String getOllamaModel() {

        return getOrDefault(
                "OLLAMA_MODEL",
                "gemma3:4b"
        );
    }

    /**
     * Returns a nonblank setting or its fallback.
     *
     * @param key dotenv key to read
     * @param defaultValue fallback when the key is absent or blank
     * @return configured value or fallback
     */
    private static String getOrDefault(
            String key,
            String defaultValue
    ) {

        String value = DOTENV.get(key);

        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        return value;
    }
}