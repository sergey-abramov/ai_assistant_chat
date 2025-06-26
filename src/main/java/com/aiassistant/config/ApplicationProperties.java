package com.aiassistant.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

/**
 * Configuration properties for the AI Assistant Bot application.
 * Follows the Single Responsibility Principle - handles only configuration.
 */
@ConfigurationProperties(prefix = "aiassistant")
@Validated
public record ApplicationProperties(
    @NotBlank(message = "Telegram bot token is required")
    String telegramBotToken,
    
    @NotBlank(message = "Telegram bot username is required")
    String telegramBotUsername,
    
    AiProviderProperties aiProvider,
    
    BotBehaviorProperties botBehavior
) {
    
    /**
     * AI Provider configuration
     */
    public record AiProviderProperties(
        @NotBlank(message = "AI provider type is required")
        String type, // openai, groq, ollama, huggingface
        
        String apiKey, // Optional for some providers
        
        @NotBlank(message = "API URL is required")
        String apiUrl,
        
        @NotBlank(message = "Model name is required")
        String model,
        
        @Min(value = 1, message = "Max tokens must be at least 1")
        @Max(value = 4096, message = "Max tokens must not exceed 4096")
        Integer maxTokens,
        
        Double temperature,
        
        @Min(value = 1000, message = "Timeout must be at least 1000ms")
        Integer timeoutMs
    ) {
        public AiProviderProperties {
            // Default values
            if (maxTokens == null) maxTokens = 1000;
            if (temperature == null) temperature = 0.7;
            if (timeoutMs == null) timeoutMs = 30000;
        }
    }
    
    /**
     * Bot behavior configuration
     */
    public record BotBehaviorProperties(
        @Min(value = 1, message = "Max message length must be at least 1")
        Integer maxMessageLength,
        
        @Min(value = 1, message = "Rate limit per minute must be at least 1")
        Integer rateLimitPerMinute,
        
        String defaultErrorMessage,
        
        String welcomeMessage,
        
        String helpMessage
    ) {
        public BotBehaviorProperties {
            // Default values
            if (maxMessageLength == null) maxMessageLength = 4000;
            if (rateLimitPerMinute == null) rateLimitPerMinute = 10;
            if (defaultErrorMessage == null) {
                defaultErrorMessage = "Извините, произошла ошибка при обработке вашего запроса. Попробуйте позже.";
            }
            if (welcomeMessage == null) {
                welcomeMessage = "Привет! Я AI-ассистент. Задайте мне любой вопрос, и я постараюсь помочь!";
            }
            if (helpMessage == null) {
                helpMessage = """
                    🤖 AI-Ассистент
                    
                    Команды:
                    /start - Начать работу с ботом
                    /help - Показать эту справку
                    
                    Просто отправьте мне сообщение, и я отвечу на ваш вопрос!
                    """;
            }
        }
    }
}
