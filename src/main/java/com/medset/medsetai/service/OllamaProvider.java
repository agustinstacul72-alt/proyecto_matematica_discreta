package com.medset.medsetai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medset.medsetai.util.AppConfig;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpTimeoutException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class OllamaProvider implements AiProvider {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    private final String url;
    private final String model;
    private final String name;

    public OllamaProvider() {
        this(
                HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(5))
                        .build(),
                new ObjectMapper(),
                AppConfig.getOllamaUrl(),
                AppConfig.getOllamaModel(),
                "Ollama · Gemma 3 4B"
        );
    }

    /**
     * Creates an Ollama provider for the selected locally available model.
     *
     * @param model Ollama model tag, such as {@code nemotron-3-nano:4b}
     * @param name display name shown in the conversation
     */
    public OllamaProvider(String model, String name) {
        this(
                HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(5))
                        .build(),
                new ObjectMapper(),
                AppConfig.getOllamaUrl(),
                model,
                name
        );
    }

    OllamaProvider(
            HttpClient httpClient,
            ObjectMapper objectMapper,
            String url,
            String model
    ) {
        this(httpClient, objectMapper, url, model, "Ollama");
    }

    OllamaProvider(
            HttpClient httpClient,
            ObjectMapper objectMapper,
            String url,
            String model,
            String name
    ) {
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
        this.url = Objects.requireNonNull(url, "url").replaceAll("/+$", "");
        this.model = Objects.requireNonNull(model, "model");
        this.name = Objects.requireNonNull(name, "name");
    }

    @Override
    public String generate(String prompt) {
        return generate(prompt, ignored -> { });
    }

    /**
     * Streams Ollama response fragments as newline-delimited JSON arrives.
     *
     * @param prompt user message to send
     * @param onText consumer for generated response fragments
     * @return complete response content
     */
    @Override
    public String generate(String prompt, Consumer<String> onText) {
        Objects.requireNonNull(prompt, "prompt");
        Objects.requireNonNull(onText, "onText");

        try {
            String json = objectMapper.writeValueAsString(
                    new OllamaRequest(
                            model,
                            new OllamaMessage("user", prompt),
                            true,
                            false,
                            "10m",
                            new OllamaOptions(512, 0.4)
                    )
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url + "/api/chat"))
                    .timeout(Duration.ofMinutes(3))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<Stream<String>> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofLines()
                    );

            try (Stream<String> lines = response.body()) {
                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    String errorBody = lines
                            .filter(line -> !line.isBlank())
                            .reduce((first, next) -> first + "\n" + next)
                            .orElse("sin detalles");
                    throw new IllegalStateException(
                            "Ollama respondió con HTTP " + response.statusCode()
                                    + ": " + errorBody
                    );
                }

                StringBuilder responseText = new StringBuilder();
                lines.filter(line -> !line.isBlank()).forEach(line -> {
                    try {
                        JsonNode root = objectMapper.readTree(line);
                        if (root.hasNonNull("error")) {
                            throw new IllegalStateException(
                                    "Ollama: " + root.path("error").asText()
                            );
                        }

                        String fragment = root.path("message")
                                .path("content")
                                .asText("");
                        if (!fragment.isEmpty()) {
                            responseText.append(fragment);
                            onText.accept(fragment);
                        }
                    } catch (IOException e) {
                        throw new IllegalStateException(
                                "Ollama devolvió una respuesta JSON inválida.",
                                e
                        );
                    }
                });

                if (responseText.isEmpty()) {
                    throw new IllegalStateException(
                            "Ollama devolvió una respuesta vacía. Verifica que el "
                                    + "modelo '" + model + "' esté instalado y disponible."
                    );
                }
                return responseText.toString();
            }
        } catch (HttpTimeoutException e) {
            throw new RuntimeException(
                    "Ollama tardó demasiado. Verifica que el servidor esté activo "
                            + "y que el modelo '" + model + "' esté cargado.",
                    e
            );
        } catch (IOException e) {
            throw new RuntimeException(
                    "No se pudo conectar con Ollama: "
                            + e.getMessage(),
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

    @Override
    public String getName() {
        return name;
    }

    private record OllamaRequest(
            String model,
            OllamaMessage[] messages,
            boolean stream,
            boolean think,
            String keep_alive,
            OllamaOptions options
    ) {

        private OllamaRequest(
                String model,
                OllamaMessage message,
                boolean stream,
                boolean think,
                String keepAlive,
                OllamaOptions options
        ) {
            this(
                    model,
                    new OllamaMessage[]{message},
                    stream,
                    think,
                    keepAlive,
                    options
            );
        }
    }

    private record OllamaOptions(int num_predict, double temperature) {
    }

    private record OllamaMessage(
            String role,
            String content
    ) {
    }
}