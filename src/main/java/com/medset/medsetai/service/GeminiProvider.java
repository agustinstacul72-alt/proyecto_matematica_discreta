package com.medset.medsetai.service;

import com.google.genai.Client;
import com.google.genai.gaos.models.interactions.CreateModelInteraction;
import com.google.genai.gaos.models.interactions.GenerationConfig;
import com.google.genai.gaos.models.interactions.InteractionSSEStreamEvent;
import com.google.genai.gaos.models.interactions.InteractionsInput;
import com.google.genai.gaos.models.interactions.StepDelta;
import com.google.genai.gaos.models.interactions.TextDelta;
import com.google.genai.gaos.models.interactions.ThinkingLevel;
import com.google.genai.gaos.models.operations.CreateInteractionRequestBody;
import com.google.genai.gaos.utils.EventStream;
import com.medset.medsetai.util.AppConfig;

import java.io.IOException;
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

        CreateModelInteraction interaction = CreateModelInteraction.builder()
                .model(model)
                .input(InteractionsInput.of(prompt))
                .stream(true)
                .store(false)
                .generationConfig(GenerationConfig.builder()
                        .maxOutputTokens(512)
                        .thinkingLevel(ThinkingLevel.MINIMAL)
                        .build())
                .build();

        StringBuilder responseText = new StringBuilder();
        try {
            var response = client.interactions.create(
                    CreateInteractionRequestBody.of(interaction)
            );
            try (EventStream<InteractionSSEStreamEvent> events = response.events()) {
                for (InteractionSSEStreamEvent event : events) {
                    event.data().ifPresent(data -> {
                        if (data instanceof StepDelta stepDelta) {
                            stepDelta.delta()
                                    .filter(TextDelta.class::isInstance)
                                    .map(TextDelta.class::cast)
                                    .flatMap(TextDelta::text)
                                    .filter(fragment -> !fragment.isEmpty())
                                    .ifPresent(fragment -> {
                                        responseText.append(fragment);
                                        onText.accept(fragment);
                                    });
                        }
                    });
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Se interrumpió la transmisión de Gemini: " + e.getMessage(),
                    e
            );
        } catch (RuntimeException e) {
            throw new IllegalStateException(
                    "Gemini no pudo generar una respuesta con Interactions API. "
                            + "Verifica GEMINI_API_KEY, GEMINI_MODEL y el acceso a la API: "
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