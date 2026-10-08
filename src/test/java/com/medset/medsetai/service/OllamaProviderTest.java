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
        JsonNode[] capturedRequest = new JsonNode[1];
        server.createContext("/api/chat", exchange -> {
            capturedRequest[0] = objectMapper.readTree(exchange.getRequestBody());
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
            assertEquals("test-model", capturedRequest[0].path("model").asText());
            assertTrue(capturedRequest[0].path("stream").asBoolean());
            assertEquals(512, capturedRequest[0]
                    .path("options")
                    .path("num_predict")
                    .asInt());
        } finally {
            server.stop(0);
        }
    }
}
