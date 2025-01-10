package com.chatgenius.util;

import com.chatgenius.model.User;
import com.chatgenius.model.enums.UserStatus;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import com.chatgenius.repository.TokenBlacklistRepository;

import java.time.ZonedDateTime;
import java.util.Set;
import java.util.UUID;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    @Mock
    private TokenBlacklistRepository tokenBlacklistRepository;

    private JwtUtil jwtUtil;
    private User testUser;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(tokenBlacklistRepository);
        ReflectionTestUtils.setField(jwtUtil, "secret", "test-jwt-secret-key-for-testing-purposes-only");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 3600000L); // 1 hour
        ReflectionTestUtils.setField(jwtUtil, "refreshExpiration", 86400000L); // 24 hours

        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setStatus(UserStatus.ONLINE);
        testUser.setCreatedAt(ZonedDateTime.now());
        testUser.setRoles(Set.of("ROLE_USER"));
    }

    @Test
    void generateToken_ShouldCreateValidToken() {
        String token = jwtUtil.generateToken(testUser);
        
        assertNotNull(token);
        assertTrue(token.length() > 0);
        assertTrue(jwtUtil.validateToken(token, testUser));
    }

    @Test
    void generateRefreshToken_ShouldCreateValidToken() {
        String refreshToken = jwtUtil.generateRefreshToken(testUser);
        
        assertNotNull(refreshToken);
        assertTrue(refreshToken.length() > 0);
        assertTrue(jwtUtil.validateToken(refreshToken, testUser));
    }

    @Test
    void extractUsername_ShouldReturnCorrectUsername() {
        String token = jwtUtil.generateToken(testUser);
        String username = jwtUtil.extractUsername(token);
        
        assertEquals(testUser.getUsername(), username);
    }

    @Test
    void validateToken_ShouldReturnFalseForInvalidUser() {
        String token = jwtUtil.generateToken(testUser);
        
        User anotherUser = new User();
        anotherUser.setUsername("anotheruser");
        
        assertFalse(jwtUtil.validateToken(token, anotherUser));
    }

    @Test
    void validateToken_ShouldReturnFalseForExpiredToken() {
        // Create a token that expires immediately
        ReflectionTestUtils.setField(jwtUtil, "expiration", 1L);
        String token = jwtUtil.generateToken(testUser);
        
        // Force token validation after expiration
        ReflectionTestUtils.setField(jwtUtil, "expiration", -1000L);
        
        assertThrows(ExpiredJwtException.class, () -> jwtUtil.validateToken(token, testUser));
    }

    @Test
    void extractClaims_ShouldContainUserInfo() {
        String token = jwtUtil.generateToken(testUser);
        
        String username = jwtUtil.extractClaim(token, Claims::getSubject);
        List<String> roles = jwtUtil.extractClaim(token, claims -> 
            ((List<?>) claims.get("roles")).stream()
                .map(Object::toString)
                .collect(Collectors.toList())
        );
        
        assertEquals("testuser", username);
        assertTrue(roles.contains("ROLE_USER"));
    }
} 