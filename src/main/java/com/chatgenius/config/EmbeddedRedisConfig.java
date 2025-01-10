package com.chatgenius.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import redis.embedded.RedisServer;

@Configuration
public class EmbeddedRedisConfig {

    @Value("${spring.redis.port}")
    private int redisPort;

    private RedisServer redisServer;

    @PostConstruct
    public void startRedis() {
        try {
            System.out.println("Starting embedded Redis server on port " + redisPort);
            redisServer = RedisServer.builder()
                .port(redisPort)
                .setting("maxheap 128M")
                .build();
            redisServer.start();
            System.out.println("Embedded Redis server started successfully");
        } catch (Exception e) {
            System.out.println("Failed to start embedded Redis server: " + e.getMessage());
            if (e.getMessage().contains("Address already in use")) {
                System.out.println("Redis server is already running on port " + redisPort);
            } else {
                throw new RuntimeException("Could not start embedded Redis", e);
            }
        }
    }

    @PreDestroy
    public void stopRedis() {
        if (redisServer != null && redisServer.isActive()) {
            System.out.println("Stopping embedded Redis server");
            redisServer.stop();
            System.out.println("Embedded Redis server stopped");
        }
    }
} 