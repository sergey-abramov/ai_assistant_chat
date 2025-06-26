package com.aiassistant.service;

import com.aiassistant.model.AiRequest;
import com.aiassistant.model.AiResponse;
import com.aiassistant.exception.AiServiceException;

import java.util.concurrent.CompletableFuture;

/**
 * Interface for AI service operations.
 * Follows Interface Segregation Principle - contains only AI-related methods.
 */
public interface AiService {
    
    /**
     * Send a chat completion request to AI service
     * 
     * @param request the AI request
     * @return CompletableFuture with AI response
     * @throws AiServiceException if request fails
     */
    CompletableFuture<AiResponse> chatCompletion(AiRequest request) throws AiServiceException;
    
    /**
     * Generate response for a simple text message
     * 
     * @param message user message
     * @return CompletableFuture with AI response text
     * @throws AiServiceException if request fails
     */
    CompletableFuture<String> generateResponse(String message) throws AiServiceException;
    
    /**
     * Check if the AI service is available
     * 
     * @return true if service is available
     */
    boolean isAvailable();
    
    /**
     * Get the name of the AI provider
     * 
     * @return provider name
     */
    String getProviderName();
}
