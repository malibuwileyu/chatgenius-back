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
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("")
@Validated
public class MessageController {

    private final MessageService messageService;

    @Autowired
    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/channels/{channelId}/messages")
    public ResponseEntity<Page<MessageResponse>> getChannelMessages(
            @PathVariable UUID channelId,
            Pageable pageable) {
        Page<Message> messagePage = messageService.getChannelMessages(channelId, pageable);
        List<MessageResponse> messageResponses = messagePage.getContent().stream()
                .map(MessageResponse::fromMessage)
                .collect(Collectors.toList());
        Page<MessageResponse> responsePage = new PageImpl<>(
                messageResponses, 
                pageable, 
                messagePage.getTotalElements()
        );
        return ResponseEntity.ok(responsePage);
    }

    @PostMapping("/channels/{channelId}/messages")
    public ResponseEntity<MessageResponse> createMessage(@Valid @RequestBody CreateMessageRequest request) {
        Message message = messageService.createMessage(request);
        return ResponseEntity.ok(MessageResponse.fromMessage(message));
    }

    @GetMapping("/messages/{id}")
    public ResponseEntity<MessageResponse> getMessage(@PathVariable UUID id) {
        Message message = messageService.getMessage(id);
        return ResponseEntity.ok(MessageResponse.fromMessage(message));
    }

    @PutMapping("/messages/{id}")
    public ResponseEntity<MessageResponse> updateMessage(
            @PathVariable UUID id,
            @RequestParam @NotBlank(message = "Content cannot be blank") String content) {
        Message message = messageService.updateMessage(id, content);
        return ResponseEntity.ok(MessageResponse.fromMessage(message));
    }

    @DeleteMapping("/messages/{id}")
    public ResponseEntity<Void> deleteMessage(@PathVariable UUID id) {
        messageService.deleteMessage(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/messages/{threadId}/replies")
    public ResponseEntity<MessageResponse> createReply(@Valid @RequestBody CreateReplyRequest request) {
        Message reply = messageService.createReply(
            request.getContent(),
            request.getThreadId(),
            request.getChannelId(),
            request.getUserId(),
            request.getType());
        return ResponseEntity.ok(MessageResponse.fromMessage(reply));
    }

    @GetMapping("/messages/{threadId}/replies")
    public ResponseEntity<List<MessageResponse>> getThreadReplies(@PathVariable UUID threadId) {
        List<MessageResponse> replies = messageService.getThreadReplies(threadId).stream()
                .map(MessageResponse::fromMessage)
                .collect(Collectors.toList());
        return ResponseEntity.ok(replies);
    }

    @GetMapping("/channels/{channelId}/messages/search")
    public ResponseEntity<List<MessageResponse>> searchMessages(
            @PathVariable UUID channelId,
            @RequestParam @NotBlank(message = "Search keyword cannot be blank") String keyword) {
        List<MessageResponse> messages = messageService.searchMessages(channelId, keyword).stream()
                .map(MessageResponse::fromMessage)
                .collect(Collectors.toList());
        return ResponseEntity.ok(messages);
    }
} 