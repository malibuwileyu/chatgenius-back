package com.chatgenius.service.impl;

import com.chatgenius.service.PresenceService;
import com.chatgenius.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PresenceServiceImpl implements PresenceService {
    private static final Logger logger = LoggerFactory.getLogger(PresenceServiceImpl.class);
    private static final String PRESENCE_KEY = "presence:";
    private static final Duration PRESENCE_TIMEOUT = Duration.ofMinutes(5);

    private final RedisTemplate<String, String> redisTemplate;
    private final UserService userService;

    @Override
    public void updateUserPresence(UUID userId, String status) {
        logger.debug("Updating presence for user {} to {}", userId, status);
        try {
            String key = PRESENCE_KEY + userId;
            redisTemplate.opsForValue().set(key, status, PRESENCE_TIMEOUT);
            logger.info("Successfully updated presence for user {} to {}", userId, status);
        } catch (Exception e) {
            logger.error("Failed to update presence for user {}", userId, e);
            throw new RuntimeException("Failed to update user presence", e);
        }
    }

    @Override
    public List<UUID> getOnlineUsers() {
        logger.debug("Fetching online users");
        try {
            Set<String> keys = redisTemplate.keys(PRESENCE_KEY + "*");
            if (keys == null) {
                return new ArrayList<>();
            }

            return keys.stream()
                .map(key -> key.substring(PRESENCE_KEY.length()))
                .map(UUID::fromString)
                .filter(userId -> "online".equals(getUserStatus(userId)))
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Failed to fetch online users", e);
            throw new RuntimeException("Failed to get online users", e);
        }
    }

    @Override
    public void handleDisconnect(UUID userId) {
        logger.debug("Handling disconnect for user {}", userId);
        try {
            updateUserPresence(userId, "offline");
            logger.info("Successfully handled disconnect for user {}", userId);
        } catch (Exception e) {
            logger.error("Failed to handle disconnect for user {}", userId, e);
            throw new RuntimeException("Failed to handle user disconnect", e);
        }
    }

    @Override
    public String getUserStatus(UUID userId) {
        logger.debug("Fetching status for user {}", userId);
        try {
            String key = PRESENCE_KEY + userId;
            String status = redisTemplate.opsForValue().get(key);
            return status != null ? status : "offline";
        } catch (Exception e) {
            logger.error("Failed to fetch status for user {}", userId, e);
            throw new RuntimeException("Failed to get user status", e);
        }
    }
} 