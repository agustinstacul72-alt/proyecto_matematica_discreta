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
     * Gets the second Ollama model used by the model selector.
     *
     * @return configured model, or {@code nemotron-3-nano:4b}
     */
    public static String getOllamaNemotronModel() {

        return getOrDefault(
                "OLLAMA_NEMOTRON_MODEL",
                "nemotron-3-nano:4b"
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