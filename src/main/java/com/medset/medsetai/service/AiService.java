package com.medset.medsetai.service;

import java.util.function.Consumer;

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
     * Sends a prompt to the current provider and reports response fragments.
     *
     * @param prompt text to send
     * @param onText consumer for generated response fragments
     * @return complete generated response
     */
    public String ask(String prompt, Consumer<String> onText) {
        AiProvider selectedProvider = provider;
        return selectedProvider.generate(prompt, onText);
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