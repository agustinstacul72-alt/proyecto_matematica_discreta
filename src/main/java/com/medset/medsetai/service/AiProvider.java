package com.medset.medsetai.service;

import java.util.function.Consumer;

/**
 * Common contract for services that generate text using an AI provider.
 */
public interface AiProvider {

    /**
     * Generates a response to the supplied prompt.
     *
     * @param prompt text to send to the provider
     * @return generated response text
     */
    String generate(String prompt);

    /**
     * Generates a response and sends each available text fragment to a
     * consumer. Providers that do not support streaming emit their completed
     * response as a single fragment.
     *
     * @param prompt text to send to the provider
     * @param onText fragment consumer, called on the generation thread
     * @return complete generated response text
     */
    default String generate(String prompt, Consumer<String> onText) {
        String response = generate(prompt);
        onText.accept(response);
        return response;
    }

    /**
     * Gets the provider's display name.
     *
     * @return provider name
     */
    String getName();
}