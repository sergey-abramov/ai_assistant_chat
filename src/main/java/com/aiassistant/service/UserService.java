package com.aiassistant.service;

import com.aiassistant.model.TelegramUser;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Optional;

/**
 * Interface for user management operations.
 * Follows Interface Segregation Principle - contains only user-related methods.
 */
public interface UserService {
    
    /**
     * Get or create user from Telegram User object
     * 
     * @param telegramUser Telegram user object
     * @return TelegramUser entity
     */
    TelegramUser getOrCreateUser(User telegramUser);
    
    /**
     * Find user by ID
     * 
     * @param userId user ID
     * @return Optional with user if found
     */
    Optional<TelegramUser> findUser(Long userId);
    
    /**
     * Update user information
     * 
     * @param user user to update
     * @return updated user
     */
    TelegramUser updateUser(TelegramUser user);
    
    /**
     * Check if user is blocked
     * 
     * @param userId user ID
     * @return true if user is blocked
     */
    boolean isUserBlocked(Long userId);
    
    /**
     * Block user
     * 
     * @param userId user ID
     */
    void blockUser(Long userId);
    
    /**
     * Unblock user
     * 
     * @param userId user ID
     */
    void unblockUser(Long userId);
}
