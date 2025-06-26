package com.aiassistant.model;

import lombok.Builder;
import lombok.Data;
import lombok.With;

import java.time.LocalDateTime;

/**
 * Telegram user model for tracking user interactions.
 * Uses Lombok for reducing boilerplate code.
 */
@Data
@Builder
@With
public class TelegramUser {
    
    private final Long userId;
    private final String username;
    private final String firstName;
    private final String lastName;
    private final String languageCode;
    
    @Builder.Default
    private final LocalDateTime firstSeenAt = LocalDateTime.now();
    
    @Builder.Default
    private final LocalDateTime lastSeenAt = LocalDateTime.now();
    
    @Builder.Default
    private final Integer messageCount = 0;
    
    @Builder.Default
    private final Boolean isBlocked = false;
    
    /**
     * Get full name of the user
     */
    public String getFullName() {
        StringBuilder name = new StringBuilder();
        if (firstName != null && !firstName.isBlank()) {
            name.append(firstName);
        }
        if (lastName != null && !lastName.isBlank()) {
            if (!name.isEmpty()) {
                name.append(" ");
            }
            name.append(lastName);
        }
        return name.isEmpty() ? username : name.toString();
    }
    
    /**
     * Get display name for the user
     */
    public String getDisplayName() {
        String fullName = getFullName();
        return fullName != null && !fullName.isBlank() ? fullName : "User " + userId;
    }
    
    /**
     * Update last seen timestamp
     */
    public TelegramUser updateLastSeen() {
        return this.withLastSeenAt(LocalDateTime.now());
    }
    
    /**
     * Increment message count
     */
    public TelegramUser incrementMessageCount() {
        return this.withMessageCount(messageCount + 1).updateLastSeen();
    }
}
