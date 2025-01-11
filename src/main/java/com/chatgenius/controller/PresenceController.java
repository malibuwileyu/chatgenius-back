package com.chatgenius.controller;

import com.chatgenius.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PresenceController {

    private final UserService userService;

    @GetMapping("/channels/{channelId}/presence")
    public ResponseEntity<?> getChannelPresence(@PathVariable UUID channelId) {
        var onlineUsers = userService.getOnlineUsers(channelId);
        return ResponseEntity.ok(onlineUsers);
    }

    @GetMapping("/users/{userId}/status")
    public ResponseEntity<?> getUserStatus(@PathVariable UUID userId) {
        var status = userService.getUserStatus(userId);
        return ResponseEntity.ok(Map.of("status", status));
    }
} 