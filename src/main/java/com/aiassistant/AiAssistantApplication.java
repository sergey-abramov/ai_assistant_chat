package com.aiassistant;

import com.aiassistant.config.ApplicationProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Main Spring Boot application class.
 * Follows Single Responsibility Principle - handles only application startup.
 */
@Slf4j
@SpringBootApplication
@EnableConfigurationProperties(ApplicationProperties.class)
public class AiAssistantApplication {
    
    public static void main(String[] args) {
        try {
            SpringApplication.run(AiAssistantApplication.class, args);
            log.info("🤖 AI Assistant Telegram Bot started successfully!");
        } catch (Exception e) {
            log.error("Failed to start AI Assistant Bot", e);
            System.exit(1);
        }
    }
}
