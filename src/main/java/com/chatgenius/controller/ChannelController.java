package com.chatgenius.controller;

import com.chatgenius.dto.request.CreateChannelRequest;
import com.chatgenius.model.Channel;
import com.chatgenius.service.ChannelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/channels")
@RequiredArgsConstructor
public class ChannelController {
    private final ChannelService channelService;

    @GetMapping
    public ResponseEntity<List<Channel>> getAllChannels() {
        return ResponseEntity.ok(channelService.getAllChannels());
    }

    @PostMapping
    public ResponseEntity<Channel> createChannel(@RequestBody @Valid CreateChannelRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Channel channel = channelService.createChannel(request, username);
        return ResponseEntity.ok(channel);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Channel> getChannel(@PathVariable UUID id) {
        return ResponseEntity.ok(channelService.getChannelById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChannel(@PathVariable UUID id) {
        channelService.deleteChannel(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{channelId}/members/{username}")
    public ResponseEntity<Void> addMember(@PathVariable UUID channelId, @PathVariable String username) {
        channelService.addMember(channelId, username);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{channelId}/members/{username}")
    public ResponseEntity<Void> removeMember(@PathVariable UUID channelId, @PathVariable String username) {
        channelService.removeMember(channelId, username);
        return ResponseEntity.ok().build();
    }
} 