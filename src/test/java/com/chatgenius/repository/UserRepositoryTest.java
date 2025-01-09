package com.chatgenius.repository;

import com.chatgenius.model.User;
import com.chatgenius.model.enums.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void createUser_Success() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setStatus(UserStatus.OFFLINE);
        user.setCreatedAt(ZonedDateTime.now());

        User savedUser = userRepository.save(user);

        assertNotNull(savedUser.getId());
        assertEquals("testuser", savedUser.getUsername());
        assertEquals("test@example.com", savedUser.getEmail());
        assertEquals(UserStatus.OFFLINE, savedUser.getStatus());
    }

    @Test
    void findByUsername_Success() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setStatus(UserStatus.OFFLINE);
        user.setCreatedAt(ZonedDateTime.now());
        userRepository.save(user);

        Optional<User> found = userRepository.findByUsername("testuser");
        assertTrue(found.isPresent());
        assertEquals("testuser", found.get().getUsername());
    }

    @Test
    void findByEmail_Success() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setStatus(UserStatus.OFFLINE);
        user.setCreatedAt(ZonedDateTime.now());
        userRepository.save(user);

        Optional<User> found = userRepository.findByEmail("test@example.com");
        assertTrue(found.isPresent());
        assertEquals("test@example.com", found.get().getEmail());
    }

    @Test
    void findByStatus_Success() {
        User user1 = new User();
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");
        user1.setPassword("password123");
        user1.setStatus(UserStatus.ONLINE);
        user1.setCreatedAt(ZonedDateTime.now());
        userRepository.save(user1);

        User user2 = new User();
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setPassword("password123");
        user2.setStatus(UserStatus.OFFLINE);
        user2.setCreatedAt(ZonedDateTime.now());
        userRepository.save(user2);

        List<User> onlineUsers = userRepository.findByStatus(UserStatus.ONLINE);
        assertEquals(1, onlineUsers.size());
        assertEquals(UserStatus.ONLINE, onlineUsers.get(0).getStatus());
    }
} 