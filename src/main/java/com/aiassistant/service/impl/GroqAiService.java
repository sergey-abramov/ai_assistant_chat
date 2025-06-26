package com.aiassistant.service.impl;

import com.aiassistant.config.ApplicationProperties;
import com.aiassistant.exception.AiServiceException;
import com.aiassistant.model.AiRequest;
import com.aiassistant.model.AiResponse;
import com.aiassistant.service.AiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Groq AI service implementation.
 * Follows Single Responsibility Principle - handles only Groq API calls.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "aiassistant.ai-provider.type", havingValue = "groq")
public class GroqAiService implements AiService {
    
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    private final ApplicationProperties properties;
    private final ObjectMapper objectMapper;
    private final OkHttpClient httpClient;
    
    @Override
    public CompletableFuture<AiResponse> chatCompletion(AiRequest request) throws AiServiceException {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String requestBody = objectMapper.writeValueAsString(request);
                log.debug("Sending request to Groq API: {}", requestBody);
                
                Request httpRequest = new Request.Builder()
                        .url(properties.aiProvider().apiUrl())
                        .addHeader("Authorization", "Bearer " + properties.aiProvider().apiKey())
                        .addHeader("Content-Type", "application/json")
                        .post(RequestBody.create(requestBody, JSON))
                        .build();
                
                try (Response response = httpClient.newCall(httpRequest).execute()) {
                    String responseBody = response.body() != null ? response.body().string() : "";
                    log.debug("Received response from Groq API: {}", responseBody);
                    
                    if (!response.isSuccessful()) {
                        throw new AiServiceException(
                            "GROQ_API_ERROR",
                            "Groq API returned error: " + response.code() + " - " + responseBody
                        );
                    }
                    
                    AiResponse aiResponse = objectMapper.readValue(responseBody, AiResponse.class);
                    
                    if (aiResponse.hasError()) {
                        throw new AiServiceException(
                            "GROQ_RESPONSE_ERROR", 
                            "Groq API returned error: " + aiResponse.getError().getMessage()
                        );
                    }
                    
                    return aiResponse;
                }
                
            } catch (IOException e) {
                log.error("Error calling Groq API", e);
                throw new AiServiceException("GROQ_IO_ERROR", "Failed to call Groq API", e);
            }
        });
    }
    
    @Override
    public CompletableFuture<String> generateResponse(String message) throws AiServiceException {
        AiRequest request = AiRequest.builder()
                .model(properties.aiProvider().model())
                .messages(List.of(AiRequest.Message.user(message)))
                .maxTokens(properties.aiProvider().maxTokens())
                .temperature(properties.aiProvider().temperature())
                .build();
        
        return chatCompletion(request)
                .thenApply(response -> {
                    String content = response.getContent();
                    if (content == null || content.isBlank()) {
                        throw new AiServiceException("GROQ_EMPTY_RESPONSE", "Received empty response from Groq");
                    }
                    return content;
                });
    }
    
    @Override
    public boolean isAvailable() {
        try {
            // Simple health check - try to call API with minimal request
            AiRequest healthCheck = AiRequest.builder()
                    .model(properties.aiProvider().model())
                    .messages(List.of(AiRequest.Message.user("Hello")))
                    .maxTokens(1)
                    .build();
            
            chatCompletion(healthCheck).get();
            return true;
        } catch (Exception e) {
            log.warn("Groq API health check failed", e);
            return false;
        }
    }
    
    @Override
    public String getProviderName() {
        return "Groq";
    }
}
