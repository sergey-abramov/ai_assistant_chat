package com.aiassistant.config;

import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * HTTP client configuration.
 * Follows Single Responsibility Principle - configures only HTTP client.
 */
@Configuration
public class HttpClientConfig {
    
    @Bean
    public OkHttpClient okHttpClient(ApplicationProperties properties) {
        long timeoutMs = properties.aiProvider().timeoutMs();
        return new OkHttpClient.Builder()
                .connectTimeout(Duration.ofMillis(timeoutMs))
                .readTimeout(Duration.ofMillis(timeoutMs))
                .writeTimeout(Duration.ofMillis(timeoutMs))
                .retryOnConnectionFailure(true)
                .build();
    }
}
