package com.aiassistant.service.impl;

import com.aiassistant.model.TelegramUser;
import com.aiassistant.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * In-memory implementation of UserService.
 * Follows Single Responsibility Principle - handles only user management.
 * Note: This is a simple implementation. In production, you'd use a database.
 */
@Slf4j
@Service
public class InMemoryUserService implements UserService {
    
    private final ConcurrentMap<Long, TelegramUser> users = new ConcurrentHashMap<>();
    
    @Override
    public TelegramUser getOrCreateUser(User telegramUser) {
        Long userId = telegramUser.getId();
        
        return users.computeIfAbsent(userId, id -> {
            TelegramUser newUser = TelegramUser.builder()
                    .userId(userId)
                    .username(telegramUser.getUserName())
                    .firstName(telegramUser.getFirstName())
                    .lastName(telegramUser.getLastName())
                    .languageCode(telegramUser.getLanguageCode())
                    .build();
            
            log.info("Created new user: {} ({})", newUser.getDisplayName(), userId);
            return newUser;
        });
    }
    
    @Override
    public Optional<TelegramUser> findUser(Long userId) {
        return Optional.ofNullable(users.get(userId));
    }
    
    @Override
    public TelegramUser updateUser(TelegramUser user) {
        users.put(user.getUserId(), user);
        log.debug("Updated user: {} ({})", user.getDisplayName(), user.getUserId());
        return user;
    }
    
    @Override
    public boolean isUserBlocked(Long userId) {
        return findUser(userId)
                .map(TelegramUser::getIsBlocked)
                .orElse(false);
    }
    
    @Override
    public void blockUser(Long userId) {
        findUser(userId).ifPresent(user -> {
            TelegramUser blockedUser = user.withIsBlocked(true);
            updateUser(blockedUser);
            log.warn("Blocked user: {} ({})", user.getDisplayName(), userId);
        });
    }
    
    @Override
    public void unblockUser(Long userId) {
        findUser(userId).ifPresent(user -> {
            TelegramUser unblockedUser = user.withIsBlocked(false);
            updateUser(unblockedUser);
            log.info("Unblocked user: {} ({})", user.getDisplayName(), userId);
        });
    }
}
