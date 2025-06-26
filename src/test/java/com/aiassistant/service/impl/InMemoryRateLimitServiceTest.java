package com.aiassistant.service.impl;

import com.aiassistant.config.ApplicationProperties;
import com.aiassistant.exception.RateLimitExceededException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InMemoryRateLimitServiceTest {

    @Mock
    private ApplicationProperties properties;

    @Mock
    private ApplicationProperties.BotBehaviorProperties botBehavior;

    private InMemoryRateLimitService rateLimitService;

    @BeforeEach
    void setUp() {
        when(properties.botBehavior()).thenReturn(botBehavior);
        when(botBehavior.rateLimitPerMinute()).thenReturn(5); // 5 запросов в минуту для тестов
        
        rateLimitService = new InMemoryRateLimitService(properties);
    }

    @Test
    void testCanMakeRequest_NewUser() {
        // Arrange
        Long userId = 123L;

        // Act
        boolean canMake = rateLimitService.canMakeRequest(userId);

        // Assert
        assertTrue(canMake);
    }

    @Test
    void testRecordRequest_WithinLimit() {
        // Arrange
        Long userId = 123L;

        // Act & Assert
        assertDoesNotThrow(() -> {
            rateLimitService.recordRequest(userId);
            rateLimitService.recordRequest(userId);
            rateLimitService.recordRequest(userId);
        });
    }

    @Test
    void testRecordRequest_ExceedsLimit() {
        // Arrange
        Long userId = 123L;

        // Act - записываем максимальное количество запросов
        assertDoesNotThrow(() -> {
            for (int i = 0; i < 5; i++) {
                rateLimitService.recordRequest(userId);
            }
        });

        // Assert - следующий запрос должен вызвать исключение
        assertThrows(RateLimitExceededException.class, () -> rateLimitService.recordRequest(userId));
    }

    @Test
    void testGetRemainingRequests_NewUser() {
        // Arrange
        Long userId = 123L;

        // Act
        int remaining = rateLimitService.getRemainingRequests(userId);

        // Assert
        assertEquals(5, remaining);
    }

    @Test
    void testGetRemainingRequests_AfterSomeRequests() {
        // Arrange
        Long userId = 123L;

        // Act
        rateLimitService.recordRequest(userId);
        rateLimitService.recordRequest(userId);
        int remaining = rateLimitService.getRemainingRequests(userId);

        // Assert
        assertEquals(3, remaining);
    }

    @Test
    void testGetTimeUntilReset() {
        // Arrange
        Long userId = 123L;

        // Act
        rateLimitService.recordRequest(userId);
        long timeUntilReset = rateLimitService.getTimeUntilReset(userId);

        // Assert
        assertTrue(timeUntilReset > 0);
        assertTrue(timeUntilReset <= 60); // не больше минуты
    }

    @Test
    void testResetRateLimit() {
        // Arrange
        Long userId = 123L;

        // Act - заполняем лимит
        for (int i = 0; i < 5; i++) {
            rateLimitService.recordRequest(userId);
        }
        
        // Проверяем, что лимит исчерпан
        assertThrows(RateLimitExceededException.class, () -> rateLimitService.recordRequest(userId));

        // Сбрасываем лимит
        rateLimitService.resetRateLimit(userId);

        // Assert - после сброса должны снова мочь делать запросы
        assertDoesNotThrow(() -> rateLimitService.recordRequest(userId));
        
        assertEquals(4, rateLimitService.getRemainingRequests(userId));
    }

    @Test
    void testCanMakeRequest_AfterLimit() {
        // Arrange
        Long userId = 123L;

        // Act - заполняем лимит
        for (int i = 0; i < 5; i++) {
            rateLimitService.recordRequest(userId);
        }

        // Assert
        assertFalse(rateLimitService.canMakeRequest(userId));
    }

    @Test
    void testMultipleUsers_IndependentLimits() {
        // Arrange
        Long user1 = 123L;
        Long user2 = 456L;

        // Act - заполняем лимит для первого пользователя
        for (int i = 0; i < 5; i++) {
            rateLimitService.recordRequest(user1);
        }

        // Assert - первый пользователь не может делать запросы
        assertFalse(rateLimitService.canMakeRequest(user1));
        
        // Но второй пользователь может
        assertTrue(rateLimitService.canMakeRequest(user2));
        assertDoesNotThrow(() -> rateLimitService.recordRequest(user2));
    }

    @Test
    void testRateLimitException_ContainsUserId() {
        // Arrange
        Long userId = 789L;

        // Act - заполняем лимит
        for (int i = 0; i < 5; i++) {
            rateLimitService.recordRequest(userId);
        }

        // Assert
        RateLimitExceededException exception = assertThrows(RateLimitExceededException.class, () -> rateLimitService.recordRequest(userId));

        assertTrue(exception.getMessage().contains("Rate limit exceeded"));
    }
}
