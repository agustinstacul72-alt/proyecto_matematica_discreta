package com.medset.medsetai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medset.medsetai.model.Symptom;
import com.medset.medsetai.util.AppConfig;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Uses a local language model to normalize symptom descriptions.
 *
 * <p>The normalizer asks Ollama Nemotron to identify the input language
 * and return standardized English symptom labels in JSON format. It
 * removes duplicate labels while preserving their order.</p>
 *
 * <p>This component performs text normalization only. It does not diagnose
 * diseases, assess medical risk, or recommend treatments.</p>
 */
public class SymptomNormalizer {

    /** Service used to send requests to the configured AI provider. */
    private final AiService aiService;

    /** JSON parser used to interpret the model response. */
    private final ObjectMapper objectMapper;

    /**
     * Creates a normalizer using the configured Nemotron model.
     */
    public SymptomNormalizer() {
        this.aiService = new AiService(
                new OllamaProvider(
                        AppConfig.getOllamaNemotronModel(),
                        "Ollama · Nemotron"
                )
        );

        this.objectMapper = new ObjectMapper();
    }

    /**
     * Converts user-provided symptom text into standardized symptom models.
     *
     * @param input free-text symptom descriptions
     * @return normalized symptoms with original text and detected language
     * @throws IllegalArgumentException if the input is blank
     * @throws IllegalStateException if the AI response cannot be processed
     */
    public List<Symptom> normalize(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException(
                    "Please enter at least one symptom."
            );
        }

        String prompt = buildPrompt(input);
        String response = aiService.ask(prompt);

        try {
            JsonNode json = extractJson(response);
            JsonNode languageNode = json.path("language");
            JsonNode symptomsNode = json.path("symptoms");

            String language = languageNode.asText("unknown")
                    .toLowerCase(Locale.ROOT);

            if (!symptomsNode.isArray()) {
                throw new IllegalStateException(
                        "The AI response does not contain a symptoms array."
                );
            }

            Set<String> normalizedNames = new LinkedHashSet<>();

            for (JsonNode symptomNode : symptomsNode) {
                String normalizedName = symptomNode.asText("")
                        .trim()
                        .toLowerCase(Locale.ROOT)
                        .replace(' ', '_');

                if (!normalizedName.isBlank()) {
                    normalizedNames.add(normalizedName);
                }
            }

            if (normalizedNames.isEmpty()) {
                throw new IllegalStateException(
                        "The AI did not return any usable symptom labels."
                );
            }

            List<Symptom> symptoms = new ArrayList<>();

            for (String normalizedName : normalizedNames) {
                symptoms.add(new Symptom(
                        input.trim(),
                        normalizedName,
                        language
                ));
            }

            return symptoms;

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Could not normalize the symptom text. "
                            + "Check that Ollama is running and the model is available.",
                    exception
            );
        }
    }

    /**
     * Builds the prompt that instructs the AI to return structured data.
     *
     * @param input original symptom descriptions
     * @return prompt sent to the language model
     */
    private String buildPrompt(String input) {
        return """
                You are a text normalization component for an educational
                application about mathematical set theory.

                Your task is to identify symptom descriptions and convert
                them into concise, standardized English labels.

                Rules:
                1. Detect the input language and return its ISO language code.
                2. Return symptom labels in English.
                3. Use lowercase snake_case labels, such as sore_throat.
                4. Remove duplicate symptoms.
                5. Do not infer symptoms that are not present in the text.
                6. Do not diagnose diseases or recommend treatment.
                7. Return valid JSON only, without Markdown fences or explanations.

                Required JSON format:
                {
                  "language": "es",
                  "symptoms": ["fever", "cough", "sore_throat"]
                }

                User input:
                %s
                """.formatted(input);
    }

    /**
     * Extracts a JSON object from a model response.
     *
     * <p>The method accepts a response containing either raw JSON or text
     * surrounding a JSON object. The extracted object is then parsed by
     * Jackson.</p>
     *
     * @param response raw response from the AI provider
     * @return parsed JSON tree
     * @throws Exception if no valid JSON object can be parsed
     */
    private JsonNode extractJson(String response) throws Exception {
        if (response == null || response.isBlank()) {
            throw new IllegalStateException("The AI returned an empty response.");
        }

        String trimmed = response.trim();
        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');

        if (start < 0 || end <= start) {
            throw new IllegalStateException(
                    "The AI response does not contain a JSON object."
            );
        }

        String jsonText = trimmed.substring(start, end + 1);
        return objectMapper.readTree(jsonText);
    }
}