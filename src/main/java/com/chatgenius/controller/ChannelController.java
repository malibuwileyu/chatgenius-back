package com.chatgenius.controller;

import com.chatgenius.dto.request.CreateChannelRequest;
import com.chatgenius.dto.request.CreateMessageRequest;
import com.chatgenius.dto.response.ChannelResponse;
import com.chatgenius.dto.response.MessageResponse;
import com.chatgenius.model.Channel;
import com.chatgenius.model.Message;
import com.chatgenius.model.User;
import com.chatgenius.service.ChannelService;
import com.chatgenius.service.MessageService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/channels")
public class ChannelController {

    private static final Logger logger = LoggerFactory.getLogger(ChannelController.class);
    private final ChannelService channelService;
    private final MessageService messageService;

    @Autowired
    public ChannelController(ChannelService channelService, MessageService messageService) {
        this.channelService = channelService;
        this.messageService = messageService;
    }

    @GetMapping
    public ResponseEntity<List<ChannelResponse>> listChannels() {
        try {
            logger.debug("Fetching public channels");
            List<Channel> publicChannels = channelService.getPublicChannels();
            logger.debug("Found {} public channels", publicChannels.size());
            
            List<ChannelResponse> channels = publicChannels.stream()
                    .map(channel -> {
                        try {
                            return ChannelResponse.fromChannel(channel);
                        } catch (Exception e) {
                            logger.error("Error converting channel to response: {}", e.getMessage());
                            throw e;
                        }
                    })
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(channels);
        } catch (Exception e) {
            logger.error("Error fetching public channels: {}", e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping
    public ResponseEntity<ChannelResponse> createChannel(@Valid @RequestBody CreateChannelRequest request) {
        Channel channel = channelService.createChannel(request);
        return ResponseEntity.ok(ChannelResponse.fromChannel(channel));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChannelResponse> getChannel(@PathVariable UUID id) {
        Channel channel = channelService.getChannel(id);
        return ResponseEntity.ok(ChannelResponse.fromChannel(channel));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChannel(@PathVariable UUID id) {
        channelService.deleteChannel(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<List<UUID>> getChannelMembers(@PathVariable UUID id) {
        return ResponseEntity.ok(channelService.getChannelMembers(id));
    }

    @PostMapping("/{channelId}/members/{userId}")
    public ResponseEntity<Void> addMember(
            @PathVariable UUID channelId,
            @PathVariable UUID userId) {
        channelService.addMember(channelId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{channelId}/members/{userId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable UUID channelId,
            @PathVariable UUID userId) {
        channelService.removeMember(channelId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<Page<MessageResponse>> getChannelMessages(
            @PathVariable UUID id,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Message> messages = messageService.getChannelMessages(id, pageable);
        List<MessageResponse> responses = messages.getContent().stream()
                .map(MessageResponse::fromMessage)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new PageImpl<>(responses, pageable, messages.getTotalElements()));
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<MessageResponse> createChannelMessage(
            @PathVariable UUID id,
            @Valid @RequestBody CreateMessageRequest request) {
        logger.debug("Received message creation request for channel: {}", id);
        logger.debug("Request body: {}", request);
        try {
            User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            logger.debug("Current user: {}", currentUser);
            
            request.setChannelId(id);
            request.setUserId(currentUser.getId());
            
            logger.debug("Updated request: {}", request);
            Message message = messageService.createMessage(request);
            logger.debug("Created message: {}", message);
            return ResponseEntity.ok(MessageResponse.fromMessage(message));
        } catch (Exception e) {
            logger.error("Error creating message: {}", e.getMessage(), e);
            throw e;
        }
    }
} 