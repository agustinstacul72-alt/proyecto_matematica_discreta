package com.medset.medsetai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medset.medsetai.util.AppConfig;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Generates text by making synchronous HTTP requests to an Ollama chat server.
 */
public class OllamaProvider implements AiProvider {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    private final String url;
    private final String model;

    /**
     * Creates the provider using the Ollama URL and model from {@link AppConfig}.
     */
    public OllamaProvider() {

        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();

        this.url = AppConfig.getOllamaUrl();
        this.model = AppConfig.getOllamaModel();
    }

    /**
     * Sends a non-streaming chat request to the configured Ollama server.
     *
     * @param prompt user message to send
     * @return response content from the server
     * @throws RuntimeException if the request fails, is interrupted, or returns
     *                          a non-success HTTP status
     */
    @Override
    public String generate(String prompt) {

        try {

            String json = objectMapper.writeValueAsString(
                    new OllamaRequest(
                            model,
                            new OllamaMessage("user", prompt),
                            false
                    )
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url + "/api/chat"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            if (response.statusCode() < 200 ||
                    response.statusCode() >= 300) {

                throw new RuntimeException(
                        "Ollama respondió con HTTP "
                                + response.statusCode()
                );
            }

            JsonNode root =
                    objectMapper.readTree(response.body());

            return root
                    .path("message")
                    .path("content")
                    .asText();

        } catch (IOException e) {

            throw new RuntimeException(
                    "No se pudo conectar con Ollama.",
                    e
            );

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "La comunicación con Ollama fue interrumpida.",
                    e
            );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "Ollama";
    }

    /**
     * JSON request body expected by the Ollama chat endpoint.
     */
    private record OllamaRequest(
            String model,
            OllamaMessage[] messages,
            boolean stream
    ) {

        private OllamaRequest(
                String model,
                OllamaMessage message,
                boolean stream
        ) {
            this(
                    model,
                    new OllamaMessage[]{message},
                    stream
            );
        }
    }

    /**
     * Message entry in an Ollama chat request.
     */
    private record OllamaMessage(
            String role,
            String content
    ) {
    }
}