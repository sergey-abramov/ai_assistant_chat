package com.aiassistant.service.impl;

import com.aiassistant.config.ApplicationProperties;
import com.aiassistant.exception.AiServiceException;
import com.aiassistant.model.AiRequest;
import com.aiassistant.model.AiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroqAiServiceTest {

    @Mock
    private ApplicationProperties properties;

    @Mock
    private ApplicationProperties.AiProviderProperties aiProvider;

    @Mock
    private ObjectMapper objectMapper;

    private MockWebServer mockWebServer;
    private GroqAiService groqAiService;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        OkHttpClient realHttpClient = new OkHttpClient();
        groqAiService = new GroqAiService(properties, objectMapper, realHttpClient);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void testChatCompletion_Success() throws Exception {
        // Arrange
        AiRequest request = AiRequest.builder()
                .model("llama-3.1-8b-instant")
                .messages(List.of(AiRequest.Message.user("Hello")))
                .maxTokens(100)
                .temperature(0.7)
                .build();
        
        String jsonRequest = "{\"model\":\"llama-3.1-8b-instant\"}";
        String jsonResponse = "{\"choices\":[{\"message\":{\"content\":\"Hi there!\"}}]}";
        
        AiResponse expectedResponse = AiResponse.builder()
                .choices(List.of(AiResponse.Choice.builder()
                        .message(AiRequest.Message.assistant("Hi there!"))
                        .build()))
                .build();
        
        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json"));

        when(properties.aiProvider()).thenReturn(aiProvider);
        when(aiProvider.apiUrl()).thenReturn(mockWebServer.url("/v1/chat/completions").toString());
        when(aiProvider.apiKey()).thenReturn("test-api-key");
        when(objectMapper.writeValueAsString(request)).thenReturn(jsonRequest);
        when(objectMapper.readValue(jsonResponse, AiResponse.class)).thenReturn(expectedResponse);

        // Act
        CompletableFuture<AiResponse> futureResponse = groqAiService.chatCompletion(request);
        AiResponse actualResponse = futureResponse.get();

        // Assert
        assertNotNull(actualResponse);
        assertEquals("Hi there!", actualResponse.getContent());
        verify(objectMapper).writeValueAsString(request);
        verify(objectMapper).readValue(jsonResponse, AiResponse.class);
    }

    @Test
    void testChatCompletion_HttpError() throws Exception {
        // Arrange
        AiRequest request = AiRequest.builder()
                .model("llama-3.1-8b-instant")
                .messages(List.of(AiRequest.Message.user("Hello")))
                .maxTokens(100)
                .build();
        
        String jsonRequest = "{\"model\":\"llama-3.1-8b-instant\"}";
        String errorResponse = "{\"error\":{\"message\":\"Rate limit exceeded\"}}";
        
        mockWebServer.enqueue(new MockResponse()
                .setBody(errorResponse)
                .setResponseCode(429));

        when(properties.aiProvider()).thenReturn(aiProvider);
        when(aiProvider.apiUrl()).thenReturn(mockWebServer.url("/v1/chat/completions").toString());
        when(aiProvider.apiKey()).thenReturn("test-api-key");
        when(objectMapper.writeValueAsString(request)).thenReturn(jsonRequest);

        // Act & Assert
        CompletableFuture<AiResponse> futureResponse = groqAiService.chatCompletion(request);
        
        ExecutionException exception = assertThrows(ExecutionException.class, futureResponse::get);

        assertInstanceOf(AiServiceException.class, exception.getCause());
        AiServiceException aiException = (AiServiceException) exception.getCause();
        assertEquals("GROQ_API_ERROR", aiException.getErrorCode());
        assertTrue(aiException.getMessage().contains("429"));
    }

    @Test
    void testGenerateResponse_Success() throws Exception {
        // Arrange
        String userMessage = "What is the weather like?";
        String aiResponseText = "I don't have access to real-time weather data.";
        
        AiResponse mockResponse = AiResponse.builder()
                .choices(List.of(AiResponse.Choice.builder()
                        .message(AiRequest.Message.assistant(aiResponseText))
                        .build()))
                .build();
        
        when(properties.aiProvider()).thenReturn(aiProvider);
        when(aiProvider.model()).thenReturn("llama-3.1-8b-instant");
        when(aiProvider.maxTokens()).thenReturn(1000);
        when(aiProvider.temperature()).thenReturn(0.7);
        when(aiProvider.apiUrl()).thenReturn(mockWebServer.url("/v1/chat/completions").toString());
        when(aiProvider.apiKey()).thenReturn("test-api-key");
        
        String jsonResponse = "{\"choices\":[{\"message\":{\"content\":\"" + aiResponseText + "\"}}]}";
        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json"));
        
        when(objectMapper.writeValueAsString(any(AiRequest.class))).thenReturn("{}");
        when(objectMapper.readValue(jsonResponse, AiResponse.class)).thenReturn(mockResponse);

        // Act
        CompletableFuture<String> futureResponse = groqAiService.generateResponse(userMessage);
        String actualResponse = futureResponse.get();

        // Assert
        assertNotNull(actualResponse);
        assertEquals(aiResponseText, actualResponse);
    }

    @Test
    void testGenerateResponse_EmptyResponse() throws Exception {
        // Arrange
        String userMessage = "Hello";
        
        AiResponse mockResponse = AiResponse.builder()
                .choices(List.of(AiResponse.Choice.builder()
                        .message(AiRequest.Message.assistant(""))
                        .build()))
                .build();
        
        when(properties.aiProvider()).thenReturn(aiProvider);
        when(aiProvider.model()).thenReturn("llama-3.1-8b-instant");
        when(aiProvider.maxTokens()).thenReturn(1000);
        when(aiProvider.temperature()).thenReturn(0.7);
        when(aiProvider.apiUrl()).thenReturn(mockWebServer.url("/v1/chat/completions").toString());
        when(aiProvider.apiKey()).thenReturn("test-api-key");
        
        String jsonResponse = "{\"choices\":[{\"message\":{\"content\":\"\"}}]}";
        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json"));
        
        when(objectMapper.writeValueAsString(any(AiRequest.class))).thenReturn("{}");
        when(objectMapper.readValue(jsonResponse, AiResponse.class)).thenReturn(mockResponse);

        // Act & Assert
        CompletableFuture<String> futureResponse = groqAiService.generateResponse(userMessage);
        
        ExecutionException exception = assertThrows(ExecutionException.class, futureResponse::get);

        assertInstanceOf(AiServiceException.class, exception.getCause());
        AiServiceException aiException = (AiServiceException) exception.getCause();
        assertEquals("GROQ_EMPTY_RESPONSE", aiException.getErrorCode());
    }

    @Test
    void testGetProviderName() {
        // Act
        String providerName = groqAiService.getProviderName();
        
        // Assert
        assertEquals("Groq", providerName);
    }
}
