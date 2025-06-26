package com.aiassistant.bot;

import com.aiassistant.config.ApplicationProperties;
import com.aiassistant.model.TelegramUser;
import com.aiassistant.service.AiService;
import com.aiassistant.service.RateLimitService;
import com.aiassistant.service.UserService;
import com.aiassistant.exception.RateLimitExceededException;
import com.aiassistant.exception.AiServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiAssistantBotTest {

    @Mock
    private ApplicationProperties properties;

    @Mock
    private ApplicationProperties.BotBehaviorProperties botBehavior;

    @Mock
    private AiService aiService;

    @Mock
    private UserService userService;

    @Mock
    private RateLimitService rateLimitService;

    @Mock
    private Update update;

    @Mock
    private Message message;

    @Mock
    private User telegramUser;

    private AiAssistantBot bot;

    @BeforeEach
    void setUp() throws TelegramApiException {
        when(properties.telegramBotUsername()).thenReturn("TestBot");
        when(properties.telegramBotToken()).thenReturn("test-token");
        when(properties.botBehavior()).thenReturn(botBehavior);
        
        bot = spy(new AiAssistantBot(properties, aiService, userService, rateLimitService));
        

        doReturn(null).when(bot).execute(any(org.telegram.telegrambots.meta.api.methods.send.SendMessage.class));
        doReturn(null).when(bot).execute(any(org.telegram.telegrambots.meta.api.methods.send.SendChatAction.class));
    }

    @Test
    void testOnUpdateReceived_StartCommand() throws Exception {
        // Arrange
        setupBasicMessage("/start");
        TelegramUser mockUser = createMockUser();
        
        when(userService.getOrCreateUser(telegramUser)).thenReturn(mockUser);
        when(userService.isUserBlocked(123L)).thenReturn(false);
        when(botBehavior.welcomeMessage()).thenReturn("Добро пожаловать!");

        // Act
        bot.onUpdateReceived(update);

        // Assert
        verify(userService).getOrCreateUser(telegramUser);
        verify(userService).isUserBlocked(123L);
        verify(userService).updateUser(any(TelegramUser.class));
        // Проверяем, что бот отправил приветственное сообщение
        verify(bot).execute(any(org.telegram.telegrambots.meta.api.methods.send.SendMessage.class));
    }

    @Test
    void testOnUpdateReceived_HelpCommand() throws Exception {
        // Arrange
        setupBasicMessage("/help");
        TelegramUser mockUser = createMockUser();
        
        when(userService.getOrCreateUser(telegramUser)).thenReturn(mockUser);
        when(userService.isUserBlocked(123L)).thenReturn(false);
        when(botBehavior.helpMessage()).thenReturn("Справка по командам");

        // Act
        bot.onUpdateReceived(update);

        // Assert
        verify(userService).getOrCreateUser(telegramUser);
        verify(bot).execute(any(org.telegram.telegrambots.meta.api.methods.send.SendMessage.class));
    }

    @Test
    void testOnUpdateReceived_StatusCommand() throws TelegramApiException {
        // Arrange
        setupBasicMessage("/status");
        TelegramUser mockUser = createMockUser();
        
        when(userService.getOrCreateUser(telegramUser)).thenReturn(mockUser);
        when(userService.findUser(123L)).thenReturn(Optional.of(mockUser));
        when(userService.isUserBlocked(123L)).thenReturn(false);
        when(rateLimitService.getRemainingRequests(123L)).thenReturn(5);
        when(rateLimitService.getTimeUntilReset(123L)).thenReturn(30L);
        when(aiService.isAvailable()).thenReturn(true);
        when(aiService.getProviderName()).thenReturn("TestAI");

        // Act
        bot.onUpdateReceived(update);

        // Assert
        verify(userService).findUser(123L);
        verify(rateLimitService).getRemainingRequests(123L);
        verify(rateLimitService).getTimeUntilReset(123L);
        verify(aiService).isAvailable();
        verify(aiService).getProviderName();
        verify(bot).execute(any(org.telegram.telegrambots.meta.api.methods.send.SendMessage.class));
    }

    @Test
    void testOnUpdateReceived_BlockedUser() throws TelegramApiException {
        // Arrange
        setupBasicMessage("Hello");
        TelegramUser mockUser = createMockUser();
        
        when(userService.getOrCreateUser(telegramUser)).thenReturn(mockUser);
        when(userService.isUserBlocked(123L)).thenReturn(true);

        // Act
        bot.onUpdateReceived(update);

        // Assert
        verify(userService).isUserBlocked(123L);
        // Проверяем, что сообщение не было отправлено
        verify(bot, never()).execute(any(org.telegram.telegrambots.meta.api.methods.send.SendMessage.class));
        verify(rateLimitService, never()).recordRequest(anyLong());
    }

    @Test
    void testOnUpdateReceived_RateLimitExceeded() throws RateLimitExceededException, TelegramApiException {
        // Arrange
        setupBasicMessage("Hello");
        TelegramUser mockUser = createMockUser();
        
        when(userService.getOrCreateUser(telegramUser)).thenReturn(mockUser);
        when(userService.isUserBlocked(123L)).thenReturn(false);
        when(botBehavior.maxMessageLength()).thenReturn(1000);
        
        doThrow(new RateLimitExceededException("Rate limit exceeded"))
            .when(rateLimitService).recordRequest(123L);
        when(rateLimitService.getTimeUntilReset(123L)).thenReturn(45L);

        // Act
        bot.onUpdateReceived(update);

        // Assert
        verify(rateLimitService).recordRequest(123L);
        verify(rateLimitService).getTimeUntilReset(123L);
        verify(bot).execute(any(org.telegram.telegrambots.meta.api.methods.send.SendMessage.class)); // Должно отправить сообщение об ошибке
        verify(aiService, never()).generateResponse(anyString());
    }

    @Test
    void testOnUpdateReceived_MessageTooLong() throws TelegramApiException {
        // Arrange
        String longMessage = "a".repeat(5000);
        setupBasicMessage(longMessage);
        TelegramUser mockUser = createMockUser();
        
        when(userService.getOrCreateUser(telegramUser)).thenReturn(mockUser);
        when(userService.isUserBlocked(123L)).thenReturn(false);
        when(botBehavior.maxMessageLength()).thenReturn(1000);

        // Act
        bot.onUpdateReceived(update);

        // Assert
        verify(bot).execute(any(org.telegram.telegrambots.meta.api.methods.send.SendMessage.class)); // Должно отправить сообщение об ошибке
        verify(aiService, never()).generateResponse(anyString());
    }

    @Test
    void testOnUpdateReceived_SuccessfulAiResponse() throws RateLimitExceededException, TelegramApiException {
        // Arrange
        setupBasicMessage("What is the weather?");
        TelegramUser mockUser = createMockUser();
        
        when(userService.getOrCreateUser(telegramUser)).thenReturn(mockUser);
        when(userService.isUserBlocked(123L)).thenReturn(false);
        when(botBehavior.maxMessageLength()).thenReturn(1000);
        doNothing().when(rateLimitService).recordRequest(123L);
        
        CompletableFuture<String> aiResponse = CompletableFuture.completedFuture("I don't have weather data");
        when(aiService.generateResponse("What is the weather?")).thenReturn(aiResponse);

        // Act
        bot.onUpdateReceived(update);

        // Assert
        verify(rateLimitService).recordRequest(123L);
        verify(aiService).generateResponse("What is the weather?");
        // Проверяем, что бот отправляет typing action и ответ
        verify(bot, atLeastOnce()).execute(any(org.telegram.telegrambots.meta.api.methods.send.SendMessage.class));
    }

    @Test
    void testOnUpdateReceived_AiServiceError() throws RateLimitExceededException, TelegramApiException {
        // Arrange
        setupBasicMessage("Hello AI");
        TelegramUser mockUser = createMockUser();
        
        when(userService.getOrCreateUser(telegramUser)).thenReturn(mockUser);
        when(userService.isUserBlocked(123L)).thenReturn(false);
        when(botBehavior.maxMessageLength()).thenReturn(1000);
        when(botBehavior.defaultErrorMessage()).thenReturn("Извините, произошла ошибка");
        doNothing().when(rateLimitService).recordRequest(123L);
        
        CompletableFuture<String> failedResponse = CompletableFuture.failedFuture(
            new AiServiceException("AI_ERROR", "AI service unavailable"));
        when(aiService.generateResponse("Hello AI")).thenReturn(failedResponse);

        // Act
        bot.onUpdateReceived(update);

        // Assert
        verify(aiService).generateResponse("Hello AI");
        verify(bot, atLeastOnce()).execute(any(org.telegram.telegrambots.meta.api.methods.send.SendMessage.class)); // typing action + error message
    }

    @Test
    void testOnUpdateReceived_NoMessage() throws TelegramApiException {
        // Arrange
        when(update.hasMessage()).thenReturn(false);

        // Act
        bot.onUpdateReceived(update);

        // Assert
        verify(userService, never()).getOrCreateUser(any());
        verify(bot, never()).execute(any(org.telegram.telegrambots.meta.api.methods.send.SendMessage.class));
    }

    @Test
    void testOnUpdateReceived_NoText() throws TelegramApiException {
        // Arrange
        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(message.hasText()).thenReturn(false);

        // Act
        bot.onUpdateReceived(update);

        // Assert
        verify(userService, never()).getOrCreateUser(any());
        verify(bot, never()).execute(any(org.telegram.telegrambots.meta.api.methods.send.SendMessage.class));
    }

    private void setupBasicMessage(String text) {
        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(message.hasText()).thenReturn(true);
        when(message.getText()).thenReturn(text);
        when(message.getFrom()).thenReturn(telegramUser);
        when(message.getChatId()).thenReturn(456L);
        
        when(telegramUser.getId()).thenReturn(123L);
        when(telegramUser.getFirstName()).thenReturn("John");
        when(telegramUser.getLastName()).thenReturn("Doe");
        when(telegramUser.getUserName()).thenReturn("johndoe");
    }

    private TelegramUser createMockUser() {
        return TelegramUser.builder()
                .userId(123L)
                .firstName("John")
                .lastName("Doe")
                .username("johndoe")
                .messageCount(0)
                .isBlocked(false)
                .build();
    }
}
