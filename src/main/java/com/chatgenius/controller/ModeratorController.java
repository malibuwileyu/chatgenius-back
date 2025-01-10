package com.chatgenius.controller;

import com.chatgenius.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/moderator")
@RequiredArgsConstructor
public class ModeratorController {

    private final ChannelService channelService;

    @DeleteMapping("/channels/{channelId}")
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<Void> deleteChannel(@PathVariable String channelId) {
        channelService.deleteChannel(UUID.fromString(channelId));
        return ResponseEntity.ok().build();
    }
} 