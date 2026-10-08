package com.medset.medsetai.service;

/**
 * Facade for delegating prompts to the currently selected AI provider.
 */
public class AiService {

    private AiProvider provider;

    /**
     * Creates the service with an initial provider.
     *
     * @param provider provider used for subsequent requests
     */
    public AiService(AiProvider provider) {
        this.provider = provider;
    }

    /**
     * Sends a prompt to the current provider.
     *
     * @param prompt text to send
     * @return generated response
     */
    public String ask(String prompt) {
        return provider.generate(prompt);
    }

    /**
     * Gets the name of the current provider.
     *
     * @return provider name
     */
    public String getProviderName() {
        return provider.getName();
    }

    /**
     * Replaces the provider used for subsequent requests.
     *
     * @param provider replacement provider
     */
    public void setProvider(AiProvider provider) {
        this.provider = provider;
    }
}