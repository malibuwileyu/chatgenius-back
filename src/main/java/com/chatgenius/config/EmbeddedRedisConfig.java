package com.chatgenius.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import redis.embedded.RedisServer;
import redis.embedded.RedisServerBuilder;

import java.io.IOException;
import java.net.ServerSocket;

@Configuration
public class EmbeddedRedisConfig {

    @Value("${spring.redis.port:6379}")
    private int redisPort;

    private RedisServer redisServer;

    private boolean isPortInUse(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            return false;
        } catch (IOException e) {
            return true;
        }
    }

    @PostConstruct
    public void startRedis() {
        try {
            System.out.println("Starting embedded Redis server on port " + redisPort);
            
            // Check if port is already in use
            if (isPortInUse(redisPort)) {
                System.out.println("Redis server is already running on port " + redisPort);
                return;
            }

            // Configure Redis with OS-specific settings
            RedisServerBuilder builder = RedisServer.builder()
                .port(redisPort)
                .setting("maxheap 128M");

            // Add Windows-specific settings
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                builder.setting("maxheap 128M")
                       .setting("bind 127.0.0.1");
            }

            redisServer = builder.build();
            redisServer.start();
            System.out.println("Embedded Redis server started successfully");
        } catch (Exception e) {
            String errorMsg = e.getMessage();
            System.out.println("Failed to start embedded Redis server: " + errorMsg);
            
            if (errorMsg != null && errorMsg.contains("No such file or directory")) {
                System.out.println("Creating Redis server directory and retrying...");
                try {
                    // Create necessary directories
                    new java.io.File(System.getProperty("java.io.tmpdir") + "/redis").mkdirs();
                    redisServer = RedisServer.builder()
                        .port(redisPort)
                        .setting("maxheap 128M")
                        .setting("dir " + System.getProperty("java.io.tmpdir") + "/redis")
                        .build();
                    redisServer.start();
                    System.out.println("Embedded Redis server started successfully after retry");
                    return;
                } catch (Exception retryEx) {
                    System.out.println("Retry failed: " + retryEx.getMessage());
                }
            }
            
            throw new RuntimeException("Could not start embedded Redis", e);
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