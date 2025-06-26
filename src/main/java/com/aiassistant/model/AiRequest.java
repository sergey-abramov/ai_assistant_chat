package com.aiassistant.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

/**
 * Request model for AI API calls.
 * Uses Lombok for reducing boilerplate code.
 */
@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AiRequest {
    
    @JsonProperty("model")
    private final String model;
    
    @JsonProperty("messages")
    private final List<Message> messages;
    
    @JsonProperty("max_tokens")
    private final Integer maxTokens;
    
    @JsonProperty("temperature")
    private final Double temperature;
    
    @JsonProperty("stream")
    @Builder.Default
    private final Boolean stream = false;
    
    /**
     * Message in conversation
     */
    @Data
    @Builder
    @Jacksonized
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Message {
        
        @JsonProperty("role")
        private final String role;
        
        @JsonProperty("content")
        private final String content;
        
        public static Message user(String content) {
            return Message.builder()
                    .role("user")
                    .content(content)
                    .build();
        }
        
        public static Message assistant(String content) {
            return Message.builder()
                    .role("assistant")
                    .content(content)
                    .build();
        }
        
        public static Message system(String content) {
            return Message.builder()
                    .role("system")
                    .content(content)
                    .build();
        }
    }
}
