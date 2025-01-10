package com.chatgenius.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Service
public class TokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String BLACKLIST_PREFIX = "blacklist:";

    public TokenBlacklistService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        System.out.println("TokenBlacklistService initialized with Redis template");
    }

    public void blacklistToken(String token, long expirationTimeInMillis) {
        String key = BLACKLIST_PREFIX + token;
        System.out.println("\n=== Blacklisting Token ===");
        System.out.println("Key: " + key);
        System.out.println("Expiration time: " + expirationTimeInMillis + "ms");
        
        try {
            redisTemplate.opsForValue().set(key, "blacklisted", expirationTimeInMillis, TimeUnit.MILLISECONDS);
            System.out.println("Token successfully blacklisted");
            System.out.println("=== Blacklisting Complete ===\n");
        } catch (Exception e) {
            System.out.println("Failed to blacklist token: " + e.getMessage());
            System.out.println("=== Blacklisting Failed ===\n");
            throw e;
        }
    }

    public boolean isTokenBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + token;
        System.out.println("\n=== Checking Token Blacklist ===");
        System.out.println("Key: " + key);
        
        try {
            Boolean result = redisTemplate.hasKey(key);
            boolean isBlacklisted = Boolean.TRUE.equals(result);
            System.out.println("Token blacklist status: " + (isBlacklisted ? "BLACKLISTED" : "NOT BLACKLISTED"));
            System.out.println("=== Blacklist Check Complete ===\n");
            return isBlacklisted;
        } catch (Exception e) {
            System.out.println("Failed to check token blacklist: " + e.getMessage());
            System.out.println("=== Blacklist Check Failed ===\n");
            throw e;
        }
    }

    public void removeFromBlacklist(String token) {
        String key = BLACKLIST_PREFIX + token;
        System.out.println("\n=== Removing Token from Blacklist ===");
        System.out.println("Key: " + key);
        
        try {
            Boolean deleted = redisTemplate.delete(key);
            System.out.println("Token removal result: " + (Boolean.TRUE.equals(deleted) ? "REMOVED" : "NOT FOUND"));
            System.out.println("=== Removal Complete ===\n");
        } catch (Exception e) {
            System.out.println("Failed to remove token from blacklist: " + e.getMessage());
            System.out.println("=== Removal Failed ===\n");
            throw e;
        }
    }
} 