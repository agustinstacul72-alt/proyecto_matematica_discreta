package com.medset.medsetai.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.medset.medsetai.util.AppConfig;

/**
 * Generates text with the Google Gemini API.
 *
 * <p>Construction requires {@code GEMINI_API_KEY} in the local configuration.</p>
 */
public class GeminiProvider implements AiProvider {

    private final Client client;
    private final String model;

    /**
     * Creates the provider using the API key and model from {@link AppConfig}.
     */
    public GeminiProvider() {

        String apiKey = AppConfig.getGeminiApiKey();

        this.client = Client.builder()
                .apiKey(apiKey)
                .build();

        this.model = AppConfig.getGeminiModel();
    }

    /**
     * Requests a response from the configured Gemini model.
     *
     * @param prompt text to send to Gemini
     * @return generated response text
     */
    @Override
    public String generate(String prompt) {

        GenerateContentResponse response =
                client.models.generateContent(
                        model,
                        prompt,
                        null
                );

        return response.text();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "Gemini";
    }
}