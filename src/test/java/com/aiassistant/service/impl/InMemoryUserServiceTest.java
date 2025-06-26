package com.aiassistant.service.impl;

import com.aiassistant.model.TelegramUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.User;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InMemoryUserServiceTest {

    private InMemoryUserService userService;

    @BeforeEach
    void setUp() {
        userService = new InMemoryUserService();
    }

    @Test
    void testGetOrCreateUser_NewUser() {
        // Arrange
        User telegramUser = mock(User.class);
        when(telegramUser.getId()).thenReturn(123L);
        when(telegramUser.getFirstName()).thenReturn("John");
        when(telegramUser.getLastName()).thenReturn("Doe");
        when(telegramUser.getUserName()).thenReturn("johndoe");

        // Act
        TelegramUser result = userService.getOrCreateUser(telegramUser);

        // Assert
        assertNotNull(result);
        assertEquals(123L, result.getUserId());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("johndoe", result.getUsername());
        assertEquals("John Doe", result.getDisplayName());
        assertEquals(0, result.getMessageCount());
        assertFalse(result.getIsBlocked());
        assertNotNull(result.getFirstSeenAt());
        assertNotNull(result.getLastSeenAt());
    }

    @Test
    void testGetOrCreateUser_ExistingUser() {
        // Arrange
        User telegramUser = mock(User.class);
        when(telegramUser.getId()).thenReturn(123L);
        when(telegramUser.getFirstName()).thenReturn("John");
        when(telegramUser.getLastName()).thenReturn("Doe");
        when(telegramUser.getUserName()).thenReturn("johndoe");

        // Создаем пользователя первый раз
        TelegramUser firstCall = userService.getOrCreateUser(telegramUser);
        LocalDateTime firstCreatedAt = firstCall.getFirstSeenAt();

        // Act - вызываем второй раз
        TelegramUser secondCall = userService.getOrCreateUser(telegramUser);

        // Assert
        assertSame(firstCall, secondCall); // Должен быть тот же объект
        assertEquals(firstCreatedAt, secondCall.getFirstSeenAt()); // Время создания не изменилось
    }

    @Test
    void testGetOrCreateUser_NullLastName() {
        // Arrange
        User telegramUser = mock(User.class);
        when(telegramUser.getId()).thenReturn(123L);
        when(telegramUser.getFirstName()).thenReturn("John");
        when(telegramUser.getLastName()).thenReturn(null);
        when(telegramUser.getUserName()).thenReturn("johndoe");

        // Act
        TelegramUser result = userService.getOrCreateUser(telegramUser);

        // Assert
        assertEquals("John", result.getDisplayName());
        assertNull(result.getLastName());
    }

    @Test
    void testGetOrCreateUser_NullUsername() {
        // Arrange
        User telegramUser = mock(User.class);
        when(telegramUser.getId()).thenReturn(123L);
        when(telegramUser.getFirstName()).thenReturn("John");
        when(telegramUser.getLastName()).thenReturn("Doe");
        when(telegramUser.getUserName()).thenReturn(null);

        // Act
        TelegramUser result = userService.getOrCreateUser(telegramUser);

        // Assert
        assertNull(result.getUsername());
        assertEquals("John Doe", result.getDisplayName());
    }

    @Test
    void testFindUser_ExistingUser() {
        // Arrange
        User telegramUser = mock(User.class);
        when(telegramUser.getId()).thenReturn(123L);
        when(telegramUser.getFirstName()).thenReturn("John");
        when(telegramUser.getLastName()).thenReturn("Doe");
        when(telegramUser.getUserName()).thenReturn("johndoe");

        userService.getOrCreateUser(telegramUser); // Создаем пользователя

        // Act
        Optional<TelegramUser> result = userService.findUser(123L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(123L, result.get().getUserId());
    }

    @Test
    void testFindUser_NonExistingUser() {
        // Act
        Optional<TelegramUser> result = userService.findUser(999L);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void testUpdateUser() {
        // Arrange
        User telegramUser = mock(User.class);
        when(telegramUser.getId()).thenReturn(123L);
        when(telegramUser.getFirstName()).thenReturn("John");
        when(telegramUser.getLastName()).thenReturn("Doe");
        when(telegramUser.getUserName()).thenReturn("johndoe");

        TelegramUser originalUser = userService.getOrCreateUser(telegramUser);
        TelegramUser updatedUser = originalUser.incrementMessageCount();

        // Act
        TelegramUser result = userService.updateUser(updatedUser);

        // Assert
        assertEquals(1, result.getMessageCount());
        
        // Проверяем, что изменения сохранились
        Optional<TelegramUser> found = userService.findUser(123L);
        assertTrue(found.isPresent());
        assertEquals(1, found.get().getMessageCount());
    }

    @Test
    void testBlockUser() {
        // Arrange
        User telegramUser = mock(User.class);
        when(telegramUser.getId()).thenReturn(123L);
        when(telegramUser.getFirstName()).thenReturn("John");

        userService.getOrCreateUser(telegramUser);

        // Act
        userService.blockUser(123L);

        // Assert
        assertTrue(userService.isUserBlocked(123L));
        
        Optional<TelegramUser> user = userService.findUser(123L);
        assertTrue(user.isPresent());
        assertTrue(user.get().getIsBlocked());
    }

    @Test
    void testUnblockUser() {
        // Arrange
        User telegramUser = mock(User.class);
        when(telegramUser.getId()).thenReturn(123L);
        when(telegramUser.getFirstName()).thenReturn("John");

        userService.getOrCreateUser(telegramUser);
        userService.blockUser(123L);

        // Act
        userService.unblockUser(123L);

        // Assert
        assertFalse(userService.isUserBlocked(123L));
        
        Optional<TelegramUser> user = userService.findUser(123L);
        assertTrue(user.isPresent());
        assertFalse(user.get().getIsBlocked());
    }

    @Test
    void testIsUserBlocked_NonExistingUser() {
        // Act & Assert
        assertFalse(userService.isUserBlocked(999L));
    }

    @Test
    void testBlockUser_NonExistingUser() {
        // Act & Assert
        assertDoesNotThrow(() -> userService.blockUser(999L));
        assertFalse(userService.isUserBlocked(999L));
    }

    @Test
    void testUnblockUser_NonExistingUser() {
        // Act & Assert
        assertDoesNotThrow(() -> userService.unblockUser(999L));
        assertFalse(userService.isUserBlocked(999L));
    }

    @Test
    void testUserDisplayName_OnlyFirstName() {
        // Arrange
        User telegramUser = mock(User.class);
        when(telegramUser.getId()).thenReturn(123L);
        when(telegramUser.getFirstName()).thenReturn("John");
        when(telegramUser.getLastName()).thenReturn("");
        when(telegramUser.getUserName()).thenReturn("johndoe");

        // Act
        TelegramUser result = userService.getOrCreateUser(telegramUser);

        // Assert
        assertEquals("John", result.getDisplayName());
    }

    @Test
    void testUserDisplayName_BothNames() {
        // Arrange
        User telegramUser = mock(User.class);
        when(telegramUser.getId()).thenReturn(123L);
        when(telegramUser.getFirstName()).thenReturn("John");
        when(telegramUser.getLastName()).thenReturn("Doe");
        when(telegramUser.getUserName()).thenReturn("johndoe");

        // Act
        TelegramUser result = userService.getOrCreateUser(telegramUser);

        // Assert
        assertEquals("John Doe", result.getDisplayName());
    }
}
