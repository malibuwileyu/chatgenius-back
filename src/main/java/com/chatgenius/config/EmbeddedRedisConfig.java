package com.chatgenius.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import redis.embedded.RedisServer;

import java.io.File;

@Configuration
@Profile("!prod") // Don't use embedded Redis in production
public class EmbeddedRedisConfig {

    @Value("${spring.redis.port}")
    private int redisPort;

    private RedisServer redisServer;

    @PostConstruct
    public void startRedis() {
        try {
            System.out.println("Starting embedded Redis server on port " + redisPort);
            
            // Create temp directory for Redis heap
            String tempDir = System.getProperty("java.io.tmpdir");
            String heapDir = tempDir + File.separator + "redis-heap";
            new File(heapDir).mkdirs();

            redisServer = RedisServer.builder()
                .port(redisPort)
                .setting("maxheap 32M") // Reduced heap size
                .setting("heapdir " + heapDir)
                .build();

            redisServer.start();
            System.out.println("Embedded Redis server started successfully");
        } catch (Exception e) {
            System.out.println("Failed to start embedded Redis server: " + e.getMessage());
            if (e.getMessage() != null && e.getMessage().contains("Address already in use")) {
                System.out.println("Redis server is already running on port " + redisPort);
                return; // Don't throw exception if Redis is already running
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