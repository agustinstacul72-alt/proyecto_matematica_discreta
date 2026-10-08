package com.medset.medsetai.service;

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
     * Gets the provider's display name.
     *
     * @return provider name
     */
    String getName();
}