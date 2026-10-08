package com.medset.medsetai.service;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AiServiceTest {

    @Test
    void forwardsResponseAndEmitsItAsAFragmentForLegacyProviders() {
        AiService service = new AiService(new TestProvider());
        List<String> fragments = new ArrayList<>();

        String response = service.ask("prompt", fragments::add);

        assertEquals("hello world", response);
        assertEquals(List.of("hello world"), fragments);
    }

    private static final class TestProvider implements AiProvider {

        @Override
        public String generate(String prompt) {
            return "hello world";
        }

        @Override
        public String getName() {
            return "test";
        }
    }
}
