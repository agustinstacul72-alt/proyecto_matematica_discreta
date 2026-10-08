package com.medset.medsetai.service;

import com.google.genai.Client;
import com.google.genai.ResponseStream;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.ThinkingConfig;
import com.medset.medsetai.util.AppConfig;

import java.util.Objects;
import java.util.function.Consumer;

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
        return generate(prompt, ignored -> { });
    }

    /**
     * Streams Gemini response fragments and limits output and reasoning for
     * quicker conversational replies.
     *
     * @param prompt text to send to Gemini
     * @param onText consumer for generated response fragments
     * @return complete generated response text
     */
    @Override
    public String generate(String prompt, Consumer<String> onText) {
        Objects.requireNonNull(prompt, "prompt");
        Objects.requireNonNull(onText, "onText");

        GenerateContentConfig config = GenerateContentConfig.builder()
                .temperature(0.4f)
                .maxOutputTokens(512)
                .thinkingConfig(ThinkingConfig.builder()
                        .thinkingBudget(0)
                        .build())
                .build();

        StringBuilder responseText = new StringBuilder();
        try (ResponseStream<GenerateContentResponse> responses =
                     client.models.generateContentStream(model, prompt, config)) {
            for (GenerateContentResponse response : responses) {
                String fragment = response.text();
                if (fragment != null && !fragment.isEmpty()) {
                    responseText.append(fragment);
                    onText.accept(fragment);
                }
            }
        } catch (RuntimeException e) {
            throw new IllegalStateException(
                    "Gemini no pudo generar una respuesta. Verifica GEMINI_API_KEY, "
                            + "GEMINI_MODEL y que la API de Gemini esté habilitada: "
                            + e.getMessage(),
                    e
            );
        }

        if (responseText.isEmpty()) {
            throw new IllegalStateException(
                    "Gemini devolvió una respuesta vacía. Verifica el modelo "
                            + "configurado y vuelve a intentarlo."
            );
        }

        return responseText.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "Gemini";
    }
}