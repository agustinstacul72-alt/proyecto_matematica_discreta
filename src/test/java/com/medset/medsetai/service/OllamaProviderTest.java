package com.medset.medsetai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OllamaProviderTest {

    @Test
    void streamsOllamaResponseAndRequestsBoundedOutput() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        HttpServer server = HttpServer.create(
                new InetSocketAddress("127.0.0.1", 0),
                0
        );
        List<JsonNode> capturedRequests = new CopyOnWriteArrayList<>();
        server.createContext("/api/chat", exchange -> {
            JsonNode request = objectMapper.readTree(exchange.getRequestBody());
            capturedRequests.add(request);
            byte[] response = (
                    "{\"message\":{\"content\":\"Hola \"},\"done\":false}\n"
                            + "{\"message\":{\"content\":\"mundo\"},\"done\":false}\n"
                            + "{\"message\":{\"content\":\"\"},\"done\":true}\n"
            ).getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add(
                    "Content-Type",
                    "application/x-ndjson"
            );
            exchange.sendResponseHeaders(200, 0);
            try (var output = exchange.getResponseBody()) {
                output.write(response);
                output.flush();
            }
        });
        server.start();

        try {
            OllamaProvider provider = new OllamaProvider(
                    HttpClient.newHttpClient(),
                    objectMapper,
                    "http://127.0.0.1:" + server.getAddress().getPort() + "/",
                    "test-model"
            );
            List<String> fragments = new ArrayList<>();

            String response = provider.generate("test prompt", fragments::add);

            assertEquals("Hola mundo", response);
            assertEquals(List.of("Hola ", "mundo"), fragments);
            JsonNode firstRequest = capturedRequests.get(0);
            assertEquals("test-model", firstRequest.path("model").asText());
            assertTrue(firstRequest.path("stream").asBoolean());
            assertEquals(512, firstRequest
                    .path("options")
                    .path("num_predict")
                    .asInt());

            OllamaProvider nemotron = new OllamaProvider(
                    HttpClient.newHttpClient(),
                    objectMapper,
                    "http://127.0.0.1:" + server.getAddress().getPort(),
                    "nemotron-3-nano:4b",
                    "Ollama · Nemotron 3 Nano 4B"
            );
            assertEquals(
                    "Ollama · Nemotron 3 Nano 4B",
                    nemotron.getName()
            );
            nemotron.generate("test prompt", ignored -> { });
            assertEquals(
                    List.of("test-model", "nemotron-3-nano:4b"),
                    capturedRequests.stream()
                            .map(request -> request.path("model").asText())
                            .toList()
            );
        } finally {
            server.stop(0);
        }
    }
}
