package com.chatgenius.service;

import com.chatgenius.dto.request.CreateUserRequest;
import com.chatgenius.exception.ResourceNotFoundException;
import com.chatgenius.exception.ValidationException;
import com.chatgenius.model.User;
import com.chatgenius.model.enums.UserStatus;
import com.chatgenius.repository.UserRepository;
import com.chatgenius.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        testUser = new User();
        testUser.setId(userId);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("hashedPassword");
        testUser.setStatus(UserStatus.OFFLINE);
        testUser.setCreatedAt(ZonedDateTime.now());
    }

    @Test
    void createUser_Success() {
        // Arrange
        CreateUserRequest request = CreateUserRequest.builder()
            .username("testuser")
            .email("test@example.com")
            .password("password")
            .build();

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(UUID.randomUUID());
            return savedUser;
        });

        // Act
        User createdUser = userService.createUser(request);

        // Assert
        assertNotNull(createdUser);
        assertEquals("testuser", createdUser.getUsername());
        assertEquals(UserStatus.OFFLINE, createdUser.getStatus());
        assertNotNull(createdUser.getCreatedAt());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_UsernameExists_ThrowsException() {
        // Arrange
        User existingUser = new User();
        existingUser.setUsername("testuser");
        existingUser.setEmail("test@example.com");
        existingUser.setStatus(UserStatus.ONLINE);
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(existingUser));

        CreateUserRequest request = CreateUserRequest.builder()
            .username("testuser")
            .email("test2@example.com")
            .password("password")
            .build();

        // Act & Assert
        assertThrows(ValidationException.class, () -> userService.createUser(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUser_Success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        User user = userService.getUser(userId);
        assertNotNull(user);
        assertEquals(userId, user.getId());
        assertEquals("testuser", user.getUsername());
    }

    @Test
    void getUser_NotFound_ThrowsException() {
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUser(UUID.randomUUID()));
    }

    @Test
    void getAllUsers_Success() {
        User user1 = new User();
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");
        user1.setStatus(UserStatus.ONLINE);

        User user2 = new User();
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setStatus(UserStatus.OFFLINE);

        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));

        List<User> users = userService.getAllUsers();
        assertEquals(2, users.size());
        assertEquals("user1", users.get(0).getUsername());
        assertEquals("user2", users.get(1).getUsername());
    }

    @Test
    void findByUsername_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        User user = userService.findByUsername("testuser");
        assertNotNull(user);
        assertEquals("testuser", user.getUsername());
    }
} 