package com.chatgenius.controller;

import com.chatgenius.dto.request.CreateMessageRequest;
import com.chatgenius.dto.request.CreateReplyRequest;
import com.chatgenius.dto.response.MessageResponse;
import com.chatgenius.model.Message;
import com.chatgenius.model.enums.MessageType;
import com.chatgenius.service.MessageService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/messages")
@Validated
public class MessageController {

    private final MessageService messageService;

    @Autowired
    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping
    public ResponseEntity<Page<MessageResponse>> getMessages(
            @RequestParam(required = false) UUID channelId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        if (channelId == null) {
            return ResponseEntity.ok(Page.empty(pageable));
        }
        Page<Message> messages = messageService.getChannelMessages(channelId, pageable);
        List<MessageResponse> responses = messages.getContent().stream()
                .map(MessageResponse::fromMessage)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new PageImpl<>(responses, pageable, messages.getTotalElements()));
    }

    @PostMapping
    public ResponseEntity<MessageResponse> createMessage(@Valid @RequestBody CreateMessageRequest request) {
        Message message = messageService.createMessage(request);
        return ResponseEntity.ok(MessageResponse.fromMessage(message));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MessageResponse> getMessage(@PathVariable UUID id) {
        Message message = messageService.getMessage(id);
        return ResponseEntity.ok(MessageResponse.fromMessage(message));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MessageResponse> updateMessage(
            @PathVariable UUID id,
            @RequestBody @NotBlank String content) {
        Message message = messageService.updateMessage(id, content);
        return ResponseEntity.ok(MessageResponse.fromMessage(message));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMessage(@PathVariable UUID id) {
        messageService.deleteMessage(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/replies")
    public ResponseEntity<MessageResponse> createReply(
            @PathVariable UUID id,
            @Valid @RequestBody CreateReplyRequest request) {
        Message reply = messageService.createReply(
            request.getContent(),
            id,
            request.getChannelId(),
            request.getUserId(),
            request.getType());
        return ResponseEntity.ok(MessageResponse.fromMessage(reply));
    }
} 