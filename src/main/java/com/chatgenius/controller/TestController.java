package com.chatgenius.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class TestController {

    @GetMapping("/test")
    public String test() {
        log.info("Test endpoint called");
        return "Server is running";
    }

    @GetMapping("/ws-test")
    public String wsTest() {
        log.info("WebSocket test endpoint called");
        return "WebSocket endpoint should be available at ws://localhost:8080/ws";
    }
} 