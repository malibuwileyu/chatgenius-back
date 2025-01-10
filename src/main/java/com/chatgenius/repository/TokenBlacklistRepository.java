package com.chatgenius.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
public class TokenBlacklistRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String KEY_PREFIX = "blacklist:token:";

    @Autowired
    public TokenBlacklistRepository(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void blacklistToken(String token, long expirationInMillis) {
        String key = KEY_PREFIX + token;
        redisTemplate.opsForValue().set(key, "blacklisted", expirationInMillis, TimeUnit.MILLISECONDS);
    }

    public boolean isTokenBlacklisted(String token) {
        String key = KEY_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void removeFromBlacklist(String token) {
        String key = KEY_PREFIX + token;
        redisTemplate.delete(key);
    }
} 