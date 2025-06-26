package com.aiassistant.bot;

import com.aiassistant.config.ApplicationProperties;
import com.aiassistant.exception.AiServiceException;
import com.aiassistant.exception.RateLimitExceededException;
import com.aiassistant.model.TelegramUser;
import com.aiassistant.service.AiService;
import com.aiassistant.service.RateLimitService;
import com.aiassistant.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * Main Telegram bot class.
 * Follows Single Responsibility Principle - handles only Telegram bot logic.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiAssistantBot extends TelegramLongPollingBot {
    
    private final ApplicationProperties properties;
    private final AiService aiService;
    private final UserService userService;
    private final RateLimitService rateLimitService;
    
    @Override
    public String getBotUsername() {
        return properties.telegramBotUsername();
    }
    
    @Override
    public String getBotToken() {
        return properties.telegramBotToken();
    }
    
    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }
        
        Message message = update.getMessage();
        Long userId = message.getFrom().getId();
        String messageText = message.getText();
        
        try {
            // Get or create user
            TelegramUser user = userService.getOrCreateUser(message.getFrom());
            
            // Check if user is blocked
            if (userService.isUserBlocked(userId)) {
                log.warn("Blocked user {} tried to send message", userId);
                return;
            }
            
            // Update user message count
            userService.updateUser(user.incrementMessageCount());
            
            // Handle commands
            if (messageText.startsWith("/")) {
                handleCommand(message, messageText);
                return;
            }
            
            // Check rate limit
            try {
                rateLimitService.recordRequest(userId);
            } catch (RateLimitExceededException e) {
                sendMessage(message.getChatId(), 
                    "⏰ Вы отправляете сообщения слишком часто. Попробуйте через " + 
                    rateLimitService.getTimeUntilReset(userId) + " секунд.");
                return;
            }
            
            // Validate message length
            if (messageText.length() > properties.botBehavior().maxMessageLength()) {
                sendMessage(message.getChatId(), 
                    "📝 Сообщение слишком длинное. Максимальная длина: " + 
                    properties.botBehavior().maxMessageLength() + " символов.");
                return;
            }
            
            // Send typing indicator and process AI request
            sendTypingAction(message.getChatId());
            processAiRequest(message, messageText);
            
        } catch (Exception e) {
            log.error("Error processing message from user {}: {}", userId, e.getMessage(), e);
            sendMessage(message.getChatId(), properties.botBehavior().defaultErrorMessage());
        }
    }
    
    private void handleCommand(Message message, String command) {
        Long chatId = message.getChatId();
        
        switch (command.toLowerCase()) {
            case "/start" -> {
                sendMessage(chatId, properties.botBehavior().welcomeMessage());
                log.info("User {} started the bot", message.getFrom().getId());
            }
            case "/help" -> {
                sendMessage(chatId, properties.botBehavior().helpMessage());
            }
            case "/status" -> {
                handleStatusCommand(message);
            }
            default -> {
                sendMessage(chatId, "❓ Неизвестная команда. Используйте /help для получения справки.");
            }
        }
    }
    
    private void handleStatusCommand(Message message) {
        Long userId = message.getFrom().getId();
        Long chatId = message.getChatId();
        
        TelegramUser user = userService.findUser(userId).orElse(null);
        if (user == null) {
            sendMessage(chatId, "❌ Пользователь не найден.");
            return;
        }
        
        int remainingRequests = rateLimitService.getRemainingRequests(userId);
        long timeUntilReset = rateLimitService.getTimeUntilReset(userId);
        boolean aiAvailable = aiService.isAvailable();
        
        String status = String.format("""
            📊 Статус бота
            
            👤 Пользователь: %s
            📝 Сообщений отправлено: %d
            ⏱️ Оставшихся запросов: %d
            🔄 Сброс лимита через: %d сек
            🤖 AI Провайдер: %s
            ✅ AI Доступен: %s
            """,
            user.getDisplayName(),
            user.getMessageCount(),
            remainingRequests,
            timeUntilReset,
            aiService.getProviderName(),
            aiAvailable ? "Да" : "Нет"
        );
        
        sendMessage(chatId, status);
    }
    
    private void processAiRequest(Message message, String messageText) {
        Long chatId = message.getChatId();
        
        aiService.generateResponse(messageText)
            .thenAccept(response -> {
                sendMessage(chatId, response);
                log.debug("Sent AI response to user {}", message.getFrom().getId());
            })
            .exceptionally(throwable -> {
                log.error("Error generating AI response", throwable);
                
                String errorMessage;
                if (throwable.getCause() instanceof AiServiceException aiException) {
                    errorMessage = "🤖 AI сервис временно недоступен: " + aiException.getMessage();
                } else {
                    errorMessage = properties.botBehavior().defaultErrorMessage();
                }
                
                sendMessage(chatId, errorMessage);
                return null;
            });
    }
    
    private void sendMessage(Long chatId, String text) {
        try {
            SendMessage message = SendMessage.builder()
                    .chatId(chatId.toString())
                    .text(text)
                    .build();
            
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error sending message to chat {}: {}", chatId, e.getMessage(), e);
        }
    }
    
    private void sendTypingAction(Long chatId) {
        try {
            org.telegram.telegrambots.meta.api.methods.send.SendChatAction chatAction = 
                org.telegram.telegrambots.meta.api.methods.send.SendChatAction.builder()
                    .chatId(chatId.toString())
                    .action("typing")
                    .build();
            execute(chatAction);
        } catch (TelegramApiException e) {
            log.debug("Error sending typing action to chat {}: {}", chatId, e.getMessage());
        }
    }
}
