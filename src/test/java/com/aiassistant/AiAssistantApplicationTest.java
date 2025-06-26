package com.aiassistant;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Test to verify application context loads successfully
 */
@SpringBootTest
@TestPropertySource(properties = {
    "aiassistant.telegram-bot-token=test_token",
    "aiassistant.telegram-bot-username=test_bot",
    "aiassistant.ai-provider.api-key=test_key"
})
class AiAssistantApplicationTest {

    @Test
    void contextLoads() {
        // This test will pass if the application context loads successfully
    }
}
